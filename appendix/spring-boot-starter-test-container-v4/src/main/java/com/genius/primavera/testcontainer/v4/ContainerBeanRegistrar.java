package com.genius.primavera.testcontainer.v4;

import com.genius.primavera.testcontainer.v4.bean.BeanCreator;
import com.genius.primavera.testcontainer.v4.bean.BeanCreatorRegistry;
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
    
    private <T> void registerContainers(
            Collection<T> containers, 
            ConfigurableListableBeanFactory beanFactory,
            Function<T, Object> beanCreator) {
        
        containers.forEach(container -> registerSingleContainer(container, beanFactory, beanCreator));
    }
    
    private <T> void registerSingleContainer(
            T container,
            ConfigurableListableBeanFactory beanFactory,
            Function<T, Object> beanCreator) {
        
        if (!isValidContainer(container)) {
            log.warn("Invalid container configuration: {}", container);
            return;
        }
        
        try {
            Object bean = beanCreator.apply(container);
            if (bean != null) {
                String beanName = extractBeanName(container);
                beanFactory.registerSingleton(beanName, bean);
                logBeanRegistration(bean, beanName, container);
            } else {
                log.warn("Bean creation returned null for container: {}", container);
            }
        } catch (Exception e) {
            handleRegistrationError(container, e);
        }
    }
    
    private boolean isValidContainer(Object container) {
        if (container instanceof ContainerInfo containerInfo) {
            return containerInfo.getName() != null && 
                   containerInfo.getType() != null && 
                   containerInfo.getContainer() != null &&
                   containerInfo.getContainer().isRunning();
        }
        return false;
    }
    
    private String extractBeanName(Object container) {
        if (container instanceof ContainerInfo containerInfo) {
            return containerInfo.getName();
        }
        return container.toString();
    }
    
    private void logBeanRegistration(Object bean, String beanName, Object container) {
        if (container instanceof ContainerInfo containerInfo) {
            log.info("Registered {} bean '{}' for container type {}", 
                bean.getClass().getSimpleName(), beanName, containerInfo.getType());
        } else {
            log.info("Registered {} bean '{}'", bean.getClass().getSimpleName(), beanName);
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
        BeanCreator creator = BeanCreatorRegistry.getCreator(containerInfo.getType());
        return creator.createBean(containerInfo);
    }
}