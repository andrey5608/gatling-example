#!/usr/bin/env sh
set -eu

INFLUX_CONFIG=/etc/influxdb/influxdb.conf
INIT_MARKER=/var/lib/influxdb/.init

if [ ! -f "$INIT_MARKER" ]; then
    influxd -config "$INFLUX_CONFIG" "$@" &
    INFLUX_PID=$!

    until wget -q "http://localhost:8086/ping" 2>/dev/null; do
        sleep 1
    done

    influx -host localhost -port 8086 -execute "CREATE USER ${INFLUX_USER} WITH PASSWORD '${INFLUX_PASSWORD}' WITH ALL PRIVILEGES"
    influx -host localhost -port 8086 -execute "CREATE DATABASE ${INFLUX_DB}"

    touch "$INIT_MARKER"
    kill -s TERM "$INFLUX_PID"
    wait "$INFLUX_PID"
fi

exec influxd -config "$INFLUX_CONFIG" "$@"
