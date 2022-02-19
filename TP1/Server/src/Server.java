import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Server {
	private static ServerSocket listener;
	private static ArrayList<MessageHandler> clientList = new ArrayList<MessageHandler>();
	private static HashMap<String, String> identificationDb = new HashMap<String, String>();
	private static JSONArray listOfServers = new JSONArray();
	private static JSONArray listOfMembers = new JSONArray();
	private static JSONObject serverDB = new JSONObject();
	private static JSONArray chatDB =  new JSONArray();
	private static HashMap<String, ArrayList<String>> chatHistory = new HashMap<String, ArrayList<String>>();
	private static ArrayList<String> chat = new ArrayList<String>();
	private static String ipAdress;
	private static Scanner myObj;
	// Application Server

	public static void main(String[] args) throws Exception {

		// Le Compteur incremente chaque connexion d'un client au serveur
		// todo : refacto ? j ai pas vraiment lu ces conditions mais bon,gg
		int clientNumber = 0;
		//lireFichier();
		 listOfServers = JSONFileHandler.readJSONFile("src/Server_chat_handler.json");
		// Adresse et port du serveur
		myObj = new Scanner(System.in); // Create a Scanner object
		// Input d'entrée de l'adresse IP
		System.out.println("Entrez l'adresse IP du poste sur lequel s’exécute le serveur: ");
		ipAdress = myObj.nextLine(); // Read user input
		while (!verifierAdresseIp(ipAdress)) {
			System.out.println("Adresse IP invalide,veuillez reessayer");
			System.out.println("Entrez l'adresse IP du poste sur lequel s’exécute le serveur: ");
			ipAdress = myObj.nextLine(); // Read user input
		}
		serverDB= JSONFileHandler.findTheRightServer(ipAdress,listOfServers) ;
		if(serverDB==null)
		{
			listOfServers = JSONFileHandler.insertNewServer(ipAdress,listOfServers);
			serverDB= JSONFileHandler.findTheRightServer(ipAdress,listOfServers) ;
		}
		

		System.out.println("IP adress is: " + ipAdress); // Output user input

		System.out.println("Entrez le port d'écoute : ");
		String port = myObj.nextLine(); // Read user input
		int portNumber = 0;
		if (isParsable(port)) {
			portNumber = Integer.parseInt(port);
		}

		while (portNumber < 5000 || portNumber > 5050) {
			System.out.println("Port entré est invalide");
			System.out.println("Entrez le port d'écoute : ");
			port = myObj.nextLine(); // Read user input
			if (isParsable(port))
				portNumber = Integer.parseInt(port);
		}
		System.out.println("port is: " + port);

		String serverAddress = "127.0.0.1";
		int serverPort = 5000;
		// Cretation de la connexion pour comunitats les clients
		listener = new ServerSocket();
		listener.setReuseAddress(true);
		InetAddress serverIP = InetAddress.getByName(serverAddress);
		// Association de l'adresse et suport a la connexion
		listener.bind(new InetSocketAddress(serverIP, serverPort));
		// listener.accept();
		System.out.format("The server is running on %s:%d%n", serverAddress, serverPort);
		try {
			// A chaque fois qu'un nouveau client se connecte, on exécute la fonction
			// Run() de l'objet clientHandler.

			while (true) {
				// Important : le fonction accept() est bloquante attend qu'un prochain client
				// se seneste
				// Une nouvelle connection on incemente le compteur clienthumber
				new ClientHandler(listener.accept(), clientNumber++).start();

			}
		} finally

		{
			// Fronture de la connexion
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
		System.out.println(adresseIp);
		String[] parts = adresseIp.split("\\.");
		if (parts.length < 4 || parts.length > 4) {
			return false;
		}
		for (String part : parts) {
			if (isParsable(part)) {
				int partIpAddress = Integer.parseInt(part);
				if (partIpAddress < 0 || partIpAddress > 999) {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}

	// lire le fichier d'identification et verifier la correspondace username/mdp
	// todo : ? lire le fichier des historiques aussi
	/*static boolean lireFichier() {
		try {
			File myObj = new File("src/BD_Identification.txt");
			Scanner myReader = new Scanner(myObj);
			identificationDb = new HashMap<String, String>();
			//if (myReader.hasNext())
				//myReader.nextLine();
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();
				System.out.println(data);
				String[] parts = data.split(" ");
				identificationDb.put(parts[0], parts[1]);
			}
			System.out.println(identificationDb);
			myReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		return true;
	}*/

	static void ecrireFichier(String username, String password) {
		try {
			FileWriter myWriter = new FileWriter("src/BD_Identification.txt",true);
			myWriter.write(username + " " + password+"\r\n");
			myWriter.close();
			System.out.println("Successfully wrote to the identification");
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	private static class MessageHandler {
		private Socket socket;
		private DataOutputStream out;
		private DataInputStream in;

		public MessageHandler(Socket socket) throws IOException {
			this.socket = socket;
			// Creation d'un canal sortant pour snygyer des messages au client
			this.out = new DataOutputStream(this.socket.getOutputStream());
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
			System.out.println("username " + username);
			password = this.messageHandler.receiveMessage();
			System.out.println("password " + password);

			// validation de l informaion du client( username et mdp) maybe refacto?
			if (JSONFileHandler.userExists(username,serverDB) && !JSONFileHandler.isPassWordValid(username,password,serverDB)) {
				this.messageHandler.sendToMe("Erreur dans la saisie du mot de passe");
				System.out.println("Connection with client closed");
				try {
					socket.close();
				} catch (IOException e) {
					System.out.println("Couldn't close a socket, what's going on?");
				}
			} else if (!JSONFileHandler.userExists(username,serverDB)) {
				// identificationDb.put(username, password);
				listOfMembers= JSONFileHandler.addANewMember(username,password,serverDB);
				//Server.clientList.add(this.messageHandler);
				//Server.ecrireFichier(username, password);
				this.messageHandler
						.sendToMe("ce nom d utilisateur n existe pas. Un nouveau compte a ete cree pour vous");
			} else {
				Server.clientList.add(this.messageHandler);
			}
			chat = JSONFileHandler.readChatHistory(serverDB);
			this.messageHandler.sendToMe("Bienvenue " + username);
		    for(int i = 0; i<chat.size();i++)
		    {
		    	this.messageHandler.sendToMe(chat.get(i));
		    }
			System.out.println(username + " " + password);
		}

		public void run() {
			// thread qui envoie et recoit les messages au client
			new Thread(new Runnable() {
				String lineMessage = "";

				@Override
				public void run() {
					lineMessage = messageHandler.receiveMessage();
					while (!lineMessage.equals("bye")) {
						
						System.out.println(lineMessage);
						messageHandler.sendToAllUsers(lineMessage);
						if (chat.size() < 15)
							chat.add(lineMessage);
						else {
							chat.remove(0);
							chat.add(lineMessage);
						}
						lineMessage = messageHandler.receiveMessage();
					}
					chatDB = JSONFileHandler.replaceChat(chat,serverDB);
					//chatHistory.put(ipAdress, chat);
					// ecriture de l historique de chat (15 dernieres lignes)
					// todo : lire le fichier existant et ecraser les donnees deja ecrite si on a
					// une historique du serveur actuel
					// todo : tester l ecriture pour plusieurs serveurs
					try {
						JSONFileHandler.updateJsonFile("src/Server_chat_handler.json" , ipAdress, serverDB,listOfServers);;
					//	}
					//	myWriter.close();
						System.out.println("Successfully wrote to the file.");
					} catch (Exception e) {
						System.out.println("An error occurred.");
						e.printStackTrace();
					}
					System.out.println(chatDB);
					try {
						socket.close();
					} catch (IOException e) {
						System.out.println("Couldn't close a socket, what's going on?");
					}
					System.out.println("Connection with client# " + clientNumber + " closed");
				}
			}).start();
		}
	}
}
