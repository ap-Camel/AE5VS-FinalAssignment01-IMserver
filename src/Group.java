import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Group {

	public  String password;
	public  String ownerName;
	public  String groupName;
	public  ArrayList<ClientHandler> clientHandlers;
	public  Map<ClientHandler, Integer> clientHandlersN;
	public  ArrayList<String> chat;
	
	public Group() {
		password = "";
		ownerName = "Guest";
		groupName = ownerName + "'s group";
		clientHandlers = new ArrayList<ClientHandler>();
		clientHandlersN = new HashMap<ClientHandler, Integer>();
		chat = new ArrayList<String>();
	}
	
	public Group(String pass, String owner) {
		password = pass;
		ownerName = owner;
		groupName = ownerName + "'s group";
		clientHandlers = new ArrayList<ClientHandler>();
		clientHandlersN = new HashMap<ClientHandler, Integer>();
		chat = new ArrayList<String>();
	}
	
	public Group(String pass, String owner, String groupName) {
		password = pass;
		ownerName = owner;
		this.groupName = groupName;
		clientHandlers = new ArrayList<ClientHandler>();
		clientHandlersN = new HashMap<ClientHandler, Integer>();
		chat = new ArrayList<String>();
	}
	
	public Group(String pass, String owner, ClientHandler client) {
		password = pass;
		ownerName = owner;
		groupName = ownerName + "'s group";
		clientHandlers = new ArrayList<ClientHandler>();
		clientHandlersN = new HashMap<ClientHandler, Integer>();		
		clientHandlers.add(client);
		clientHandlersN.put(client, 0);
		chat = new ArrayList<String>();
	}
	
	public Group(String pass, String owner, ClientHandler client, Integer times) {
		password = pass;
		ownerName = owner;
		groupName = ownerName + "'s group" + times;
		clientHandlers = new ArrayList<ClientHandler>();
		clientHandlersN = new HashMap<ClientHandler, Integer>();		
		clientHandlers.add(client);
		clientHandlersN.put(client, 0);
		chat = new ArrayList<String>();
	}
	
	public Group(String pass, String owner, ClientHandler client, String groupName) {
		password = pass;
		ownerName = owner;
		this.groupName = groupName;
		clientHandlers = new ArrayList<ClientHandler>();
		clientHandlersN = new HashMap<ClientHandler, Integer>();
		clientHandlers.add(client);
		clientHandlersN.put(client, 0);
		chat = new ArrayList<String>();
	}
	
	public boolean UserExist(String user) {
		for(ClientHandler client:clientHandlers) {
			if(client.userName == user) {
				return true;
			}
		}
		return false;
	}
}
