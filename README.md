JConsole
========

A Java Command Prompt - use it to work with Rails inside Netbeans and Windows

* This is a general purpose system console app using SwingWorker and
* ProcessBuilder.
* 
* Built primarily so I wouldn't have to leave Netbeans in order
* to play with Rails on the Windows platform.
*
* JConsole simply forks a system process via ProcessBuilder and handles IO via
* SwingWorker without using or starting any additional threads. 
* 
* Enjoy!!! :)
*
* -- History -------------------------------------
* v0.1 
*  - Added commandline history. Use UP/DOWN arrow keys. 
*  - Created as Netbeans Module.
*
* -- Notes ---------------------------------------
* v0.1
*  - Not tested on OSX or Linux. 
*  - Currently uses cmd.exe as shell (see CmdLineProcessor class below) 
*  - Does not yet handle CNTRL+Z events to kill runaway scripts
*
*
* @author Luis Fung <fungl164@hotmail.com> - 06/06/2012
