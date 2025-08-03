#\!/bin/bash

# Vault Configuration for SpringSecurityBasicApplication
export VAULT_ADDR='http://127.0.0.1:8205'
export VAULT_TOKEN='primavera-vault-token'

echo "Initializing Vault for chap09..."

# Enable KV secrets engine
vault secrets enable -path=secret kv-v2

# Configure SpringSecurityBasicApplication
vault kv put secret/SpringSecurityBasicApplication/local \
  spring.datasource.driver-class-name=org.mariadb.jdbc.Driver \
  spring.datasource.url=jdbc:mariadb://localhost:3313/primavera \
  spring.datasource.username=primavera \
  spring.datasource.password=primavera

echo "Vault configuration completed for chap09"
