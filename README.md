# DesktopOmegle

A Desktop Application for the online chat Omegle. 

Currenlty supports just the basic feature of chatting with random strangers.

# Overview

This was written mainly as an excercise. The intention was to make this software follow the high cohesion-low coupling principle, and allow people to take one module (the service or the client) and reuse it for their purposes. For example if one might want to develop his own chat application he could take just the client part and only change the service calls from it.

I hope someone will find this useful.

The API of the Omegle site was discovered using open source packet sniffers like WireShark and Fiddler. I had second thoughts before releasing this code since it could be used to write botnets (which Omegle is already full of), but when I found out Omegle was implementing reCaptcha to fight bots I decided it was OK. 

# Prerequisites

Written with Java 8 (JavaFX was used for the UI part). 

Built with Gradle.

# Contributing

Contributions are welcome. Please submit an issue before submitting a pull request detailing the changes. 

Possibilities for enhancement:

1) Write basic tests for the service module.

2) Support the feature of connecting strangers on the basis of interests.

3) Support the reCaptcha service using JavaFX webpage rendering (when Omegle supports this). 

