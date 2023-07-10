@echo off

cd .\code\gateway-hub\
call gradlew task extractUberJar

cd ..\record-router\
call gradlew task extractUberJar

cd ...\
docker compose up --build --force-recreate
