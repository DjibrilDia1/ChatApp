package client;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

/**
 * Client de Chat avec Interface Graphique
 * Permet de communiquer avec d'autres clients et le serveur
 */
public class ChatClient extends JFrame {
    private static final String ADRESSE_SERVEUR = "localhost";
    private static final int PORT = 5000;
    
    // Composants graphiques
    private JTextArea zoneMessages;
    private JTextField champMessage;
    private JButton boutonEnvoyer;
    private JButton boutonConnexion;
    private JButton boutonDeconnexion;
    private JTextField champNom;
    private JLabel labelStatut;
    
    // Composants réseau
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean connecte = false;
    
    public ChatClient() {
        configurerInterface();
    }
    
    private void configurerInterface() {
        setTitle("Chat Client-Serveur - Cas 3");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Panel principal avec BorderLayout
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelPrincipal.setBackground(new Color(240, 240, 245));
        
        // ═══════════════════ PANEL CONNEXION (HAUT) ═══════════════════
        JPanel panelConnexion = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelConnexion.setBackground(new Color(52, 152, 219));
        panelConnexion.setBorder(new CompoundBorder(
            new LineBorder(new Color(41, 128, 185), 2),
            new EmptyBorder(5, 5, 5, 5)
        ));
        
        JLabel labelNom = new JLabel("Nom d'utilisateur:");
        labelNom.setForeground(Color.WHITE);
        labelNom.setFont(new Font("Arial", Font.BOLD, 12));
        
        champNom = new JTextField(15);
        champNom.setFont(new Font("Arial", Font.PLAIN, 12));
        
        boutonConnexion = new JButton(" Connexion");
        boutonConnexion.setBackground(new Color(46, 204, 113));
        boutonConnexion.setForeground(Color.WHITE);
        boutonConnexion.setFont(new Font("Arial", Font.BOLD, 12));
        boutonConnexion.setFocusPainted(false);
        boutonConnexion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        boutonDeconnexion = new JButton("🔴 Déconnexion");
        boutonDeconnexion.setBackground(new Color(231, 76, 60));
        boutonDeconnexion.setForeground(Color.WHITE);
        boutonDeconnexion.setFont(new Font("Arial", Font.BOLD, 12));
        boutonDeconnexion.setFocusPainted(false);
        boutonDeconnexion.setEnabled(false);
        boutonDeconnexion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        labelStatut = new JLabel("⚫ Déconnecté");
        labelStatut.setForeground(Color.WHITE);
        labelStatut.setFont(new Font("Arial", Font.BOLD, 12));
        
        panelConnexion.add(labelNom);
        panelConnexion.add(champNom);
        panelConnexion.add(boutonConnexion);
        panelConnexion.add(boutonDeconnexion);
        panelConnexion.add(Box.createHorizontalStrut(20));
        panelConnexion.add(labelStatut);
        
        // ═══════════════════ ZONE DE MESSAGES (CENTRE) ═══════════════════
        zoneMessages = new JTextArea();
        zoneMessages.setEditable(false);
        zoneMessages.setFont(new Font("Consolas", Font.PLAIN, 13));
        zoneMessages.setLineWrap(true);
        zoneMessages.setWrapStyleWord(true);
        zoneMessages.setBackground(Color.WHITE);
        zoneMessages.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(zoneMessages);
        scrollPane.setBorder(new CompoundBorder(
            new TitledBorder(new LineBorder(new Color(52, 152, 219), 2), 
                "Messages", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), new Color(52, 152, 219)),
            new EmptyBorder(5, 5, 5, 5)
        ));
        
        // ═══════════════════ PANEL ENVOI (BAS) ═══════════════════
        JPanel panelEnvoi = new JPanel(new BorderLayout(10, 0));
        panelEnvoi.setBorder(new EmptyBorder(10, 0, 0, 0));
        panelEnvoi.setBackground(new Color(240, 240, 245));
        
        champMessage = new JTextField();
        champMessage.setFont(new Font("Arial", Font.PLAIN, 13));
        champMessage.setEnabled(false);
        champMessage.setBorder(new CompoundBorder(
            new LineBorder(new Color(189, 195, 199), 2),
            new EmptyBorder(8, 10, 8, 10)
        ));
        
        boutonEnvoyer = new JButton("Envoyer");
        boutonEnvoyer.setBackground(new Color(52, 152, 219));
        boutonEnvoyer.setForeground(Color.WHITE);
        boutonEnvoyer.setFont(new Font("Arial", Font.BOLD, 13));
        boutonEnvoyer.setFocusPainted(false);
        boutonEnvoyer.setEnabled(false);
        boutonEnvoyer.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boutonEnvoyer.setPreferredSize(new Dimension(120, 40));
        
        panelEnvoi.add(champMessage, BorderLayout.CENTER);
        panelEnvoi.add(boutonEnvoyer, BorderLayout.EAST);
        
        // Panel d'aide
        JPanel panelAide = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelAide.setBackground(new Color(240, 240, 245));
        JLabel labelAide = new JLabel("Commandes: /liste | /prive @utilisateur message | /quitter");
        labelAide.setFont(new Font("Arial", Font.ITALIC, 11));
        labelAide.setForeground(new Color(127, 140, 141));
        panelAide.add(labelAide);
        
        JPanel panelBas = new JPanel(new BorderLayout());
        panelBas.setBackground(new Color(240, 240, 245));
        panelBas.add(panelEnvoi, BorderLayout.CENTER);
        panelBas.add(panelAide, BorderLayout.SOUTH);
        
        // Assemblage
        panelPrincipal.add(panelConnexion, BorderLayout.NORTH);
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);
        panelPrincipal.add(panelBas, BorderLayout.SOUTH);
        
        add(panelPrincipal);
        
        // ═══════════════════ ÉVÉNEMENTS ═══════════════════
        boutonConnexion.addActionListener(e -> connecterAuServeur());
        boutonDeconnexion.addActionListener(e -> deconnecter());
        boutonEnvoyer.addActionListener(e -> envoyerMessage());
        
        champMessage.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    envoyerMessage();
                }
            }
        });
        
        champNom.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !connecte) {
                    connecterAuServeur();
                }
            }
        });
        
        // Message de bienvenue
        ajouterMessage("═══════════════════════════════════════════════════════\n");
        ajouterMessage("    Bienvenue dans le Chat Client-Serveur!\n");
        ajouterMessage("═══════════════════════════════════════════════════════\n");
        ajouterMessage("Entrez votre nom et cliquez sur 'Connexion' pour commencer.\n\n");
    }
    
    private void connecterAuServeur() {
        String nomUtilisateur = champNom.getText().trim();
        
        if (nomUtilisateur.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez entrer un nom d'utilisateur!", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            socket = new Socket(ADRESSE_SERVEUR, PORT);
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), "UTF-8")
            );

            out = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), "UTF-8"),
                    true
            );

            
            connecte = true;
            
            // Envoyer le nom d'utilisateur
            String premierMessage = in.readLine(); // Message de bienvenue du serveur
            out.println(nomUtilisateur);
            
            // Mise à jour de l'interface
            boutonConnexion.setEnabled(false);
            boutonDeconnexion.setEnabled(true);
            champNom.setEnabled(false);
            champMessage.setEnabled(true);
            boutonEnvoyer.setEnabled(true);
            labelStatut.setText("🟢 Connecté");
            labelStatut.setForeground(Color.GREEN);
            
            ajouterMessage("✓ Connecté au serveur en tant que: " + nomUtilisateur + "\n\n");
            
            // Thread pour recevoir les messages
            new Thread(new RecepteurMessages()).start();
            
            champMessage.requestFocus();
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Impossible de se connecter au serveur!\nVérifiez que le serveur est démarré.", 
                "Erreur de connexion", 
                JOptionPane.ERROR_MESSAGE);
            ajouterMessage("✗ Erreur de connexion au serveur.\n");
        }
    }
    
    private void envoyerMessage() {
        if (!connecte) return;
        
        String message = champMessage.getText().trim();
        if (message.isEmpty()) return;
        
        out.println(message);
        champMessage.setText("");
        champMessage.requestFocus();
        
        // Traiter la commande /quitter localement
        if (message.equals("/quitter")) {
            deconnecter();
        }
    }
    
    private void deconnecter() {
        if (!connecte) return;
        
        try {
            if (out != null) {
                out.println("/quitter");
            }
            
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
            
            connecte = false;
            
            // Mise à jour de l'interface
            boutonConnexion.setEnabled(true);
            boutonDeconnexion.setEnabled(false);
            champNom.setEnabled(true);
            champMessage.setEnabled(false);
            boutonEnvoyer.setEnabled(false);
            labelStatut.setText("⚫ Déconnecté");
            labelStatut.setForeground(Color.WHITE);
            
            ajouterMessage("\n✓ Déconnecté du serveur.\n\n");
            
        } catch (IOException e) {
            ajouterMessage("Erreur lors de la déconnexion.\n");
        }
    }
    
    private void ajouterMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            zoneMessages.append(message);
            zoneMessages.setCaretPosition(zoneMessages.getDocument().getLength());
        });
    }
    
    // Thread pour recevoir les messages du serveur
    class RecepteurMessages implements Runnable {
        @Override
        public void run() {
            try {
                String message;
                while (connecte && (message = in.readLine()) != null) {
                    ajouterMessage(message + "\n");
                }
            } catch (IOException e) {
                if (connecte) {
                    ajouterMessage("\n✗ Connexion au serveur perdue.\n");
                    SwingUtilities.invokeLater(() -> deconnecter());
                }
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Utiliser le Look and Feel du système
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            ChatClient client = new ChatClient();
            client.setVisible(true);
        });
    }
}