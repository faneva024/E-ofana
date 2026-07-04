#!/bin/bash

echo "====================================="
echo " Démarrage du projet E-OFANA"
echo " Backend Spring Boot + Frontend Vue"
echo "====================================="

ROOT_DIR="$(pwd)"

echo ""
echo "0) Libération des anciens ports..."
fuser -k 8080/tcp 5173/tcp 5174/tcp 5175/tcp 5176/tcp 2>/dev/null

sleep 2

echo ""
echo "1) Démarrage du backend sur http://localhost:8080"
cd "$ROOT_DIR"
mvn spring-boot:run &
BACKEND_PID=$!

echo ""
echo "2) Attente du backend..."
sleep 12

echo ""
echo "3) Démarrage du frontend sur http://localhost:5174"
cd "$ROOT_DIR/frontend"

if [ ! -d "node_modules" ]; then
  echo "Installation des dépendances frontend..."
  npm install
fi

npm run dev &
FRONTEND_PID=$!

echo ""
echo "Projet lancé."
echo "Frontend : http://localhost:5174"
echo "Backend  : http://localhost:8080"
echo ""
echo "Appuie sur Ctrl + C pour arrêter."

trap "echo 'Arrêt du projet...'; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; exit" INT

wait

