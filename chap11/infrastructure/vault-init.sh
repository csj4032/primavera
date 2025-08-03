#\!/bin/bash

# Vault Configuration for BoardSystemApplication
export VAULT_ADDR='http://127.0.0.1:8207'
export VAULT_TOKEN='primavera-vault-token'

echo "Initializing Vault for chap11..."

# Enable KV secrets engine
vault secrets enable -path=secret kv-v2

# Configure BoardSystemApplication
vault kv put secret/BoardSystemApplication/local \
  spring.datasource.driver-class-name=org.mariadb.jdbc.Driver \
  spring.datasource.url=jdbc:mariadb://localhost:3315/primavera \
  spring.datasource.username=primavera \
  spring.datasource.password=primavera

echo "Vault configuration completed for chap11"
