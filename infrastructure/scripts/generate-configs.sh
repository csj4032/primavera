#\!/bin/bash

# Generate config files for each chapter
chapters=(
    "chap06:ValidationApplication:3310:8202"
    "chap07:ThymeleafWebApplication:3311:8203"
    "chap08:SecurityFilterApplication:3312:8204"
    "chap09:SpringSecurityBasicApplication:3313:8205"
    "chap10:OAuth2SocialLoginApplication:3314:8206"
    "chap11:BoardSystemApplication:3315:8207"
    "chap12:HierarchicalCommentApplication:3316:8208"
    "chap14:AdvancedAuthorizationApplication:3318:8210"
    "chap15:JpaAdvancedMappingApplication:3319:8211"
    "chap16:FileProcessingMonitoringApplication:3320:8212"
    "chap17:MicroserviceApplication:3321:8213"
)

for chapter_info in "${chapters[@]}"; do
    IFS=':' read -r chap app_name mariadb_port vault_port <<< "$chapter_info"
    
    cat > "/Users/genius/Workspace/primavera/infrastructure/scripts/configs/${chap}-config.env" << EOL
CHAPTER=${chap}
APP_NAME=${app_name}
SERVICES=mariadb,vault
MARIADB_PORT=${mariadb_port}
VAULT_PORT=${vault_port}
SQL_INIT_PATH=../sql/${chap}-init.sql
VAULT_INIT_PATH=../vault/vault-init.sh
EOL

    echo "Generated config for ${chap}"
done

echo "All config files generated successfully\!"
