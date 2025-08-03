#\!/bin/bash

# Vault Configuration for SecurityFilterApplication
export VAULT_ADDR='http://127.0.0.1:8204'
export VAULT_TOKEN='primavera-vault-token'

echo "Initializing Vault for chap08..."

# Enable KV secrets engine
vault secrets enable -path=secret kv-v2

# Configure SecurityFilterApplication
vault kv put secret/SecurityFilterApplication/local \
  spring.datasource.driver-class-name=org.mariadb.jdbc.Driver \
  spring.datasource.url=jdbc:mariadb://localhost:3312/primavera \
  spring.datasource.username=primavera \
  spring.datasource.password=primavera

echo "Vault configuration completed for chap08"
