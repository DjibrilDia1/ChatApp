# Java Socket Chat — Client/Serveur

Application de chat en temps réel développée en Java avec les sockets TCP/UDP.

---

## Description

Ce projet implémente une application de messagerie client/serveur en Java. Il couvre trois cas d'usage progressifs :

- **Cas 1** : Communication bidirectionnelle entre un client et le serveur
- **Cas 2** : Le serveur relaie les messages entre clients (le serveur ne communique pas lui-même)
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
│   ├── cas1/
│   │   ├── Serveur.java
│   │   └── Client.java
│   ├── cas2/
│   │   ├── Serveur.java
│   │   └── Client.java
│   └── cas3/
│       ├── Serveur.java
│       └── Client.java
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
javac src/cas1/Serveur.java
javac src/cas1/Client.java
```

### Exécution

Démarrer le serveur :
```bash
java cas1.Serveur
```

Démarrer le client dans un autre terminal :
```bash
java cas1.Client
```

Remplacer `cas1` par `cas2` ou `cas3` selon le cas souhaité.

---

## Architecture réseau

```
         [ Serveur ]
        /           \
  [ Client1 ]   [ Client2 ]
```

- Le serveur écoute sur un port défini (ex : 12345)
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

- Nom Prénom — Etudiant(e)
- Encadrant(e) : Nom du professeur

---

## Informations

- Durée : 3 semaines
- Date de présentation : 13 février 2026
- Documentation : support de cours, notes des cours magistraux, TP réalisés en classe, [documentation Java java.net](https://docs.oracle.com/javase/8/docs/api/java/net/package-summary.html)
