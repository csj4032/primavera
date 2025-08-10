package com.genius.primavera.testcontainers.factory;

import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.*;
import com.genius.primavera.testcontainers.strategy.ContainerTypeStrategyRegistry;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 컨테이너 타입별 기본 Spec 인스턴스 생성을 담당하는 팩토리 클래스
 * 리플렉션과 캐싱을 사용하여 성능 최적화
 */
@Slf4j
public class SpecFactory {
    
    private static final ConcurrentMap<ContainerType, Constructor<? extends BaseContainerSpec>> CONSTRUCTOR_CACHE = 
        new ConcurrentHashMap<>();
    
    /**
     * 주어진 컨테이너 타입에 해당하는 기본 Spec 인스턴스 생성
     * 
     * @param type 컨테이너 타입
     * @return 생성된 Spec 인스턴스
     */
    public static BaseContainerSpec createDefaultSpec(ContainerType type) {
        try {
            Constructor<? extends BaseContainerSpec> constructor = getConstructor(type);
            BaseContainerSpec spec = constructor.newInstance();
            applyDefaultValues(spec, type);
            
            log.debug("Created default spec for type {}: {}", type, spec.getClass().getSimpleName());
            return spec;
            
        } catch (Exception e) {
            log.error("Failed to create default spec for type {}", type, e);
            return createFallbackSpec(type);
        }
    }
    
    private static Constructor<? extends BaseContainerSpec> getConstructor(ContainerType type) {
        return CONSTRUCTOR_CACHE.computeIfAbsent(type, t -> {
            try {
                Class<? extends BaseContainerSpec> specClass = t.getSpecClass();
                Constructor<? extends BaseContainerSpec> constructor = specClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor;
            } catch (Exception e) {
                throw new RuntimeException("Failed to get constructor for " + t, e);
            }
        });
    }
    
    private static void applyDefaultValues(BaseContainerSpec spec, ContainerType type) {
        // 기본 이미지 설정
        if (spec.getImage() == null) {
            spec.setImage(type.getDefaultImage());
        }
        
        // 타입별 특화 기본값 설정
        applyTypeSpecificDefaults(spec, type);
    }
    
    private static void applyTypeSpecificDefaults(BaseContainerSpec spec, ContainerType type) {
        ContainerTypeStrategyRegistry.getStrategy(type)
            .ifPresent(strategy -> strategy.applyDefaults(spec));
    }
    
    // Removed individual type-specific methods - now handled by Strategy pattern
    
    private static BaseContainerSpec createFallbackSpec(ContainerType type) {
        log.warn("Using fallback BaseContainerSpec for type {}", type);
        BaseContainerSpec fallback = new BaseContainerSpec() {
            // Anonymous concrete implementation for fallback
        };
        fallback.setImage(type.getDefaultImage());
        return fallback;
    }
}