README

 Nouvelle librairie:
Ce projet utilise une librairie org.json.simple, dont le fichier jar a été ajouté dans le build path et dans le dossier serveur.

Connexion au serveur:

le programme cherche le serveur parmis la liste des serveurs dans le fichier json et cree un nouveau profil serveur si le serveur n est pas dans la liste.
le programme accede à tous les informations du serveur grace à l adresse IP de ce dernier et sauvegarde ceux ci dans une variable qui représente le serveur, un JSONObject.
  
Connexion du client
lors de sa connexion , le programme refuse l acces au client dont le nom existe dans le serveur auquel il veut se connecter,mais avec un mauvais mot de passe.
il cree un nouveau profil ( membre) à un client dont le nom existe pas dans le serveur auquel il veut se connecter.
le programme charge ensuite les 15 derniers messages du serveur et les personnes présentes peuvent communiquer.

Déconnexion des clients:

lorsque les clients se deconnectent et qu'il n y a plus personne dans la salle de clavardage, le programme:
-met à jour les données enregistrées dans le serveur ( 15 derniers messages, nouveaux membres etc,) 
-enregistre le serveur en tant que objet JSON
-écrase l'ancien fichier et cree un nouveau de meme nom.

La classe JSON_handler a été testé dans le main pour assurer que la création et l'enregistrement des données s'est bien effectué. 

Important :
 la creation du nouveau fichier JSON ne formatte pas celui. pour verifier que le fichier est bien à jour, ctrl A sur le fichier JSON, ctrl C, ctrl V dans une 
application de formatage JSON.
un exemple d'application : https://jsonformatter.curiousconcept.com/# 
