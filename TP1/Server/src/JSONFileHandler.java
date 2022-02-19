
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
//import java.text.ParseException;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

//cette classe permet de lire la base de donnee du cote serveur 
// lire le fichier JSON de notre BD
//  mettre des elemenent dans cette BD

public class JSONFileHandler {

	public static void main(String[] args) {
		JSONArray servers = readJSONFile("src/Server_chat_handler.json");
		System.out.println(servers);
		JSONObject item = findTheRightServer("1.1.0.0",servers);
		// testing findServer
		System.out.println(item);
		// testing user and passwords vilidation
		System.out.println(userExists("bryan",item));
		System.out.println(userExists("claude",item));
		System.out.println(isPassWordValid("bryan","helloWorld",item));
		System.out.println(isPassWordValid("bryan","hellWorld",item));
		System.out.println(isPassWordValid("bran","helloWorld",item));
		System.out.println(isPassWordValid("bran","helloWrld",item));
		// testing adding members
		System.out.println(addANewMember("hello","world",item));
		//testing modification of members list in the server
		 JSONArray members = (JSONArray) item.get("membres");
		 members  =addANewMember("hi","world",item);
		 item.remove("membres");
		 System.out.println(item);
		 item.put("membres",members);
		 System.out.println(members);
		 // testing modification of chat in the server
		 
		 JSONArray chat = (JSONArray) item.get("derniersMessages");
	     ArrayList<String> arr = new ArrayList<String>();
	     arr.add("hello");
	     arr.add("hi");
		 chat  = replaceChat(arr,item);
		 item.remove("derniersMessages");
		 System.out.println(item);
		 item.put("derniersMessages",chat);
		 System.out.println(item);
		 //insering a new server 
		 System.out.println(insertNewServer("1.1.1.0", servers));
		 // testing the JSONFile update
		 updateJsonFile("src/Server_chat_handler.json","1.1.0.0",item,servers);
		 
        }
	
	// lis le fichier et sauvegarde son contenu, supprime l ancienne version du serveur qui est en parametre,
	//remplace celui ci par une nouvelle version passée en parametre, le rajoute dans le serveur, cree un nouveau fichier.
	static void updateJsonFile(String fileName , String ipAdress, JSONObject newServerInstence,JSONArray oldInstance )
	 {
		 System.out.println(oldInstance);
		 JSONObject oldServer = findTheRightServer(ipAdress,oldInstance);
		 oldInstance.remove(oldServer);
		 oldInstance.add(newServerInstence);
		 System.out.println(oldInstance);
		 deleteFile(fileName);
		 createNewJSONFile(fileName,oldInstance);
	 }
	static void createNewJSONFile(String fileName,JSONArray instance)
	{
		 try {
	         FileWriter file = new FileWriter(fileName);
	         file.write(instance.toJSONString());
	         file.close();
	      } catch (IOException e) {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	      }
	      System.out.println("fichier mis a jour "+ instance);
	}
	
//  lis le fichier des serveurs et retourne l objet serveur dont l adresseIP est en parametre
	static void deleteFile(String fileName)
	{
		try  
		{         File f= new File(fileName);           
		if(f.delete()){  
		System.out.println("mise a jour du serveur");   }  
		else  {  
		System.out.println("probleme de serveur");  }  
		}  
		catch(Exception e)  {  e.printStackTrace();  }  
	}
	static JSONObject findTheRightServer(String IpAdress , JSONArray servers)
	 {
		JSONObject item = null;
	            for(int i = 0; i<servers.size(); i++)
	            {
	            	JSONObject temp  =  (JSONObject)servers.get(i);
	            	 String adress = (String) temp.get("adresseIp");
	            	
	            	if(adress.equals(IpAdress))
	            	{
	            		item = temp;
	            	}
	            }
			return item;	 
	 }

	static JSONArray readJSONFile(String fileName)
	{
		 JSONParser parser = new JSONParser();
		 JSONArray servers = null;
			try (Reader reader = new FileReader(fileName)) {

	           Object object =parser.parse(reader);
	             servers = (JSONArray) object;
			  } catch (IOException | org.json.simple.parser.ParseException e) {
		            e.printStackTrace();
		        }
				return servers;	 
	}
	
	
	 
	 static boolean userExists(String userName,JSONObject server)
	 {
		 boolean exists = false;
		JSONArray members = (JSONArray) server.get("membres");
		for(int i = 0; i<members.size(); i++)
        {
        	JSONObject tempMember  =  (JSONObject)members.get(i);
        	 String name = (String) tempMember.get("nom");
        	
        	if(name.equals(userName))
        	{
        		exists = true;
        	}
        }
		return exists;
		 
	 }
	 static boolean isPassWordValid(String userName, String passWord,JSONObject server)
	 {
		 boolean isValid = false;
			JSONArray members = (JSONArray) server.get("membres");
			for(int i = 0; i<members.size(); i++)
	        {
	        	JSONObject tempMember  =  (JSONObject)members.get(i);
	        	 String name = (String) tempMember.get("nom");
	        	 String pW = (String) tempMember.get("motDePasse");
	        	
	        	if(name.equals(userName) && pW.equals(passWord))
	        	{
	        		isValid = true;
	        	}
	        }
			return isValid;
	 }
	 // ajoute un nouveau membre dans le clavardage
	  // retourne le nouveau tableau des membres 
	 static JSONArray addANewMember(String userName, String passWord,JSONObject server) 
	 {
		 JSONArray members = (JSONArray) server.get("membres");
		 JSONObject memberDetails = new JSONObject();
		 memberDetails.put("nom",userName);
		 memberDetails.put("motDePasse",passWord);
		 members.add(memberDetails);
		 return members;	 
	 }
	
   // mets a jour la sauvegarde des 15 derniers messages du chat
	//  parametres : nom de l element qui doi etre remplacer pour acceder a celui ci : String 
	   
	 static JSONArray replaceChat(ArrayList<String> savedChat,JSONObject server)
	 {
		 JSONArray chat = (JSONArray) server.get("derniersMessages");
		 for(int i = 0; i< savedChat.size();i++)
		 {
			 chat.add(savedChat.get(i));
		 }
		 return chat;
	 }
		static ArrayList<String> readChatHistory(JSONObject server)
		{
			ArrayList<String> chatHistory = new ArrayList<String>();
			
			 JSONArray chat = (JSONArray) server.get("derniersMessages");
			 for(int i = 0; i< chat.size();i++)
			 {
				 chatHistory.add((String) chat.get(i));
			 }
			 return chatHistory;
		}
	 @SuppressWarnings("unchecked")
	static JSONArray insertNewServer(String ipAdress,JSONArray obj)
	 {
		 JSONObject server = new JSONObject();
		 JSONArray member = new JSONArray();
		 JSONObject memberDetails = new JSONObject();
		 memberDetails.put("nom","");
		 memberDetails.put("motDePasse","");
		 member.add(memberDetails);
		 server.put("derniersMessages","[]");
		 server.put("adresseIp",ipAdress);
		 server.put("membres",member);
		 obj.add(server);
		 return obj;
	 }
      
	
	

}
