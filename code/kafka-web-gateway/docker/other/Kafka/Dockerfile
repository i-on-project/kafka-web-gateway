
# CURRENTLY NOT WORKING


FROM ubuntu:latest

WORKDIR /kafka-folder

#RUN sudo adduser kafka \
#    && sudo adduser kafka sudo \
#    && su --login kafka \

RUN apt-get update && apt-get install curl -y
#    curl \
RUN mkdir Downloads \
    && curl "https://downloads.apache.org/kafka/2.8.2/kafka_2.13-2.8.2.tgz" -o /Downloads/kafka.tgz \
    && mkdir kafka \
    && cd /kafka \
    && tar -xvzf /kafka-folder/Downloads/kafka.tgz --strip 1

CMD ["bin/zookeeper-server-start.sh", "config/zookeeper.properties"]

CMD ["bin/kafka-server-start.sh", "config/server.properties"]


