Documentation du Projet Java Avancé - Gestion Médicale

 a. Architecture

L'application suit une architecture en couches pour assurer une séparation claire des responsabilités :

1.  Couche Présentation (`projetjava.presentation`) :
       Gérée par `SimpleHttpServer`.
       Responsable de l'exposition des endpoints HTTP (REST API).
      Reçoit les requêtes, extrait les données et renvoie les réponses JSON.

2.  Couche Service (`projetjava.service`):
      Contient la logique métier principale (`MedicalService`).
      Orchestre les interactions entre les entités, les dépôts de données et le système de notification.
       Gère les fonctionnalités avancées comme le Undo/Redo.

3.  Couche Accès aux Données (`projetjava.repositories`) :
       Gère la persistance des objets (`PatientRepository`, `MedecinRepository`, `RendezVousRepository`).
       Simule une base de données en mémoire.

4.  Couche Transverse / Observabilité (`projetjava.observer`)** :
       Gère les notifications et les logs (`NotificationManager`, `ConsoleLogger`, `FileAuditLogger`).

 b. Patterns & Justification

Le projet met en œuvre plusieurs patrons de conception pour répondre aux exigences de flexibilité et de maintenabilité :

1.  Repository Pattern :
       *Usage* : Classes `PatientRepository`, `MedecinRepository`, etc.
       *Justification* : Abstraire l'accès aux données. Si on passe d'une liste en mémoire à une base SQL, seule cette couche change, sans impacter le service.

2.  Observer Pattern** :
       *Usage* : `NotificationManager` (Sujet) et `ConsoleLogger`, `FileAuditLogger` (Observateurs).
       *Justification* : Permet de découpler la logique métier des effets de bord (logging, audit). Lorsqu'une action se produit (ex: création d'un RDV), le service notifie le manager sans savoir qui écoute.

3.  Strategy Pattern :
       Usage : `DatePriorityStrategy` injecté dans le service via `setPriorityStrategy`.
       Justification : Permet de changer dynamiquement l'algorithme de tri ou de priorité des rendez-vous sans modifier le code du service.

4.  Command Pattern (Utilisé pour Undo/Redo) :
      Usage : Gestion des actions annulables via `undoLastAction` / `redoLastAction`.
      Justification : Encapsule les requêtes sous forme d'objets, permettant de stocker l'historique des opérations pour les annuler ou les refaire.

5.  Dependency Injection (DI) :
       *Usage* : Dans `Main.java`, les dépendances sont injectées manuellement dans le constructeur de `MedicalService`.
       *Justification* : Facilite les tests et réduit le couplage.

c. Flow complet d’une requête http

Prenons l'exemple d'une requête POST /patients (Création d'un patient) :

1.  Réception : Le client envoie une requête HTTP POST sur le port 8000. Le `HttpServer` intercepte la requête.
2.  Routing : Le serveur identifie le contexte `/patients` et délègue le traitement au `PatientHandler` (classe interne de `SimpleHttpServer`).
3.  Parsing :
       Le `PatientHandler` vérifie la méthode (POST).
       Il lit le corps de la requête (JSON brut) via la méthode utilitaire `getBody()`.
      Il extrait les champs `nom` et `email` via `extractJsonValue()`.
4.  Traitement Métier :
       Le Handler appelle `medicalService.registerPatient(nom, email)`.
       `MedicalService` crée l'objet, le sauvegarde via le Repository et notifie les Observers.
5.  Réponse :
       `MedicalService` retourne l'objet créé.
       Le Handler convertit cet objet en JSON et envoie une réponse HTTP 201 via `sendResponse()`.