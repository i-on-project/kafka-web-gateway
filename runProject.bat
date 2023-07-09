@echo off

cd .\code\kafka-web-gateway\
call gradlew task extractUberJar

cd ..\kafka-streams-module-demo\
call gradlew task extractUberJar

cd ...\
docker compose up --build --force-recreate
