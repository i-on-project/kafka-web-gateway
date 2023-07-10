<h1 align="center">
  <br>
  Kafka Web Gateway
  <br>
</h1>

<h4 align="center">A scalable intermediary server to connect Browser clients to Apache Kafka</h4>

<p align="center">
  <a href="../../graphs/contributors">
    <img src="https://img.shields.io/github/contributors/i-on-project/kafka-web-gateway" alt="Contributors"/>
  </a>
  <a href="../../stargazers">
     <img src="https://img.shields.io/github/stars/i-on-project/kafka-web-gateway" alt="Stars"/>
  </a>
  <a href="../../issues">
     <img src="https://img.shields.io/github/issues/i-on-project/kafka-web-gateway" alt="Issues"/>
  </a>
  <a href="../../pulls">
     <img src="https://img.shields.io/github/issues-pr/i-on-project/kafka-web-gateway" alt="Pull requests"/>
  </a>
  <a href="../../commits/master">
     <img src="https://img.shields.io/github/last-commit/i-on-project/kafka-web-gateway" alt="Last commit"/>
  </a>
</p>

<p align="center">
  <a href="#deployment">Deployment</a> •
  <a href="#documentation">Documentation</a> •
  <a href="#key-features">Key Features</a> •
  <a href="#authors">Authors</a>
</p>

Kafka uses a binary protocol over TCP. A web browser cannot natively establish a TCP connection with a Kafka Cluster which leads to the need of a protocol translation intermediary infrastructure. This intermediary takes advantage of the official Kafka Client APIs to interact with the Kafka cluster and expose a WebSocket protocol oriented API to establish bidirectional message based communication with the web browsers. This procedure will allow any other platform supporting WebSockets to establish a connection with the mentioned intermediary infrastructure.

To address this limitation, the intermediary infrastructure solution provides the ability of associating a number of client browsers to a much reduced number of Kafka Clients.
With this approach, the web browser uses a library with the capability of communicating with the exposed intermediary infrastructure’s WebSocket API. 
Associated with the scalling limitation issue, since one of the goals of this project is to allow a high number of web clients to access Kafka then some strategy needs to me applied in order to supply each web client with only the information they seek.
For example: If a Battleship application with thousands of players and thousands of
matches is using this solution then a strategy to represent a game in Kafka needs to be
created since a topic cannot represent a game due to the low topic limit for this sort of scale (4000 topics if each topic possesses a partition only).

The solution provides the option for the web clients to specify
the keys from each topic that they seek to consume, while the intermediary infrastructure
subscribes to the whole topics in Kafka. In the mentioned example a key could be used in
each record to distinguish what information each web client will consume.
---

## Documentation


Additionally, a final report of the project is
available ADD LINK.

---

## Developers

* [Alexandre Silva](https://github.com/Cors00)
* [Miguel Marques](https://github.com/mjbmarques)

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
