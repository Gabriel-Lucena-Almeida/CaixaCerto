#!/bin/sh
# Compila e roda o CaixaCerto. Exemplo com dados de demonstração: ./rodar.sh --demo
cd "$(dirname "$0")" || exit 1
mkdir -p out
javac -encoding UTF-8 -d out src/br/ufpb/caixacerto/*.java || exit 1
exec java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp out br.ufpb.caixacerto.Main "$@"
