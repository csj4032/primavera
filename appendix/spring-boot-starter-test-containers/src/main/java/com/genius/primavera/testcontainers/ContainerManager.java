package com.genius.primavera.testcontainers;

import com.genius.primavera.testcontainers.config.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePropertySource;
import org.testcontainers.containers.GenericContainer;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ContainerManager {

    private final EnableTestContainers annotation;
    private final Class<?> testClass;
    private final Map<String, ContainerInfo> containers = new ConcurrentHashMap<>();
    private final ContainerConfiguration configuration;
    private volatile boolean started = false;

    public ContainerManager(EnableTestContainers annotation, Class<?> testClass) {
        this.annotation = annotation;
        this.testClass = testClass;
        this.configuration = loadConfiguration();
    }

    public void startContainers() {
        if (started) {
            return;
        }

        synchronized (this) {
            if (started) {
                return;
            }

            log.info("Starting {} containers for test class: {}",
                    annotation.value().length, testClass.getSimpleName());

            List<CompletableFuture<Void>> futures = Arrays.stream(annotation.value())
                    .map(this::startContainerAsync)
                    .toList();

            try {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .get(120, TimeUnit.SECONDS);
                started = true;
                log.info("All containers started successfully for test class: {}", testClass.getSimpleName());
            } catch (Exception e) {
                log.error("Failed to start containers for test class: {}", testClass.getSimpleName(), e);
                throw new RuntimeException("Container startup failed", e);
            }
        }
    }

    public void stopContainers() {
        if (!started) {
            return;
        }

        log.info("Stopping {} containers for test class: {}",
                containers.size(), testClass.getSimpleName());

        containers.values().parallelStream().forEach(containerInfo -> {
            try {
                containerInfo.container().stop();
                log.debug("Stopped container: {}", containerInfo.name());
            } catch (Exception e) {
                log.error("Failed to stop container: {}", containerInfo.name(), e);
            }
        });

        containers.clear();
        started = false;
        log.info("All containers stopped for test class: {}", testClass.getSimpleName());
    }

    public boolean isStarted() {
        return started;
    }

    public Collection<ContainerInfo> getAllContainers() {
        return Collections.unmodifiableCollection(containers.values());
    }

    public ContainerInfo getContainer(String name) {
        return containers.get(name);
    }

    private CompletableFuture<Void> startContainerAsync(EnableTestContainers.TestContainer containerDef) {
        return CompletableFuture.runAsync(() -> {
            String name = containerDef.name();
            ContainerType type = containerDef.type();

            try {
                log.info("Starting {} container: {}", type, name);

                Optional<ContainerConfiguration.ContainerInstanceConfig> configOpt = configuration.getContainerConfig(name);
                log.info("Configuration found for container '{}': {}", name, configOpt.isPresent());
                
                ContainerConfiguration.ContainerInstanceConfig instanceConfig = configOpt
                        .orElse(createDefaultInstanceConfig(name, type));
                
                if (instanceConfig.getType() != type) {
                    log.warn("Type mismatch for container '{}': annotation={}, config={}. Using annotation type.", 
                        name, type, instanceConfig.getType());
                    instanceConfig.setType(type);
                }
                
                BaseContainerSpec spec = instanceConfig.getSpecForType();
                log.info("Spec for container '{}': {}", name, spec != null ? spec.getClass().getSimpleName() : "null");
                if (spec == null) {
                    log.info("Creating default spec for container '{}'", name);
                    spec = createDefaultSpec(type);
                }

                GenericContainer<?> container = ContainerFactory.create(type, spec);
                container.start();

                ContainerInfo info = new ContainerInfo(name, type, container, spec);
                containers.put(name, info);

                log.info("Started {} container '{}' on {}:{}",
                        type, name, container.getHost(), container.getFirstMappedPort());

            } catch (Exception e) {
                log.error("Failed to start container: {}", name, e);
                throw new RuntimeException("Failed to start container: " + name, e);
            }
        });
    }

    private ContainerConfiguration loadConfiguration() {
        try {
            ConfigurableEnvironment environment = new StandardEnvironment();
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

            String[] yamlFiles = {"application-test.yml", "application-test.yaml"};
            boolean yamlLoaded = false;
            for (String yamlFile : yamlFiles) {
                try {
                    Resource resource = resolver.getResource("classpath:" + yamlFile);
                    log.info("Checking YAML file '{}': exists={}", yamlFile, resource.exists());
                    if (resource.exists()) {
                        YamlPropertySourceLoader yamlLoader = new YamlPropertySourceLoader();
                        List<PropertySource<?>> propertySources = yamlLoader.load(yamlFile, resource);
                        for (PropertySource<?> propertySource : propertySources) {
                            environment.getPropertySources().addFirst(propertySource);
                        }
                        log.info("Loaded YAML configuration from '{}'", yamlFile);
                        yamlLoaded = true;
                        break;
                    }
                } catch (Exception e) {
                    log.warn("Failed to load YAML configuration from '{}': {}", yamlFile, e.getMessage());
                }
            }
            
            log.info("YAML configuration loaded: {}", yamlLoaded);

            String[] propFiles = {"application-test.properties"};
            for (String propFile : propFiles) {
                try {
                    Resource resource = resolver.getResource("classpath:" + propFile);
                    if (resource.exists()) {
                        ResourcePropertySource source = new ResourcePropertySource(propFile, resource);
                        environment.getPropertySources().addFirst(source);
                        log.info("Loaded properties configuration from '{}'", propFile);
                        break;
                    }
                } catch (Exception e) {
                    log.warn("Failed to load properties configuration from '{}': {}", propFile, e.getMessage());
                }
            }
            log.info("Properties loaded: {}", yamlLoaded);
            ContainerConfiguration config = Binder.get(environment)
                    .bind("testcontainers", ContainerConfiguration.class)
                    .orElse(new ContainerConfiguration());
            
            log.info("Configuration containers: {}", config.getContainers().keySet());
            config.getContainers().forEach((name, instanceConfig) -> {
                log.info("Container '{}' type: {}, spec: {}", name, instanceConfig.getType(), 
                    instanceConfig.getSpecForType() != null ? instanceConfig.getSpecForType().getClass().getSimpleName() : "null");
            });
            
            return config;

        } catch (Exception e) {
            log.warn("Failed to load configuration, using defaults: {}", e.getMessage());
            return new ContainerConfiguration();
        }
    }

    private ContainerConfiguration.ContainerInstanceConfig createDefaultInstanceConfig(String name, ContainerType type) {
        ContainerConfiguration.ContainerInstanceConfig config = new ContainerConfiguration.ContainerInstanceConfig();
        config.setType(type);
        
        BaseContainerSpec spec = createDefaultSpec(type);
        setSpecForType(config, type, spec);
        
        log.info("Created default instance config for container '{}' of type {}", name, type);
        return config;
    }
    
    private void setSpecForType(ContainerConfiguration.ContainerInstanceConfig config, ContainerType type, BaseContainerSpec spec) {
        switch (type) {
            case MARIADB -> {
                if (spec instanceof MariaDbContainerSpec mariaDbSpec) {
                    config.setMariadb(mariaDbSpec);
                } else {
                    config.setMariadb(createDefaultMariaDbSpec());
                }
            }
            case MYSQL -> {
                if (spec instanceof MySqlContainerSpec mysqlSpec) {
                    config.setMysql(mysqlSpec);
                } else {
                    config.setMysql(createDefaultMySqlSpec());
                }
            }
            case POSTGRESQL -> {
                if (spec instanceof PostgreSqlContainerSpec pgSpec) {
                    config.setPostgresql(pgSpec);
                } else {
                    config.setPostgresql(createDefaultPostgreSqlSpec());
                }
            }
            case REDIS -> {
                if (spec instanceof RedisContainerSpec redisSpec) {
                    config.setRedis(redisSpec);
                } else {
                    config.setRedis(createDefaultRedisSpec());
                }
            }
            case MONGODB -> {
                if (spec instanceof MongoContainerSpec mongoSpec) {
                    config.setMongodb(mongoSpec);
                } else {
                    config.setMongodb(createDefaultMongoSpec());
                }
            }
            case KAFKA -> {
                if (spec instanceof KafkaContainerSpec kafkaSpec) {
                    config.setKafka(kafkaSpec);
                } else {
                    config.setKafka(createDefaultKafkaSpec());
                }
            }
            case ELASTICSEARCH -> {
                if (spec instanceof ElasticsearchContainerSpec esSpec) {
                    config.setElasticsearch(esSpec);
                } else {
                    config.setElasticsearch(createDefaultElasticsearchSpec());
                }
            }
            case VAULT -> {
                if (spec instanceof VaultContainerSpec vaultSpec) {
                    config.setVault(vaultSpec);
                } else {
                    config.setVault(createDefaultVaultSpec());
                }
            }
            case LOCALSTACK -> {
                if (spec instanceof LocalStackContainerSpec localStackSpec) {
                    config.setLocalstack(localStackSpec);
                } else {
                    config.setLocalstack(createDefaultLocalStackSpec());
                }
            }
        }
    }
    
    private BaseContainerSpec createDefaultSpec(ContainerType type) {
        return switch (type) {
            case MARIADB -> createDefaultMariaDbSpec();
            case MYSQL -> createDefaultMySqlSpec();
            case POSTGRESQL -> createDefaultPostgreSqlSpec();
            case REDIS -> createDefaultRedisSpec();
            case MONGODB -> createDefaultMongoSpec();
            case KAFKA -> createDefaultKafkaSpec();
            case ELASTICSEARCH -> createDefaultElasticsearchSpec();
            case VAULT -> createDefaultVaultSpec();
            case LOCALSTACK -> createDefaultLocalStackSpec();
            default -> createDefaultBaseSpec(type);
        };
    }
    
    private MariaDbContainerSpec createDefaultMariaDbSpec() {
        MariaDbContainerSpec spec = new MariaDbContainerSpec();
        spec.setImage(ContainerType.MARIADB.getDefaultImage());
        return spec;
    }
    
    private DatabaseContainerSpec createDefaultDatabaseSpec() {
        DatabaseContainerSpec spec = new DatabaseContainerSpec();
        spec.setDatabase("primavera");
        spec.setUsername("primavera");
        spec.setPassword("primavera");
        return spec;
    }
    
    private RedisContainerSpec createDefaultRedisSpec() {
        RedisContainerSpec spec = new RedisContainerSpec();
        spec.setImage(ContainerType.REDIS.getDefaultImage());
        return spec;
    }
    
    private MySqlContainerSpec createDefaultMySqlSpec() {
        MySqlContainerSpec spec = new MySqlContainerSpec();
        spec.setImage(ContainerType.MYSQL.getDefaultImage());
        return spec;
    }
    
    private PostgreSqlContainerSpec createDefaultPostgreSqlSpec() {
        PostgreSqlContainerSpec spec = new PostgreSqlContainerSpec();
        spec.setImage(ContainerType.POSTGRESQL.getDefaultImage());
        return spec;
    }
    
    private MongoContainerSpec createDefaultMongoSpec() {
        MongoContainerSpec spec = new MongoContainerSpec();
        spec.setImage(ContainerType.MONGODB.getDefaultImage());
        return spec;
    }
    
    private KafkaContainerSpec createDefaultKafkaSpec() {
        KafkaContainerSpec spec = new KafkaContainerSpec();
        spec.setImage(ContainerType.KAFKA.getDefaultImage());
        return spec;
    }
    
    private ElasticsearchContainerSpec createDefaultElasticsearchSpec() {
        ElasticsearchContainerSpec spec = new ElasticsearchContainerSpec();
        spec.setImage(ContainerType.ELASTICSEARCH.getDefaultImage());
        return spec;
    }
    
    private VaultContainerSpec createDefaultVaultSpec() {
        VaultContainerSpec spec = new VaultContainerSpec();
        spec.setImage(ContainerType.VAULT.getDefaultImage());
        return spec;
    }
    
    private LocalStackContainerSpec createDefaultLocalStackSpec() {
        LocalStackContainerSpec spec = new LocalStackContainerSpec();
        spec.setImage(ContainerType.LOCALSTACK.getDefaultImage());
        return spec;
    }
    
    private BaseContainerSpec createDefaultBaseSpec(ContainerType type) {
        return new BaseContainerSpec() {
            {
                setImage(type.getDefaultImage());
            }
        };
    }
}