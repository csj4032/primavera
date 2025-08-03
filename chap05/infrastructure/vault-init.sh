#!/bin/bash

# Vault Configuration for Chapter 05 - MyBatis Logging Application
export VAULT_ADDR='http://127.0.0.1:8201'
export VAULT_TOKEN='primavera-vault-token'

echo "Initializing Vault for Chapter 05..."

# Enable KV secrets engine
vault secrets enable -path=secret kv-v2

# Configure MyBatisLoggingApplication
vault kv put secret/MyBatisLoggingApplication/local \
  spring.datasource.driver-class-name=org.mariadb.jdbc.Driver \
  spring.datasource.url=jdbc:mariadb://localhost:3309/primavera \
  spring.datasource.username=primavera \
  spring.datasource.password=primavera

echo "Vault configuration completed for Chapter 05"