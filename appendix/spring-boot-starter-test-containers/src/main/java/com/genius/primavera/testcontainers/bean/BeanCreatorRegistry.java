package com.genius.primavera.testcontainers.bean;

import com.genius.primavera.testcontainers.ContainerType;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Slf4j
public class BeanCreatorRegistry {

    private static final Map<ContainerType, BeanCreator> creators = new ConcurrentHashMap<>();
    private static final Map<ContainerType, Boolean> enabledTypes = new ConcurrentHashMap<>();
    private static volatile boolean initialized = false;

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        log.info("Initializing BeanCreatorRegistry...");

        registerBuiltInCreators();
        loadExternalCreators();

        initialized = true;
        log.info("BeanCreatorRegistry initialized with {} creators", creators.size());
    }

    private static void registerBuiltInCreators() {
        log.debug("Registering built-in bean creators...");
        registerCreator(new DataSourceBeanCreator.MariaDBBeanCreator());
        registerCreator(new DataSourceBeanCreator.MySQLBeanCreator());
        registerCreator(new DataSourceBeanCreator.PostgreSQLBeanCreator());
        registerCreator(new MongoDBBeanCreator());
        registerCreator(new RedisBeanCreator());
        registerCreator(new KafkaBeanCreator());
        registerCreator(new ElasticsearchBeanCreator());
        log.debug("Registered {} built-in bean creators", creators.size());
    }

    private static void loadExternalCreators() {
        log.debug("Loading external bean creators via SPI...");

        ServiceLoader<BeanCreator> serviceLoader = ServiceLoader.load(BeanCreator.class);
        int externalCount = 0;

        for (BeanCreator creator : serviceLoader) {
            try {
                registerCreator(creator);
                externalCount++;
                log.info("Loaded external bean creator: {} for type {}", creator.getClass().getSimpleName(), creator.getSupportedType());
            } catch (Exception e) {
                log.warn("Failed to load external bean creator: {}", creator.getClass().getSimpleName(), e);
            }
        }

        if (externalCount > 0) {
            log.info("Loaded {} external bean creators", externalCount);
        } else {
            log.debug("No external bean creators found");
        }
    }

    public static void registerCreator(BeanCreator creator) {
        if (creator == null) {
            log.warn("Attempted to register null bean creator");
            return;
        }

        ContainerType type = creator.getSupportedType();
        if (type == null) {
            log.warn("Bean creator {} has null supported type", creator.getClass().getSimpleName());
            return;
        }

        BeanCreator existing = creators.put(type, creator);
        if (existing != null && !existing.getClass().equals(creator.getClass())) {
            log.info("Replaced bean creator for type {}: {} -> {}",
                    type, existing.getClass().getSimpleName(), creator.getClass().getSimpleName());
        } else {
            log.debug("Registered bean creator for type {}: {}", type, creator.getClass().getSimpleName());
        }

        enabledTypes.putIfAbsent(type, true);
    }

    public static void setTypeEnabled(ContainerType type, boolean enabled) {
        enabledTypes.put(type, enabled);
        log.debug("Bean creator for type {} is now {}", type, enabled ? "enabled" : "disabled");
    }

    public static Optional<BeanCreator> findCreator(ContainerType type) {
        ensureInitialized();

        if (!isTypeEnabled(type)) {
            log.debug("Bean creator for type {} is disabled", type);
            return Optional.empty();
        }

        return Optional.ofNullable(creators.get(type));
    }

    @Deprecated(since = "4.0", forRemoval = true)
    public static BeanCreator getCreator(ContainerType type) {
        return findCreator(type)
                .orElseThrow(() -> new IllegalArgumentException("No bean creator registered for container type: " + type));
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
                .filter(BeanCreatorRegistry::isTypeEnabled)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }

    private static void ensureInitialized() {
        if (!initialized) {
            initialize();
        }
    }

    private static void registerCreatorIfClassExists(String className, Supplier<BeanCreator> creatorSupplier) {
        try {
            Class.forName(className);
            registerCreator(creatorSupplier.get());
            log.debug("Registered bean creator for class: {}", className);
        } catch (ClassNotFoundException e) {
            log.debug("Skipping bean creator registration - class not found: {}", className);
        } catch (Exception e) {
            log.warn("Failed to register bean creator for class {}: {}", className, e.getMessage());
        }
    }

    private static void registerCreatorIfClassExists(String dependencyClassName, String creatorClassName) {
        try {
            Class.forName(dependencyClassName);
            @SuppressWarnings("unchecked")
            Class<? extends BeanCreator> creatorClass = (Class<? extends BeanCreator>) Class.forName(creatorClassName);
            BeanCreator creator = creatorClass.getDeclaredConstructor().newInstance();
            registerCreator(creator);
            log.debug("Registered bean creator {} for dependency class: {}", creatorClassName, dependencyClassName);
        } catch (ClassNotFoundException e) {
            log.debug("Skipping bean creator registration - dependency class not found: {}", dependencyClassName);
        } catch (Exception e) {
            log.warn("Failed to register bean creator {} for dependency class {}: {}", creatorClassName, dependencyClassName, e.getMessage());
        }
    }
}