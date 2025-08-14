#!/bin/bash

echo "🚀 Starting Chapter 18 Microservices Architecture"
echo "================================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to check if service is running
check_service() {
    local port=$1
    local name=$2
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/actuator/health | grep -q "200"; then
        echo -e "${GREEN}✅ $name Service (Port $port): UP${NC}"
        return 0
    else
        echo -e "${RED}❌ $name Service (Port $port): DOWN${NC}"
        return 1
    fi
}

# Step 1: Start Infrastructure
echo -e "\n${YELLOW}Step 1: Starting Infrastructure...${NC}"
cd infrastructure
docker-compose -f docker-compose-full.yml up -d
echo "Waiting for infrastructure to be ready..."
sleep 30

# Initialize Kafka topics
./kafka-init.sh

cd ..

# Step 2: Start Configuration Server
echo -e "\n${YELLOW}Step 2: Starting Configuration Server...${NC}"
SPRING_PROFILES_ACTIVE=native ./gradlew :chap18:configuration:bootRun &
CONFIG_PID=$!
echo "Configuration Server PID: $CONFIG_PID"
echo "Waiting for Configuration Server to start..."
sleep 20

# Check Configuration Server
check_service 8888 "Configuration"

# Step 3: Start Backend Services
echo -e "\n${YELLOW}Step 3: Starting Backend Services...${NC}"

# Start Account Service
echo "Starting Account Service..."
SPRING_PROFILES_ACTIVE=default ./gradlew :chap18:account:bootRun &
ACCOUNT_PID=$!
echo "Account Service PID: $ACCOUNT_PID"

# Start Order Service
echo "Starting Order Service..."
SPRING_PROFILES_ACTIVE=default ./gradlew :chap18:order:bootRun &
ORDER_PID=$!
echo "Order Service PID: $ORDER_PID"

# Start Product Service
echo "Starting Product Service..."
SPRING_PROFILES_ACTIVE=default ./gradlew :chap18:product:bootRun &
PRODUCT_PID=$!
echo "Product Service PID: $PRODUCT_PID"

echo "Waiting for backend services to start..."
sleep 30

# Step 4: Start Front Gateway
echo -e "\n${YELLOW}Step 4: Starting Front Gateway...${NC}"
SPRING_PROFILES_ACTIVE=default ./gradlew :chap18:front:bootRun &
FRONT_PID=$!
echo "Front Gateway PID: $FRONT_PID"
sleep 15

# Step 5: Health Check
echo -e "\n${YELLOW}Step 5: Checking All Services...${NC}"
echo "================================================"

check_service 8888 "Configuration"
check_service 8081 "Account"
check_service 8082 "Order"
check_service 8083 "Product"
check_service 8080 "Front Gateway"

echo -e "\n${GREEN}✨ All services are running!${NC}"
echo "================================================"
echo "Service URLs:"
echo "  - Configuration: http://localhost:8888"
echo "  - Account:       http://localhost:8081"
echo "  - Order:         http://localhost:8082"
echo "  - Product:       http://localhost:8083"
echo "  - Front Gateway: http://localhost:8080"
echo "  - Kafka UI:      http://localhost:8090"
echo ""
echo "Infrastructure:"
echo "  - MariaDB:       localhost:3318"
echo "  - Redis:         localhost:6379"
echo "  - MongoDB:       localhost:27017"
echo "  - Kafka:         localhost:9092"
echo "  - Vault:         http://localhost:8200"
echo ""
echo "PIDs:"
echo "  - Configuration: $CONFIG_PID"
echo "  - Account:       $ACCOUNT_PID"
echo "  - Order:         $ORDER_PID"
echo "  - Product:       $PRODUCT_PID"
echo "  - Front:         $FRONT_PID"
echo ""
echo "To stop all services, run: ./stop-services.sh"
echo "================================================"

# Save PIDs to file for stop script
echo "$CONFIG_PID" > .pids/config.pid
echo "$ACCOUNT_PID" > .pids/account.pid
echo "$ORDER_PID" > .pids/order.pid
echo "$PRODUCT_PID" > .pids/product.pid
echo "$FRONT_PID" > .pids/front.pid

# Keep script running
wait