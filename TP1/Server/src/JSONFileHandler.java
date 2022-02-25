
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

//cette classe permet de lire la base de donnee du cote serveur 
// lire le fichier JSON de notre BD
//  mettre des elemenent dans cette BD

public class JSONFileHandler {

	// lis le fichier et sauvegarde son contenu, supprime l ancienne version du serveur qui est en parametre,
	//remplace celui ci par une nouvelle version passée en parametre, le rajoute dans le serveur, cree un nouveau fichier.
	static void updateJsonFile(String fileName , String ipAdress, JSONObject newServerInstence,JSONArray oldListofServers )
	 {
		 JSONObject oldServer = findTheRightServer(ipAdress,oldListofServers);
		 oldListofServers.remove(oldServer);
		 oldListofServers.add(newServerInstence);
		 deleteFile(fileName);
		 createNewJSONFile(fileName,oldListofServers);
	 }
	static void createNewJSONFile(String fileName,JSONArray instance)
	{
		 try {
	         FileWriter file = new FileWriter(fileName);
	         file.write(instance.toJSONString());
	         file.close();
	      } catch (IOException e) {
	         e.printStackTrace();
	      }
	      System.out.println("fichier mis a jour ");
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
	 static void addANewMember(String userName, String passWord,JSONObject server) 
	 {
		 JSONArray members = (JSONArray) server.get("membres");
		 server.remove("membres");
		 JSONObject memberDetails = new JSONObject();
		 memberDetails.put("nom",userName);
		 memberDetails.put("motDePasse",passWord);
		 members.add(memberDetails);
		 server.put("membres",members); 
	 }
	
     //    mets a jour la sauvegarde des 15 derniers messages du chat
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
		 JSONArray chat = new JSONArray();
		 server.put("derniersMessages",chat);
		 server.put("adresseIp",ipAdress);
		 server.put("membres",member);
		 obj.add(server);
		 return obj;
	 }
      
	
	

}
