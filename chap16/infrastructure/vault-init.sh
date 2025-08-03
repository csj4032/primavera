#\!/bin/bash

# Vault Configuration for FileProcessingMonitoringApplication
export VAULT_ADDR='http://127.0.0.1:8212'
export VAULT_TOKEN='primavera-vault-token'

echo "Initializing Vault for chap16..."

# Enable KV secrets engine
vault secrets enable -path=secret kv-v2

# Configure FileProcessingMonitoringApplication
vault kv put secret/FileProcessingMonitoringApplication/local \
  spring.datasource.driver-class-name=org.mariadb.jdbc.Driver \
  spring.datasource.url=jdbc:mariadb://localhost:3320/primavera \
  spring.datasource.username=primavera \
  spring.datasource.password=primavera

echo "Vault configuration completed for chap16"
