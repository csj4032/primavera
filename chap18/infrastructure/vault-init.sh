#!/bin/bash

export VAULT_ADDR='http://127.0.0.1:8200'
export VAULT_TOKEN='primavera-vault-token'

echo "Initializing Vault for Spring Cloud Config Server..."

vault secrets enable -path=secret kv-v2 2>/dev/null || true

echo "Storing application default configuration..."
vault kv put secret/application \
  message="Default configuration from Vault" \
  server.port=8080 \
  spring.datasource.driver-class-name=org.mariadb.jdbc.Driver \
  spring.datasource.url=jdbc:mariadb://localhost:3318/primavera \
  spring.datasource.username=primavera \
  spring.datasource.password=primavera

echo "Storing front service configuration..."
vault kv put secret/front \
  server.port=8081 \
  api.gateway.url=http://localhost:8080 \
  security.oauth2.client.registration.kakao.client-id=your-kakao-client-id \
  security.oauth2.client.registration.kakao.client-secret=your-kakao-client-secret \
  jwt.secret=your-jwt-secret-key

echo "Storing order service configuration..."
vault kv put secret/order \
  server.port=8082 \
  spring.kafka.bootstrap-servers=localhost:9092 \
  spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer \
  spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer

echo "Storing product service configuration..."
vault kv put secret/product \
  server.port=8083 \
  spring.data.redis.host=localhost \
  spring.data.redis.port=6379 \
  spring.cache.type=redis \
  spring.cache.redis.time-to-live=600000

echo "Storing production profile configuration..."
vault kv put secret/application,prod \
  spring.datasource.url=jdbc:mariadb://prod-db:3306/primavera \
  spring.datasource.username=produser \
  spring.datasource.password=prodpass \
  logging.level.root=WARN \
  management.metrics.export.prometheus.enabled=true

echo "Storing development profile configuration..."
vault kv put secret/application,dev \
  spring.jpa.show-sql=true \
  spring.jpa.properties.hibernate.format_sql=true \
  logging.level.root=DEBUG \
  logging.level.com.genius.primavera=TRACE

echo "Vault configuration completed successfully!"
echo "Configuration Server can now fetch configurations from Vault"