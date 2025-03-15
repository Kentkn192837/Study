FROM openjdk:11-slim

RUN apt-get update && \
    apt-get install -y default-mysql-client wget && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# MySQLのJDBCドライバをダウンロード
RUN mkdir -p /usr/src/lib && \
    wget https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.28/mysql-connector-java-8.0.28.jar -O /usr/src/lib/mysql-connector-java-8.0.28.jar

WORKDIR /usr/src
