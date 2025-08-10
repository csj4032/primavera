package com.genius.primavera.testcontainers.bean.aws;

import com.genius.primavera.testcontainers.config.LocalStackContainerSpec;
import org.testcontainers.containers.localstack.LocalStackContainer;

public abstract class AwsServiceClientFactory {

    public abstract Object createClient(LocalStackContainer container);

    public abstract boolean isAvailable();

    public abstract LocalStackContainerSpec.AwsService getSupportedService();

    public String getBeanName() {
        String serviceName = getSupportedService().name().toLowerCase();
        return serviceName + "Client";
    }

    public boolean isPrimary() {
        return false;
    }

    protected String getEndpointUrl(LocalStackContainer container, LocalStackContainer.Service service) {
        return container.getEndpointOverride(service).toString();
    }

    protected String getAccessKey(LocalStackContainer container) {
        return container.getAccessKey();
    }

    protected String getSecretKey(LocalStackContainer container) {
        return container.getSecretKey();
    }

    protected String getRegion(LocalStackContainer container) {
        return container.getRegion();
    }

    protected boolean isClassAvailable(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    protected boolean areClassesAvailable(String... classNames) {
        for (String className : classNames) {
            if (!isClassAvailable(className)) {
                return false;
            }
        }
        return true;
    }
}