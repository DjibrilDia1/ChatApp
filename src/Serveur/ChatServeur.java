package Serveur;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * Serveur de Chat Multi-Clients
 * Gère plusieurs clients simultanément et permet la communication entre tous
 */
public class ChatServeur {
    private static final int PORT = 5000;
    private static Set<GestionnaireClient> clients = Collections.synchronizedSet(new HashSet<>());
    private static int compteurClients = 0;
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║   SERVEUR DE CHAT - Démarrage...    ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println("Port d'écoute : " + PORT);
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("✓ Serveur démarré avec succès!");
            System.out.println("En attente de connexions...\n");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                compteurClients++;
                
                GestionnaireClient client = new GestionnaireClient(clientSocket, compteurClients);
                clients.add(client);
                
                Thread thread = new Thread(client);
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("Erreur serveur : " + e.getMessage());
        }
    }
    
    // Diffuser un message à tous les clients
    public static void diffuserMessage(String message, GestionnaireClient expediteur) {
        synchronized (clients) {
            for (GestionnaireClient client : clients) {
                client.envoyerMessage(message);
            }
        }
        
        // Afficher le message dans la console du serveur
        System.out.println(message);
    }
    
    // Diffuser un message privé à un client spécifique
    public static void envoyerMessagePrive(String destinataire, String message) {
        synchronized (clients) {
            for (GestionnaireClient client : clients) {
                if (client.getNomUtilisateur().equals(destinataire)) {
                    client.envoyerMessage(message);
                    return;
                }
            }
        }
    }
    
    // Retirer un client de la liste
    public static void retirerClient(GestionnaireClient client) {
        clients.remove(client);
        System.out.println("Client déconnecté : " + client.getNomUtilisateur());
    }
    
    // Obtenir la liste des utilisateurs connectés
    public static String getListeUtilisateurs() {
        StringBuilder liste = new StringBuilder("Utilisateurs connectés : ");
        synchronized (clients) {
            for (GestionnaireClient client : clients) {
                liste.append(client.getNomUtilisateur()).append(", ");
            }
        }
        if (liste.toString().endsWith(", ")) {
            liste.setLength(liste.length() - 2);
        }
        return liste.toString();
    }
}

/**
 * Classe pour gérer chaque client individuellement
 */
class GestionnaireClient implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String nomUtilisateur;
    private int idClient;
    
    public GestionnaireClient(Socket socket, int id) {
        this.socket = socket;
        this.idClient = id;
    }
    
    @Override
    public void run() {
        try {
            // Initialisation des flux
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            // Demander le nom d'utilisateur
            out.println("SERVEUR: Bienvenue! Veuillez entrer votre nom d'utilisateur:");
            nomUtilisateur = in.readLine();
            
            if (nomUtilisateur == null || nomUtilisateur.trim().isEmpty()) {
                nomUtilisateur = "Utilisateur" + idClient;
            }
            
            // Message de bienvenue
            String bienvenue = getHorodatage() + " SERVEUR: " + nomUtilisateur + " a rejoint le chat!";
            ChatServeur.diffuserMessage(bienvenue, this);
            
            // Envoyer les instructions au nouveau client
            out.println("SERVEUR: ═══════════════════════════════════════");
            out.println("SERVEUR: Commandes disponibles:");
            out.println("SERVEUR: /liste - Voir les utilisateurs connectés");
            out.println("SERVEUR: /prive @utilisateur message - Envoyer un message privé");
            out.println("SERVEUR: /quitter - Quitter le chat");
            out.println("SERVEUR: ═══════════════════════════════════════");
            
            // Boucle de réception des messages
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("/quitter")) {
                    break;
                } else if (message.startsWith("/liste")) {
                    out.println(getHorodatage() + " " + ChatServeur.getListeUtilisateurs());
                } else if (message.startsWith("/prive ")) {
                    traiterMessagePrive(message);
                } else {
                    String messageComplet = getHorodatage() + " " + nomUtilisateur + ": " + message;
                    ChatServeur.diffuserMessage(messageComplet, this);
                }
            }
            
        } catch (IOException e) {
            System.err.println("Erreur avec le client " + nomUtilisateur + ": " + e.getMessage());
        } finally {
            deconnecter();
        }
    }
    
    private void traiterMessagePrive(String commande) {
        try {
            // Format: /prive @utilisateur message
            String[] parties = commande.substring(7).split(" ", 2);
            if (parties.length < 2 || !parties[0].startsWith("@")) {
                out.println("SERVEUR: Format incorrect. Utilisez: /prive @utilisateur message");
                return;
            }
            
            String destinataire = parties[0].substring(1);
            String message = parties[1];
            String messageFormate = getHorodatage() + " [Privé] " + nomUtilisateur + " -> " + destinataire + ": " + message;
            
            ChatServeur.envoyerMessagePrive(destinataire, messageFormate);
            out.println(messageFormate); // Confirmation à l'expéditeur
            
        } catch (Exception e) {
            out.println("SERVEUR: Erreur lors de l'envoi du message privé.");
        }
    }
    
    public void envoyerMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
    
    private void deconnecter() {
        try {
            ChatServeur.retirerClient(this);
            String messageDepart = getHorodatage() + " SERVEUR: " + nomUtilisateur + " a quitté le chat.";
            ChatServeur.diffuserMessage(messageDepart, null);
            
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException e) {
            System.err.println("Erreur lors de la déconnexion : " + e.getMessage());
        }
    }
    
    private String getHorodatage() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return "[" + sdf.format(new Date()) + "]";
    }
    
    public String getNomUtilisateur() {
        return nomUtilisateur;
    }
}