# 📦 Fapecargo
> **Projet Android M2** - Application de collection de cartes géolocalisées avec un système de capture par mini-jeux sensoriels.

---

## 🚀 Concept du Projet
**Fapecargo** transforme l'environnement du joueur en un espace de découverte. Les utilisateurs explorent la carte (OpenStreetMap) pour trouver des personnages ou des lieux d'intérêt. Pour obtenir une carte et l'ajouter à sa collection, le joueur doit prouver son habileté en réussissant l'un des **5 mini-jeux interactifs** exploitant les capteurs du smartphone.

---

## 🛠 Stack Technique
* **Langage :** Kotlin 1.9+
* **Interface (UI) :** Jetpack Compose (Flat Design, harmonisation des composants)
* **Réseau & API :** Ktor Client
* **Base de données (Local) :** Room Persistence (Cache offline & Historique)
* **Cartographie :** OpenStreetMap (OSMDroid)
* **CI/CD :** Fastlane (Automatisation des builds)

---

## 🏗 Architecture & Structure
L'application respecte les principes de l'architecture **MVVM (Model-View-ViewModel)**.

* `ui/` : Interface utilisateur moderne (Auth, Map, Collection, Theme).
* `ui/game/` : Regroupe la logique isolée de tous les mini-jeux.
* `data/` : Gestion des données brutes (Ktor, Room, Repository).
* `Services/` : Gestion de la musique de fond (`MusicService`) et de la connectivité (`AirplaneModeReceiver`).

---

## 🎮 Les 5 Mini-Jeux de Capture
La grande force de Fapecargo réside dans sa variété de gameplay pour la capture des cartes :

1. **Synchronisation d'Archive (`Proximitygame`) :** Utilise le **capteur de proximité**. Le joueur doit effectuer une série d'impulsions (passages de la main) au-dessus du téléphone dans un temps imparti pour télécharger la carte. Inclut un retour haptique (vibrations).
2. **Récolte des Pommes (`ShakeAppleTree`) :** Utilise l'**accéléromètre**. Le joueur doit secouer physiquement son appareil pour faire tomber des éléments à l'écran.
3. **Désamorçage (`BombDefuseMiniGame`) :** Mini-jeu de tension, de logique et de rapidité sous la pression d'un chronomètre.
4. **Extinction (`FireGame`) :** Mini-jeu de dextérité et de réactivité face à une épreuve de feu virtuel.
5. **Recherche de Signal (`signalGame`) :** Mécanique de jeu basée sur l'intensité et la détection.

---

## 🗂 Ma Collection & Échanges
Une fois capturées, les cartes sont stockées dans l'onglet **Ma Collection**.
* **Historique détaillé :** Chaque carte garde en mémoire son historique d'obtention (Capturée, Gagnée en bataille, Échangée).
* **Système de Transfert :** Les joueurs peuvent se donner des cartes via leur ID unique.
* **Traitement des données :** L'application utilise des utilitaires de formatage (ex: `cleanName()`) pour transformer les données brutes de l'API en affichage UI propre et lisible.

---

## ⚙️ Installation
1. Cloner le dépôt.
2. Configurer les clés API dans le `local.properties`.
3. Lancer via Android Studio ou générer un build avec Fastlane : `fastlane build_and_test`.