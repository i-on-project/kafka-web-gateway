Open terminal and look for the docker container:
```
docker exec -ti docker-kafka-1 bash
```
* Create topic
```
/opt/bitnami/kafka/bin/kafka-topics.sh --create --topic topic-nr-1 --bootstrap-server localhost:9092
```

* Create Producer
```
/opt/bitnami/kafka/bin/kafka-console-producer.sh --topic topic-nr-1 --bootstrap-server localhost:9092
```

* Create Consumer
```
/opt/bitnami/kafka/bin/kafka-console-consumer.sh --topic topic-nr-1 --from-beginning --bootstrap-server localhost:9092
```