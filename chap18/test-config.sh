#!/bin/bash

echo "===== Starting Infrastructure Services ====="
cd infrastructure
docker-compose up -d

echo "Waiting for services to be ready..."
sleep 10

echo "===== Verifying Vault Status ====="
export VAULT_ADDR='http://localhost:8200'
export VAULT_TOKEN='primavera-vault-token'
vault status

echo "===== Checking Vault Configurations ====="
echo "Application Common Config:"
vault kv get secret/application

echo -e "\nAccount Service Config:"
vault kv get secret/account-service

echo -e "\nOrder Service Config:"
vault kv get secret/order-service

echo -e "\nProduct Service Config:"
vault kv get secret/product-service

echo -e "\nFront Service Config:"
vault kv get secret/front-service

cd ..

echo -e "\n===== Starting Configuration Server ====="
./gradlew :chap18:configuration:bootRun &
CONFIG_PID=$!

echo "Waiting for Configuration Server to start..."
sleep 15

echo -e "\n===== Testing Configuration Endpoints ====="
echo "Account Service Configuration:"
curl -s http://localhost:8888/account-service/default | jq '.'

echo -e "\nOrder Service Configuration:"
curl -s http://localhost:8888/order-service/default | jq '.'

echo -e "\nProduct Service Configuration:"
curl -s http://localhost:8888/product-service/default | jq '.'

echo -e "\nFront Service Configuration:"
curl -s http://localhost:8888/front-service/default | jq '.'

echo -e "\n===== Testing with Local Profile ====="
echo "Application with local profile:"
curl -s http://localhost:8888/application/local | jq '.'

echo -e "\n===== Cleanup ====="
echo "Stopping Configuration Server..."
kill $CONFIG_PID

echo "Test completed!"