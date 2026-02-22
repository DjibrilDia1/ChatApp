# Java Socket Chat — Client/Serveur

Application de chat en temps réel développée en Java avec les sockets TCP/UDP.

---

## Description

Ce projet implémente une application de messagerie client/serveur en Java. Il couvre trois cas d'usage progressifs :

- **Cas 3** : Communication totale — clients entre eux et clients avec le serveur

---

## Technologies utilisées

- Langage : Java (JDK 8+)
- Réseau : Java Sockets (`java.net`)
- Protocoles : TCP et UDP
- Interface : Console (invite de commande) / Interface graphique (optionnel)

---

## Structure du projet

```
JavaSocketChat/
│
├── src/
│   └── cas3/
│       ├── Serveur.java
|                    |-- ChatServeur.java
│       └── Client.java
|                    |-- ClientServeur.java
│
├── docs/
│   └── presentation.pdf
│
└── README.md
```

---

## Lancer l'application

### Prérequis

- Java JDK 8 ou supérieur installé
- Un terminal / invite de commande

### Compilation

```bash
javac src/Serveur/ChatServeur.java
javac src/Client/ChatClient.java
```

### Exécution

Démarrer le serveur :
```bash
java Serveur.ChatServeur.java
```

Démarrer le client dans un autre terminal :
```bash
java Client.ChatClient.java
```

## Architecture réseau

```
         [ Serveur ]
        /           \
  [ Client1 ]   [ Client2 ]
```

- Le serveur écoute sur un port défini (6000)
- Chaque client se connecte via l'adresse IP du serveur et le port
- Les messages sont échangés via des flux InputStream / OutputStream (TCP) ou des DatagramPacket (UDP)

---

## Fonctionnalités

- Envoi et réception de messages texte
- Communication client / serveur
- Relais de messages entre clients via le serveur
- Gestion de plusieurs clients simultanés avec les threads
- Support TCP et UDP

---

## Auteurs

- Djibril Dia / Matar Gueye

---
