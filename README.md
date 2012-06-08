# JConsole - An ultra light-weight Java console app
-------------------------------------

This is a general-purpose system console app built primarily so I wouldn't 
have to leave **Netbeans** in order to play with **Ruby on Rails (RoR)** on the Windows platform.

**JConsole** is very light-weight and can be easily embedded into any app.  

It can also be used as a standalone system shell or command prompt **(e.g. cmd.exe)** for running 
scripts, code generators and/or other system/development utilities.


## How does it work?
**JConsole** simply forks a system process via **\<ProcessBuilder\>** and handles I/O via a
**\<SwingWorker\>** without using or spawning any additional background threads. 

It does not depend on any **external libraries or 3rd party packages** and can be 
extended to use other shells besides cmd.exe **(e.g. /bin/bash)**.
 
## History 

 v0.1 - 06/06/2012
 - Added output display size limits to help with memory consumption
 - Added commandline history. Use UP/DOWN arrow keys. 
 - Created as Netbeans Module.

## Notes 

v0.1 - 06/06/2012
- Not tested on **OSX** or **Linux**. 
- Currently uses **cmd.exe** as the default shell
- Does not yet handle **CNTRL+Z** events to kill runaway scripts
- Probably has some bugs, so


Feel free to send feedback and/or contribute...

Enjoy!!! :)

@author Luis Fung - 06/06/2012


Architecture
-------------------------------------
![JConsole Architecture](https://github.com/fungl164/JConsole/blob/master/Arch%20Diagram.png?raw=true "JConsole Architecture")

Tools
-------------------------------------
**Ruby and Rails** - Netbeans plugin - http://plugins.netbeans.org/plugin/38549

**NodeJS** - Netbeans plugin - http://plugins.netbeans.org/plugin/36653/nodejs

**JConsole** - Netbeans plugin - https://github.com/downloads/fungl164/JConsole/org-stagezero-jconsole.nbm