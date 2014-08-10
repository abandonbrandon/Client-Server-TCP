README

TCP message system between server & client. 

Allows for client to send message, print messages from the server, and exit the server.


Too compile:
javac client_java_tcp.java
javac server_java_tcp.java

To run:
(Make sure server is running first!)
java server_java_tcp 1025 welcome.txt
java client_java_tcp localhost 1025 username

