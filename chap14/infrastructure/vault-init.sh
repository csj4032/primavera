#\!/bin/bash

# Vault Configuration for AdvancedJpaApplication
export VAULT_ADDR='http://127.0.0.1:8210'
export VAULT_TOKEN='primavera-vault-token'

echo "Initializing Vault for chap14..."

# Enable KV secrets engine
vault secrets enable -path=secret kv-v2

# Configure AdvancedJpaApplication
vault kv put secret/AdvancedJpaApplication/local \
  spring.datasource.driver-class-name=org.mariadb.jdbc.Driver \
  spring.datasource.url=jdbc:mariadb://localhost:3318/primavera \
  spring.datasource.username=primavera \
  spring.datasource.password=primavera

echo "Vault configuration completed for chap14"
