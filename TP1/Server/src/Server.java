import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
	private static ServerSocket listener;
	private static ArrayList<MessageHandler> clientList = new ArrayList<MessageHandler>();
	private static HashMap<String, String> identificationDb = new HashMap<String, String>();
	private static HashMap<String, ArrayList<String>> chatHistory = new HashMap<String, ArrayList<String>>();
	private static ArrayList<String> chat = new ArrayList<String>();
	private static String ipAdress;
	// Application Server

	public static void main(String[] args) throws Exception {

		// Le Compteur incremente chaque connexion d'un client au serveur
		// todo : refacto ? j ai pas vraiment lu ces conditions mais bon,gg
		int clientNumber = 0;

		lireFichier();
		// Adresse et port du serveur
		Scanner myObj = new Scanner(System.in); // Create a Scanner object
		// Input d'entrée de l'adresse IP
		System.out.println("Entrez l'adresse IP du poste sur lequel s’exécute le serveur: ");
		ipAdress = myObj.nextLine(); // Read user input
		while (!verifierAdresseIp(ipAdress)) {
			System.out.println("Adresse IP invalide,veuillez reessayer");
			System.out.println("Entrez l'adresse IP du poste sur lequel s’exécute le serveur: ");
			ipAdress = myObj.nextLine(); // Read user input
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
	static boolean lireFichier() {
		try {
			File myObj = new File("src/BD_Identification.txt");
			Scanner myReader = new Scanner(myObj);
			identificationDb = new HashMap<String, String>();
			if (myReader.hasNext())
				myReader.nextLine();
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();
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
	}

	private static class MessageHandler {
		private Socket socket;
		private DataOutputStream out;
		private DataInputStream in;

		public MessageHandler(Socket socket) throws IOException {
			this.socket = socket;
			// Creation d'un canal sortant pour snygyer des messages au client
			this.out = new DataOutputStream(socket.getOutputStream());
			this.in = new DataInputStream(socket.getInputStream());

		}

		void sendToMe(String message) {
			try {
				this.out.writeUTF(message);
				this.out.flush();
			} catch (IOException ioe) {
				System.out.println("Failed to send message to user");
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
		private Socket socket;
		private int clientNumber;
		private MessageHandler messageHandler;
		public ClientHandler(Socket socket, int clientNumber) throws IOException {
			this.socket = socket;
			this.clientNumber = clientNumber;
			System.out.println("New connection with client#" + clientNumber + " at " + socket);
			// Creation d'un canal sortant pour snygyer des messages au client
			this.messageHandler = new MessageHandler(socket);

		}

		// Une thread se charge d'envoyer au client un message de bienvenue
		public void run() {

			try {
				// Envoie d'un message au client
				String username = "";
				String password = "";
				this.messageHandler.sendToMe("veuillez entrer votre username ");
				username = this.messageHandler.receiveMessage();
				System.out.println("username "+ username);
				this.messageHandler.sendToMe("veuillez entrer votre mot de passe ");
				password = this.messageHandler.receiveMessage();
				System.out.println("password "+ password);

				// validation de l informaion du client( username et mdp) maybe refacto?
				if (identificationDb.containsKey(username) && !identificationDb.get(username).equals(password)) {
					this.messageHandler.sendToMe("votre mot de passe est icorrect, la connexion va se fermer");
					System.out.println("Connection with client closed");
					try {
						socket.close();
					} catch (IOException e) {
						System.out.println("Couldn't close a socket, what's going on?");
					}
				} else if (!identificationDb.containsKey(username)) {
					//identificationDb.put(username, password);
					Server.clientList.add(this.messageHandler);
					this.messageHandler.sendToMe("ce nom d utilisateur n existe pas. Un nouveau compte a ete cree pour vous");
				}
				// question chargé: le out de la prochaine ligne apparait apres que le client
				// quitte le chat
				else {
					Server.clientList.add(this.messageHandler);
				}
				this.messageHandler.sendToMe("Bienvenue " + username);
				System.out.println(username + " " + password);
				boolean done = false;
				// lecture de chaque ligne du chat pour un seul client, pas encore essayé pour
				// de multiples clients
				// todo : essayer pour multiples clients, clean up, refactorisation
				while (!done) {
						String line = this.messageHandler.receiveMessage();
						System.out.println("message : " + line);
						this.messageHandler.sendToAllUsers(line);
						done = line.equals(".bye");
						if (chat.size() < 15)
							chat.add(line);
						else {
							chat.remove(0);
							chat.add(line);
						}
					}
			}finally {
				try {
					// Fermeture de la connexion avec le client
					chatHistory.put(ipAdress, chat);
					// ecriture de l historique de chat (15 dernieres lignes)
					// todo : lire le fichier existant et ecraser les donnees deja ecrite si on a
					// une historique du serveur actuel
					// todo : tester l ecriture pour plusieurs serveurs
					try {
						FileWriter myWriter = new FileWriter("chat_history.txt");
						String newLine = System.getProperty("line.separator");
						myWriter.write(ipAdress + newLine);
						for (int i = 0; i < chat.size(); i++) {
							myWriter.write(chat.get(i) + newLine);
						}
						myWriter.close();
						System.out.println("Successfully wrote to the file.");
					} catch (IOException e) {
						System.out.println("An error occurred.");
						e.printStackTrace();
					}
					System.out.println(chatHistory);
					socket.close();
				} catch (IOException e) {
					System.out.println("Couldn't close a socket, what's going on?");
				}
				System.out.println("Connection with client# " + clientNumber + " closed");
			}
		}
	}
}
