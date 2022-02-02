import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client
{
	private static Socket socket;
	private static PrintWriter output;
	/* Application client
	 */
	public static void main(String[] args) throws Exception
	{
		//Adresse et port du serveur
		// Adresse et port du serveur
	    Scanner myObj = new Scanner(System.in);  // Create a Scanner object
	    //Input d'entrée de l'adresse IP
	    System.out.println("Entrez l'adresse IP du serveur: ");
	    String ipAdress = myObj.nextLine();  // Read user input
	    while(!verifierAdresseIp(ipAdress)) {
	    	System.out.println("Adresse IP invalide,veuillez reessayer");
		    System.out.println("Entrez l'adresse IP du serveur: ");
		    ipAdress = myObj.nextLine();  // Read user input
	    }

	    System.out.println("IP adress is: " + ipAdress);  // Output user input
	    
	    System.out.println("Entrez le port d'écoute : ");
	    String port = myObj.nextLine();  // Read user input
	    int portNumber = 0;
	    if(isParsable(port)) {
	    	portNumber = Integer.parseInt(port);
	    }

	    while(portNumber <5000 || portNumber >5050 ) {
	    	System.out.println("Port entré est invalide");
		    System.out.println("Entrez le port d'écoute : ");
		    port = myObj.nextLine();  // Read user input
		    if(isParsable(port))
		    	portNumber = Integer.parseInt(port);
	    }
	    System.out.println("port is: " + port);
	    
		String serverAddress = "127.0.0.1";
		int portN = 5000;
		// Création d'une nouvelle connexion avec le serveur
		 socket = new Socket(serverAddress, portN);
		System.out.format("The server is running on %s:%d%n", serverAddress, portN);
		// Création d'un canal entrant pour recevoir les messages envoyés par le serveur
		// Création d'un canal sortant pour envoyer les messages au serveur
		DataInputStream in = new DataInputStream(socket.getInputStream());
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		
		// authentification des utilisateurs
		//// todo: refacto: creer une fonction pour ce bloc 
		String q1,q2,messageConnexion,username,password=  "";
		String ok = "";
		q1 = in.readUTF();
		System.out.println(q1);
	     username = myObj.nextLine();
		out.writeUTF(username);
		q2 = in.readUTF();
		System.out.println(q2);
		password = myObj.nextLine();
		out.writeUTF(password);
		messageConnexion = in.readUTF();
		System.out.println(messageConnexion);
		//////
		 String line = "";
		 // chat du client 
		 // todo: remplacer bryan par les usernames indiqués dans l énoncé et ajouter le temps et le reste de bails
		 // toodo: refacto : creer une fonction pour ce bloc
	      while (!line.equals("bye"))
	      {  try
	         {  line = myObj.nextLine();
	            out.writeUTF("bryan " + line);
	            out.flush();
	         }
	         catch(IOException ioe)
	         {  System.out.println("Sending error: " + ioe.getMessage());
	         }
	      }
		// Création d'un canal entrant pour recevoir les messages envoyés par le serveur
		//DataInputStream in = new DataInputStream(socket.getInputStream());
		
		// Attente de la réception d'un message envoyé par le serveur sur le canal
		String helloMessageFromServer = in.readUTF();
		System.out.println(helloMessageFromServer);
		
		// Fermeture de la connexion aves le serveur
		socket.close();
	}
	//méthodes
	static boolean isParsable(String input) {
	    try {
	        Integer.parseInt(input);
	        return true;
	    } catch (final NumberFormatException e) {
	        return false;
	    }
	}
	static boolean verifierAdresseIp(String adresseIp){
		System.out.println(adresseIp);
		String[] parts = adresseIp.split("\\.");
		if(parts.length < 4 || parts.length > 4) {
			return false;
		}
		for (String part : parts) {
			if(isParsable(part)) {
				int partIpAddress = Integer.parseInt(part);
				if(partIpAddress < 0 || partIpAddress > 999) {
					return false;
				}
			}else {
				return false;
			}
		}
		return true;
	}
}

