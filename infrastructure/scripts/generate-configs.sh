#!/usr/bin/env bash

# Primavera Multi-Module Docker Configuration Generator
# 완전한 멀티모듈 아키텍처 지원 (chap17, chap18, chap19 포함)

set -e

# Check if we have bash 4+ for associative arrays
if [[ ${BASH_VERSION%%.*} -lt 4 ]]; then
    echo "❌ Error: This script requires bash 4.0 or later"
    echo "Your bash version: $BASH_VERSION"
    echo "Please install a newer version of bash or use: brew install bash"
    exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
CONFIGS_DIR="$SCRIPT_DIR/configs"

mkdir -p "$CONFIGS_DIR"

echo "🏗️  Primavera Multi-Module Docker Configuration Generator"
echo "========================================================"

# 프로젝트 구조 분석 결과를 바탕으로 한 완전한 모듈 정의
declare -A single_modules=(
    # 기본 학습 모듈들 (chap04부터 Docker 사용)
    ["chap04"]="DataAccessApplication:mariadb,vault"
    ["chap05"]="MyBatisLoggingApplication:mariadb,vault"
    ["chap06"]="ValidationApplication:mariadb,vault"
    ["chap07"]="ThymeleafWebApplication:mariadb,vault"
    ["chap08"]="SecurityFilterApplication:mariadb,vault"
    ["chap09"]="SpringSecurityBasicApplication:mariadb,vault"
    ["chap10"]="OAuth2SocialLoginApplication:mariadb,redis,vault"
    ["chap11"]="BoardSystemApplication:mariadb,vault"
    ["chap12"]="HierarchicalCommentApplication:mariadb,vault"
    ["chap13"]="AdvancedAuthorizationApplication:mariadb,mongodb,redis,vault"
    ["chap14"]="AdvancedJpaApplication:mariadb,mongodb,vault"
    ["chap15"]="JpaAdvancedMappingApplication:vault"
    ["chap16"]="FileProcessingMonitoringApplication:mariadb,vault"
    ["chap19"]="PrimaveraPerformanceApplication:mariadb,redis,vault"
)

# 멀티모듈 아키텍처 정의 (chap17, chap18)
declare -A multi_modules=(
    # chap17: Multi-Module Architecture
    ["chap17"]="MicroserviceApplication:mariadb-cdc,elasticsearch,vault:batch=ProductBatchApplication,streaming=ProductStreamingApplication"
    
    # chap18: Complete Microservices Architecture  
    ["chap18"]="ComplexMicroserviceApplication:mariadb,mongodb,redis,kafka,vault:account=AccountServiceApplication,configuration=ConfigurationServiceApplication,front=FrontServiceApplication,order=OrderServiceApplication,product=ProductServiceApplication"
)

# 포트 기본 설정
declare -A base_ports=(
    ["mariadb"]="3306"
    ["vault"]="8200"
    ["elasticsearch"]="9200"
    ["elasticsearch-transport"]="9300"
    ["mongodb"]="27017"
    ["redis"]="6379"
    ["kafka"]="9092"
    ["zookeeper"]="2181"
    ["kibana"]="5601"
)

# 기본 포트 반환 함수 (챕터별 구분 없음)
calculate_port() {
    base_port=$1
    chapter=$2
    service=$3
    
    # 모든 챕터에서 동일한 기본 포트 사용
    echo $base_port
}

# Vault 설정 생성 함수
generate_vault_configs() {
    chapter=$1
    app_name=$2
    services=$3
    sub_modules=$4
    
    vault_configs=""
    has_mariadb=false
    has_mongodb=false
    has_redis=false
    has_elasticsearch=false
    
    # 서비스 체크
    if [[ "$services" =~ mariadb ]]; then
        has_mariadb=true
    fi
    if [[ "$services" =~ mongodb ]]; then
        has_mongodb=true
    fi
    if [[ "$services" =~ redis ]]; then
        has_redis=true
    fi
    if [[ "$services" =~ elasticsearch ]]; then
        has_elasticsearch=true
    fi
    
    # 메인 애플리케이션 Vault 설정 (보기 좋게 포맷팅)
    main_config="echo 'Configuring $app_name...' && \\\\\\n             vault kv put secret/$app_name/local"
    
    if $has_mariadb; then
        mariadb_port=$(calculate_port ${base_ports[mariadb]} $chapter mariadb)
        main_config+=" \\\\\\n               spring.datasource.driver-class-name=org.mariadb.jdbc.Driver"
        main_config+=" \\\\\\n               spring.datasource.url=jdbc:mariadb://localhost:3306/primavera"
        main_config+=" \\\\\\n               spring.datasource.username=primavera"
        main_config+=" \\\\\\n               spring.datasource.password=primavera"
    fi
    
    if $has_mongodb; then
        mongodb_port=$(calculate_port ${base_ports[mongodb]} $chapter mongodb)
        main_config+=" \\\\\\n               spring.data.mongodb.uri=mongodb://primavera:primavera@localhost:27017/primavera?authSource=admin"
    fi
    
    if $has_redis; then
        main_config+=" \\\\\\n               spring.data.redis.host=localhost"
        main_config+=" \\\\\\n               spring.data.redis.port=6379"
        main_config+=" \\\\\\n               spring.data.redis.password=primavera"
    fi
    
    if $has_elasticsearch; then
        main_config+=" \\\\\\n               spring.elasticsearch.uris=http://localhost:9200"
        main_config+=" \\\\\\n               spring.elasticsearch.username=primavera"
        main_config+=" \\\\\\n               spring.elasticsearch.password=primavera"
        main_config+=" \\\\\\n               elasticsearch.host=localhost"
        main_config+=" \\\\\\n               elasticsearch.port=9200"
        main_config+=" \\\\\\n               elasticsearch.username=primavera"
        main_config+=" \\\\\\n               elasticsearch.password=primavera"
        main_config+=" \\\\\\n               elasticsearch.scheme=http"
    fi
    
    vault_configs="$main_config"
    
    # 서브 모듈 Vault 설정 생성 (멀티모듈의 경우)
    if [[ -n "$sub_modules" ]]; then
        IFS=',' read -ra SUB_MODULE_LIST <<< "$sub_modules"
        for sub_module in "${SUB_MODULE_LIST[@]}"; do
            IFS='=' read -r module_name module_app <<< "$sub_module"
            
            sub_config=" && \\\\\\n             echo 'Configuring $module_app ($module_name module)...' && \\\\\\n             vault kv put secret/$module_app/local"
            
            case $module_name in
                batch)
                    # Batch 모듈 설정
                    if $has_mariadb; then
                        sub_config+=" \\\\\\n               spring.datasource.driver-class-name=org.mariadb.jdbc.Driver"
                        sub_config+=" \\\\\\n               spring.datasource.url=jdbc:mariadb://localhost:3306/primavera"
                        sub_config+=" \\\\\\n               spring.datasource.username=primavera"
                        sub_config+=" \\\\\\n               spring.datasource.password=primavera"
                        sub_config+=" \\\\\\n               spring.batch.initialize-schema=always"
                    fi
                    if $has_elasticsearch; then
                        sub_config+=" \\\\\\n               spring.elasticsearch.uris=http://localhost:9200"
                        sub_config+=" \\\\\\n               spring.elasticsearch.username=primavera"
                        sub_config+=" \\\\\\n               spring.elasticsearch.password=primavera"
                        sub_config+=" \\\\\\n               elasticsearch.host=localhost"
                        sub_config+=" \\\\\\n               elasticsearch.port=9200"
                        sub_config+=" \\\\\\n               elasticsearch.username=primavera"
                        sub_config+=" \\\\\\n               elasticsearch.password=primavera"
                        sub_config+=" \\\\\\n               elasticsearch.scheme=http"
                    fi
                    ;;
                streaming)
                    # Streaming 모듈 설정 (WebFlux, 별도 포트)
                    sub_config+=" \\\\\\n               server.port=8081"
                    sub_config+=" \\\\\\n               spring.webflux.base-path=/streaming"
                    if $has_mariadb; then
                        sub_config+=" \\\\\\n               spring.datasource.driver-class-name=org.mariadb.jdbc.Driver"
                        sub_config+=" \\\\\\n               spring.datasource.url=jdbc:mariadb://localhost:3306/primavera"
                        sub_config+=" \\\\\\n               spring.datasource.username=primavera"
                        sub_config+=" \\\\\\n               spring.datasource.password=primavera"
                    fi
                    if $has_elasticsearch; then
                        sub_config+=" \\\\\\n               spring.elasticsearch.uris=http://localhost:9200"
                        sub_config+=" \\\\\\n               spring.elasticsearch.username=primavera"
                        sub_config+=" \\\\\\n               spring.elasticsearch.password=primavera"
                        sub_config+=" \\\\\\n               elasticsearch.host=localhost"
                        sub_config+=" \\\\\\n               elasticsearch.port=9200"
                        sub_config+=" \\\\\\n               elasticsearch.username=primavera"
                        sub_config+=" \\\\\\n               elasticsearch.password=primavera"
                        sub_config+=" \\\\\\n               elasticsearch.scheme=http"
                    fi
                    ;;
                account|configuration|front|order|product)
                    # 마이크로서비스 모듈별 설정
                    service_port=$((8080 + ${#module_name}))
                    sub_config+=" \\\\\\n               server.port=$service_port"
                    if $has_mariadb; then
                        sub_config+=" \\\\\\n               spring.datasource.driver-class-name=org.mariadb.jdbc.Driver"
                        sub_config+=" \\\\\\n               spring.datasource.url=jdbc:mariadb://localhost:3306/primavera"
                        sub_config+=" \\\\\\n               spring.datasource.username=primavera"
                        sub_config+=" \\\\\\n               spring.datasource.password=primavera"
                    fi
                    if $has_mongodb; then
                        sub_config+=" \\\\\\n               spring.data.mongodb.uri=mongodb://primavera:primavera@localhost:27017/primavera?authSource=admin"
                    fi
                    if $has_redis; then
                        sub_config+=" \\\\\\n               spring.data.redis.host=localhost"
                        sub_config+=" \\\\\\n               spring.data.redis.port=6379"
                        sub_config+=" \\\\\\n               spring.data.redis.password=primavera"
                    fi
                    ;;
            esac
            
            vault_configs+="$sub_config"
        done
    fi
    
    echo "$vault_configs"
}

# Vault 의존성 생성 함수
generate_vault_depends_on() {
    services=$1
    depends=""
    
    if [[ "$services" =~ mariadb ]]; then
        depends="mariadb-\${CHAPTER}:\n        condition: service_healthy"
    fi
    
    echo "$depends"
}

# 단일 모듈 설정 생성
echo "📝 Generating single module configurations..."
for chapter in "${!single_modules[@]}"; do
    IFS=':' read -r app_name services <<< "${single_modules[$chapter]}"
    
    echo "  ├── $chapter ($app_name) [Services: $services]"
    
    # 기본 설정
    config_content="CHAPTER=$chapter
APP_NAME=$app_name
SERVICES=$services
MODULE_TYPE=single"

    # 서비스별 포트 및 경로 설정
    IFS=',' read -ra SERVICE_LIST <<< "$services"
    for service in "${SERVICE_LIST[@]}"; do
        service=$(echo "$service" | xargs)
        case $service in
            basic)
                # 기본 모듈, 추가 서비스 없음
                ;;
            mariadb)
                port=$(calculate_port ${base_ports[mariadb]} $chapter mariadb)
                config_content+="\nMARIADB_PORT=$port"
                config_content+="\nSQL_INIT_PATH=../sql/${chapter}-init.sql"
                ;;
            vault)
                port=$(calculate_port ${base_ports[vault]} $chapter vault)
                config_content+="\nVAULT_PORT=$port"
                config_content+="\nVAULT_INIT_PATH=../vault/vault-init.sh"
                
                # Vault 설정 생성
                vault_configs=$(generate_vault_configs "$chapter" "$app_name" "$services" "")
                config_content+="\nVAULT_CONFIGURATIONS=\"$vault_configs\""
                
                vault_depends=$(generate_vault_depends_on "$services")
                if [[ -n "$vault_depends" ]]; then
                    config_content+="\nVAULT_DEPENDS_ON=\"$vault_depends\""
                else
                    config_content+="\nVAULT_DEPENDS_ON=\"\""
                fi
                ;;
            mongodb)
                port=$(calculate_port ${base_ports[mongodb]} $chapter mongodb)
                config_content+="\nMONGODB_PORT=$port"
                config_content+="\nMONGODB_INIT_PATH=../mongodb/${chapter}-mongo-init.js"
                ;;
            redis)
                port=$(calculate_port ${base_ports[redis]} $chapter redis)
                config_content+="\nREDIS_PORT=$port"
                ;;
            elasticsearch)
                port=$(calculate_port ${base_ports[elasticsearch]} $chapter elasticsearch)
                transport_port=$(calculate_port ${base_ports[elasticsearch-transport]} $chapter elasticsearch-transport)
                config_content+="\nELASTICSEARCH_PORT=$port"
                config_content+="\nELASTICSEARCH_TRANSPORT_PORT=$transport_port"
                ;;
            kafka)
                port=$(calculate_port ${base_ports[kafka]} $chapter kafka)
                zk_port=$(calculate_port ${base_ports[zookeeper]} $chapter zookeeper)
                config_content+="\nKAFKA_PORT=$port"
                config_content+="\nZOOKEEPER_PORT=$zk_port"
                ;;
            kibana)
                port=$(calculate_port ${base_ports[kibana]} $chapter kibana)
                config_content+="\nKIBANA_PORT=$port"
                ;;
        esac
    done
    
    # 설정 파일 생성
    echo -e "$config_content" > "$CONFIGS_DIR/${chapter}-config.env"
done

echo ""
echo "🏗️  Generating multi-module configurations..."

# 멀티모듈 설정 생성
for chapter in "${!multi_modules[@]}"; do
    IFS=':' read -r app_name services sub_modules <<< "${multi_modules[$chapter]}"
    
    echo "  ├── $chapter ($app_name) [Services: $services]"
    if [[ -n "$sub_modules" ]]; then
        echo "  │   └── Sub-modules: $sub_modules"
    fi
    
    # 기본 설정
    config_content="CHAPTER=$chapter
APP_NAME=$app_name
SERVICES=$services
MODULE_TYPE=multi"

    # 서브 모듈 정보 추가
    if [[ -n "$sub_modules" ]]; then
        config_content+="\nSUB_MODULES=$sub_modules"
    fi

    # 서비스별 포트 및 경로 설정
    IFS=',' read -ra SERVICE_LIST <<< "$services"
    for service in "${SERVICE_LIST[@]}"; do
        service=$(echo "$service" | xargs)
        case $service in
            mariadb|mariadb-cdc)
                port=$(calculate_port ${base_ports[mariadb]} $chapter mariadb)
                config_content+="\nMARIADB_PORT=$port"
                config_content+="\nSQL_INIT_PATH=../sql/${chapter}-init.sql"
                ;;
            vault)
                port=$(calculate_port ${base_ports[vault]} $chapter vault)
                config_content+="\nVAULT_PORT=$port"
                config_content+="\nVAULT_INIT_PATH=../vault/vault-init.sh"
                
                # 멀티모듈 Vault 설정 생성
                vault_configs=$(generate_vault_configs "$chapter" "$app_name" "$services" "$sub_modules")
                config_content+="\nVAULT_CONFIGURATIONS=\"$vault_configs\""
                
                vault_depends=$(generate_vault_depends_on "$services")
                if [[ -n "$vault_depends" ]]; then
                    config_content+="\nVAULT_DEPENDS_ON=\"$vault_depends\""
                else
                    config_content+="\nVAULT_DEPENDS_ON=\"\""
                fi
                ;;
            mongodb)
                port=$(calculate_port ${base_ports[mongodb]} $chapter mongodb)
                config_content+="\nMONGODB_PORT=$port"
                config_content+="\nMONGODB_INIT_PATH=../mongodb/${chapter}-mongo-init.js"
                ;;
            redis)
                port=$(calculate_port ${base_ports[redis]} $chapter redis)
                config_content+="\nREDIS_PORT=$port"
                ;;
            elasticsearch)
                port=$(calculate_port ${base_ports[elasticsearch]} $chapter elasticsearch)
                transport_port=$(calculate_port ${base_ports[elasticsearch-transport]} $chapter elasticsearch-transport)
                config_content+="\nELASTICSEARCH_PORT=$port"
                config_content+="\nELASTICSEARCH_TRANSPORT_PORT=$transport_port"
                ;;
            kafka)
                port=$(calculate_port ${base_ports[kafka]} $chapter kafka)
                zk_port=$(calculate_port ${base_ports[zookeeper]} $chapter zookeeper)
                config_content+="\nKAFKA_PORT=$port"
                config_content+="\nZOOKEEPER_PORT=$zk_port"
                ;;
            kibana)
                port=$(calculate_port ${base_ports[kibana]} $chapter kibana)
                config_content+="\nKIBANA_PORT=$port"
                ;;
        esac
    done
    
    # 설정 파일 생성
    echo -e "$config_content" > "$CONFIGS_DIR/${chapter}-config.env"
done

echo ""
echo "✅ Configuration generation completed!"
echo "📁 Config files location: $CONFIGS_DIR"
echo ""
echo "📊 Generated configurations:"
echo "├── Single modules: ${#single_modules[@]} chapters"
echo "├── Multi-modules: ${#multi_modules[@]} chapters"
echo "└── Total: $((${#single_modules[@]} + ${#multi_modules[@]})) configurations"
echo ""
echo "🔍 Available configurations:"
ls -1 "$CONFIGS_DIR"/*.env 2>/dev/null | sed 's/.*\///;s/-config\.env$//' | sort | sed 's/^/  • /'