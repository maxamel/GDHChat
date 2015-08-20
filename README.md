[![Travis CI](https://travis-ci.org/maxamel/DesktopOmegle.svg)](https://travis-ci.org/maxamel/DesktopOmegle)<br/>
[![Coverity Scan](https://scan.coverity.com/projects/5872/badge.svg?flat=1)](https://scan.coverity.com/projects/5872?tab=overview)<br/>
# DesktopOmegle

A Desktop Application for the online chat Omegle. 

Currenlty supports just the basic feature of chatting with random strangers or with strangers with similar interests.

# Overview

This was written mainly as a learning experience. 
After developing with Java Swing for many years I wanted to explore the possibilities of JavaFX, and I'm grateful I did. A whole new world was uncovered before my eyes.
The intention was to make a software project which would follow the high-cohesion, low-coupling principle. If anyone has doubts or questions regarding the design and code please raise an issue, I'd be happy to hear.

I hope someone will find this useful.

The API of the Omegle site was discovered using open source packet sniffers like WireShark and Fiddler. I had second thoughts before releasing this code since it could be used to write botnets (which Omegle is already full of), but when I found out Omegle was implementing reCaptcha to fight bots I decided it was OK. 

Supports auto disconnection upon discovering bad connectivity. The connectivity is indicated by the color on the status of your conversation counterpart, so the disconnection won't come as a surprise.  

#Testing

The application was mostly tested manually, there a few unit tests for the Service module. 
Currently writing further tests is the top priority. 

# Prerequisites

Written with Java 8 (JavaFX was used for the UI part). 

Built with Gradle.

# Contributing

Contributions are welcome. Please submit an issue before submitting a pull request detailing the changes. 

Possibilities for enhancement:

1) Write tests for server+client modules.

2) Add progress bar when searching for users with common interests. Currently fade out is supported when connecting.


