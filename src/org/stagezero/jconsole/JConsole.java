/**
 *
 * Copyright 2012 Luis Fung <fungl164@hotmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package org.stagezero.jconsole;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * JConsole - A Java Console application -------------------------------------
 *
 * This is a general purpose system console app using SwingWorker and
 * ProcessBuilder. Built primarily so I wouldn't have to leave Netbeans in
 * order to play with Ruby on Rails (RoR) on the Windows platform.
 *
 * JConsole can be used as an embedded or standalone command prompt (e.g. cmd.exe). 
 * 
 * It simply forks a system process via ProcessBuilder and handles I/O via 
 * SwingWorker without using or starting any additional threads. It can also be
 * extended to use other shells. Enjoy!!! :)
 *
 * @author Luis Fung <fungl164@hotmail.com> - 06/06/2012
 *
 */
public class JConsole {

    public static final String VERSION = "v0.1";
    private static final int MAX = 20;
    private ConsoleDisplay view;
    private CmdLineProcessor shell;
    private String[] history;
    int last, prev;

    public JConsole(JTextComponent display) throws IOException {
        view = new ConsoleDisplay(this, display);
        shell = new CmdLineProcessor(this);
        history = new String[MAX];
        last = 0;
    }

    public ConsoleDisplay getDisplay() {
        return view;
    }

    public ProgressListener getProgressListener() {
        return view;
    }

    public String getCurrentCommand() {
        return history[last];
    }

    public String getPreviousCommand() {
        return history[check(--prev)];
    }

    public String getNextCommand() {
        return history[check(++prev)];
    }

    public int check(int prev) {
        int mod = prev % history.length;
        return mod < 0 ? mod + history.length : mod;
    }

    public void execute(String command) throws ExecutionException {
        if (command == null) {
            return;
        }
        shell.execute(command);

        history[last] = command;
        last = (last + 1) % MAX;
        prev = last;
    }

    public void close() {
        shell.quit();
    }

    public static void main(String[] args) throws IOException {
        JTextArea consoleDisplay = new JTextArea();
        JConsole console = new JConsole(consoleDisplay);

        JFrame fr = new JFrame("JConsole [" + VERSION + "]");
        fr.setPreferredSize(new Dimension(640, 480));
        fr.add(new JScrollPane(consoleDisplay));
        //fr.validate();
        fr.pack();
        fr.setVisible(true);
    }
}

class CmdLineProcessor implements CommandProcessor {

    public static final String SHELL = "cmd", PARAMS = "/k";
    private ProcessBuilder builder;
    private ForkedProcess proc;

    public CmdLineProcessor(JConsole console) throws IOException {
        builder = new ProcessBuilder(SHELL, PARAMS);
        builder.redirectErrorStream(true);

        proc = new ForkedProcess(builder.start(), console.getProgressListener(), console.getDisplay());
        proc.execute();
    }

    @Override
    public void execute(String command) throws ExecutionException {
        proc.execute(command);
    }

    public void abort() {
        proc.cancel(true);
    }

    public void quit() {
        proc.cancel(true);
    }
}

class ForkedProcess extends SwingWorker<Void, String> implements ChildProcess {

    private Process process;
    private ProgressListener io;
    private ConsoleView view;
    private boolean skip;

    public ForkedProcess(Process proc, ProgressListener listener, ConsoleView view) throws IOException {
        this.process = proc;
        this.io = listener;
        this.view = view;

    }

    @Override
    protected void process(java.util.List<String> chunks) {
        // Done on the event thread
        Iterator<String> it = chunks.iterator();
        while (it.hasNext()) {
            String rcvd = it.next();
            if (skip) {
                // FIX ME!!!  Nasty hackety hack to prevent command from being displayed twice.
                view.stdout(process, "\n");
                skip = false;
                continue;
            }
            view.stdout(process, rcvd);
        }
    }

    @Override
    public Void doInBackground() {
        io.started(process);

        try {
            InputStream in = process.getInputStream();
            byte[] buffer = new byte[128];
            int len;

            while ((len = in.read(buffer, 0, buffer.length)) != -1) {
                publish(new String(buffer, 0, len));
                if (isCancelled()) {
                    break;
                }
            }
        } catch (Exception e) {
            io.error(process, e);
        }
        if (process != null) {
            process.destroy();
        }
        return null;  // Don't care
    }

    public Process getProcess() {
        return process;
    }

    public void execute(String args) throws ExecutionException {
        try {
            if (process == null) {
                return;
            }
            skip = true;
            process.getOutputStream().write(args.getBytes());
            process.getOutputStream().write('\n');
            process.getOutputStream().flush();
        } catch (IOException ex) {
            throw new ExecutionException(ex);
        }
    }

    @Override
    protected void done() {
        // Done on the swing event thread
        io.ended(process, 0);
    }
}

class ConsoleDisplay implements ConsoleView, ProgressListener {

    private static int MAX_DOC_LENGTH = 8192;
    private JTextComponent display;
    private Document doc;
    private Map<Action, ActionListener> actionkeymapper;
    int promptOffset, tempOffset;
    boolean executing;

    public ConsoleDisplay(final JConsole console, JTextComponent disp) {
        this.display = disp;
        this.doc = display.getDocument();
        this.actionkeymapper = new HashMap<Action, ActionListener>();

        display.setCaret(new BlockCaret());
        display.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                if (display.getCaretPosition() < promptOffset) {
                    display.setCaretPosition(doc.getLength());
                }
            }

            @Override
            public void keyPressed(KeyEvent e) { //Ignore
            }

            @Override
            public void keyReleased(KeyEvent e) { //Ignore
            }
        });

        onPress(KeyStroke.getKeyStroke("BACK_SPACE"), new ConsoleDisplay.CheckedConsoleAction());
        onPress(KeyStroke.getKeyStroke("LEFT"), new ConsoleDisplay.CheckedConsoleAction());
        onPress(KeyStroke.getKeyStroke("UP"), new ConsoleDisplay.ConsoleAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String prev = console.getPreviousCommand();
                if (prev == null) {
                    return;
                }
                removeText(doc.getLength() - tempOffset, tempOffset);
                tempOffset = prev.length();
                update(prev);
            }
        });
        onPress(KeyStroke.getKeyStroke("DOWN"), new ConsoleDisplay.ConsoleAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String next = console.getNextCommand();
                if (next == null) {
                    return;
                }
                removeText(doc.getLength() - tempOffset, tempOffset);
                tempOffset = next.length();
                update(next);
            }
        });
        onPress(KeyStroke.getKeyStroke("pressed ENTER"), new ConsoleDisplay.ConsoleAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    promptOffset -= tempOffset;
                    console.execute(getText());
                    tempOffset = 0;
                } catch (ExecutionException ex) {
                    //Logger.getLogger(ConsoleDisplay.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        // TODO: Handle CNTRL_Z events
        //onPress(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), new JConsoleDisplay.ConsoleAction() {
        //    @Override
        //    public void actionPerformed(ActionEvent e) {
        //        console.handle(KeyEvent.VK_Z);
        //    }
        //});
    }

    /**
     * Display Handler Callback Methods
     */
    @Override
    public void started(Process process) {
        update("JConsole [" + JConsole.VERSION + "]\nCopyright (c) 2012 stagezero.org. All rights reserved.\n\n");
    }

    @Override
    public void aborted(Process process, String line) {
        //System.out.println("ABORTED.");
    }

    @Override
    public void ended(Process process, int value) {
        //System.out.println("Exit(" + value + ")");
    }

    @Override
    public void error(Process process, Throwable th) {
        //System.out.println("ERROR: " + th.getCause());
    }

    @Override
    public void stdout(Process process, String line) {
        update(line);
    }

    @Override
    public void stderr(Process process, String line) {
        //System.out.println(line);
    }

    @Override
    public void clear() {
        display.setText("");
        promptOffset = tempOffset = 0;
    }

    /**
     * JTextComponent Helper Methods
     */
    public void update(final String text) {
        appendText(text);
    }

    public void appendText(final String str) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                try {
                    synchronized (doc) {
                        if (doc.getLength() > MAX_DOC_LENGTH) { // Remove ~10% of total from the beginning to make room for new stuff                                                       
                            removeText(0, doc.getLength() - MAX_DOC_LENGTH + (MAX_DOC_LENGTH / 8));
                        }

                        doc.insertString(doc.getLength(), str, null);
                        promptOffset = doc.getLength();
                    }
                } catch (BadLocationException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }

    public void removeText(int offset, int length) {
        try {
            synchronized (doc) {
                doc.remove(offset, length);
                promptOffset -= length;
            }
        } catch (BadLocationException ex) {
            Logger.getLogger(ConsoleDisplay.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getText() {
        try {
            synchronized (doc) {
                String cmd = doc.getText(promptOffset, doc.getLength() - promptOffset);
                return cmd;
            }
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    public void actionPerformed(Action action, ActionEvent event) { // send action to downstream component
        actionkeymapper.get(action).actionPerformed(event);
    }

    public void onPress(KeyStroke key, Action action) {
        actionkeymapper.put(action, display.getActionForKeyStroke(key));
        display.getActionMap().put(
                display.getInputMap().get(key),
                action);
    }

    public boolean checkBounds() { // true if caretpos > prompt 
        if (display.getCaretPosition() > promptOffset) {
            return true;
        }
        return false;
    }

    abstract class ConsoleAction extends AbstractAction {
    }

    class CheckedConsoleAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {

            if (!checkBounds()) {
                return;
            };
            ConsoleDisplay.this.actionPerformed(this, e);
        }
    }
}

interface ConsoleView {

    public void stdout(Process process, String line);

    public void stderr(Process process, String line);

    public void clear();
}


interface ProgressListener {

    public void started(Process process);

    public void aborted(Process process, String line);

    public void ended(Process process, int value);

    public void error(Process process, Throwable th);
}

interface ChildProcess {
}

interface CommandProcessor {

    public void execute(String command) throws ExecutionException;
}
