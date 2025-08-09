#!/usr/bin/env bash

# Primavera Multi-Module Docker Infrastructure Manager
# 완전한 멀티모듈 아키텍처 지원 (chap17, chap18 포함)

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
INFRA_DIR="$SCRIPT_DIR/.."
TEMPLATES_DIR="$SCRIPT_DIR/templates"
CONFIGS_DIR="$SCRIPT_DIR/configs"
GENERATED_DIR="$INFRA_DIR/generated"

mkdir -p "$GENERATED_DIR"

show_usage() {
    echo "🌸 Primavera Multi-Module Docker Infrastructure Manager"
    echo "=================================================="
    echo ""
    echo "Usage: $0 <command> [chapter] [options]"
    echo ""
    echo "📋 Commands:"
    echo "  start <chapter>         Start Docker services for specific chapter"
    echo "  stop <chapter>          Stop Docker services for specific chapter"
    echo "  restart <chapter>       Restart Docker services for specific chapter"
    echo "  status <chapter>        Show detailed status of Docker services"
    echo "  start-all              Start all available Docker services"
    echo "  stop-all               Stop all running Docker services"
    echo "  status-all             Show status of all Docker services"
    echo "  list                   List all available chapters with details"
    echo "  clean <chapter>         Remove containers and volumes for specific chapter"
    echo "  clean-all              Remove all containers and volumes"
    echo "  logs <chapter> [service] Show logs for chapter services"
    echo "  generate-configs       Generate all configuration files"
    echo "  info <chapter>         Show detailed chapter information"
    echo ""
    echo "🚀 Examples:"
    echo "  $0 start chap04                # Basic MyBatis + MariaDB"
    echo "  $0 start chap10                # OAuth2 + Redis + MariaDB"
    echo "  $0 start chap13                # Reactive + MongoDB + Redis"
    echo "  $0 start chap17                # Multi-module + Elasticsearch + CDC"
    echo "  $0 start chap18                # Complete Microservices + Full Stack"
    echo "  $0 status chap17               # Check chap17 with sub-modules status"
    echo "  $0 info chap17                 # Show chap17 detailed information"
    echo "  $0 logs chap17 vault           # Show vault logs for chap17"
    echo ""
    echo "📚 Chapter Categories:"
    echo "  chap01-04: Foundation (Basic Spring Boot concepts)"
    echo "  chap05-08: Web Development (MVC, Security, Templates)"
    echo "  chap09-12: Authentication & Authorization"
    echo "  chap13-16: Advanced Features (Reactive, JPA, Cloud)"
    echo "  chap17:    Multi-Module Architecture (batch + streaming)"
    echo "  chap18:    Complete Microservices (5 services)"
    echo "  preface:   Framework fundamentals"
}

validate_chapter() {
    local chapter=$1
    if [[ ! -f "$CONFIGS_DIR/${chapter}-config.env" ]]; then
        echo "❌ Error: Configuration not found for chapter '$chapter'"
        echo ""
        echo "💡 Available chapters:"
        list_chapters
        echo ""
        echo "🔧 To generate missing configs: $0 generate-configs"
        exit 1
    fi
}

list_chapters() {
    if [[ ! -d "$CONFIGS_DIR" ]] || [[ -z "$(ls -A "$CONFIGS_DIR" 2>/dev/null)" ]]; then
        echo "⚠️  No configuration files found."
        echo "🔧 Run '$0 generate-configs' to create them."
        return
    fi
    
    echo "🏗️  Available chapters:"
    echo "=================="
    
    local single_count=0
    local multi_count=0
    
    for config in "$CONFIGS_DIR"/*-config.env; do
        if [[ -f "$config" ]]; then
            local chapter=$(basename "$config" | sed 's/-config\.env$//')
            local app_name=$(grep "^APP_NAME=" "$config" | cut -d'=' -f2)
            local services=$(grep "^SERVICES=" "$config" | cut -d'=' -f2)
            local module_type=$(grep "^MODULE_TYPE=" "$config" | cut -d'=' -f2 || echo "single")
            local sub_modules=$(grep "^SUB_MODULES=" "$config" | cut -d'=' -f2 2>/dev/null || echo "")
            
            if [[ "$module_type" == "multi" ]]; then
                printf "🏗️  %-12s - %-30s [%s]\n" "$chapter" "$app_name" "$services"
                if [[ -n "$sub_modules" ]]; then
                    IFS=',' read -ra SUB_MODULE_LIST <<< "$sub_modules"
                    for sub_module in "${SUB_MODULE_LIST[@]}"; do
                        IFS='=' read -r module_name module_app <<< "$sub_module"
                        printf "    └── %-8s - %s\n" "$module_name" "$module_app"
                    done
                fi
                ((multi_count++))
            else
                printf "📦 %-12s - %-30s [%s]\n" "$chapter" "$app_name" "$services"
                ((single_count++))
            fi
        fi
    done
    
    echo ""
    echo "📊 Summary: $single_count single modules, $multi_count multi-modules"
}

load_config() {
    local chapter=$1
    local config_file="$CONFIGS_DIR/${chapter}-config.env"
    
    if [[ ! -f "$config_file" ]]; then
        echo "❌ Error: Config file not found: $config_file"
        exit 1
    fi
    
    # Load configuration
    source "$config_file"
    
    # Export all variables for template processing
    export CHAPTER APP_NAME SERVICES MODULE_TYPE SUB_MODULES
    export MARIADB_PORT VAULT_PORT MONGODB_PORT REDIS_PORT
    export ELASTICSEARCH_PORT ELASTICSEARCH_TRANSPORT_PORT KIBANA_PORT
    export KAFKA_PORT ZOOKEEPER_PORT
    export SQL_INIT_PATH VAULT_INIT_PATH MONGODB_INIT_PATH
    export VAULT_CONFIGURATIONS VAULT_DEPENDS_ON
}

generate_docker_compose() {
    local chapter=$1
    local output_file="$GENERATED_DIR/${chapter}-docker-compose.yml"
    
    load_config "$chapter"
    
    echo "🔨 Generating Docker Compose for $chapter..." >&2
    
    cat > "$output_file" << EOF
# Generated Docker Compose for $chapter ($APP_NAME)
# Services: $SERVICES
# Module Type: $MODULE_TYPE

services:
EOF

    # Parse and generate services
    IFS=',' read -ra SERVICE_LIST <<< "$SERVICES"
    local has_dependency=false
    
    for service in "${SERVICE_LIST[@]}"; do
        service=$(echo "$service" | xargs)
        case $service in
            basic)
                # No services needed for basic chapters
                ;;
            mariadb)
                envsubst < "$TEMPLATES_DIR/mariadb-template.yml" >> "$output_file"
                echo "" >> "$output_file"
                has_dependency=true
                ;;
            mariadb-cdc)
                envsubst < "$TEMPLATES_DIR/mariadb-cdc-template.yml" >> "$output_file"
                echo "" >> "$output_file"
                has_dependency=true
                ;;
            vault)
                # Vault 템플릿 처리 시 VAULT_CONFIGURATIONS과 VAULT_DEPENDS_ON 사용
                envsubst < "$TEMPLATES_DIR/vault-template.yml" >> "$output_file"
                echo "" >> "$output_file"
                ;;
            mongodb)
                envsubst < "$TEMPLATES_DIR/mongodb-template.yml" >> "$output_file"
                echo "" >> "$output_file"
                ;;
            redis)
                envsubst < "$TEMPLATES_DIR/redis-template.yml" >> "$output_file"
                echo "" >> "$output_file"
                ;;
            elasticsearch)
                envsubst < "$TEMPLATES_DIR/elasticsearch-template.yml" >> "$output_file"
                echo "" >> "$output_file"
                ;;
            kafka)
                envsubst < "$TEMPLATES_DIR/kafka-template.yml" >> "$output_file"
                echo "" >> "$output_file"
                ;;
            kibana)
                envsubst < "$TEMPLATES_DIR/kibana-template.yml" >> "$output_file"
                echo "" >> "$output_file"
                ;;
        esac
    done
    
    # Generate volumes section
    cat >> "$output_file" << EOF

volumes:
EOF
    
    for service in "${SERVICE_LIST[@]}"; do
        service=$(echo "$service" | xargs)
        case $service in
            basic)
                ;;
            mariadb|mariadb-cdc)
                echo "  mariadb-${CHAPTER}-data:" >> "$output_file"
                echo "    name: mariadb-${CHAPTER}-data" >> "$output_file"
                ;;
            vault)
                echo "  vault-${CHAPTER}-data:" >> "$output_file"
                echo "    name: vault-${CHAPTER}-data" >> "$output_file"
                ;;
            mongodb)
                echo "  mongodb-${CHAPTER}-data:" >> "$output_file"
                echo "    name: mongodb-${CHAPTER}-data" >> "$output_file"
                ;;
            redis)
                echo "  redis-${CHAPTER}-data:" >> "$output_file"
                echo "    name: redis-${CHAPTER}-data" >> "$output_file"
                ;;
            elasticsearch)
                echo "  elasticsearch-${CHAPTER}-data:" >> "$output_file"
                echo "    name: elasticsearch-${CHAPTER}-data" >> "$output_file"
                ;;
            kafka)
                echo "  kafka-${CHAPTER}-data:" >> "$output_file"
                echo "    name: kafka-${CHAPTER}-data" >> "$output_file"
                echo "  zookeeper-${CHAPTER}-data:" >> "$output_file"
                echo "    name: zookeeper-${CHAPTER}-data" >> "$output_file"
                echo "  zookeeper-${CHAPTER}-log:" >> "$output_file"
                echo "    name: zookeeper-${CHAPTER}-log" >> "$output_file"
                ;;
        esac
    done
    
    # Generate networks section
    cat >> "$output_file" << EOF

networks:
  ${CHAPTER}-network:
    name: primavera-${CHAPTER}-network
    driver: bridge
EOF

    echo "$output_file"
}

start_chapter() {
    local chapter=$1
    validate_chapter "$chapter"
    
    echo "🚀 Starting Docker services for $chapter..."
    
    local compose_file=$(generate_docker_compose "$chapter")
    echo "📄 Generated compose file: $compose_file"
    
    cd "$INFRA_DIR"
    docker-compose -f "$compose_file" up -d
    
    # Wait for services to be healthy
    echo "⏳ Waiting for services to be ready..."
    sleep 5
    
    # Show post-start information
    show_post_start_info "$chapter"
    
    echo "✅ $chapter services started successfully!"
}

stop_chapter() {
    local chapter=$1
    validate_chapter "$chapter"
    
    echo "🛑 Stopping Docker services for $chapter..."
    
    local compose_file="$GENERATED_DIR/${chapter}-docker-compose.yml"
    
    if [[ -f "$compose_file" ]]; then
        cd "$INFRA_DIR"
        docker-compose -f "$compose_file" down
        echo "✅ $chapter services stopped successfully!"
    else
        echo "⚠️  No compose file found for $chapter, trying to stop by container names..."
        stop_by_container_names "$chapter"
    fi
}

stop_by_container_names() {
    local chapter=$1
    load_config "$chapter"
    
    IFS=',' read -ra SERVICE_LIST <<< "$SERVICES"
    local containers=()
    
    for service in "${SERVICE_LIST[@]}"; do
        service=$(echo "$service" | xargs)
        case $service in
            mariadb|mariadb-cdc) containers+=(\"mariadb-primavera-$chapter\") ;;
            vault) containers+=(\"vault-primavera-$chapter\") ;;
            mongodb) containers+=(\"mongodb-primavera-$chapter\") ;;
            redis) containers+=(\"redis-primavera-$chapter\") ;;
            elasticsearch) containers+=(\"elasticsearch-primavera-$chapter\") ;;
            kafka) containers+=(\"kafka-primavera-$chapter\" \"zookeeper-primavera-$chapter\") ;;
            kibana) containers+=(\"kibana-primavera-$chapter\") ;;
        esac
    done
    
    if [[ ${#containers[@]} -gt 0 ]]; then
        docker stop "${containers[@]}" 2>/dev/null || true
        echo "✅ $chapter services stopped!"
    fi
}

status_chapter() {
    local chapter=$1
    validate_chapter "$chapter"
    
    load_config "$chapter"
    
    echo "📊 Status for $chapter ($APP_NAME):"
    echo "$(printf '=%.0s' {1..50})"
    
    # Multi-module 특별 처리
    if [[ "$MODULE_TYPE" == "multi" ]]; then
        show_multi_module_status "$chapter"
        return 0
    fi
    
    # Standard single-module status check
    show_single_module_status "$chapter"
}

show_single_module_status() {
    local chapter=$1
    local all_running=true
    
    IFS=',' read -ra SERVICE_LIST <<< "$SERVICES"
    
    for service in "${SERVICE_LIST[@]}"; do
        service=$(echo "$service" | xargs)
        
        if [[ "$service" == "basic" ]]; then
            echo "📋 Basic chapter - no external services required"
            continue
        fi
        
        local container_name=""
        local service_display=""
        
        case $service in
            mariadb|mariadb-cdc) 
                container_name="mariadb-primavera-$chapter"
                service_display="MariaDB Database"
                [[ "$service" == "mariadb-cdc" ]] && service_display="$service_display (CDC enabled)"
                ;;
            vault) 
                container_name="vault-primavera-$chapter"
                service_display="HashiCorp Vault"
                ;;
            mongodb) 
                container_name="mongodb-primavera-$chapter"
                service_display="MongoDB"
                ;;
            redis) 
                container_name="redis-primavera-$chapter"
                service_display="Redis Cache"
                ;;
            elasticsearch) 
                container_name="elasticsearch-primavera-$chapter"
                service_display="Elasticsearch"
                ;;
            kafka) 
                container_name="kafka-primavera-$chapter"
                service_display="Apache Kafka"
                ;;
            kibana) 
                container_name="kibana-primavera-$chapter"
                service_display="Kibana Dashboard"
                ;;
        esac
        
        if [[ -n "$container_name" ]]; then
            if docker ps --format "table {{.Names}}\t{{.Status}}" | grep -q "$container_name"; then
                local status=$(docker ps --format "{{.Status}}" --filter "name=$container_name")
                echo "✅ $service_display ($container_name): $status"
            else
                echo "❌ $service_display ($container_name): Not running"
                all_running=false
            fi
        fi
    done
    
    echo ""
    if $all_running; then
        echo "🎉 All services are running healthy!"
    else
        echo "⚠️  Some services are not running. Use '$0 start $chapter' to start them."
    fi
    
    # Show connection information
    show_connection_info "$chapter"
}

show_multi_module_status() {
    local chapter=$1
    
    echo "🏗️  Multi-Module Architecture Status:"
    echo ""
    echo "📋 Infrastructure Services:"
    
    local containers=()
    local services_display=()
    local all_infra_running=true
    
    # Infrastructure services 매핑
    IFS=',' read -ra SERVICE_LIST <<< "$SERVICES"
    for service in "${SERVICE_LIST[@]}"; do
        service=$(echo "$service" | xargs)
        case $service in
            mariadb|mariadb-cdc)
                containers+=("mariadb-primavera-$chapter")
                services_display+=("MariaDB Database" $(if [[ "$service" == "mariadb-cdc" ]]; then echo "(CDC enabled)"; fi))
                ;;
            vault)
                containers+=("vault-primavera-$chapter")
                services_display+=("HashiCorp Vault")
                ;;
            mongodb)
                containers+=("mongodb-primavera-$chapter")
                services_display+=("MongoDB")
                ;;
            redis)
                containers+=("redis-primavera-$chapter")
                services_display+=("Redis Cache")
                ;;
            elasticsearch)
                containers+=("elasticsearch-primavera-$chapter")
                services_display+=("Elasticsearch")
                ;;
            kafka)
                containers+=("kafka-primavera-$chapter")
                services_display+=("Apache Kafka")
                containers+=("zookeeper-primavera-$chapter")
                services_display+=("Apache Zookeeper")
                ;;
            kibana)
                containers+=("kibana-primavera-$chapter")
                services_display+=("Kibana Dashboard")
                ;;
        esac
    done
    
    # Check infrastructure services status
    for i in "${!containers[@]}"; do
        local container_name="${containers[$i]}"
        local service_name="${services_display[$i]}"
        
        if docker ps --format "table {{.Names}}\t{{.Status}}" | grep -q "$container_name"; then
            local status=$(docker ps --format "{{.Status}}" --filter "name=$container_name")
            echo "  ✅ $service_name ($container_name): $status"
        else
            echo "  ❌ $service_name ($container_name): Not running"
            all_infra_running=false
        fi
    done
    
    echo ""
    echo "📦 Sub-modules:"
    
    # Show sub-modules information
    if [[ -n "$SUB_MODULES" ]]; then
        IFS=',' read -ra SUB_MODULE_LIST <<< "$SUB_MODULES"
        for sub_module in "${SUB_MODULE_LIST[@]}"; do
            IFS='=' read -r module_name module_app <<< "$sub_module"
            
            case $module_name in
                batch)
                    echo "  ├── 🔄 batch: $module_app"
                    echo "  │   ├── Purpose: Spring Batch + Elasticsearch processing"
                    echo "  │   ├── Port: 8080 (default)"
                    echo "  │   └── Dependencies: MariaDB + Elasticsearch"
                    ;;
                streaming)
                    echo "  └── 📡 streaming: $module_app"
                    echo "      ├── Purpose: WebFlux reactive streaming"
                    echo "      ├── Port: 8081"
                    echo "      └── Dependencies: WebFlux (no database)"
                    ;;
                account|configuration|front|order|product)
                    local service_port=$((8080 + ${#module_name}))
                    echo "  ├── 🌐 $module_name: $module_app"
                    echo "  │   ├── Port: $service_port"
                    echo "  │   └── Purpose: Microservice component"
                    ;;
            esac
        done
    fi
    
    echo ""
    
    if $all_infra_running; then
        echo "🎉 All $chapter infrastructure services are running!"
        echo "🚀 You can now start the sub-module applications:"
        
        if [[ -n "$SUB_MODULES" ]]; then
            IFS=',' read -ra SUB_MODULE_LIST <<< "$SUB_MODULES"
            for sub_module in "${SUB_MODULE_LIST[@]}"; do
                IFS='=' read -r module_name module_app <<< "$sub_module"
                echo "   • ./gradlew :$chapter:$module_name:bootRun"
            done
        fi
    else
        echo "⚠️  Some infrastructure services are not running."
        echo "💡 Use '$0 start $chapter' to start infrastructure services."
    fi
    
    # Show connection information
    show_connection_info "$chapter"
}

show_connection_info() {
    local chapter=$1
    
    echo ""
    echo "🔗 Connection Information:"
    
    IFS=',' read -ra SERVICE_LIST <<< "$SERVICES"
    for service in "${SERVICE_LIST[@]}"; do
        service=$(echo "$service" | xargs)
        case $service in
            mariadb|mariadb-cdc)
                echo "   📊 MariaDB: localhost:${MARIADB_PORT:-3306} (primavera/primavera)"
                ;;
            vault)
                echo "   🔐 Vault: http://localhost:${VAULT_PORT:-8200} (token: primavera-vault-token)"
                ;;
            mongodb)
                echo "   🍃 MongoDB: localhost:${MONGODB_PORT:-27017} (primavera/primavera)"
                ;;
            redis)
                echo "   🔴 Redis: localhost:${REDIS_PORT:-6379} (password: primavera)"
                ;;
            elasticsearch)
                echo "   🔍 Elasticsearch: http://localhost:${ELASTICSEARCH_PORT:-9200}"
                ;;
            kafka)
                echo "   📨 Kafka: localhost:${KAFKA_PORT:-9092}"
                echo "   🔄 Zookeeper: localhost:${ZOOKEEPER_PORT:-2181}"
                ;;
            kibana)
                echo "   📈 Kibana: http://localhost:${KIBANA_PORT:-5601}"
                ;;
        esac
    done
}

show_post_start_info() {
    local chapter=$1
    load_config "$chapter"
    
    echo ""
    echo "📋 Post-Start Information for $chapter:"
    
    # Chapter-specific information
    if [[ "$MODULE_TYPE" == "multi" ]]; then
        echo "   🏗️  Multi-module infrastructure ready"
        if [[ -n "$SUB_MODULES" ]]; then
            IFS=',' read -ra SUB_MODULE_LIST <<< "$SUB_MODULES"
            for sub_module in "${SUB_MODULE_LIST[@]}"; do
                IFS='=' read -r module_name module_app <<< "$sub_module"
                echo "   💡 Start $module_name: ./gradlew :$chapter:$module_name:bootRun"
            done
        fi
    else
        case $chapter in
            chap01|chap02|chap03|preface)
                echo "   📚 Basic learning module (no web interface)"
                echo "   💡 Run: ./gradlew :${chapter}:bootRun"
                ;;
            chap04|chap05|chap06|chap07|chap08|chap09|chap11|chap12)
                echo "   🌐 Application: http://localhost:8080"
                echo "   💡 Run: ./gradlew :${chapter}:bootRun"
                ;;
            chap10)
                echo "   🌐 Application: http://localhost:8080"
                echo "   🔴 Redis Cache enabled for session management"
                echo "   💡 Run: ./gradlew :${chapter}:bootRun"
                ;;
            chap13|chap14)
                echo "   🌐 Reactive Application: http://localhost:8080"
                echo "   🍃 MongoDB reactive support enabled"
                echo "   💡 Run: ./gradlew :${chapter}:bootRun"
                ;;
            chap15)
                echo "   🏗️  JPA-only module (no web interface)"
                echo "   🔐 Vault configuration only"
                echo "   💡 Run: ./gradlew :${chapter}:bootRun"
                ;;
            chap16)
                echo "   🌐 File Processing & Monitoring: http://localhost:8080"
                echo "   📁 File upload functionality enabled"
                echo "   💡 Run: ./gradlew :${chapter}:bootRun"
                ;;
        esac
    fi
}

show_chapter_info() {
    local chapter=$1
    validate_chapter "$chapter"
    
    load_config "$chapter"
    
    echo "ℹ️  Detailed Information for $chapter"
    echo "$(printf '=%.0s' {1..50})"
    echo "📋 Application: $APP_NAME"
    echo "🔧 Services: $SERVICES"
    echo "🏗️  Module Type: $MODULE_TYPE"
    
    if [[ "$MODULE_TYPE" == "multi" && -n "$SUB_MODULES" ]]; then
        echo "📦 Sub-modules: $SUB_MODULES"
        echo ""
        echo "🏗️  Sub-module Details:"
        
        IFS=',' read -ra SUB_MODULE_LIST <<< "$SUB_MODULES"
        for sub_module in "${SUB_MODULE_LIST[@]}"; do
            IFS='=' read -r module_name module_app <<< "$sub_module"
            echo "  ├── $module_name"
            echo "  │   ├── Application: $module_app"
            echo "  │   ├── Path: $chapter:$module_name"
            case $module_name in
                batch)
                    echo "  │   ├── Type: Spring Batch Application"
                    echo "  │   └── Features: CDC, Elasticsearch indexing"
                    ;;
                streaming)
                    echo "  │   ├── Type: WebFlux Reactive Application"
                    echo "  │   └── Features: Real-time streaming, WebSocket"
                    ;;
                *)
                    echo "  │   └── Type: Microservice Application"
                    ;;
            esac
        done
    fi
    
    echo ""
    echo "🔌 Port Configuration:"
    [[ -n "$MARIADB_PORT" ]] && echo "  MariaDB: $MARIADB_PORT"
    [[ -n "$VAULT_PORT" ]] && echo "  Vault: $VAULT_PORT"
    [[ -n "$MONGODB_PORT" ]] && echo "  MongoDB: $MONGODB_PORT"
    [[ -n "$REDIS_PORT" ]] && echo "  Redis: $REDIS_PORT"
    [[ -n "$ELASTICSEARCH_PORT" ]] && echo "  Elasticsearch: $ELASTICSEARCH_PORT"
    [[ -n "$KAFKA_PORT" ]] && echo "  Kafka: $KAFKA_PORT"
    [[ -n "$KIBANA_PORT" ]] && echo "  Kibana: $KIBANA_PORT"
}

show_logs() {
    local chapter=$1
    local service=${2:-"all"}
    validate_chapter "$chapter"
    
    local compose_file="$GENERATED_DIR/${chapter}-docker-compose.yml"
    
    if [[ ! -f "$compose_file" ]]; then
        compose_file=$(generate_docker_compose "$chapter")
    fi
    
    echo "📋 Showing logs for $chapter services..."
    
    cd "$INFRA_DIR"
    if [[ "$service" == "all" ]]; then
        docker-compose -f "$compose_file" logs --tail=50 -f
    else
        docker-compose -f "$compose_file" logs --tail=50 -f "$service-$chapter"
    fi
}

clean_chapter() {
    local chapter=$1
    validate_chapter "$chapter"
    
    echo "🧹 Cleaning up Docker resources for $chapter..."
    
    stop_chapter "$chapter"
    
    load_config "$chapter"
    
    # Remove containers and volumes
    IFS=',' read -ra SERVICE_LIST <<< "$SERVICES"
    local containers=()
    local volumes=()
    
    for service in "${SERVICE_LIST[@]}"; do
        service=$(echo "$service" | xargs)
        case $service in
            mariadb|mariadb-cdc)
                containers+=(\"mariadb-primavera-$chapter\")
                volumes+=(\"mariadb-${chapter}-data\")
                ;;
            vault)
                containers+=(\"vault-primavera-$chapter\")
                volumes+=(\"vault-${chapter}-data\")
                ;;
            mongodb)
                containers+=(\"mongodb-primavera-$chapter\")
                volumes+=(\"mongodb-${chapter}-data\")
                ;;
            redis)
                containers+=(\"redis-primavera-$chapter\")
                volumes+=(\"redis-${chapter}-data\")
                ;;
            elasticsearch)
                containers+=(\"elasticsearch-primavera-$chapter\")
                volumes+=(\"elasticsearch-${chapter}-data\")
                ;;
            kafka)
                containers+=(\"kafka-primavera-$chapter\" \"zookeeper-primavera-$chapter\")
                volumes+=(\"kafka-${chapter}-data\" \"zookeeper-${chapter}-data\" \"zookeeper-${chapter}-log\")
                ;;
            kibana)
                containers+=(\"kibana-primavera-$chapter\")
                ;;
        esac
    done
    
    # Remove containers
    if [[ ${#containers[@]} -gt 0 ]]; then
        docker rm "${containers[@]}" 2>/dev/null || true
    fi
    
    # Remove volumes
    if [[ ${#volumes[@]} -gt 0 ]]; then
        docker volume rm "${volumes[@]}" 2>/dev/null || true
    fi
    
    # Remove network
    docker network rm "primavera-${chapter}-network" 2>/dev/null || true
    
    # Remove generated compose file
    local compose_file="$GENERATED_DIR/${chapter}-docker-compose.yml"
    if [[ -f "$compose_file" ]]; then
        rm "$compose_file"
        echo "🗑️  Removed generated compose file"
    fi
    
    echo "✅ $chapter cleanup completed!"
}

generate_configs() {
    echo "🔧 Generating configuration files..."
    cd "$SCRIPT_DIR"
    ./generate-configs.sh
    echo "✅ Configuration files generated!"
}

# Main command dispatcher
main() {
    case "${1:-}" in
        start)
            if [[ -z "${2:-}" ]]; then
                echo "❌ Error: Chapter required for start command"
                show_usage
                exit 1
            fi
            start_chapter "$2"
            ;;
        stop)
            if [[ -z "${2:-}" ]]; then
                echo "❌ Error: Chapter required for stop command"
                show_usage
                exit 1
            fi
            stop_chapter "$2"
            ;;
        restart)
            if [[ -z "${2:-}" ]]; then
                echo "❌ Error: Chapter required for restart command"
                show_usage
                exit 1
            fi
            stop_chapter "$2"
            sleep 3
            start_chapter "$2"
            ;;
        status)
            if [[ -z "${2:-}" ]]; then
                echo "❌ Error: Chapter required for status command"
                show_usage
                exit 1
            fi
            status_chapter "$2"
            ;;
        info)
            if [[ -z "${2:-}" ]]; then
                echo "❌ Error: Chapter required for info command"
                show_usage
                exit 1
            fi
            show_chapter_info "$2"
            ;;
        logs)
            if [[ -z "${2:-}" ]]; then
                echo "❌ Error: Chapter required for logs command"
                show_usage
                exit 1
            fi
            show_logs "$2" "${3:-all}"
            ;;
        start-all)
            echo "🚀 Starting all available chapters..."
            for config in "$CONFIGS_DIR"/*-config.env; do
                if [[ -f "$config" ]]; then
                    chapter=$(basename "$config" | sed 's/-config\.env$//')
                    echo ""
                    start_chapter "$chapter"
                fi
            done
            ;;
        stop-all)
            echo "🛑 Stopping all chapters..."
            for config in "$CONFIGS_DIR"/*-config.env; do
                if [[ -f "$config" ]]; then
                    chapter=$(basename "$config" | sed 's/-config\.env$//')
                    echo ""
                    stop_chapter "$chapter"
                fi
            done
            ;;
        status-all)
            echo "📊 Checking status of all chapters..."
            for config in "$CONFIGS_DIR"/*-config.env; do
                if [[ -f "$config" ]]; then
                    chapter=$(basename "$config" | sed 's/-config\.env$//')
                    echo ""
                    status_chapter "$chapter"
                fi
            done
            ;;
        clean)
            if [[ -z "${2:-}" ]]; then
                echo "❌ Error: Chapter required for clean command"
                show_usage
                exit 1
            fi
            echo "⚠️  This will remove all containers, volumes, and networks for $2"
            read -p "Are you sure? (y/N): " -n 1 -r
            echo
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                clean_chapter "$2"
            else
                echo "❌ Operation cancelled"
            fi
            ;;
        clean-all)
            echo "⚠️  This will remove ALL containers, volumes, and networks for ALL chapters"
            read -p "Are you sure? (y/N): " -n 1 -r
            echo
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                for config in "$CONFIGS_DIR"/*-config.env; do
                    if [[ -f "$config" ]]; then
                        chapter=$(basename "$config" | sed 's/-config\.env$//')
                        clean_chapter "$chapter"
                    fi
                done
            else
                echo "❌ Operation cancelled"
            fi
            ;;
        generate-configs)
            generate_configs
            ;;
        list)
            list_chapters
            ;;
        help|--help|-h)
            show_usage
            ;;
        "")
            echo "❌ Error: No command specified"
            show_usage
            exit 1
            ;;
        *)
            echo "❌ Error: Unknown command '$1'"
            show_usage
            exit 1
            ;;
    esac
}

if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi