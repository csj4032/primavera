#!/bin/bash

export VAULT_ADDR='http://localhost:8200'
export VAULT_TOKEN='primavera-vault-token'

echo "Initializing Vault for chap18 microservices..."

# Enable KV v2 secrets engine
vault secrets enable -version=2 -path=secret kv || true

# Application common configuration
vault kv put secret/application \
  spring.jackson.time-zone=UTC \
  logging.config=classpath:logback-spring.xml \
  logging.level.root=INFO \
  logging.level.com.genius.primavera=DEBUG \
  server.tomcat.threads.max=1

# Account Service configuration
vault kv put secret/account-service \
  spring.application.name=AccountApplication \
  server.port=8080 \
  spring.data.redis.host=localhost \
  spring.data.redis.port=6379 \
  spring.data.redis.username=primavera \
  spring.data.redis.password=primavera \
  primavera.config.name=account \
  primavera.config.enabled=true \
  primavera.config.logs.path=./logs/account

# Order Service configuration
vault kv put secret/order-service \
  spring.application.name=OrderApplication \
  server.port=8082 \
  spring.r2dbc.url=r2dbc:mariadb://localhost:3318/order_service \
  spring.r2dbc.username=primavera \
  spring.r2dbc.password=primavera \
  spring.r2dbc.pool.initial-size=10 \
  spring.r2dbc.pool.max-size=20 \
  spring.r2dbc.pool.max-idle-time=300m \
  spring.r2dbc.pool.validation-query="SELECT 1" \
  spring.kafka.bootstrap-servers=localhost:9092 \
  spring.kafka.consumer.group-id=order-service \
  spring.kafka.consumer.auto-offset-reset=earliest \
  spring.kafka.consumer.enable-auto-commit=false \
  spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer \
  spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer \
  spring.kafka.consumer.properties.spring.json.trusted.packages="*" \
  spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer \
  spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer \
  spring.kafka.producer.acks=all \
  spring.kafka.producer.retries=3 \
  spring.kafka.producer.properties.enable.idempotence=true \
  kafka.topics.order-events=order-events \
  kafka.topics.inventory-events=inventory-events \
  primavera.config.name=order \
  primavera.config.enabled=true \
  primavera.config.logs.path=./logs/order

# Product Service configuration
vault kv put secret/product-service \
  spring.application.name=productApplication \
  server.port=8083 \
  spring.data.mongodb.host=localhost \
  spring.data.mongodb.port=27017 \
  spring.data.mongodb.database=primavera_products \
  spring.data.mongodb.username=root \
  spring.data.mongodb.password=primavera \
  spring.data.mongodb.authentication-database=admin \
  spring.kafka.bootstrap-servers=localhost:9092 \
  spring.kafka.consumer.group-id=inventory-service \
  spring.kafka.consumer.auto-offset-reset=earliest \
  spring.kafka.consumer.enable-auto-commit=false \
  spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer \
  spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer \
  spring.kafka.consumer.properties.spring.json.trusted.packages="*" \
  spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer \
  spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer \
  spring.kafka.producer.acks=all \
  spring.kafka.producer.retries=3 \
  spring.kafka.producer.properties.enable.idempotence=true \
  kafka.topics.order-events=order-events \
  kafka.topics.inventory-events=inventory-events \
  primavera.config.name=product \
  primavera.config.enabled=true \
  primavera.config.logs.path=./logs/product

# Front Service configuration
vault kv put secret/front-service \
  spring.application.name=FrontApplication \
  server.port=8081 \
  management.endpoints.web.exposure.include=refresh \
  primavera.config.name=front \
  primavera.config.enabled=true \
  primavera.config.logs.path=./logs/front

# Local profile specific configurations
vault kv put secret/application,local \
  spring.profiles.active=local \
  spring.profiles.group.local=console-appender,file-debug-appender,file-error-appender,file-info-appender,file-warn-appender \
  spring.profiles.group.test=console-appender \
  logging.level.org.springframework.web=DEBUG \
  logging.level.org.springframework.cloud.config=DEBUG

echo "Vault configuration completed for chap18"