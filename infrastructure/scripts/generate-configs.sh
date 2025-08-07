#\!/bin/bash

# Generate config files for each chapter with standard ports
chapters=(
    "chap04:PrimaveraApplication"
    "chap05:PrimaveraApplication"
    "chap06:ValidationApplication"
    "chap07:ThymeleafWebApplication"
    "chap08:SecurityFilterApplication"
    "chap09:SpringSecurityBasicApplication"
    "chap10:OAuth2SocialLoginApplication"
    "chap11:BoardSystemApplication"
    "chap12:HierarchicalCommentApplication"
    "chap13:AdvancedAuthorizationApplication"
    "chap14:AdvancedJpaApplication"
    "chap15:JpaAdvancedMappingApplication"
    "chap16:FileProcessingMonitoringApplication"
    "chap17:MicroserviceApplication"
)

# Standard ports for all chapters
STANDARD_MARIADB_PORT=3306
STANDARD_VAULT_PORT=8200
STANDARD_ELASTICSEARCH_PORT=9200
STANDARD_ELASTICSEARCH_TRANSPORT_PORT=9300

for chapter_info in "${chapters[@]}"; do
    IFS=':' read -r chap app_name <<< "$chapter_info"
    
    # Default services for most chapters
    services="mariadb,vault"
    config_content="CHAPTER=${chap}
APP_NAME=${app_name}
SERVICES=${services}
MARIADB_PORT=${STANDARD_MARIADB_PORT}
VAULT_PORT=${STANDARD_VAULT_PORT}
SQL_INIT_PATH=../sql/${chap}-init.sql
VAULT_INIT_PATH=../vault/vault-init.sh"

    # Special configuration for chap17 (needs Elasticsearch)
    if [ "$chap" == "chap17" ]; then
        services="mariadb-cdc,elasticsearch,vault"
        config_content="CHAPTER=${chap}
APP_NAME=${app_name}
SERVICES=${services}
MARIADB_PORT=${STANDARD_MARIADB_PORT}
ELASTICSEARCH_PORT=${STANDARD_ELASTICSEARCH_PORT}
ELASTICSEARCH_TRANSPORT_PORT=${STANDARD_ELASTICSEARCH_TRANSPORT_PORT}
VAULT_PORT=${STANDARD_VAULT_PORT}
SQL_INIT_PATH=../sql/${chap}-init.sql
VAULT_INIT_PATH=../vault/vault-init.sh"
    fi
    
    echo "$config_content" > "/Users/genius/Workspace/primavera/infrastructure/scripts/configs/${chap}-config.env"
    echo "Generated config for ${chap} with standard ports"
done

echo "All config files generated successfully\!"
