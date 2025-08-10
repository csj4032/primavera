package com.genius.primavera.testcontainers.factory;

import com.genius.primavera.testcontainers.config.BaseContainerSpec;
import com.genius.primavera.testcontainers.ContainerCreator;
import com.genius.primavera.testcontainers.ContainerType;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.vault.VaultContainer;

import java.time.Duration;

public class VaultContainerCreator implements ContainerCreator {

    @Override
    public GenericContainer<?> create(BaseContainerSpec spec) {
        String image = spec.getImage() != null ? spec.getImage() : ContainerType.VAULT.getDefaultImage();
        Integer timeout = spec.getStartupTimeout() != null ? spec.getStartupTimeout() : 60;
        
        VaultContainer container = new VaultContainer(DockerImageName.parse(image))
                .withVaultToken("primavera-vault-token");
        
        container.withStartupTimeout(Duration.ofSeconds(timeout));
        
        if (spec.getEnvironment() != null) {
            spec.getEnvironment().forEach(container::withEnv);
        }
        
        return container;
    }

    @Override
    public ContainerType getSupportedType() {
        return ContainerType.VAULT;
    }
}
