# /build.sh
#!/bin/bash

# Define cores para o output do terminal
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "Compilando pseudo-SO para Java 21..."

# Cria o diretório de saída caso não exista
mkdir -p out

# Encontra todos os .java recursivamente e compila garantindo o padrão Java 21
find src -name "*.java" >sources.txt
javac -d out --release 21 @sources.txt
COMPILATION_STATUS=$?
rm sources.txt

if [ $COMPILATION_STATUS -eq 0 ]; then
  echo -e "${GREEN}Compilação concluída. Os binários estão na pasta /out.${NC}"
  echo "Para executar, use: java -cp out Dispatcher <processes.txt> <files.txt> <string.txt>"
else
  echo -e "${RED}Erro durante a compilação.${NC}"
  exit 1
fi
