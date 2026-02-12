package client;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.text.*;

/**
 * Client de Chat Moderne - 100% Compatible avec Server.ChatServeur
 */
public class ChatClient extends JFrame {
    private static final String ADRESSE_SERVEUR = "localhost";
    private static final int PORT = 6000;
    
    // Couleurs du thème moderne
    private static final Color COULEUR_PRIMAIRE = new Color(37, 211, 102);
    private static final Color COULEUR_SECONDAIRE = new Color(30, 39, 46);
    private static final Color COULEUR_FOND = new Color(18, 18, 18);
    private static final Color COULEUR_SIDEBAR = new Color(32, 44, 51);
    private static final Color COULEUR_TEXTE = new Color(230, 230, 230);
    
    // Composants graphiques
    private JTextPane zoneMessagesPublic;
    private JTextField champMessage;
    private JButton boutonEnvoyer;
    private JTextField champNom;
    private JPanel panelConnexion;
    private JPanel panelChat;
    private JLabel labelStatut;
    private DefaultListModel<String> modeleListe;
    private JList<String> listeUtilisateurs;
    private JTabbedPane onglets;
    private Map<String, ConversationPrivee> conversationsPrivees;
    
    // Composants réseau
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean connecte = false;
    private String monPseudo;
    
    public ChatClient() {
        conversationsPrivees = new HashMap<>();
        configurerInterface();
    }
    
    private void configurerInterface() {
        setTitle("💬 Chat Moderne - Client-Serveur");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COULEUR_FOND);
        
        panelConnexion = creerEcranConnexion();
        panelChat = creerInterfaceChat();
        panelChat.setVisible(false);
        
        setLayout(new CardLayout());
        add(panelConnexion, "connexion");
        add(panelChat, "chat");
    }
    
    private JPanel creerEcranConnexion() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COULEUR_FOND);
        GridBagConstraints gbc = new GridBagConstraints();
        
        JLabel titre = new JLabel("💬 Chat Moderne");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 42));
        titre.setForeground(COULEUR_PRIMAIRE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 30, 0);
        panel.add(titre, gbc);
        
        JLabel sousTitre = new JLabel("Connectez-vous pour commencer");
        sousTitre.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        sousTitre.setForeground(COULEUR_TEXTE);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 50, 0);
        panel.add(sousTitre, gbc);
        
        JPanel formulaire = new JPanel();
        formulaire.setLayout(new BoxLayout(formulaire, BoxLayout.Y_AXIS));
        formulaire.setBackground(COULEUR_SIDEBAR);
        formulaire.setBorder(new EmptyBorder(40, 50, 40, 50));
        
        JLabel labelNom = new JLabel("Nom d'utilisateur");
        labelNom.setFont(new Font("Segoe UI", Font.BOLD, 14));
        labelNom.setForeground(COULEUR_TEXTE);
        labelNom.setAlignmentX(Component.LEFT_ALIGNMENT);
        formulaire.add(labelNom);
        formulaire.add(Box.createRigidArea(new Dimension(0, 10)));
        
        champNom = new JTextField(20);
        champNom.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        champNom.setMaximumSize(new Dimension(300, 45));
        champNom.setBackground(COULEUR_FOND);
        champNom.setForeground(COULEUR_TEXTE);
        champNom.setCaretColor(COULEUR_PRIMAIRE);
        champNom.setBorder(new CompoundBorder(
            new LineBorder(COULEUR_PRIMAIRE, 2),
            new EmptyBorder(10, 15, 10, 15)
        ));
        champNom.setAlignmentX(Component.LEFT_ALIGNMENT);
        formulaire.add(champNom);
        formulaire.add(Box.createRigidArea(new Dimension(0, 30)));
        
        JButton boutonConnexion = new JButton("Se Connecter");
        boutonConnexion.setFont(new Font("Segoe UI", Font.BOLD, 16));
        boutonConnexion.setMaximumSize(new Dimension(300, 45));
        boutonConnexion.setBackground(COULEUR_PRIMAIRE);
        boutonConnexion.setForeground(Color.WHITE);
        boutonConnexion.setFocusPainted(false);
        boutonConnexion.setBorderPainted(false);
        boutonConnexion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boutonConnexion.setAlignmentX(Component.LEFT_ALIGNMENT);
        boutonConnexion.addActionListener(e -> connecterAuServeur());
        
        boutonConnexion.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                boutonConnexion.setBackground(COULEUR_PRIMAIRE.brighter());
            }
            public void mouseExited(MouseEvent e) {
                boutonConnexion.setBackground(COULEUR_PRIMAIRE);
            }
        });
        
        formulaire.add(boutonConnexion);
        
        champNom.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    connecterAuServeur();
                }
            }
        });
        
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(formulaire, gbc);
        
        JLabel infoServeur = new JLabel("Serveur: " + ADRESSE_SERVEUR + ":" + PORT);
        infoServeur.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        infoServeur.setForeground(new Color(150, 150, 150));
        gbc.gridy = 3;
        gbc.insets = new Insets(30, 0, 0, 0);
        panel.add(infoServeur, gbc);
        
        return panel;
    }
    
    private JPanel creerInterfaceChat() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COULEUR_FOND);
        
        JPanel barreHaut = new JPanel(new BorderLayout());
        barreHaut.setBackground(COULEUR_SECONDAIRE);
        barreHaut.setBorder(new EmptyBorder(15, 20, 15, 20));
        barreHaut.setPreferredSize(new Dimension(0, 70));
        
        JLabel titre = new JLabel("💬 Chat Moderne");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titre.setForeground(COULEUR_PRIMAIRE);
        
        JPanel panelDroite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panelDroite.setOpaque(false);
        
        labelStatut = new JLabel("🟢 Connecté");
        labelStatut.setFont(new Font("Segoe UI", Font.BOLD, 14));
        labelStatut.setForeground(COULEUR_PRIMAIRE);
        
        JButton boutonListe = creerBoutonModerne("📋 Liste", COULEUR_PRIMAIRE);
        boutonListe.addActionListener(e -> {
            if (connecte) {
                out.println("/liste");
                System.out.println("Demande de liste envoyée au serveur");
            }
        });
        
        JButton boutonDeconnexion = creerBoutonModerne("Déconnexion", new Color(231, 76, 60));
        boutonDeconnexion.addActionListener(e -> deconnecter());
        
        panelDroite.add(labelStatut);
        panelDroite.add(boutonListe);
        panelDroite.add(boutonDeconnexion);
        
        barreHaut.add(titre, BorderLayout.WEST);
        barreHaut.add(panelDroite, BorderLayout.EAST);
        
        JPanel sidebar = creerSidebar();
        JPanel zoneCentrale = creerZoneCentrale();
        
        panel.add(barreHaut, BorderLayout.NORTH);
        panel.add(sidebar, BorderLayout.WEST);
        panel.add(zoneCentrale, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel creerSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(COULEUR_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, COULEUR_FOND));
        
        JLabel titreUtilisateurs = new JLabel("  👥 Utilisateurs Connectés");
        titreUtilisateurs.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titreUtilisateurs.setForeground(COULEUR_TEXTE);
        titreUtilisateurs.setBorder(new EmptyBorder(20, 10, 15, 10));
        
        modeleListe = new DefaultListModel<>();
        listeUtilisateurs = new JList<>(modeleListe);
        listeUtilisateurs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        listeUtilisateurs.setBackground(COULEUR_SIDEBAR);
        listeUtilisateurs.setForeground(COULEUR_TEXTE);
        listeUtilisateurs.setSelectionBackground(COULEUR_PRIMAIRE);
        listeUtilisateurs.setSelectionForeground(Color.WHITE);
        listeUtilisateurs.setBorder(new EmptyBorder(5, 15, 5, 15));
        listeUtilisateurs.setFixedCellHeight(45);
        
        listeUtilisateurs.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
                label.setText("  🟢 " + value.toString());
                label.setBorder(new EmptyBorder(8, 10, 8, 10));
                
                if (isSelected) {
                    label.setBackground(COULEUR_PRIMAIRE);
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(COULEUR_SIDEBAR);
                    label.setForeground(COULEUR_TEXTE);
                }
                
                return label;
            }
        });
        
        listeUtilisateurs.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = listeUtilisateurs.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        String utilisateur = modeleListe.getElementAt(index);
                        System.out.println("Double-clic sur: " + utilisateur);
                        ouvrirConversationPrivee(utilisateur);
                    }
                }
            }
        });
        
        JScrollPane scrollListe = new JScrollPane(listeUtilisateurs);
        scrollListe.setBorder(null);
        scrollListe.getViewport().setBackground(COULEUR_SIDEBAR);
        
        JPanel panelInstructions = new JPanel();
        panelInstructions.setBackground(COULEUR_SIDEBAR);
        panelInstructions.setBorder(new EmptyBorder(10, 15, 15, 15));
        
        JTextArea instructions = new JTextArea(
            "💡 Double-cliquez sur un\nutilisateur pour démarrer\nune conversation privée"
        );
        instructions.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        instructions.setForeground(new Color(150, 150, 150));
        instructions.setBackground(COULEUR_SIDEBAR);
        instructions.setEditable(false);
        instructions.setLineWrap(true);
        instructions.setWrapStyleWord(true);
        panelInstructions.add(instructions);
        
        sidebar.add(titreUtilisateurs, BorderLayout.NORTH);
        sidebar.add(scrollListe, BorderLayout.CENTER);
        sidebar.add(panelInstructions, BorderLayout.SOUTH);
        
        return sidebar;
    }
    
    private JPanel creerZoneCentrale() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COULEUR_FOND);
        
        onglets = new JTabbedPane();
        onglets.setFont(new Font("Segoe UI", Font.BOLD, 13));
        onglets.setBackground(COULEUR_FOND);
        onglets.setForeground(COULEUR_TEXTE);
        
        JPanel panelPublic = creerPanelChatPublic();
        onglets.addTab("💬 Chat Public", panelPublic);
        
        panel.add(onglets, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel creerPanelChatPublic() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(COULEUR_FOND);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        zoneMessagesPublic = new JTextPane();
        zoneMessagesPublic.setEditable(false);
        zoneMessagesPublic.setBackground(COULEUR_FOND);
        zoneMessagesPublic.setForeground(COULEUR_TEXTE);
        zoneMessagesPublic.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        zoneMessagesPublic.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollMessages = new JScrollPane(zoneMessagesPublic);
        scrollMessages.setBorder(new LineBorder(COULEUR_SIDEBAR, 1));
        scrollMessages.getViewport().setBackground(COULEUR_FOND);
        
        JPanel panelEnvoi = new JPanel(new BorderLayout(10, 0));
        panelEnvoi.setBackground(COULEUR_FOND);
        
        champMessage = new JTextField();
        champMessage.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        champMessage.setBackground(COULEUR_SIDEBAR);
        champMessage.setForeground(COULEUR_TEXTE);
        champMessage.setCaretColor(COULEUR_PRIMAIRE);
        champMessage.setBorder(new CompoundBorder(
            new LineBorder(COULEUR_SIDEBAR, 1),
            new EmptyBorder(12, 15, 12, 15)
        ));
        
        boutonEnvoyer = creerBoutonModerne("Envoyer ➤", COULEUR_PRIMAIRE);
        boutonEnvoyer.setPreferredSize(new Dimension(130, 45));
        boutonEnvoyer.addActionListener(e -> envoyerMessage());
        
        champMessage.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    envoyerMessage();
                }
            }
        });
        
        panelEnvoi.add(champMessage, BorderLayout.CENTER);
        panelEnvoi.add(boutonEnvoyer, BorderLayout.EAST);
        
        panel.add(scrollMessages, BorderLayout.CENTER);
        panel.add(panelEnvoi, BorderLayout.SOUTH);
        
        ajouterMessageSystemePublic("═══════════════════════════════════════════════");
        ajouterMessageSystemePublic("    Bienvenue dans le Chat Public!");
        ajouterMessageSystemePublic("═══════════════════════════════════════════════\n");
        
        return panel;
    }
    
    private JButton creerBoutonModerne(String texte, Color couleur) {
        JButton bouton = new JButton(texte);
        bouton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bouton.setBackground(couleur);
        bouton.setForeground(Color.WHITE);
        bouton.setFocusPainted(false);
        bouton.setBorderPainted(false);
        bouton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bouton.setBorder(new EmptyBorder(8, 20, 8, 20));
        
        bouton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                bouton.setBackground(couleur.brighter());
            }
            public void mouseExited(MouseEvent e) {
                bouton.setBackground(couleur);
            }
        });
        
        return bouton;
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
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            
            connecte = true;
            monPseudo = nomUtilisateur;
            
            // Lire le message de bienvenue du serveur
            String messageBienvenue = in.readLine();
            System.out.println("Message du serveur: " + messageBienvenue);
            
            // Envoyer le pseudo
            out.println(nomUtilisateur);
            System.out.println("Pseudo envoyé: " + nomUtilisateur);
            
            // Changer d'écran
            panelConnexion.setVisible(false);
            panelChat.setVisible(true);
            
            ajouterMessageSystemePublic("✓ Connecté en tant que: " + nomUtilisateur + "\n");
            
            // Démarrer le thread de réception
            new Thread(new RecepteurMessages()).start();
            
            // Demander la liste après 1 seconde
            Timer timer = new Timer(1000, e -> {
                if (connecte) {
                    out.println("/liste");
                    System.out.println("Demande initiale de liste envoyée");
                }
            });
            timer.setRepeats(false);
            timer.start();
            
            champMessage.requestFocus();
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Impossible de se connecter au serveur!\n" +
                "Vérifiez que le serveur est démarré sur " + ADRESSE_SERVEUR + ":" + PORT, 
                "Erreur de connexion", 
                JOptionPane.ERROR_MESSAGE);
            connecte = false;
        }
    }
    
    private void envoyerMessage() {
        if (!connecte) return;
        
        String message = champMessage.getText().trim();
        if (message.isEmpty()) return;
        
        int indexOnglet = onglets.getSelectedIndex();
        
        if (indexOnglet == 0) {
            // Chat public uniquement
            out.println(message);
            champMessage.setText("");
            champMessage.requestFocus();
        }
    }
    
    private void ouvrirConversationPrivee(String utilisateur) {
        if (utilisateur.equals(monPseudo)) {
            JOptionPane.showMessageDialog(this, 
                "Vous ne pouvez pas discuter avec vous-même!", 
                "Info", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Vérifier si la conversation existe déjà
        if (conversationsPrivees.containsKey(utilisateur)) {
            // Chercher l'onglet et le sélectionner
            for (int i = 0; i < onglets.getTabCount(); i++) {
                if (onglets.getTitleAt(i).equals("💬 " + utilisateur)) {
                    onglets.setSelectedIndex(i);
                    System.out.println("Onglet existant sélectionné pour: " + utilisateur);
                    return;
                }
            }
        }
        
        // Créer nouvelle conversation
        ConversationPrivee conv = new ConversationPrivee(utilisateur);
        conversationsPrivees.put(utilisateur, conv);
        
        onglets.addTab("💬 " + utilisateur, conv.getPanel());
        onglets.setSelectedIndex(onglets.getTabCount() - 1);
        
        System.out.println("Nouvelle conversation privée ouverte avec: " + utilisateur);
    }
    
    private void ajouterMessageSystemePublic(String message) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = zoneMessagesPublic.getStyledDocument();
                SimpleAttributeSet style = new SimpleAttributeSet();
                StyleConstants.setForeground(style, new Color(150, 150, 150));
                StyleConstants.setItalic(style, true);
                
                doc.insertString(doc.getLength(), message + "\n", style);
                zoneMessagesPublic.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }
    
    private void ajouterMessagePublic(String message) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = zoneMessagesPublic.getStyledDocument();
                SimpleAttributeSet style = new SimpleAttributeSet();
                StyleConstants.setForeground(style, COULEUR_TEXTE);
                
                doc.insertString(doc.getLength(), message + "\n", style);
                zoneMessagesPublic.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }
    
    private void deconnecter() {
        if (!connecte) return;
        
        try {
            out.println("/quitter");
            
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
            
            connecte = false;
            
            panelChat.setVisible(false);
            panelConnexion.setVisible(true);
            
            modeleListe.clear();
            conversationsPrivees.clear();
            
            while (onglets.getTabCount() > 1) {
                onglets.removeTabAt(1);
            }
            
            champNom.setText("");
            champNom.requestFocus();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    class RecepteurMessages implements Runnable {
        @Override
        public void run() {
            try {
                String message;
                while (connecte && (message = in.readLine()) != null) {
                    System.out.println("Message reçu: " + message);
                    traiterMessageRecu(message);
                }
            } catch (IOException e) {
                if (connecte) {
                    SwingUtilities.invokeLater(() -> {
                        ajouterMessageSystemePublic("✗ Connexion au serveur perdue.");
                        deconnecter();
                    });
                }
            }
        }
    }
    
    private void traiterMessageRecu(String message) {
        // Détecter la liste d'utilisateurs (peu importe l'encodage)
        // On cherche le pattern "Utilisateurs connect" suivi de " : "
        if (message.toLowerCase().contains("utilisateurs connect") && message.contains(" : ")) {
            System.out.println("Message de liste détecté!");
            
            // Trouver la position du " : " 
            int indexDeuxPoints = message.indexOf(" : ");
            if (indexDeuxPoints != -1 && indexDeuxPoints < message.length() - 3) {
                String liste = message.substring(indexDeuxPoints + 3).trim();
                System.out.println("Liste extraite: [" + liste + "]");
                mettreAJourListeUtilisateurs(liste);
            }
            
            // Afficher aussi dans le chat public
            ajouterMessagePublic(message);
        }
        // Message privé: "[HH:mm:ss] [Privé] Alice -> Bob: message"
        else if (message.contains("[Priv") && message.contains("->")) {
            System.out.println("Message privé détecté");
            traiterMessagePrive(message);
        }
        // Rafraîchir liste si quelqu'un rejoint/quitte
        else if (message.contains("a rejoint le chat") || message.contains("a quitt")) {
            ajouterMessagePublic(message);
            System.out.println("Quelqu'un a rejoint/quitté - demande de rafraîchissement");
            
            // Rafraîchir la liste après un court délai
            Timer timer = new Timer(500, e -> {
                if (connecte) {
                    out.println("/liste");
                    System.out.println("Demande de rafraîchissement envoyée");
                }
            });
            timer.setRepeats(false);
            timer.start();
        }
        // Message normal
        else {
            ajouterMessagePublic(message);
        }
    }
    
    private void traiterMessagePrive(String message) {
        try {
            // Format: [HH:mm:ss] [Privé] Alice -> Bob: message
            int indexPrive = message.indexOf("[Priv");
            int debut = message.indexOf("]", indexPrive) + 2;
            int fleche = message.indexOf("->", debut);
            int deuxPoints = message.indexOf(":", fleche);
            
            String expediteur = message.substring(debut, fleche).trim();
            String destinataire = message.substring(fleche + 2, deuxPoints).trim();
            String contenu = message.substring(deuxPoints + 1).trim();
            
            System.out.println("Message privé - De: " + expediteur + " À: " + destinataire + " Contenu: " + contenu);
            
            // Si c'est un message qu'on a envoyé
            if (expediteur.equals(monPseudo)) {
                System.out.println("Message privé envoyé par moi - déjà affiché");
                return;
            }
            
            // Message reçu d'un autre utilisateur
            System.out.println("Message privé reçu de: " + expediteur);
            
            if (!conversationsPrivees.containsKey(expediteur)) {
                System.out.println("Ouverture automatique de conversation avec: " + expediteur);
                ouvrirConversationPrivee(expediteur);
            }
            
            ConversationPrivee conv = conversationsPrivees.get(expediteur);
            if (conv != null) {
                conv.ajouterMessageRecu(contenu);
                System.out.println("Message ajouté à la conversation");
            }
        } catch (Exception e) {
            System.out.println("Erreur traitement message privé: " + e.getMessage());
            e.printStackTrace();
            ajouterMessagePublic(message);
        }
    }
    
    private void mettreAJourListeUtilisateurs(String liste) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("=== MISE À JOUR DE LA LISTE ===");
            System.out.println("Liste reçue: [" + liste + "]");
            
            modeleListe.clear();
            
            if (liste == null || liste.trim().isEmpty()) {
                System.out.println("Liste vide ou nulle");
                return;
            }
            
            String[] utilisateurs = liste.split(",");
            System.out.println("Nombre d'utilisateurs après split: " + utilisateurs.length);
            
            for (String user : utilisateurs) {
                String userTrim = user.trim();
                System.out.println("  - Utilisateur trouvé: [" + userTrim + "]");
                
                if (!userTrim.isEmpty() && !userTrim.equals(monPseudo)) {
                    modeleListe.addElement(userTrim);
                    System.out.println("    ✓ Ajouté à la liste!");
                } else if (userTrim.equals(monPseudo)) {
                    System.out.println("    ✗ C'est moi, ignoré");
                }
            }
            
            System.out.println("Taille finale du modèle: " + modeleListe.getSize());
            System.out.println("=================================");
        });
    }
    
    class ConversationPrivee {
        private String destinataire;
        private JPanel panel;
        private JTextPane zoneMessages;
        
        public ConversationPrivee(String destinataire) {
            this.destinataire = destinataire;
            creerPanel();
        }
        
        private void creerPanel() {
            panel = new JPanel(new BorderLayout(0, 15));
            panel.setBackground(COULEUR_FOND);
            panel.setBorder(new EmptyBorder(15, 15, 15, 15));
            
            zoneMessages = new JTextPane();
            zoneMessages.setEditable(false);
            zoneMessages.setBackground(COULEUR_FOND);
            zoneMessages.setForeground(COULEUR_TEXTE);
            zoneMessages.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            zoneMessages.setBorder(new EmptyBorder(10, 10, 10, 10));
            
            JScrollPane scrollMessages = new JScrollPane(zoneMessages);
            scrollMessages.setBorder(new LineBorder(COULEUR_SIDEBAR, 1));
            scrollMessages.getViewport().setBackground(COULEUR_FOND);
            
            // Panneau d'envoi pour les messages privés
            JPanel panelEnvoiPrive = new JPanel(new BorderLayout(10, 0));
            panelEnvoiPrive.setBackground(COULEUR_FOND);
            
            JTextField champMessagePrive = new JTextField();
            champMessagePrive.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            champMessagePrive.setBackground(COULEUR_SIDEBAR);
            champMessagePrive.setForeground(COULEUR_TEXTE);
            champMessagePrive.setCaretColor(COULEUR_PRIMAIRE);
            champMessagePrive.setBorder(new CompoundBorder(
                new LineBorder(COULEUR_SIDEBAR, 1),
                new EmptyBorder(12, 15, 12, 15)
            ));
            
            JButton boutonEnvoyerPrive = creerBoutonModerne("Envoyer ➤", COULEUR_PRIMAIRE);
            boutonEnvoyerPrive.setPreferredSize(new Dimension(130, 45));
            
            // Action d'envoi pour le bouton
            boutonEnvoyerPrive.addActionListener(e -> {
                String msg = champMessagePrive.getText().trim();
                if (!msg.isEmpty() && connecte) {
                    String commande = "/prive @" + destinataire + " " + msg;
                    System.out.println("Envoi message privé: " + commande);
                    out.println(commande);
                    ajouterMessageEnvoye(msg);
                    champMessagePrive.setText("");
                    champMessagePrive.requestFocus();
                }
            });
            
            // Action d'envoi avec la touche Entrée
            champMessagePrive.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        String msg = champMessagePrive.getText().trim();
                        if (!msg.isEmpty() && connecte) {
                            String commande = "/prive @" + destinataire + " " + msg;
                            System.out.println("Envoi message privé (Enter): " + commande);
                            out.println(commande);
                            ajouterMessageEnvoye(msg);
                            champMessagePrive.setText("");
                        }
                    }
                }
            });
            
            panelEnvoiPrive.add(champMessagePrive, BorderLayout.CENTER);
            panelEnvoiPrive.add(boutonEnvoyerPrive, BorderLayout.EAST);
            
            panel.add(scrollMessages, BorderLayout.CENTER);
            panel.add(panelEnvoiPrive, BorderLayout.SOUTH);
            
            ajouterMessageSysteme("Conversation privée avec " + destinataire);
            ajouterMessageSysteme("═══════════════════════════════════════\n");
        }
        
        public JPanel getPanel() {
            return panel;
        }
        
        public void ajouterMessageEnvoye(String message) {
            SwingUtilities.invokeLater(() -> {
                try {
                    StyledDocument doc = zoneMessages.getStyledDocument();
                    
                    SimpleAttributeSet style = new SimpleAttributeSet();
                    StyleConstants.setForeground(style, COULEUR_PRIMAIRE);
                    StyleConstants.setBold(style, true);
                    
                    SimpleAttributeSet styleMsg = new SimpleAttributeSet();
                    StyleConstants.setForeground(styleMsg, COULEUR_TEXTE);
                    
                    doc.insertString(doc.getLength(), "Vous: ", style);
                    doc.insertString(doc.getLength(), message + "\n", styleMsg);
                    
                    zoneMessages.setCaretPosition(doc.getLength());
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            });
        }
        
        public void ajouterMessageRecu(String message) {
            SwingUtilities.invokeLater(() -> {
                try {
                    StyledDocument doc = zoneMessages.getStyledDocument();
                    
                    SimpleAttributeSet style = new SimpleAttributeSet();
                    StyleConstants.setForeground(style, new Color(100, 181, 246));
                    StyleConstants.setBold(style, true);
                    
                    SimpleAttributeSet styleMsg = new SimpleAttributeSet();
                    StyleConstants.setForeground(styleMsg, COULEUR_TEXTE);
                    
                    doc.insertString(doc.getLength(), destinataire + ": ", style);
                    doc.insertString(doc.getLength(), message + "\n", styleMsg);
                    
                    zoneMessages.setCaretPosition(doc.getLength());
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            });
        }
        
        private void ajouterMessageSysteme(String message) {
            try {
                StyledDocument doc = zoneMessages.getStyledDocument();
                SimpleAttributeSet style = new SimpleAttributeSet();
                StyleConstants.setForeground(style, new Color(150, 150, 150));
                StyleConstants.setItalic(style, true);
                
                doc.insertString(doc.getLength(), message + "\n", style);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            ChatClient client = new ChatClient();
            client.setVisible(true);
        });
    }
}