#\!/bin/bash

# Vault Configuration for HierarchicalCommentApplication
export VAULT_ADDR='http://127.0.0.1:8208'
export VAULT_TOKEN='primavera-vault-token'

echo "Initializing Vault for chap12..."

# Enable KV secrets engine
vault secrets enable -path=secret kv-v2

# Configure HierarchicalCommentApplication
vault kv put secret/HierarchicalCommentApplication/local \
  spring.datasource.driver-class-name=org.mariadb.jdbc.Driver \
  spring.datasource.url=jdbc:mariadb://localhost:3316/primavera \
  spring.datasource.username=primavera \
  spring.datasource.password=primavera

echo "Vault configuration completed for chap12"
