
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
	public static void main(String[] args) {
		
		try {
			ExecutorService executor = Executors.newFixedThreadPool(10000);
			ServerSocket serverSock = new ServerSocket(1000);	
			HashMap<InetAddress , Integer> numOfConnections = new HashMap<InetAddress, Integer>();
			Scanner scanner = new Scanner(new File("Credentials.txt"));
			HashMap<String, String> usrs = new HashMap<String, String>();
			ClientHandler.credentials = usrs;
			
			ArrayList<ClientHandler> clientHandlers = new ArrayList<ClientHandler>();
			ClientHandler.clientHandlers = clientHandlers;
			
			ArrayList<Group> clientGroups = new ArrayList<Group>();
			ClientHandler.clientGroups = clientGroups;
			clientGroups.add(new Group("0000", "guest"));
			
			String info;
			String[] result;
			while (scanner.hasNext()) {
				info = scanner.nextLine();
				result = info.split(",");
				if(result.length == 2) {
					usrs.put(result[0].strip(), result[1].strip());
				}
				
			}
			System.out.println("the server started");
			while (true) {
				
				Socket s = serverSock.accept();
				InetAddress ip = s.getInetAddress();
				Integer number= numOfConnections.get(ip);
				
				if(number == null || number <= 1000) {
					if(number == null) {
						number =0;
					}
					number++;
					numOfConnections.put(ip, number);
					ClientHandler client=new ClientHandler(s);
//					clientHandlers.add(client);
					executor.execute(client);
					executor.execute(client.queueHandler);
				}
				else {
					s.close();
					System.out.println("Attack Detected!");
				}
				
				
			
				System.out.println("new client connected");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
