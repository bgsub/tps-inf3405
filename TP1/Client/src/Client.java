import java.io.*;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	private static Socket socket;
	private static String ipAdress;
	private static int portNumber;
	private static String username;

	/*
	 * Application client
	 */
	public static void main(String[] args) throws Exception {
		// Adresse et port du serveur
		Scanner myObj = new Scanner(System.in); // Create a Scanner object
		// Input d'entr�e de l'adresse IP
		System.out.println("Entrez l'adresse IP du serveur: ");
		ipAdress = myObj.nextLine(); // Read user input 
		while (!verifierAdresseIp(ipAdress)) {
			System.out.println("Adresse IP invalide,veuillez reessayer");
			System.out.println("Entrez l'adresse IP du serveur: ");
			ipAdress = myObj.nextLine(); // Read user input
		}

		System.out.println("IP adress is: " + ipAdress); // Output user input

		System.out.println("Entrez le port d'�coute : ");
		String port = myObj.nextLine(); // Read user input
		portNumber = 0;
		if (isParsable(port)) {
			portNumber = Integer.parseInt(port);
		}

		while (portNumber < 5000 || portNumber > 5050) {
			System.out.println("Port entr� est invalide");
			System.out.println("Entrez le port d'�coute : ");
			port = myObj.nextLine(); // Read user input
			if (isParsable(port))
				portNumber = Integer.parseInt(port);
		}
		System.out.println("port is: " + port);

		String serverAddress = "127.0.0.1";
		int portN = 5000;
		// Cr�ation d'une nouvelle connexion avec le serveur
		socket = new Socket(serverAddress, portN);
		System.out.format("The server is running on %s:%d%n", serverAddress, portN);
		// Cr�ation d'un canal entrant pour recevoir les messages envoy�s par le serveur
		// Cr�ation d'un canal sortant pour envoyer les messages au serveur
		DataInputStream in = new DataInputStream(socket.getInputStream());
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());

		// authentification des utilisateurs
		//// todo: refacto: creer une fonction pour ce bloc
		String messageConnexion, password = "";
		System.out.println("veuillez entrer votre username : ");
		username = myObj.nextLine();
		out.writeUTF(username);
		System.out.println("veuillez entrer votre mot de passe : ");
		password = myObj.nextLine();
		out.writeUTF(password);
		messageConnexion = in.readUTF();
		System.out.println(messageConnexion);
		if (messageConnexion.equals("Erreur dans la saisie du mot de passe")) {
			System.out.println("Vous avez �t� deconnect� ,mot de passe invalide !");
			socket.close();
			myObj.close();
			return;
		}
		//////
		// chat du client
		// todo: remplacer bryan par les usernames indiqu�s dans l �nonc� et ajouter le
		// temps et le reste de bails
		// toodo: refacto : creer une fonction pour ce bloc
		Thread receiver = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String message = in.readUTF();
					while (!message.equals("bye")) {
						System.out.println(message);
						message = in.readUTF();
					}
				} catch (IOException ioe) {
					System.out.println("Message received Failed : " + ioe.getMessage());
				}
			}
		});
		receiver.start();
		Thread sender = new Thread(new Runnable() {
			String lineMessage =myObj.nextLine();

			@Override
			public void run() {
				try {
					while (!lineMessage.equals("bye")) {
						System.out.println(lineMessage);
						if (lineMessage.length() <= 200) {
							out.writeUTF(messageFormatter(username, lineMessage));
							out.flush();
						} else {
							System.out.println("Message non envoy� : Taille du message d�passe 200 caract�res !");
						}
						lineMessage = myObj.nextLine();
						
					}
					out.writeUTF(messageFormatter(username, lineMessage));
				    out.flush();
					System.out.println("Vous avez quitt� le chat !");
					// Fermeture de la connexion aves le serveur
					socket.close();
					myObj.close();
				} catch (IOException ioe) {
					System.out.println("Message received Failed : " + ioe.getMessage());
				}
			}
		});
		sender.start();
	}

	// m�thodes
	static boolean isParsable(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch (final NumberFormatException e) {
			return false;
		}
	}
	
	static String messageFormatter(String username, String message) {
		DateTimeFormatter date = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		DateTimeFormatter time = DateTimeFormatter.ofPattern("HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		message = "[" + username + " - " + ipAdress + ":" + portNumber + " - " + date.format(now)
		+ "@" + time.format(now) + "]: " + message;
		return message;
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
				if (partIpAddress < 0 || partIpAddress > 255) {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}
}
