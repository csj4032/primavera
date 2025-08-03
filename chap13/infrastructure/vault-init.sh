#\!/bin/bash

# Vault Configuration for AdvancedAuthorizationApplication
export VAULT_ADDR='http://127.0.0.1:8209'
export VAULT_TOKEN='primavera-vault-token'

echo "Initializing Vault for chap13..."

# Enable KV secrets engine
vault secrets enable -path=secret kv-v2

# Configure AdvancedAuthorizationApplication
vault kv put secret/AdvancedAuthorizationApplication/local \
  spring.datasource.driver-class-name=org.mariadb.jdbc.Driver \
  spring.datasource.url=jdbc:mariadb://localhost:3317/primavera \
  spring.datasource.username=primavera \
  spring.datasource.password=primavera \
  spring.data.mongodb.host=localhost \
  spring.data.mongodb.port=27017 \
  spring.data.mongodb.database=primavera \
  spring.data.mongodb.username=primavera \
  spring.data.mongodb.password=primavera \
  spring.data.mongodb.authentication-database=admin

echo "Vault configuration completed for chap13"
