import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class Server {
	private static ServerSocket listener;
	private static ArrayList<MessageHandler> clientList = new ArrayList<MessageHandler>();
	private static JSONArray listOfServers = new JSONArray();
	private static JSONArray listOfMembers = new JSONArray();
	private static JSONObject serverDB = new JSONObject();
	private static JSONArray chatDB =  new JSONArray();
	private static ArrayList<String> chat = new ArrayList<String>();
	private static String ipAdress;
	private static Scanner myObj;
	private static int numberOfClients = 0;
	final static int MIN_PORT = 5000;
	final static int MAX_PORT = 5050;
	// Application Server

	public static void main(String[] args) throws Exception {
		 listOfServers = JSONFileHandler.readJSONFile("src/Server_chat_handler.json");
		// Adresse et port du serveur
		myObj = new Scanner(System.in); // Create a Scanner object
		// Input d'entrée de l'adresse IP
		System.out.println("Entrez l'adresse IP du poste sur lequel s’exécute le serveur: ");
		ipAdress = myObj.nextLine();
		while (!verifierAdresseIp(ipAdress)) {
			System.out.println("Adresse IP invalide,veuillez reessayer");
			System.out.println("Entrez l'adresse IP du poste sur lequel s’exécute le serveur: ");
			ipAdress = myObj.nextLine();
		}
		serverDB= JSONFileHandler.findTheRightServer(ipAdress,listOfServers) ;
		if(serverDB==null)
		{
			listOfServers = JSONFileHandler.insertNewServer(ipAdress,listOfServers);
			serverDB= JSONFileHandler.findTheRightServer(ipAdress,listOfServers) ;
		}
		if(numberOfClients==0) chat = JSONFileHandler.readChatHistory(serverDB);

		System.out.println("IP adress is: " + ipAdress); 
		System.out.println("Entrez le port d'écoute : ");
		String port = myObj.nextLine(); 
		int portNumber = 0;
		if (isParsable(port)) {
			portNumber = Integer.parseInt(port);
		}

		while (portNumber < MIN_PORT || portNumber > MAX_PORT) {
			System.out.println("Port entré est invalide");
			System.out.println("Entrez le port d'écoute : ");
			port = myObj.nextLine(); 
			if (isParsable(port))
				portNumber = Integer.parseInt(port);
		}
		System.out.println("port is: " + port);

		// Cretation de la connexion pour communiquer les clients
		listener = new ServerSocket();
		listener.setReuseAddress(true);
		InetAddress serverIP = InetAddress.getByName(ipAdress);
		// Association de l'adresse et suport a la connexion
		listener.bind(new InetSocketAddress(serverIP, portNumber));
		System.out.format("The server is running on %s:%d%n", ipAdress, portNumber);
		try {
			while (true) {
				new ClientHandler(listener.accept(), numberOfClients++).start();

			}
		} finally

		{
			listener.close();
		}

	}

	static boolean isParsable(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch (final NumberFormatException e) {
			return false;
		}
	}

	static boolean verifierAdresseIp(String adresseIp) {
		final int MAX_IP_PARTS = 4;
		final int MAX_OCT = 255;
		String[] parts = adresseIp.split("\\.");
		if (parts.length < MAX_IP_PARTS || parts.length > MAX_IP_PARTS) {
			return false;
		}
		for (String part : parts) {
			if (isParsable(part)) {
				int partIpAddress = Integer.parseInt(part);
				if (partIpAddress < 0 || partIpAddress > MAX_OCT) {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}

	private static class MessageHandler {
		private Socket socket;
		private DataOutputStream out;
		private DataInputStream in;

		public MessageHandler(Socket socket) throws IOException {
			this.socket = socket;
			// Creation d'un canal sortant pour envoyer des messages au client
			this.out = new DataOutputStream(this.socket.getOutputStream());
			// Creation d'un canal entrant pour recevoir des messages au client
			this.in = new DataInputStream(this.socket.getInputStream());

		}

		void sendToMe(String message) {
			try {
				this.out.writeUTF(message);
				this.out.flush();
			} catch (IOException ioe) {
				System.out.println("Failed to send message");
			}
		}

		String receiveMessage() {
			String message = "";
			try {
				message = this.in.readUTF();
			} catch (IOException ioe) {
				System.out.println("Failed to received message");
			}
			return message;
		}

		void sendToAllUsers(String message) {
			for (MessageHandler user : Server.clientList) {
				user.sendToMe(message);
			}
		}
	}

	/*
	 * Une thread qui se charge de traiter la demande de chaque client sur un socket
	 * particulier
	 */
	private static class ClientHandler extends Thread {
		Socket socket;
		int clientNumber;
		MessageHandler messageHandler;

		public ClientHandler(Socket socket, int clientNumber) {
			this.socket = socket;
			this.clientNumber = clientNumber;
			System.out.println("New connection with client#" + clientNumber + " at " + socket);
			try {
				this.messageHandler = new MessageHandler(socket);
				this.connectToApp();
			} catch (IOException e) {
				System.out.println("Erreur message handler");
			}

		}

		public void connectToApp() {
			String username = "";
			String password = "";
			username = this.messageHandler.receiveMessage();
			password = this.messageHandler.receiveMessage();
			if (JSONFileHandler.userExists(username,serverDB) && !JSONFileHandler.isPassWordValid(username,password,serverDB)) {
				this.messageHandler.sendToMe("Erreur dans la saisie du mot de passe");
				System.out.println("Connection with client closed");
				try {
					socket.close();
				} catch (IOException e) {
					System.out.println("Couldn't close a socket, what's going on?");
				}
			} else if (!JSONFileHandler.userExists(username,serverDB)) {
				listOfMembers= JSONFileHandler.addANewMember(username,password,serverDB);
				Server.clientList.add(this.messageHandler);
				this.messageHandler
						.sendToMe("ce nom d utilisateur n existe pas. Un nouveau compte a ete cree pour vous");
			} else {
				Server.clientList.add(this.messageHandler);
			}
			this.messageHandler.sendToMe("Bienvenue " + username+"\nGuide: Pour quitter le chat ,écrivez le message: 'bye' .");
		    for(int i = 0; i<chat.size();i++)
		    {
		    	this.messageHandler.sendToMe(chat.get(i));
		    }
			System.out.println( "nombre de clients: " + numberOfClients );
		}

		public void run() {
			// thread qui envoie et recoit les messages au client
			new Thread(new Runnable() {
				String lineMessage = "";
				final int CHAT_HISTORY_LENGTH = 15;
				final int BYE_LENGTH = 3;
				@Override
				public void run() {
					lineMessage = messageHandler.receiveMessage();
					String message = lineMessage.substring(lineMessage.length() - BYE_LENGTH);
					while (!message.equals("bye")) {
						
						System.out.println(lineMessage);
						messageHandler.sendToAllUsers(lineMessage);
						if (chat.size() < CHAT_HISTORY_LENGTH)
							chat.add(lineMessage);
						else {
							chat.remove(0);
							chat.add(lineMessage);
						}
						lineMessage = messageHandler.receiveMessage();
						message = lineMessage.substring(lineMessage.length() - BYE_LENGTH);
					}
					
					try {
						socket.close();
						numberOfClients-=1;
					} catch (IOException e) {
						System.out.println("Couldn't close a socket, what's going on?");
					}
					System.out.println("Connection with client# " + clientNumber + " closed");
					if(numberOfClients==0)
					{
					chatDB = JSONFileHandler.replaceChat(chat,serverDB);
					try {
						JSONFileHandler.updateJsonFile("src/Server_chat_handler.json" , ipAdress, serverDB,listOfServers);
						System.out.println("Successfully wrote to the file.");
					} catch (Exception e) {
						System.out.println("An error occurred.");
						e.printStackTrace();
					}
					System.out.println(chatDB);
					}
				}
			}).start();
		}
	}
}
