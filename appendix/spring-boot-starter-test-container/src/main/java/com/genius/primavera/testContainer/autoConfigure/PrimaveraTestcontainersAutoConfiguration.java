package com.genius.primavera.testContainer.autoConfigure;

import com.genius.primavera.testContainer.PrimaveraTestcontainersContextInitializer;
import com.genius.primavera.testContainer.PrimaveraTestcontainersListener;
import com.genius.primavera.testContainer.PrimaveraTestcontainersProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({PrimaveraTestcontainersContextInitializer.class, PrimaveraTestcontainersListener.class})
@Conditional(EnablePrimaveraTestcontainersPresentCondition.class)
@EnableConfigurationProperties(PrimaveraTestcontainersProperties.class)
public class PrimaveraTestcontainersAutoConfiguration {
}
