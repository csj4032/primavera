#\!/bin/bash

# Generate config files for each chapter with standard ports
chapters=(
    "chap04:DataAccessApplication"
    "chap05:MyBatisLoggingApplication"
    "chap06:VaadinApplication"
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
    "chap18:ComplexMicroserviceApplication"
)

# Standard ports for all chapters
STANDARD_MARIADB_PORT=3306
STANDARD_VAULT_PORT=8200
STANDARD_ELASTICSEARCH_PORT=9200
STANDARD_ELASTICSEARCH_TRANSPORT_PORT=9300
STANDARD_MONGODB_PORT=27017
STANDARD_REDIS_PORT=6379
STANDARD_KAFKA_PORT=9092
STANDARD_ZOOKEEPER_PORT=2181

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

    # Special configuration for chap13 (needs MongoDB)
    if [ "$chap" == "chap13" ]; then
        services="mariadb,mongodb,vault"
        config_content="CHAPTER=${chap}
APP_NAME=${app_name}
SERVICES=${services}
MARIADB_PORT=${STANDARD_MARIADB_PORT}
MONGODB_PORT=${STANDARD_MONGODB_PORT}
VAULT_PORT=${STANDARD_VAULT_PORT}
SQL_INIT_PATH=../sql/${chap}-init.sql
MONGODB_INIT_PATH=../mongodb/${chap}-mongo-init.js
VAULT_INIT_PATH=../vault/vault-init.sh"
    fi

    # Special configuration for chap14 (needs MongoDB)
    if [ "$chap" == "chap14" ]; then
        services="mariadb,mongodb,vault"
        config_content="CHAPTER=${chap}
APP_NAME=${app_name}
SERVICES=${services}
MARIADB_PORT=${STANDARD_MARIADB_PORT}
MONGODB_PORT=${STANDARD_MONGODB_PORT}
VAULT_PORT=${STANDARD_VAULT_PORT}
SQL_INIT_PATH=../sql/${chap}-init.sql
MONGODB_INIT_PATH=../mongodb/${chap}-mongo-init.js
VAULT_INIT_PATH=../vault/vault-init.sh"
    fi

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

    # Special configuration for chap18 (complex microservices - needs all services)
    if [ "$chap" == "chap18" ]; then
        services="mariadb,mongodb,redis,kafka,vault"
        config_content="CHAPTER=${chap}
APP_NAME=${app_name}
SERVICES=${services}
MARIADB_PORT=${STANDARD_MARIADB_PORT}
MONGODB_PORT=${STANDARD_MONGODB_PORT}
REDIS_PORT=${STANDARD_REDIS_PORT}
KAFKA_PORT=${STANDARD_KAFKA_PORT}
ZOOKEEPER_PORT=${STANDARD_ZOOKEEPER_PORT}
VAULT_PORT=${STANDARD_VAULT_PORT}
SQL_INIT_PATH=../sql/${chap}-init.sql
MONGODB_INIT_PATH=../mongodb/${chap}-mongo-init.js
VAULT_INIT_PATH=../vault/vault-init.sh"
    fi

    echo "$config_content" > "configs/${chap}-config.env"
done

echo "All config files generated successfully"
