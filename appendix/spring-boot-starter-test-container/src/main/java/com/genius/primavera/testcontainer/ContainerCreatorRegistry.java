package com.genius.primavera.testcontainer;

import com.genius.primavera.testcontainer.factory.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ContainerCreatorRegistry {
    
    private static final Map<ContainerType, ContainerCreator> creators = new ConcurrentHashMap<>();
    private static final Map<ContainerType, Boolean> enabledTypes = new ConcurrentHashMap<>();
    private static volatile boolean initialized = false;
    
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        
        log.info("Initializing ContainerCreatorRegistry...");
        
        registerBuiltInCreators();
        loadExternalCreators();
        
        initialized = true;
        log.info("ContainerCreatorRegistry initialized with {} creators", creators.size());
    }
    
    private static void registerBuiltInCreators() {
        log.debug("Registering built-in container creators...");
        
        registerCreator(new MariaDBContainerCreator());
        registerCreator(new MySQLContainerCreator());
        registerCreator(new PostgreSQLContainerCreator());
        registerCreator(new RedisContainerCreator());
        registerCreator(new MongoDBContainerCreator());
        registerCreator(new KafkaContainerCreator());
        registerCreator(new ElasticsearchContainerCreator());
        
        log.debug("Registered {} built-in container creators", creators.size());
    }
    
    private static void loadExternalCreators() {
        log.debug("Loading external container creators via SPI...");
        
        ServiceLoader<ContainerCreator> serviceLoader = ServiceLoader.load(ContainerCreator.class);
        int externalCount = 0;
        
        for (ContainerCreator creator : serviceLoader) {
            try {
                registerCreator(creator);
                externalCount++;
                log.info("Loaded external container creator: {} for type {}", 
                    creator.getClass().getSimpleName(), creator.getSupportedType());
            } catch (Exception e) {
                log.warn("Failed to load external container creator: {}", creator.getClass().getSimpleName(), e);
            }
        }
        
        if (externalCount > 0) {
            log.info("Loaded {} external container creators", externalCount);
        } else {
            log.debug("No external container creators found");
        }
    }
    
    public static void registerCreator(ContainerCreator creator) {
        if (creator == null) {
            log.warn("Attempted to register null container creator");
            return;
        }
        
        ContainerType type = creator.getSupportedType();
        if (type == null) {
            log.warn("Container creator {} has null supported type", creator.getClass().getSimpleName());
            return;
        }
        
        ContainerCreator existing = creators.put(type, creator);
        if (existing != null && !existing.getClass().equals(creator.getClass())) {
            log.info("Replaced container creator for type {}: {} -> {}", 
                type, existing.getClass().getSimpleName(), creator.getClass().getSimpleName());
        } else {
            log.debug("Registered container creator for type {}: {}", type, creator.getClass().getSimpleName());
        }
        
        enabledTypes.putIfAbsent(type, true);
    }
    
    public static void setTypeEnabled(ContainerType type, boolean enabled) {
        enabledTypes.put(type, enabled);
        log.debug("Container type {} is now {}", type, enabled ? "enabled" : "disabled");
    }
    
    public static Optional<ContainerCreator> findCreator(ContainerType type) {
        ensureInitialized();
        
        if (!isTypeEnabled(type)) {
            log.debug("Container type {} is disabled", type);
            return Optional.empty();
        }
        
        return Optional.ofNullable(creators.get(type));
    }
    
    @Deprecated(since = "4.0", forRemoval = true)
    public static ContainerCreator getCreator(ContainerType type) {
        return findCreator(type)
            .orElseThrow(() -> new IllegalArgumentException("No creator registered for container type: " + type));
    }
    
    public static boolean isSupported(ContainerType type) {
        ensureInitialized();
        return creators.containsKey(type) && isTypeEnabled(type);
    }
    
    public static boolean isTypeEnabled(ContainerType type) {
        return enabledTypes.getOrDefault(type, false);
    }
    
    public static Set<ContainerType> getSupportedTypes() {
        ensureInitialized();
        return creators.keySet().stream()
            .filter(ContainerCreatorRegistry::isTypeEnabled)
            .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }
    
    public static void unregisterCreator(ContainerType type) {
        ContainerCreator removed = creators.remove(type);
        enabledTypes.remove(type);
        
        if (removed != null) {
            log.info("Unregistered container creator for type {}: {}", type, removed.getClass().getSimpleName());
        }
    }
    
    public static void reset() {
        log.info("Resetting ContainerCreatorRegistry...");
        creators.clear();
        enabledTypes.clear();
        initialized = false;
    }
    
    private static void ensureInitialized() {
        if (!initialized) {
            initialize();
        }
    }
}
