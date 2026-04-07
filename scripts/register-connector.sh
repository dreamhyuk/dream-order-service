#!/bin/bash

echo "Debezium 커넥터 등록을 시작합니다..."

# 1. 커넥터 등록 요청
curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" \
  localhost:8083/connectors/ \
  -d '{
  "name": "inventory-connector",
  "config": {
    "connector.class": "io.debezium.connector.mysql.MySqlConnector",
    "tasks.max": "1",
    "database.hostname": "mysql",
    "database.port": "3306",
    "database.user": "root",
    "database.password": "password",
    "database.server.id": "1",
    "topic.prefix": "dbserver",
    "database.include.list": "mydb",
    "schema.history.internal.kafka.bootstrap.servers": "kafka:29092",
    "schema.history.internal.kafka.topic": "schemahistory.inventory"
  }
}'

echo -e "\n\n커넥터가 준비될 때까지 3초간 기다립니다..."
sleep 3

echo "상태 확인:"
curl localhost:8083/connectors/inventory-connector/status