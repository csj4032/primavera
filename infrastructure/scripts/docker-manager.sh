#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
INFRA_DIR="$SCRIPT_DIR/.."
TEMPLATES_DIR="$SCRIPT_DIR/templates"
CONFIGS_DIR="$SCRIPT_DIR/configs"

show_usage() {
    echo "Primavera Docker Infrastructure Manager"
    echo ""
    echo "Usage: $0 <command> [chapter]"
    echo ""
    echo "Commands:"
    echo "  start <chapter>     Start Docker services for specific chapter (e.g., chap04)"
    echo "  stop <chapter>      Stop Docker services for specific chapter"
    echo "  restart <chapter>   Restart Docker services for specific chapter"
    echo "  status <chapter>    Show status of Docker services for specific chapter"
    echo "  start-all          Start all Docker services"
    echo "  stop-all           Stop all Docker services"
    echo "  status-all         Show status of all Docker services"
    echo "  list               List all available chapters"
    echo "  clean <chapter>     Remove Docker containers and volumes for specific chapter"
    echo "  clean-all          Remove all Docker containers and volumes"
    echo ""
    echo "Examples:"
    echo "  $0 start chap04"
    echo "  $0 stop chap13"
    echo "  $0 restart chap05"
    echo "  $0 status-all"
}

validate_chapter() {
    local chapter=$1
    if [[ ! -f "$CONFIGS_DIR/${chapter}-config.env" ]]; then
        echo "Error: Configuration not found for chapter '$chapter'"
        echo "Available chapters:"
        list_chapters
        exit 1
    fi
}

list_chapters() {
    echo "Available chapters:"
    for config in "$CONFIGS_DIR"/*-config.env; do
        if [[ -f "$config" ]]; then
            basename "$config" | sed 's/-config\.env$//'
        fi
    done
}

load_config() {
    local chapter=$1
    local config_file="$CONFIGS_DIR/${chapter}-config.env"
    
    if [[ ! -f "$config_file" ]]; then
        echo "Error: Config file not found: $config_file"
        exit 1
    fi
    
    source "$config_file"
    
    export CHAPTER APP_NAME SERVICES MARIADB_PORT VAULT_PORT MONGODB_PORT
    export SQL_INIT_PATH VAULT_INIT_PATH MONGO_INIT_PATH
}

generate_docker_compose() {
    local chapter=$1
    local output_file="$INFRA_DIR/generated/${chapter}-docker-compose.yml"
    
    mkdir -p "$INFRA_DIR/generated"
    
    load_config "$chapter"
    
    cat > "$output_file" << EOF
version: '3.8'

services:
EOF

    IFS=',' read -ra SERVICE_LIST <<< "$SERVICES"
    for service in "${SERVICE_LIST[@]}"; do
        service=$(echo "$service" | xargs)
        case $service in
            mariadb)
                envsubst < "$TEMPLATES_DIR/mariadb-template.yml" >> "$output_file"
                echo "" >> "$output_file"
                ;;
            vault)
                envsubst < "$TEMPLATES_DIR/vault-template.yml" >> "$output_file"
                echo "" >> "$output_file"
                ;;
            mongodb)
                envsubst < "$TEMPLATES_DIR/mongodb-template.yml" >> "$output_file"
                echo "" >> "$output_file"
                ;;
        esac
    done
    
    cat >> "$output_file" << EOF

volumes:
EOF
    
    for service in "${SERVICE_LIST[@]}"; do
        service=$(echo "$service" | xargs)
        case $service in
            mariadb)
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
        esac
    done
    
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
    
    echo "Starting Docker services for $chapter..."
    
    local compose_file=$(generate_docker_compose "$chapter")
    
    cd "$INFRA_DIR"
    docker-compose -f "$compose_file" up -d
    
    echo "✅ $chapter services started successfully!"
    echo "Generated compose file: $compose_file"
}

stop_chapter() {
    local chapter=$1
    validate_chapter "$chapter"
    
    echo "Stopping Docker services for $chapter..."
    
    local compose_file="$INFRA_DIR/generated/${chapter}-docker-compose.yml"
    
    if [[ -f "$compose_file" ]]; then
        cd "$INFRA_DIR"
        docker-compose -f "$compose_file" down
        echo "✅ $chapter services stopped successfully!"
    else
        echo "⚠️  No compose file found for $chapter, trying to stop by container names..."
        docker stop mariadb-primavera-$chapter vault-primavera-$chapter mongodb-primavera-$chapter 2>/dev/null || true
        echo "✅ $chapter services stopped!"
    fi
}

status_chapter() {
    local chapter=$1
    validate_chapter "$chapter"
    
    echo "Status for $chapter:"
    echo "===================="
    
    load_config "$chapter"
    
    IFS=',' read -ra SERVICE_LIST <<< "$SERVICES"
    for service in "${SERVICE_LIST[@]}"; do
        service=$(echo "$service" | xargs)
        case $service in
            mariadb)
                container_name="mariadb-primavera-$chapter"
                ;;
            vault)
                container_name="vault-primavera-$chapter"
                ;;
            mongodb)
                container_name="mongodb-primavera-$chapter"
                ;;
        esac
        
        if docker ps --format "table {{.Names}}\t{{.Status}}" | grep -q "$container_name"; then
            echo "✅ $service ($container_name): $(docker ps --format "{{.Status}}" --filter "name=$container_name")"
        else
            echo "❌ $service ($container_name): Not running"
        fi
    done
    echo ""
}

clean_chapter() {
    local chapter=$1
    validate_chapter "$chapter"
    
    echo "Cleaning up Docker resources for $chapter..."
    
    stop_chapter "$chapter"
    
    docker rm mariadb-primavera-$chapter vault-primavera-$chapter mongodb-primavera-$chapter 2>/dev/null || true
    docker volume rm mariadb-${chapter}-data vault-${chapter}-data mongodb-${chapter}-data 2>/dev/null || true
    docker network rm primavera-${chapter}-network 2>/dev/null || true
    
    local compose_file="$INFRA_DIR/generated/${chapter}-docker-compose.yml"
    if [[ -f "$compose_file" ]]; then
        rm "$compose_file"
        echo "Removed generated compose file"
    fi
    
    echo "✅ $chapter cleanup completed!"
}

main() {
    case "${1:-}" in
        start)
            if [[ -z "${2:-}" ]]; then
                echo "Error: Chapter required for start command"
                show_usage
                exit 1
            fi
            start_chapter "$2"
            ;;
        stop)
            if [[ -z "${2:-}" ]]; then
                echo "Error: Chapter required for stop command"
                show_usage
                exit 1
            fi
            stop_chapter "$2"
            ;;
        restart)
            if [[ -z "${2:-}" ]]; then
                echo "Error: Chapter required for restart command"
                show_usage
                exit 1
            fi
            stop_chapter "$2"
            sleep 2
            start_chapter "$2"
            ;;
        status)
            if [[ -z "${2:-}" ]]; then
                echo "Error: Chapter required for status command"
                show_usage
                exit 1
            fi
            status_chapter "$2"
            ;;
        start-all)
            for config in "$CONFIGS_DIR"/*-config.env; do
                if [[ -f "$config" ]]; then
                    chapter=$(basename "$config" | sed 's/-config\.env$//')
                    start_chapter "$chapter"
                fi
            done
            ;;
        stop-all)
            for config in "$CONFIGS_DIR"/*-config.env; do
                if [[ -f "$config" ]]; then
                    chapter=$(basename "$config" | sed 's/-config\.env$//')
                    stop_chapter "$chapter"
                fi
            done
            ;;
        status-all)
            for config in "$CONFIGS_DIR"/*-config.env; do
                if [[ -f "$config" ]]; then
                    chapter=$(basename "$config" | sed 's/-config\.env$//')
                    status_chapter "$chapter"
                fi
            done
            ;;
        clean)
            if [[ -z "${2:-}" ]]; then
                echo "Error: Chapter required for clean command"
                show_usage
                exit 1
            fi
            echo "⚠️  This will remove all containers, volumes, and networks for $2"
            read -p "Are you sure? (y/N): " -n 1 -r
            echo
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                clean_chapter "$2"
            else
                echo "Operation cancelled"
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
                echo "Operation cancelled"
            fi
            ;;
        list)
            list_chapters
            ;;
        help|--help|-h)
            show_usage
            ;;
        "")
            echo "Error: No command specified"
            show_usage
            exit 1
            ;;
        *)
            echo "Error: Unknown command '$1'"
            show_usage
            exit 1
            ;;
    esac
}

if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi