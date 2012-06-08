# JConsole - An ultra light-weight Java console
-------------------------------------

This is a general-purpose system console app built primarily so I wouldn't 
have to leave Netbeans in order to play with Ruby on Rails (RoR) on the Windows platform.

JConsole is very light-weight and can be used as an embedded or standalone system command prompt (e.g. cmd.exe)

It simply forks a system process via ProcessBuilder and handles I/O via
SwingWorker without using or starting any additional threads. It can also be
extended to use other shells (e.g. /bin/bash).
 
Enjoy!!! :)

## History 

 v0.1 - 06/06/2012
 - Added output display size limits to help with memory consumption
 - Added commandline history. Use UP/DOWN arrow keys. 
 - Created as Netbeans Module.

## Notes 

v0.1 - 06/06/2012
- Not tested on OSX or Linux. 
- Currently uses cmd.exe as shell (see CmdLineProcessor class below) 
- Does not yet handle CNTRL+Z events to kill runaway scripts
- Probably has some bugs, so


Feel free to send feedback and/or contribute...


@author Luis Fung - 06/06/2012

Architecture
-------------------------------------
![JConsole Architecture](https://github.com/fungl164/JConsole/blob/master/Arch%20Diagram.png?raw=true "JConsole Architecture")

Tools
-------------------------------------
Ruby and Rails - Netbeans plugin - http://plugins.netbeans.org/plugin/38549

JConsole - Netbeans plugin - https://github.com/downloads/fungl164/JConsole/org-stagezero-jconsole.nbm