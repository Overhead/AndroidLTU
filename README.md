AndroidLTU
==========

Android course at LTU 2014
How to write ReadMe: http://daringfireball.net/projects/markdown/syntax#precode

In order to set up this Application and get it working, you would need a Google GCM server.

A guide on how to move forward on this can be found here.
http://developer.android.com/google/gcm/gs.html


Then when GCM is set up, you need to apply the Project_ID and Auth_Key variables in the different apps.


Variables that has to be set:

###ProjectApp:
 - [ReceiverActivity](https://github.com/Overhead/AndroidLTU/blob/master/ProjectApp/src/com/mini/project/RecieverActivity.java):
  <pre><code>SENDER_ID = "" // GCM Project ID
   AUTH_KEY = "" // GCM Auth Key
  </code></pre>
   
###ProjectAppSender:
 - [MainActivity](https://github.com/Overhead/AndroidLTU/blob/master/ProjectAppSender/src/com/example/porjectappsender/MainActivity.java):
   <pre><code>AUTH_KEY = "" // GCM Auth Key
   </code></pre>
   
###ServerApp:
 - [GCMSender](https://github.com/Overhead/AndroidLTU/blob/master/ServerApp/src/GCMSender.java):
  <pre><code>con.setRequestProperty("Authorization", "key="); // GCM Auth Key

 - [ServerClient](https://github.com/Overhead/AndroidLTU/blob/master/ServerApp/src/ServerClient.java):
  <pre><code>DB_USERNAME = "" // Username of DB authentication
  DB_PW = ""       // Password of DB authentication
  AUTH_KEY = ""    // GCM Auth Key
  </code></pre>

##Database Setup
The database that is used in this project is a normal MySQL database on Ubuntu.
To create the tables needed just run the [SQLScript](https://github.com/Overhead/AndroidLTU/blob/master/Project%20DB/Database%20CreateScript.sql) found in the folder Project_DB.

####DB Model
![Alt text](https://raw.githubusercontent.com/Overhead/AndroidLTU/master/Project%20DB/Database%20Model.png "Optional title")
