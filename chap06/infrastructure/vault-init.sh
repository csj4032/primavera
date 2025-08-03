#\!/bin/bash

# Vault Configuration for ValidationApplication
export VAULT_ADDR='http://127.0.0.1:8202'
export VAULT_TOKEN='primavera-vault-token'

echo "Initializing Vault for chap06..."

# Enable KV secrets engine
vault secrets enable -path=secret kv-v2

# Configure ValidationApplication
vault kv put secret/ValidationApplication/local \
  spring.datasource.driver-class-name=org.mariadb.jdbc.Driver \
  spring.datasource.url=jdbc:mariadb://localhost:3310/primavera \
  spring.datasource.username=primavera \
  spring.datasource.password=primavera

echo "Vault configuration completed for chap06"
