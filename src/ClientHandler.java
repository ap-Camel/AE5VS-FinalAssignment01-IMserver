
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
public class ClientHandler implements Runnable {
	public class QueueHandler implements Runnable{

		@Override
		public void run() {
			while(true) {
				try {
					String message = messageInbox.take();
					os.write(message.getBytes());
					os.flush();
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// if something got broken we should remove it from clientHandlers and close it.
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// TODO Auto-generated method stub
			
		}
		
	}
	QueueHandler queueHandler = new QueueHandler();
	
	Socket socket;
	ArrayBlockingQueue<String > messageInbox = new ArrayBlockingQueue<String>(2000);
	static HashMap<String, String> credentials;				//hash map of credentials
	static ArrayList<Group> clientGroups;					//holds all the groups in the server
	ArrayList<Integer> myGroups;							//index of all the groups the client is part of
	static ArrayList<ClientHandler> clientHandlers;			//list of all clients in the server
	boolean groupchat = false;								//if the application is in group chat mode
	String userName;
	int activeGroupIndex;									//the group the user is in now
	int groupsCreated = 0;									//the number of groups the client has created
	
	
	InputStream is;
	OutputStream os;
		
	public ClientHandler(Socket s) {
		this.socket = s;
		myGroups = new ArrayList<Integer>();
		activeGroupIndex = 0;
	}
	
	// check if a group exists with the given password
	public boolean CheckForDuplicateGroup(String password) {
		boolean wasUsed = false;
		for(Group g:clientGroups) {
			if(g.password == password && this.userName == g.ownerName) {
				wasUsed = true;
			}
		}
		return wasUsed;
	}
	
	//create a new group inside the server with the given password
	public void CreateNewGroup(String password) {
		Group group = new Group(password, userName, this, groupsCreated);
		clientGroups.add(group);
		groupsCreated++;		
		myGroups.add(clientGroups.indexOf(group));
		activeGroupIndex = myGroups.size();
	}
	
	public void run() {
		byte[] byteArr = new byte[1000];
		int num;
			
		
		try {
			is = socket.getInputStream();
			os=socket.getOutputStream();
			boolean first = true;
			String [] result;
			String dbPassword;
			
			while ((num = is.read(byteArr)) > 0) {
				if(byteArr[0] == '\r') 
					continue;
				//check if its the first time entering the application to verify the credentials
				if (first)
				{
					first = false;
					String s = new String(byteArr,0,num);
					result = s.split(",");
					userName = result[0];
					if(result.length == 2) {
						dbPassword = credentials.get(result[0]);
						String pswd = result[1].strip();
						if (dbPassword == null || (!dbPassword.equals(pswd)))
						{
							System.out.print("Wrong Credentials\n");
							System.out.print("Result is :" +result[1]+"\nx = " + dbPassword);
							socket.close();
							return;   
						}else {
							clientGroups.get(0).clientHandlers.add(this);
							//clientHandlers.add(this);
						}
						
					}else {
						System.out.print("Missing credentials removing client from server\n");
						socket.close();
						clientHandlers.remove(this);
						return;
						
					}
					byteArr = new byte[1000];
				} else {
					System.out.print("\r\n Client sent: ");
					System.out.write(byteArr, 0, num);
					System.out.flush();
					
					if(byteArr[0] == '#') {
						String command = new String(byteArr, 1, num-1);
						String[] arr = command.split(" ");
						String pass;
						Random rand = new Random();
						switch(arr[0]) {
						// for changing the name of the user
						case "setMyName":
							// check if the user has entered a string for the name
							if(arr.length > 1) {
								userName = arr[1];
								os.write(("your username was changed to " + userName).getBytes());								
							} else {
								os.write("please enter a username".getBytes());
							}	
							byteArr = new byte[1000];
							break;
							
						// case for creating a new group
						// checks if if the password entered or the randomly generated is used for another group
						case "createNewGroup":			
							
							// condition for when a password was given
							if(arr.length > 1) {																
								
								// for checking if the password was already in use and creating a new group
								if(!CheckForDuplicateGroup(arr[1])) {									
									CreateNewGroup(arr[1]);									
									os.write(("\r\ngroups was created, password is: " + arr[1] + "  owner is: " + this.userName).getBytes());																
								} else {
									os.write("\r\npassword already in use".getBytes());
								}
							} 
							// condition for when a password was not given so it is randomly generated
							else {
								while(true) {
									pass = Integer.toString(rand.nextInt(9999));
									if(!CheckForDuplicateGroup(pass)) {										
										CreateNewGroup(pass);										
										os.write(("\r\ngroups was created, password is: " + pass + "  owner is: " + this.userName + "\r\n").getBytes());																		
									}											
										break;
									} 
								}							
							byteArr = new byte[1000];
							break;
						
						// case for joining a new group or if the user was already part of the group
						case "joinGroup":
							
							// checks if the user has entered a password and username
							if(arr.length > 2) {
								for(int i = 0; i < clientGroups.size(); i++) {
									
									if(arr[1].equals(clientGroups.get(i).ownerName) && arr[2].equals(clientGroups.get(i).password)) {
										if(myGroups.contains(i)) {
												os.write("\r\nyou have joined the group\r\n".getBytes());
												activeGroupIndex = i;
												
												int chatSize = clientGroups.get(activeGroupIndex).chat.size();
												// print the missed messages depending on the number of missed messages from notifications
												for(int j = chatSize - clientGroups.get(activeGroupIndex).clientHandlersN.get(this); j < chatSize; j++) {
													os.write(("message: " + clientGroups.get(activeGroupIndex).chat.get(j)).getBytes());													
												}																								
											} 
											// if the user enters the group for the first time
											else {
												clientGroups.get(i).clientHandlers.add(this);	
												clientGroups.get(i).clientHandlersN.put(this, 0);	
												myGroups.add(i);
												activeGroupIndex = i;
												os.write("\r\nyou have joined a new group\r\n".getBytes());
																																																										
											}
									}
								}
							} else {
								os.write("\r\nusername or password is wrong\r\n".getBytes());	
							}
							byteArr = new byte[1000];
							os.write("\r\n".getBytes());
							break;
							
						// case for showing the groups the user is part of
						case "showMyGroups":
							
							// goes through indexes in myGroup array and gets the groups from it
							for(Integer i:myGroups) {
								String message ="\r\ngroupName: " + clientGroups.get(i).groupName + "  username: " + clientGroups.get(i).ownerName + "  password: " + clientGroups.get(i).password;
								this.messageInbox.offer(message);
								
							}
							break;
							
						// just puts some \n so it appears that it clearing but it is not just goes down a bit
						case "clear": 
							//os.write("\\033[H\\033[2J".getBytes());
							os.write("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\r\n".getBytes());
							os.flush();
							break;
						
						// case for changing thename of the group
						case "changeGroupName":
							// checks if a name was entered
							if(arr.length > 1) {
								// checks if the one who is trying to change the name is the group owner
								if(this.userName == this.clientGroups.get(activeGroupIndex).ownerName) {
									clientGroups.get(activeGroupIndex).groupName = arr[1];
									os.write(("\r\nthe group name was changed to: " + arr[1] + "\r\n").getBytes());
								} else {
									os.write("\r\nyou dont have the right to change the name of this group\r\n".getBytes());
								}
							} else {
								os.write("\r\ninvalid group name\r\n".getBytes());
								}
							break;
						
						// show the users notifications, in which groups, how many messages they have missed
						case "showNotifications": 
							for(Integer i:myGroups) {
								if(clientGroups.get(i).clientHandlersN.containsKey(this)) {
									os.write(("\r\nyou have " + clientGroups.get(i).clientHandlersN.get(this) + " notifications from group: " + clientGroups.get(i).groupName).getBytes());
									byteArr = new byte[1000];
									//os.write("\n".getBytes());										
									}
							}
							break;
							
						// show message history depending on the number of desiered messages wanting to be shown
						case"showMessageHistory": 
							// check if number entered and is smaller than the number of all messages in the chat history
							int chatSize = clientGroups.get(activeGroupIndex).chat.size();
							if(arr.length > 1 && Integer.parseInt(arr[1]) < clientGroups.get(activeGroupIndex).chat.size()) {
								
								for(int i = chatSize - Integer.parseInt(arr[1]); i < chatSize; i++) {
									os.write(("message: " + clientGroups.get(activeGroupIndex).chat.get(i)).getBytes());
								}
							} 
							// if the number was bigger just print all messages in history
							else {
								for(int i = 0; i < chatSize; i++) {
									os.write(("message: " + clientGroups.get(activeGroupIndex).chat.get(i)).getBytes());
								}
							}
							os.write("\r\n".getBytes());
							
							break;
							
						// default case 
						default:
							os.write("\r\nnot a valid command\r\n".getBytes());
							break;
						}
					} else {
						String message = userName + ":" + new String(byteArr) + "\r\n";
						byteArr = new byte[1000];
						if(!groupchat) {
							boolean firstEnter = true;
							
							// goes through all the clients in the server
							for(ClientHandler client:clientGroups.get(activeGroupIndex).clientHandlers) {
								
								// so that a message is not added to chat history twice
								if(firstEnter) {
									clientGroups.get(this.activeGroupIndex).chat.add(message);
									firstEnter = false;
								}
								
								// to send the message to the desired users in the group and not sending back to self
								if(client.activeGroupIndex == this.activeGroupIndex && client.userName != this.userName) {
									
									// the actual sending of the message
									if(!client.messageInbox.offer(message)) {
										System.out.print("\r\nClient has a problem with connection");
										System.out.print("\r\nClient name: " + userName);
									}
								}
								
								// if the user is chatting in another group but is part of this group, increment the number of missed messages for this group
								for(Integer i:client.myGroups) {
									if(i == this.activeGroupIndex && client.activeGroupIndex != this.activeGroupIndex) {
										clientGroups.get(activeGroupIndex).clientHandlersN.put(client, clientGroups.get(activeGroupIndex).clientHandlersN.get(client) + 1);
									}
								}
							}
						} else {
							/*
							for(ClientHandler client:groups) {
								if(!client.messageInbox.offer(message)) {
									System.out.print("\r\nClient has a problem with connection");
									System.out.print("\r\nClient name: " + userName);
								}
							}
							*/
						}												
						
					}										
				}								
			}
			System.out.print("\r\nClient disconnected removing client from server!\n");
			clientHandlers.remove(this);
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.print("\r\nClient removed from server!\n");
			clientHandlers.remove(this);
		}
	}
}

