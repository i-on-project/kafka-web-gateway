# kafka-web-gateway

**Enter project directory**

```
cd ./code/kafka-web-gateway/
```

**Generate uberJar with gradle task extractUberJar**

```
./gradlew task extractUberJar
```

**Start docker containers**

* PostgreSQL Database
* 2 Gateway's
* 3 Apache Kafka Servers/Nodes
* 3 ZooKeeper's Servers
* Nginx

```
./gradlew task composeUp
```

* If preferred, use docker compose

```
docker compose up --build --force-recreate
```

**Utils**

* Start shell on postgres container

```
docker exec -ti db-g06-tests bash
```

* Start `psql` inside postgres container

```
psql -U gateway_user -d gateway_db