package com.genius.primavera.lightweight.framework;

import com.genius.primavera.lightweight.annotations.PrimaveraAutowired;
import com.genius.primavera.lightweight.annotations.PrimaveraBean;
import com.genius.primavera.lightweight.annotations.PrimaveraComponent;
import com.genius.primavera.lightweight.annotations.PrimaveraConfiguration;
import com.genius.primavera.lightweight.annotations.PrimaveraPostConstruct;
import com.genius.primavera.lightweight.annotations.PrimaveraPreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Primavera 경량 프레임워크의 핵심 ApplicationContext
 * Spring의 ApplicationContext와 유사한 역할을 합니다.
 * 
 * Bean 등록, 관리, 의존성 주입을 담당합니다.
 */
@Slf4j
public class PrimaveraApplicationContext {
    
    private final Map<String, Object> beanContainer = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> beanDefinitions = new ConcurrentHashMap<>();
    private final Set<String> basePackages = new HashSet<>();
    private final List<Object> beansWithPreDestroy = new ArrayList<>();
    
    public PrimaveraApplicationContext(String... basePackages) {
        this.basePackages.addAll(Arrays.asList(basePackages));
        initialize();
    }
    
    /**
     * 컨텍스트 초기화
     * 1. 컴포넌트 스캔
     * 2. Bean 등록
     * 3. 의존성 주입
     */
    private void initialize() {
        log.info("🌸 Primavera ApplicationContext 초기화 시작...");
        
        try {
            // 1단계: 컴포넌트 스캔
            scanComponents();
            
            // 2단계: Bean 인스턴스 생성
            createBeanInstances();
            
            // 3단계: 의존성 주입
            injectDependencies();
            
            // 4단계: @PostConstruct 메서드 호출
            invokePostConstructMethods();
            
            // 5단계: 종료 훅 등록
            registerShutdownHook();
            
            log.info("🌸 Primavera ApplicationContext 초기화 완료! 등록된 Bean 수: {}", beanContainer.size());
            logRegisteredBeans();
            
        } catch (Exception e) {
            log.error("ApplicationContext 초기화 중 오류 발생", e);
            throw new RuntimeException("ApplicationContext 초기화 실패", e);
        }
    }
    
    /**
     * 지정된 패키지에서 @PrimaveraComponent, @PrimaveraConfiguration 어노테이션이 
     * 붙은 클래스들을 스캔합니다.
     */
    private void scanComponents() throws Exception {
        for (String basePackage : basePackages) {
            log.debug("패키지 스캔 시작: {}", basePackage);
            
            String packagePath = basePackage.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL resource = classLoader.getResource(packagePath);
            
            if (resource != null) {
                File packageDir = new File(resource.toURI());
                scanDirectory(packageDir, basePackage);
            }
        }
    }
    
    /**
     * 디렉토리를 재귀적으로 스캔하여 클래스 파일을 찾습니다.
     */
    private void scanDirectory(File directory, String packageName) throws Exception {
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }
        
        File[] files = directory.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().replace(".class", "");
                try {
                    Class<?> clazz = Class.forName(className);
                    
                    if (clazz.isAnnotationPresent(PrimaveraComponent.class) || 
                        clazz.isAnnotationPresent(PrimaveraConfiguration.class)) {
                        registerBeanDefinition(clazz);
                    }
                } catch (ClassNotFoundException e) {
                    log.warn("클래스를 찾을 수 없습니다: {}", className);
                }
            }
        }
    }
    
    /**
     * Bean 정의를 등록합니다.
     */
    private void registerBeanDefinition(Class<?> clazz) {
        String beanName = getBeanName(clazz);
        beanDefinitions.put(beanName, clazz);
        log.debug("Bean 정의 등록: {} -> {}", beanName, clazz.getSimpleName());
    }
    
    /**
     * Bean 이름을 결정합니다.
     */
    private String getBeanName(Class<?> clazz) {
        // @PrimaveraComponent 어노테이션에서 값 확인
        if (clazz.isAnnotationPresent(PrimaveraComponent.class)) {
            PrimaveraComponent annotation = clazz.getAnnotation(PrimaveraComponent.class);
            if (!annotation.value().isEmpty()) {
                return annotation.value();
            }
        }
        
        // @PrimaveraConfiguration 어노테이션에서 값 확인
        if (clazz.isAnnotationPresent(PrimaveraConfiguration.class)) {
            PrimaveraConfiguration annotation = clazz.getAnnotation(PrimaveraConfiguration.class);
            if (!annotation.value().isEmpty()) {
                return annotation.value();
            }
        }
        
        // 기본값: 클래스명의 첫 글자를 소문자로
        String className = clazz.getSimpleName();
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }
    
    /**
     * Bean 인스턴스를 생성합니다.
     */
    private void createBeanInstances() throws Exception {
        for (Map.Entry<String, Class<?>> entry : beanDefinitions.entrySet()) {
            String beanName = entry.getKey();
            Class<?> beanClass = entry.getValue();
            
            if (!beanContainer.containsKey(beanName)) {
                Object beanInstance = createBeanInstance(beanClass);
                beanContainer.put(beanName, beanInstance);
                log.debug("Bean 인스턴스 생성: {} = {}", beanName, beanInstance.getClass().getSimpleName());
                
                // @PrimaveraConfiguration 클래스의 @PrimaveraBean 메서드 처리
                if (beanClass.isAnnotationPresent(PrimaveraConfiguration.class)) {
                    processBeanMethods(beanInstance);
                }
            }
        }
    }
    
    /**
     * Bean 인스턴스를 생성합니다.
     */
    private Object createBeanInstance(Class<?> beanClass) throws Exception {
        return beanClass.getDeclaredConstructor().newInstance();
    }
    
    /**
     * @PrimaveraConfiguration 클래스의 @PrimaveraBean 메서드를 처리합니다.
     */
    private void processBeanMethods(Object configInstance) throws Exception {
        Class<?> configClass = configInstance.getClass();
        Method[] methods = configClass.getDeclaredMethods();
        
        for (Method method : methods) {
            if (method.isAnnotationPresent(PrimaveraBean.class)) {
                PrimaveraBean beanAnnotation = method.getAnnotation(PrimaveraBean.class);
                String beanName = beanAnnotation.value().isEmpty() ? method.getName() : beanAnnotation.value();
                
                method.setAccessible(true);
                Object beanInstance = method.invoke(configInstance);
                beanContainer.put(beanName, beanInstance);
                log.debug("@PrimaveraBean 메서드로 Bean 생성: {} = {}", beanName, beanInstance.getClass().getSimpleName());
            }
        }
    }
    
    /**
     * 모든 Bean에 의존성을 주입합니다.
     */
    private void injectDependencies() throws Exception {
        for (Object bean : beanContainer.values()) {
            injectFieldDependencies(bean);
        }
    }
    
    /**
     * 필드 의존성을 주입합니다.
     */
    private void injectFieldDependencies(Object bean) throws Exception {
        Class<?> beanClass = bean.getClass();
        Field[] fields = beanClass.getDeclaredFields();
        
        for (Field field : fields) {
            if (field.isAnnotationPresent(PrimaveraAutowired.class)) {
                PrimaveraAutowired autowired = field.getAnnotation(PrimaveraAutowired.class);
                Object dependency = getBean(field.getType());
                
                if (dependency != null) {
                    field.setAccessible(true);
                    field.set(bean, dependency);
                    log.debug("의존성 주입: {}.{} = {}", 
                            beanClass.getSimpleName(), 
                            field.getName(), 
                            dependency.getClass().getSimpleName());
                } else if (autowired.required()) {
                    throw new RuntimeException(String.format(
                            "필수 의존성을 찾을 수 없습니다: %s.%s (타입: %s)", 
                            beanClass.getSimpleName(), 
                            field.getName(), 
                            field.getType().getSimpleName()));
                }
            }
        }
    }
    
    /**
     * Bean을 이름으로 조회합니다.
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(String beanName) {
        Object bean = beanContainer.get(beanName);
        if (bean == null) {
            throw new RuntimeException("Bean을 찾을 수 없습니다: " + beanName);
        }
        return (T) bean;
    }
    
    /**
     * Bean을 타입으로 조회합니다.
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> beanType) {
        for (Object bean : beanContainer.values()) {
            if (beanType.isAssignableFrom(bean.getClass())) {
                return (T) bean;
            }
        }
        return null;
    }
    
    /**
     * 모든 Bean 이름을 반환합니다.
     */
    public Set<String> getBeanNames() {
        return new HashSet<>(beanContainer.keySet());
    }
    
    /**
     * Bean이 존재하는지 확인합니다.
     */
    public boolean containsBean(String beanName) {
        return beanContainer.containsKey(beanName);
    }
    
    /**
     * 모든 Bean의 @PostConstruct 메서드를 호출합니다.
     */
    private void invokePostConstructMethods() throws Exception {
        log.debug("🌸 @PostConstruct 메서드 호출 시작...");
        
        for (Object bean : beanContainer.values()) {
            invokePostConstructMethod(bean);
        }
        
        log.debug("🌸 @PostConstruct 메서드 호출 완료");
    }
    
    /**
     * 특정 Bean의 @PostConstruct 메서드를 호출합니다.
     */
    private void invokePostConstructMethod(Object bean) throws Exception {
        Class<?> beanClass = bean.getClass();
        Method[] methods = beanClass.getDeclaredMethods();
        
        for (Method method : methods) {
            if (method.isAnnotationPresent(PrimaveraPostConstruct.class)) {
                // @PostConstruct 메서드 검증
                validateLifecycleMethod(method, PrimaveraPostConstruct.class.getSimpleName());
                
                method.setAccessible(true);
                method.invoke(bean);
                log.debug("@PostConstruct 메서드 실행: {}.{}", 
                        beanClass.getSimpleName(), method.getName());
            }
        }
        
        // @PreDestroy 메서드가 있는 Bean을 목록에 추가
        if (hasPreDestroyMethod(beanClass)) {
            beansWithPreDestroy.add(bean);
        }
    }
    
    /**
     * 라이프사이클 메서드의 유효성을 검증합니다.
     */
    private void validateLifecycleMethod(Method method, String annotationName) {
        // 매개변수가 없어야 함
        if (method.getParameterCount() > 0) {
            throw new RuntimeException(String.format(
                    "%s 메서드는 매개변수가 없어야 합니다: %s.%s", 
                    annotationName, method.getDeclaringClass().getSimpleName(), method.getName()));
        }
        
        // static이 아니어야 함
        if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
            throw new RuntimeException(String.format(
                    "%s 메서드는 static이 아니어야 합니다: %s.%s", 
                    annotationName, method.getDeclaringClass().getSimpleName(), method.getName()));
        }
        
        // final이 아니어야 함
        if (java.lang.reflect.Modifier.isFinal(method.getModifiers())) {
            throw new RuntimeException(String.format(
                    "%s 메서드는 final이 아니어야 합니다: %s.%s", 
                    annotationName, method.getDeclaringClass().getSimpleName(), method.getName()));
        }
    }
    
    /**
     * 클래스에 @PreDestroy 메서드가 있는지 확인합니다.
     */
    private boolean hasPreDestroyMethod(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(PrimaveraPreDestroy.class)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * JVM 종료 시 실행될 Shutdown Hook을 등록합니다.
     */
    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("🌸 Primavera ApplicationContext 종료 중...");
            invokePreDestroyMethods();
            log.info("🌸 Primavera ApplicationContext 종료 완료");
        }, "PrimaveraShutdownHook"));
        
        log.debug("🌸 Shutdown Hook 등록 완료");
    }
    
    /**
     * 모든 Bean의 @PreDestroy 메서드를 호출합니다.
     */
    private void invokePreDestroyMethods() {
        log.debug("🌸 @PreDestroy 메서드 호출 시작...");
        
        // 역순으로 처리 (의존성 순서의 반대)
        for (int i = beansWithPreDestroy.size() - 1; i >= 0; i--) {
            Object bean = beansWithPreDestroy.get(i);
            try {
                invokePreDestroyMethod(bean);
            } catch (Exception e) {
                log.error("@PreDestroy 메서드 실행 중 오류 발생: {}", bean.getClass().getSimpleName(), e);
            }
        }
        
        log.debug("🌸 @PreDestroy 메서드 호출 완료");
    }
    
    /**
     * 특정 Bean의 @PreDestroy 메서드를 호출합니다.
     */
    private void invokePreDestroyMethod(Object bean) throws Exception {
        Class<?> beanClass = bean.getClass();
        Method[] methods = beanClass.getDeclaredMethods();
        
        for (Method method : methods) {
            if (method.isAnnotationPresent(PrimaveraPreDestroy.class)) {
                // @PreDestroy 메서드 검증
                validateLifecycleMethod(method, PrimaveraPreDestroy.class.getSimpleName());
                
                method.setAccessible(true);
                method.invoke(bean);
                log.debug("@PreDestroy 메서드 실행: {}.{}", 
                        beanClass.getSimpleName(), method.getName());
            }
        }
    }
    
    /**
     * ApplicationContext를 종료합니다.
     */
    public void close() {
        log.info("🌸 ApplicationContext 수동 종료 요청");
        invokePreDestroyMethods();
        beanContainer.clear();
        beanDefinitions.clear();
        beansWithPreDestroy.clear();
        log.info("🌸 ApplicationContext 수동 종료 완료");
    }
    
    /**
     * 등록된 Bean들을 로그로 출력합니다.
     */
    private void logRegisteredBeans() {
        log.info("=== 등록된 Bean 목록 ===");
        beanContainer.forEach((name, bean) -> 
            log.info("  • {} -> {}", name, bean.getClass().getSimpleName()));
        log.info("========================");
    }
}