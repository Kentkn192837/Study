FROM openjdk:11-slim

# 必要なパッケージのインストール
RUN apt-get update && \
    apt-get install -y libaio1 wget unzip curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Oracle Instant Clientのインストール（JavaアプリからOracleに接続するため）
RUN mkdir -p /opt/oracle && \
    cd /opt/oracle && \
    wget https://download.oracle.com/otn_software/linux/instantclient/instantclient-basiclite-linuxx64.zip && \
    unzip instantclient-basiclite-linuxx64.zip && \
    rm instantclient-basiclite-linuxx64.zip && \
    mkdir -p /usr/src/lib

# OJDBCドライバのダウンロード
RUN cd /usr/src/lib && \
    wget https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc8/21.5.0.0/ojdbc8-21.5.0.0.jar

# 環境変数の設定
ENV LD_LIBRARY_PATH=/opt/oracle/instantclient_21_1:$LD_LIBRARY_PATH
ENV TNS_ADMIN=/opt/oracle/instantclient_21_1/network/admin
ENV PATH=/opt/oracle/instantclient_21_1:$PATH

WORKDIR /usr/src
