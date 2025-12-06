FROM influxdb:1.8

WORKDIR /app

COPY entrypoint.sh ./
RUN chmod +x entrypoint.sh

COPY influxdb.conf /etc/influxdb/influxdb.conf

EXPOSE 8086 2003

ENTRYPOINT ["/app/entrypoint.sh"]
