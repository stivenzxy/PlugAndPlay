#!/bin/bash

echo "================================="
echo "  Setup Servidor Vosk para AudioToTextPlugin"
echo "================================="
echo

echo "Verificando si Docker está instalado..."
if ! command -v docker &> /dev/null; then
    echo "ERROR: Docker no está instalado o no está en el PATH"
    echo "Por favor instala Docker desde: https://docs.docker.com/get-docker/"
    exit 1
fi

echo "Docker encontrado. Iniciando servidor Vosk..."
echo

# Cambiar al directorio docker
cd "$(dirname "$0")/../.docker"

echo "Iniciando servidor Vosk con modelo español grande (puede tomar unos minutos la primera vez)..."

# Descargar modelo si no existe en el repo
MODEL_DIR="$(dirname "$0")/../models/vosk-model-es-0.42"
if [ ! -d "$MODEL_DIR" ]; then
  echo "Descargando modelo a $MODEL_DIR ..."
  mkdir -p "$(dirname "$MODEL_DIR")"
  TMP_ZIP="$(mktemp -u).zip"
  curl -L -o "$TMP_ZIP" https://alphacephei.com/vosk/models/vosk-model-es-0.42.zip
  unzip -q "$TMP_ZIP" -d "$(dirname "$MODEL_DIR")"
  rm -f "$TMP_ZIP"
  if [ ! -d "$MODEL_DIR" ]; then
    mv "$(dirname "$MODEL_DIR")/vosk-model-es-0.42" "$MODEL_DIR"
  fi
fi

# Exportar variable para docker-compose (.env)
export VOSK_MODEL_DIR="$(cd "$MODEL_DIR" && pwd)"
echo "VOSK_MODEL_DIR=$VOSK_MODEL_DIR"
if docker compose version >/dev/null 2>&1; then
  docker compose up -d vosk-server
elif docker-compose --version >/dev/null 2>&1; then
  docker-compose up -d vosk-server
else
  echo "No se encontró Docker Compose. Instala Docker Desktop reciente o Compose v1."
  echo "   Guia: https://docs.docker.com/compose/"
  exit 1
fi

if [ $? -eq 0 ]; then
    echo
    echo "Servidor Vosk iniciado correctamente!"
    echo
    echo "El servidor está disponible en: ws://localhost:2700"
    echo "Interfaz web (opcional): http://localhost:2700"
    echo
    echo "Para verificar el estado:"
    echo "  docker ps"
    echo
    echo "Para ver logs:"
    echo "  docker logs plugandplay-vosk-server"
    echo
    echo "Para detener el servidor:"
    echo "  docker compose stop vosk-server  (o)"
    echo "  docker-compose stop vosk-server"
else
    echo
    echo "Error al iniciar el servidor Vosk"
    echo "Revisa los logs con: docker-compose logs vosk-server"
fi

echo
