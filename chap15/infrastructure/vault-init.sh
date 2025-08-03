#\!/bin/bash

# Vault Configuration for JpaAdvancedMappingApplication
export VAULT_ADDR='http://127.0.0.1:8211'
export VAULT_TOKEN='primavera-vault-token'

echo "Initializing Vault for chap15..."

# Enable KV secrets engine
vault secrets enable -path=secret kv-v2

# Configure JpaAdvancedMappingApplication
vault kv put secret/JpaAdvancedMappingApplication/local \
  spring.datasource.driver-class-name=org.mariadb.jdbc.Driver \
  spring.datasource.url=jdbc:mariadb://localhost:3319/primavera \
  spring.datasource.username=primavera \
  spring.datasource.password=primavera

echo "Vault configuration completed for chap15"
