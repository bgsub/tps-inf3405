import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	private static ServerSocket listener;
	// Application Server

	public static void main(String[] args) throws Exception
	{
		//Il Compteur incremente chegue connexion d'un client au serveur
		int clientNumber = 0 ;
		// Adresse et port du secev
		String serverAddress = "127.0.0.1";
		int serverPort = 5000;
		// Cretation de la connexion pour comunitats les clients
		listener =new ServerSocket();
		listener.setReuseAddress(true);
		InetAddress serverIP = InetAddress.getByName(serverAddress);
		// Association de l'adresse et suport a la connexion
		listener.bind(new InetSocketAddress(serverIP, serverPort));
		System.out.format("The server is running on %s:%d%n", serverAddress, serverPort);
		try
		{
			//A chaque fois qu'un nouveau client se connecte, on exécute la fonction
			//Run() de l'objet clientHandler.

			while (true)
			{
				// Important : le fonction accept() est bloquante attend qu'un prochain client se seneste
				// Une nouvelle connection on incemente le compteur clienthumber
				new ClientHandler(listener.accept(), clientNumber++).start();
			}
		}finally

		{
			// Fronture de la connexion
			listener.close();
		}
	}


	/*Une thread qui se charge de traiter la demande de chaque client
sur un socket particulier*/
	private static class ClientHandler extends Thread
	{
		private Socket socket;
		private int clientNumber;
		public ClientHandler(Socket socket, int clientNumber)
		{
			this.socket = socket;
			this.clientNumber = clientNumber;
			System.out.println("New connection with client#" + clientNumber+" at "+socket);
		}
		//Une thread se charge d'envoyer au client un message de bienvenue
		public void run()
		{
			try
			{ 
				// Creation d'un canal sortant pour snygyer des messages au client
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				// Envoie d'un message au client
				out.writeUTF("Hello from server you are client#" + clientNumber);
			} catch (IOException e) {
				System.out.println("Error handling client# " + clientNumber + ":" + e);
			}
			finally
			{ 
				try {
					// Fermeture de la connexion avec le client
					socket.close();
				}
				catch (IOException e) {
					System.out.println("Couldn't close a socket, what's going on?");
				}
				System.out.println("Connection with client# " + clientNumber + " closed");
			}
		}
	}
}

