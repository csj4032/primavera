package com.genius.primavera.testContainer.autoConfigure;

import com.genius.primavera.testContainer.PrimaveraTestcontainersContextInitializer;
import com.genius.primavera.testContainer.PrimaveraTestcontainersListener;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({PrimaveraTestcontainersContextInitializer.class, PrimaveraTestcontainersListener.class})
@Conditional(EnablePrimaveraTestcontainersPresentCondition.class)
public class PrimaveraTestcontainersAutoConfiguration {
}
