Android Catchoom Recognition Client
===================================

Description
-----------
The project contains an Android app that acts as example client for the Catchoom Recognition Service. It shows the scenario of validating collection tokens against the server and perform image recognition. The application uses the [Catchoom Android SDK](https://github.com/Catchoom/android-sdk "Catchoom Android SDK") to interact with the Catchoom API.

Requirements
------------
In order to execute only the application you will need an Android device version 2.3 or above.
If you want to build the project, you will need Eclipse, the Android SDK and Android ADT tools.

Installation
------------
There are two main ways to install the application on your Android device:
* Download the packaged `Android-CRC.apk` Android application and install it normally on your Android device (Remember to allow third party apps on your device's settings).
* Download the Eclipse project `Android CRC`, open it on Eclipse and execute it as an Android project.

Usage
-----
The application consists of 3 screens:
* **Splash screen:** When the application is initialized, a splash screen with the Catchoom logo will be shown. If a collection token has been saved previously, it will be checked and you will be leded to the next screen (it will depend on the checking result). During this process you can touch any point on the screen to open a web browser with the Catchoom website.
* **Settings screen** If a token has not been saved previously or it is incorrect, a screen where you can introduce a token will be presented. There you will be able to connect to the server with a collection token and save it for posterior uses.
* **Results screen** Finally the results screen will be shown, where you can perform image recognition against the collection you have connected with using the token. When the search retrieves any results, they are shown in a list where you can see the name and score of the items and a thumbnail. Clicking on any of those items you will be leaded to their webpage.