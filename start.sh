#!/bin/bash

echo "Démarrage de l'environnement E-OFANA..."

if [ ! -f .env ]; then
  echo "Création du fichier .env depuis .env.example..."
  cp .env.example .env
fi

docker compose --env-file .env up --build
