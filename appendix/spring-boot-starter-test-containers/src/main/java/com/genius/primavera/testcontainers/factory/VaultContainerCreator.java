package com.genius.primavera.testcontainers.factory;

import com.genius.primavera.testcontainers.ContainerConfiguration;
import com.genius.primavera.testcontainers.ContainerCreator;
import com.genius.primavera.testcontainers.ContainerType;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.vault.VaultContainer;

public class VaultContainerCreator implements ContainerCreator {

    @Override
    public GenericContainer<?> create(ContainerConfiguration.ContainerSpec spec) {
        VaultContainer container = new VaultContainer(DockerImageName.parse(spec.getImageOrDefault(ContainerType.VAULT)))
                .withVaultToken(spec.getToken());
        ContainerConfigurationHelper.configureContainer(container, spec);
        return container;
    }

    @Override
    public ContainerType getSupportedType() {
        return ContainerType.VAULT;
    }
}
