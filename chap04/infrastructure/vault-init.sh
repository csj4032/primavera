#!/bin/bash

# Vault Configuration for Chapter 04 - Data Access Application
export VAULT_ADDR='http://127.0.0.1:8200'
export VAULT_TOKEN='primavera-vault-token'

echo "Initializing Vault for Chapter 04..."

# Enable KV secrets engine
vault secrets enable -path=secret kv-v2

# Configure DataAccessApplication
vault kv put secret/DataAccessApplication/local \
  spring.datasource.driver-class-name=org.mariadb.jdbc.Driver \
  spring.datasource.url=jdbc:mariadb://localhost:3308/primavera \
  spring.datasource.username=primavera \
  spring.datasource.password=primavera

echo "Vault configuration completed for Chapter 04"