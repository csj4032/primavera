package com.genius.primavera.testcontainer.v4;

import com.genius.primavera.testcontainer.v4.bean.BeanCreator;
import com.genius.primavera.testcontainer.v4.bean.BeanCreatorRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

@Slf4j
@RequiredArgsConstructor
public class ContainerBeanRegistrar {
    
    private final ContainerManager containerManager;
    
    public void registerBeans(ConfigurableListableBeanFactory beanFactory) {
        containerManager.getAllContainers().forEach(containerInfo -> {
            try {
                Object bean = createBean(containerInfo);
                if (bean != null) {
                    beanFactory.registerSingleton(containerInfo.getName(), bean);
                    log.info("Registered {} bean '{}' for container type {}", 
                        bean.getClass().getSimpleName(), containerInfo.getName(), containerInfo.getType());
                }
            } catch (Exception e) {
                log.error("Failed to register bean for container: {}", containerInfo.getName(), e);
            }
        });
    }
    
    private Object createBean(ContainerInfo containerInfo) {
        BeanCreator creator = BeanCreatorRegistry.getCreator(containerInfo.getType());
        return creator.createBean(containerInfo);
    }
}