//Brandon Starler

//server_java_tcp.java

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

class server_java_tcp {

	int port;
	String username;
	int command; //from client coming in as bits
	byte[] message;
	byte[] commandmessage;
	byte[] messagestosend;
	Random randomGenerator = new Random(); // UUID
	int generatedUUID;
	String inputs = "";
	int index;
	String file;
	
	//initialize list of usernameUUID combinations
	ArrayList<usernameUUID> list = new ArrayList<usernameUUID>();
	
	//intialize sockets
	ServerSocket serversocket = null;
	Socket connectionSocket = null;
	DataInputStream inFromClient = null;
	DataOutputStream outToClient = null;	
	
	server_java_tcp(int port)
	{
		this.port = port;
		//this.file = file;
	}
	
    public static void main (String args[])
    {
		/* HAD TO REMOVE BECAUSE COULDN'T GET WELCOME.TXT READIN TO WORK
		if (args.length != 2)
		{
			System.err.println("Invalid number of args. Terminating.");
			System.exit(0);
		}
		*/
		
		//call up the constructor for the server
		server_java_tcp server = new server_java_tcp(Integer.parseInt(args[0]));
		
		/*MY ATTEMPT TO READ THE FILE AND PARSE IT ONTO A LINE
		String file = args[1];
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		String welcometext = "";
	
		while ((line = br.readLine()) != null)
		{
			welcometext = welcometext + line;
		}
		br.close();
		*/
		
		//RUN
		server.run();
	}
	
	public void run()
	{
		try
		{
			//Creating new socket for the client to connect
			serversocket = new ServerSocket(port);
		}
		catch(IOException ex)
		{
			System.err.println("Could not bind port. Terminating.");
			System.exit(0);
		}
		
		while (true) 
		{
			try
			{
			//Accepts the request that comes in from the client
			connectionSocket = serversocket.accept();
			//Input Stream from the client attached to the socket
			inFromClient = new DataInputStream(connectionSocket.getInputStream());
			//Creating the output stream back to the client attached to the socket
			outToClient = new DataOutputStream(connectionSocket.getOutputStream());

			}
			catch(IOException ex) 
			{
				System.err.println("Could not open data streams. Terminating");
				System.exit(0);
			}
		
			//reading 0x01 from the client and taking in the username
			try
			{
				//recieves command from client to server
				//written as the next four bytes of this input stream
					//similar to recv(4)
				command = inFromClient.readInt();
				if (command != 1)
					outToClient.writeInt(0);
				else
				{
					//accepted command sent by the server back to the client. Followed by the UUID
					outToClient.writeInt(2);
				}
				
				//read how many bytes to expect
				int usernamelength = inFromClient.readInt();
				message = new byte[usernamelength];
				inFromClient.read(message, 0, usernamelength);			
				
				
				//random number to be used as UUID
				int randomIntUUID = randomGenerator.nextInt(1000);
				list.add(new usernameUUID(new String(message, "UTF-8"), randomIntUUID));
				
				outToClient.writeInt(randomIntUUID);
							
			}	
			catch(IOException ex)
			{
			System.err.println("Couldn't recieve.. :(");
			System.exit(0);
			}
			
			while(true)
			{
				try
				{
					command = inFromClient.readInt();
					
					///  SEND  ///
					if (command == 3)
					{
						//send confirmation byte 3
						outToClient.writeInt(3);
						
						//get message length
						int messagelength = inFromClient.readInt();
						commandmessage = new byte[messagelength];
						
						//read message up until the message.length
						inFromClient.read(commandmessage, 0, messagelength);
						
						//get the generated UUID from the client
						generatedUUID = inFromClient.readInt();

						//grab the corresponding UUID in the list of usernameUUID's						
						for(int i=0; i < list.size(); i++)
						{
							if (list.get(i).uuidd == generatedUUID){
								index = i;
								break;
							}
						}
									
						// add the message that was recieved to the index of the
						//corresponding uuid
						list.get(index).appendMessage(new String(commandmessage));
						
					}
			
					///  PRINT  ///
					if (command == 4)
					{
						outToClient.writeInt(4);
						//send confirmation byte 4
						
						//for all the usernames in the list
						for (usernameUUID you : list)
						{
							try
							{
								//for all messages within the specific username
								for(String s : you.arraymessages)
								{

									inputs = s;
									// keeps looping and sending 9 to make sure the 
									// client is constantly ready to receive until the
									// messages are done looping, and 9 is no longer sent
									outToClient.writeInt(9);

									//convert username to bytes to send
									messagestosend = you.username.getBytes("UTF-8");
									
									//send length so client can expect a certain length
									//for username
									outToClient.writeInt(messagestosend.length);
									
									//send username
									outToClient.write(messagestosend);
									
									//convert the messages to bytes to send
									messagestosend = inputs.getBytes("UTF-8");
									
									//send length of messages
									outToClient.writeInt(messagestosend.length);
									
									//send message
									outToClient.write(messagestosend);
									
									//keep looping
								}
																						
								
							}
							catch(IOException ex)
							{
								System.err.println("Couldn't print.");
								System.exit(0);
							}
							

						}
						//send 8 to stop the loop of printing messages
						outToClient.writeInt(8);
					}
					
					//  EXIT  //
					if (command == 5)
					{
						//send confirmation int 5
						outToClient.writeInt(5);

						//close connections to the client
						connectionSocket.close();
						outToClient.close();
						inFromClient.close();
						break;
					
					}
				}
				catch(IOException ex)
				{
					System.err.println("No valid command was sent. Terminating.");
					System.exit(0);
				}
			}
		}
    }
	
	public class usernameUUID
	{
		String username;
		int uuidd;
		ArrayList<String> arraymessages = new ArrayList<String>();
		
		//Constructor that initializes username and UUID
		usernameUUID(String username, int uuidd)
		{
			this.username = username;
			this.uuidd = uuidd;
		}
		
		public void setUsername(String username)
		{
			this.username = username;
		}
		
		public void setUUIDD(int uuidd)
		{
			this.uuidd = uuidd;
		}
		
		public ArrayList<String> getMessages()
		{
			return arraymessages;
		}
		
		public void appendMessage(String mesage)
		{
			arraymessages.add(mesage);
		}
		
		public String getUsername()
		{
			return username;
		}
		
		public int getUUIDD()
		{
			return uuidd;
		}
		
	}
}
