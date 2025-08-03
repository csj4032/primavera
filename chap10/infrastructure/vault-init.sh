#\!/bin/bash

# Vault Configuration for OAuth2SocialLoginApplication
export VAULT_ADDR='http://127.0.0.1:8206'
export VAULT_TOKEN='primavera-vault-token'

echo "Initializing Vault for chap10..."

# Enable KV secrets engine
vault secrets enable -path=secret kv-v2

# Configure OAuth2SocialLoginApplication
vault kv put secret/OAuth2SocialLoginApplication/local \
  spring.datasource.driver-class-name=org.mariadb.jdbc.Driver \
  spring.datasource.url=jdbc:mariadb://localhost:3314/primavera \
  spring.datasource.username=primavera \
  spring.datasource.password=primavera

echo "Vault configuration completed for chap10"
