# E-ofana
Projet Mini-soutenance

# Versions et Prérequis du Projet

## Environnement de développement

### Système d'exploitation

* Linux (Ubuntu 22.04+ recommandé)
* Windows 10/11
* macOS

### Java

* Version minimale : Java 17

### Maven

* Version recommandée : Maven 4.1.0

## Lancement de l'application

### Via Maven

```bash
mvn spring-boot:run
```
## Génération du fichier JAR

```bash
mvn package
```

## Structure du projet
### Arborescence principale

```text
src/
└── main/
    ├── java/
    │   └── com/
    │       └── eofana/
    │           ├── controller/
    │           ├── service/
    │           ├── repository/
    │           ├── model/
    │           └── EofanaApplication.java
    └── resources/
        ├── application.properties
        └── static/
```

## Notes importantes

### Configuration de la base de données

Modifier les paramètres dans :

```text
src/main/resources/application.properties
```

### Compatibilité
Le projet est développé avec Java 17 et reste compatible avec les versions supérieures de Java.
