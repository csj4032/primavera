#\!/bin/bash

# Vault Configuration for MicroserviceApplication
export VAULT_ADDR='http://127.0.0.1:8213'
export VAULT_TOKEN='primavera-vault-token'

echo "Initializing Vault for chap17..."

# Enable KV secrets engine
vault secrets enable -path=secret kv-v2

# Configure MicroserviceApplication
vault kv put secret/MicroserviceApplication/local \
  spring.datasource.driver-class-name=org.mariadb.jdbc.Driver \
  spring.datasource.url=jdbc:mariadb://localhost:3321/primavera \
  spring.datasource.username=primavera \
  spring.datasource.password=primavera

echo "Vault configuration completed for chap17"
