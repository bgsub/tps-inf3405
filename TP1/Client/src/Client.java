import java.io.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	private static Socket socket;
	private static String ipAdress;
	private static int portNumber;
	private static String username;
	final static int MAX_MESSAGE_LENGTH = 200;
	final static int MIN_PORT = 5000;
	final static int MAX_PORT = 5050;
	final static String END_MESSAGE = "bye";
	/*
	 * Application client
	 */
	public static void main(String[] args) throws Exception {
		// Adresse et port du serveur
		Scanner myObj = new Scanner(System.in);
		// Input d'entrée de l'adresse IP
		System.out.println("Entrez l'adresse IP du serveur: ");
		ipAdress = myObj.nextLine();
		while (!verifierAdresseIp(ipAdress)) {
			System.out.println("Adresse IP invalide,veuillez reessayer");
			System.out.println("Entrez l'adresse IP du serveur: ");
			ipAdress = myObj.nextLine();
		}

		System.out.println("IP adress is: " + ipAdress);

		System.out.println("Entrez le port d'écoute : ");
		String port = myObj.nextLine();
		portNumber = 0;
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
		System.out.println("Port is: " + portNumber);
		// Création d'une nouvelle connexion avec le serveur
		socket = new Socket(ipAdress, portNumber);
		// Création d'un canal entrant pour recevoir les messages envoyés par le serveur
		// Création d'un canal sortant pour envoyer les messages au serveur
		DataInputStream in = new DataInputStream(socket.getInputStream());
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());

		// authentification des utilisateurs
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
			System.out.println("Vous avez été deconnecté ,mot de passe invalide !");
			socket.close();
			myObj.close();
			return;
		}
		//////
		// chat du client
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
			String lineMessage = myObj.nextLine();

			@Override
			public void run() {
				try {
					while (!lineMessage.equals(END_MESSAGE)) {
						if (lineMessage.length() <= MAX_MESSAGE_LENGTH) {
							out.writeUTF(messageFormatter(username, lineMessage));
							out.flush();
						} else {
							System.out.println("Message non envoyé : Taille du message dépasse 200 caractéres !");
						}
						lineMessage = myObj.nextLine();

					}
					out.writeUTF(messageFormatter(username, lineMessage));
					out.flush();
					// Fermeture de la connexion aves le serveur
					socket.close();
					myObj.close();
					System.out.println("Vous avez quitté le chat !");
				} catch (IOException ioe) {
					System.out.println("Message received Failed : " + ioe.getMessage());
				}
			}
		});
		sender.start();
	}

	// méthodes
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
		message = "[" + username + " - " + ipAdress + ":" + portNumber + " - " + date.format(now) + "@"
				+ time.format(now) + "]: " + message;
		return message;
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
}
