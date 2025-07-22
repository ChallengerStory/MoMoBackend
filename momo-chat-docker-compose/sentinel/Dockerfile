FROM redis:latest

EXPOSE 26379

ADD sentinel.conf /etc/redis/sentinel.conf

RUN mkdir -p /var/lib/redis && \
    chmod 777 /var/lib/redis && \
    chown redis:redis /etc/redis/sentinel.conf

COPY sentinel-entrypoint.sh /usr/local/bin/

RUN chmod +x /usr/local/bin/sentinel-entrypoint.sh

ENTRYPOINT ["sentinel-entrypoint.sh"]
