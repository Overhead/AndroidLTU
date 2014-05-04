AndroidLTU
==========

Android course at LTU 2014


In order to set up this Application and get it working, you would need a Google GCM server.

A guide on how to move forward on this can be found here.
http://developer.android.com/google/gcm/gs.html


Then when GCM is set up, you need to apply the Project_ID and Auth_Key variables in the different apps.


Variables that has to be set:

ProjectApp:
 - ReceiverActivity:
   - SENDER_ID = "" // GCM Project ID
   - AUTH_KEY = "" // GCM Auth Key
   
ProjectAppSender:
 - MainActivity:
   - AUTH_KEY = "" // GCM Auth Key
   
ServerApp:
 - GCMSender:
  - con.setRequestProperty("Authorization", "key=#INSERT_AUTH_KEY#"); // GCM Auth Key

 - ServerClient:
  - DB_USERNAME = "" // Username of DB authentication
  - DB_PW = ""       // Password of DB authentication
  - AUTH_KEY = ""    // GCM Auth Key
