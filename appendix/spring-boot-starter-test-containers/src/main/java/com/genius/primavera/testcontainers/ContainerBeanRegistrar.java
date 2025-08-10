package com.genius.primavera.testcontainers;

import com.genius.primavera.testcontainers.bean.BeanCreatorRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.Collection;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class ContainerBeanRegistrar {

    private final ContainerManager containerManager;

    public void registerBeans(ConfigurableListableBeanFactory beanFactory) {
        Collection<ContainerInfo> containers = containerManager.getAllContainers();
        registerContainers(containers, beanFactory, this::createBean);
    }

    private <T> void registerContainers(Collection<T> containers, ConfigurableListableBeanFactory beanFactory, Function<T, Object> beanCreator) {
        containers.forEach(container -> registerSingleContainer(container, beanFactory, beanCreator));
    }

    private <T> void registerSingleContainer(T container, ConfigurableListableBeanFactory beanFactory, Function<T, Object> beanCreator) {
        if (!isValidContainer(container)) {
            log.warn("Invalid container configuration: {}", container);
            return;
        }

        try {
            Object bean = beanCreator.apply(container);
            if (bean != null) {
                String beanName = extractBeanName(container);
                registerBeanSafely(beanFactory, beanName, bean, container);
            } else {
                handleNullBeanCreation(container);
            }
        } catch (Exception e) {
            handleRegistrationError(container, e);
        }
    }

    private boolean isValidContainer(Object container) {
        if (!(container instanceof ContainerInfo containerInfo)) {
            log.warn("Container validation failed: not a ContainerInfo instance - {}", container.getClass().getSimpleName());
            return false;
        }

        if (containerInfo.name() == null || containerInfo.name().trim().isEmpty()) {
            log.warn("Container validation failed: name is null or empty for container {}", containerInfo);
            return false;
        }

        if (containerInfo.type() == null) {
            log.warn("Container validation failed: container type is null for container '{}'", containerInfo.name());
            return false;
        }

        if (containerInfo.container() == null) {
            log.warn("Container validation failed: underlying container instance is null for '{}'", containerInfo.name());
            return false;
        }

        if (!containerInfo.container().isRunning()) {
            log.warn("Container validation failed: container '{}' is not running (state: {})",
                    containerInfo.name(),
                    containerInfo.container().isCreated() ? "created" : "stopped");
            return false;
        }

        return true;
    }

    private String extractBeanName(Object container) {
        if (container instanceof ContainerInfo containerInfo) return containerInfo.name();
        return container.toString();
    }

    private void logBeanRegistration(Object bean, String beanName, Object container) {
        if (container instanceof ContainerInfo containerInfo) {
            log.info("Registered {} bean '{}' for container type {}", bean.getClass().getSimpleName(), beanName, containerInfo.type());
        } else {
            log.info("Registered {} bean '{}'", bean.getClass().getSimpleName(), beanName);
        }
    }

    private synchronized void registerBeanSafely(ConfigurableListableBeanFactory beanFactory, String beanName, Object bean, Object container) {
        if (beanFactory.containsSingleton(beanName)) {
            log.warn("Bean '{}' already exists in factory. Skipping registration for container: {}", beanName, container);
            return;
        }

        try {
            beanFactory.registerSingleton(beanName, bean);
            logBeanRegistration(bean, beanName, container);
        } catch (Exception e) {
            log.error("Failed to register bean '{}' in factory for container: {}", beanName, container, e);
            throw e;
        }
    }

    private void handleNullBeanCreation(Object container) {
        String containerName = extractBeanName(container);

        if (container instanceof ContainerInfo containerInfo) {
            boolean creatorExists = BeanCreatorRegistry.isSupported(containerInfo.type());
            if (!creatorExists) {
                log.error("Bean creation returned null: No bean creator registered for container type '{}' (container: '{}')", containerInfo.type(), containerName);
            } else {
                log.warn("Bean creation returned null: Bean creator exists but failed to create bean for container '{}' (type: '{}')", containerName, containerInfo.type());
            }
        } else {
            log.warn("Bean creation returned null for container: {}", container);
        }
    }

    private void handleRegistrationError(Object container, Exception e) {
        String containerName = extractBeanName(container);
        log.error("Failed to register bean for container: {}", containerName, e);

        if (e instanceof IllegalArgumentException) {
            log.error("Container type not supported or configuration invalid for: {}", containerName);
        } else if (e instanceof RuntimeException) {
            log.error("Runtime error during bean creation for: {}", containerName);
        }
    }

    private Object createBean(ContainerInfo containerInfo) {
        return BeanCreatorRegistry.findCreator(containerInfo.type()).map(creator -> creator.createBean(containerInfo)).orElse(null);
    }
}