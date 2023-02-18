FROM wordpress:latest

# 必要なツール郡をインストール
RUN apt-get update && \
    apt-get -y install wget unzip

# WP プラグイン (zip) ダウンロード
WORKDIR /tmp/wp-plugins
RUN wget https://downloads.wordpress.org/plugin/contact-form-7.4.6.zip && \
    wget https://downloads.wordpress.org/plugin/syntaxhighlighter.zip && \
    wget https://downloads.wordpress.org/plugin/wp-emmet.zip && \
# Zip ファイルを解凍してインストール
    unzip './*.zip' -d /usr/src/wordpress/wp-content/plugins && \
# 不要になった一時ファイルを削除
    apt-get clean && \
    rm -rf /tmp/* && \
# サーバが読めるように wp-content 以下の所有者を変更
    chown -R www-data:www-data /usr/src/wordpress/wp-content

WORKDIR /var/www/html
