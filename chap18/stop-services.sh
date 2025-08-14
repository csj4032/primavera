#!/bin/bash

echo "🛑 Stopping Chapter 18 Microservices Architecture"
echo "================================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Step 1: Stop Application Services
echo -e "\n${YELLOW}Step 1: Stopping Application Services...${NC}"

# Function to stop service
stop_service() {
    local pid_file=$1
    local service_name=$2
    
    if [ -f "$pid_file" ]; then
        PID=$(cat "$pid_file")
        if ps -p $PID > /dev/null 2>&1; then
            echo "Stopping $service_name (PID: $PID)..."
            kill $PID
            sleep 2
            # Force kill if still running
            if ps -p $PID > /dev/null 2>&1; then
                kill -9 $PID
            fi
            echo -e "${GREEN}✅ $service_name stopped${NC}"
        else
            echo -e "${YELLOW}⚠️  $service_name was not running${NC}"
        fi
        rm -f "$pid_file"
    else
        echo -e "${YELLOW}⚠️  No PID file for $service_name${NC}"
    fi
}

# Create .pids directory if it doesn't exist
mkdir -p .pids

# Stop all services
stop_service ".pids/front.pid" "Front Gateway"
stop_service ".pids/account.pid" "Account Service"
stop_service ".pids/order.pid" "Order Service"
stop_service ".pids/product.pid" "Product Service"
stop_service ".pids/config.pid" "Configuration Server"

# Step 2: Stop Infrastructure (optional)
echo -e "\n${YELLOW}Step 2: Infrastructure Status${NC}"
echo "To stop infrastructure, run:"
echo "  cd infrastructure && docker-compose -f docker-compose-full.yml down"
echo ""
echo "To stop and remove all data:"
echo "  cd infrastructure && docker-compose -f docker-compose-full.yml down -v"

# Clean up
rm -rf .pids

echo -e "\n${GREEN}✨ All application services stopped!${NC}"
echo "================================================"