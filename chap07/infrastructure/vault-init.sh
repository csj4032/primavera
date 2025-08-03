#\!/bin/bash

# Vault Configuration for ThymeleafWebApplication
export VAULT_ADDR='http://127.0.0.1:8203'
export VAULT_TOKEN='primavera-vault-token'

echo "Initializing Vault for chap07..."

# Enable KV secrets engine
vault secrets enable -path=secret kv-v2

# Configure ThymeleafWebApplication
vault kv put secret/ThymeleafWebApplication/local \
  spring.datasource.driver-class-name=org.mariadb.jdbc.Driver \
  spring.datasource.url=jdbc:mariadb://localhost:3311/primavera \
  spring.datasource.username=primavera \
  spring.datasource.password=primavera

echo "Vault configuration completed for chap07"
