![Alt text](src/main/resources/images/omegle.png)<br/>
[![Travis CI](https://travis-ci.org/maxamel/DesktopOmegle.svg)](https://travis-ci.org/maxamel/DesktopOmegle)<br/>
[![Coverity Scan](https://scan.coverity.com/projects/5872/badge.svg?flat=1)](https://scan.coverity.com/projects/5872?tab=overview)<br/>
# DesktopOmegle

A Desktop Application for the online chat Omegle. 

Currenlty supports just the basic feature of chatting with random strangers or with strangers with similar interests.
A new enhancement is the auto disconnection upon discovering bad connectivity. You can toggle this feature on and off. The connectivity level is estimated by the number of timeouts during the conversation. After ten consecutive timeouts the connection is broken off. The connectivity is indicated by the color on the status of your conversation counterpart, so the disconnection won't come as a surprise.  

<img src="https://github.com/maxamel/DesktopOmegle/blob/master/src/main/resources/images/screen.png" align="center" />

# Overview

After developing with Java Swing for many years I wanted to explore the possibilities of JavaFX. Most of the concepts, such as keeping the UI thead free, are similar.
The intention was to make a software project which would follow the high-cohesion, low-coupling principle. If anyone has doubts or questions regarding the design and code please raise an issue, I'd be happy to hear.

I hope someone will find this useful. I'm interested in adding features which are not present in the original Omegle website, so any ideas are welcome.

The API of the Omegle site was discovered using open source packet sniffers like WireShark and Fiddler. 

# Testing and Code Quality

The application was mostly tested manually, there are a few unit tests for the Service module. 
Currently writing further tests is the top priority. 
Code quality is maintained by using static analysis tools: FindBugs and Coverity.
During every build process gradle runs findbugs to check for defects. Every now and then I also upload the project to the Coverity site to run a full scan. In the future, this process can be automated by configuring Travis CI to run Coverity every build.

# Prerequisites

Written in Java 8. 

Built with Gradle.

The repository provides the JavaFX jar prebuilt, as there are issues with JavaFX dependencies on different platforms. For example, Oracle JDK 8 provides it, while OpenJDK does not. This means one will have to install OpenJFX to get this to work, which can be challenging (at least on Windows). A prebuilt jar solves all these issues. However, this conveniece comes with a heavy price of 17MB.

# Installation

Get the code and build it:
```
git clone https://github.com/maxamel/DesktopOmegle
cd DesktopOmegle
gradle clean build
```
After a successful build, run the Jar in the build directory.

# Contributing

Contributions are welcome. Please submit an issue before submitting a pull request detailing the changes. 

Possibilities for enhancement:

1) Write tests for server+client modules.

2) Add progress bar when searching for users with common interests. Currently fade out is supported when connecting.


