//Brandon Starler

//client_java_tcp.java

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;


public class client_java_tcp {
	String host;
	int port;
	String username;
	int length;
	int theUUID;
	int commandnum;
	String command;
	int confirmation;
	byte[] sendbytes;
	byte[] message;
	
	//intializing port and socket to null
	Socket clientSocket = null;
	Scanner userInput = null;
	DataInputStream inFromServer = null;
	DataOutputStream outToServer = null;
	
	//constructor for client
	client_java_tcp(String host, int port, String username)
	{
		this.host = host;
		this.port = port;
		this.username = username;
	}
	
	public static void main (String args[]) throws Exception
    {
		if(args.length != 3)
		{
			System.err.println("Invalid number of args. Terminating.");
			System.exit(0);
		}
		
		client_java_tcp client = new client_java_tcp(args[0], Integer.parseInt(args[1]), args[2]);
		client.run();
	}
	
	public void run()
	{
		try
		{
			//Creates new socket connection to the host and port
			clientSocket = new Socket(host, port);
			
			//Creates input stream directly from User
			userInput = new Scanner(System.in);
			
			//Creates output stream attached to the socket to be sent to the server
			outToServer = new DataOutputStream(clientSocket.getOutputStream());
			
			//Creates input stream from the server attached to the socket
			inFromServer = new DataInputStream(clientSocket.getInputStream());
		}
		catch(IOException ex)
		{
			System.out.println("Could not connect to server. Terminating");
			System.exit(0);
		}
		
		//command 0x01, connect command client->server followed by the username
		try
		{
		
			//convert username into sendbytes
			sendbytes = username.getBytes("UTF-8");
			
			//send out 1 to initialize response from server
			outToServer.writeInt(1);
			
			//receive confirmation after sending out 1 by receiving 2
			int confirmation = inFromServer.readInt();
			
			System.out.println("Welcome message: welcome to the server \n");
			
			//convert username into sendbytes
			//take length of sendbytes and send that information to the server

			int sendbyteslength = sendbytes.length;

			outToServer.writeInt(sendbyteslength);
			//send sendbytes, the username, over to the server
			outToServer.write(sendbytes);
			
			//receive UUID as integers
			theUUID = inFromServer.readInt();

		
		}	
		catch (IOException ex)
		{
			System.err.println("Could not connect to server. Terminating.");
			System.exit(0);
		}
		
		while(true)
		{
			try
			{
				//ask for command and take in the command
				System.out.println("Enter a command: (send, print, or exit)");
				command = userInput.nextLine();
		
				//Send
				if (command.equals("send"))
				{
					//send 3
					outToServer.writeInt(3);
					
					//recieve confirmation
					confirmation = inFromServer.readInt();
					
					try
					{
						System.out.println("Enter your message:");
						String acommand = userInput.nextLine();
						//convert message to bytes to be sent
						message = acommand.getBytes("UTF-8");
						int sendbyteslength = message.length;
						//get length of bytes
						outToServer.writeInt(message.length);
						outToServer.write(message);
						outToServer.writeInt(theUUID);
					}
					catch(IOException ex)
					{
						System.err.println("Failed to send message. Terminating");
						System.exit(0);
					}	
				}
				
				//Print
				if (command.equals("print"))
				{
					//send command
					outToServer.writeInt(4);
					
					//recieve confirmation
					inFromServer.readInt();
					
					commandnum = inFromServer.readInt();
					while (commandnum == 9)
					{
						//receive length of username
						length = inFromServer.readInt();
						
						message = new byte[length];
						//username incoming
						inFromServer.read(message, 0, length);
						System.out.print((new String(message)) + ": ");
						//Brandon: ----
						length = inFromServer.readInt();
						message = new byte[length];
						inFromServer.read(message, 0, length);
						//print message and combine with username
						System.out.println(new String(message) + "\n");
						commandnum = inFromServer.readInt();
					}														
				}
				
				//Exit
				else if (command.equals("exit"))
				{
					//send out command 5, corresponding to exit command
					outToServer.writeInt(5);
					
					//receive confirmation bit 5
					confirmation = inFromServer.readInt();
					
					//close all open channels
					inFromServer.close();
					outToServer.close();
					userInput.close();
					clientSocket.close();
					System.exit(0);
					//get out of the function, you're done
					//return;
				}
			}
			catch(IOException ex)
			{
				System.err.println("Couldn't do it");
				System.exit(0);
			}

		}

    }
	
}
