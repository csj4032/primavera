package com.genius.primavera.testContainer.autoConfigure;

import com.genius.primavera.testContainer.EnablePrimaveraTestcontainers;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class EnablePrimaveraTestcontainersPresentCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        try {
            Class.forName(EnablePrimaveraTestcontainers.class.getName());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
