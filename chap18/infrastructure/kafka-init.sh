#!/bin/bash

echo "Waiting for Kafka to be ready..."
sleep 10

# Create Kafka topics
echo "Creating Kafka topics..."

# Create order-events topic
docker exec kafka-primavera-chap18 kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --partitions 3 \
  --replication-factor 1 \
  --if-not-exists

# Create inventory-events topic
docker exec kafka-primavera-chap18 kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic inventory-events \
  --partitions 3 \
  --replication-factor 1 \
  --if-not-exists

# List all topics
echo "Listing all Kafka topics:"
docker exec kafka-primavera-chap18 kafka-topics --list \
  --bootstrap-server localhost:9092

echo "Kafka initialization completed!"