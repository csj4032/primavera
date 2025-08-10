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

    private void initialize() {
        log.info(" Primavera ApplicationContext connection test...");

        try {

            scanComponents();

            createBeanInstances();

            injectDependencies();

            invokePostConstructMethods();

            registerShutdownHook();

            log.info(" Primavera ApplicationContext connection completed! created successfully Bean should: {}", beanContainer.size());
            logRegisteredBeans();

        } catch (Exception e) {
            log.error("ApplicationContext connection failed with error", e);
            throw new RuntimeException("ApplicationContext connection failure", e);
        }
    }

    private void scanComponents() throws Exception {
        for (String basePackage : basePackages) {
            log.debug("connection test: {}", basePackage);

            String packagePath = basePackage.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL resource = classLoader.getResource(packagePath);

            if (resource != null) {
                File packageDir = new File(resource.toURI());
                scanDirectory(packageDir, basePackage);
            }
        }
    }

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
                    log.warn("file test should file: {}", className);
                }
            }
        }
    }

    private void registerBeanDefinition(Class<?> clazz) {
        String beanName = getBeanName(clazz);
        beanDefinitions.put(beanName, clazz);
        log.debug("Bean test registration: {} -> {}", beanName, clazz.getSimpleName());
    }

    private String getBeanName(Class<?> clazz) {

        if (clazz.isAnnotationPresent(PrimaveraComponent.class)) {
            PrimaveraComponent annotation = clazz.getAnnotation(PrimaveraComponent.class);
            if (!annotation.value().isEmpty()) {
                return annotation.value();
            }
        }

        if (clazz.isAnnotationPresent(PrimaveraConfiguration.class)) {
            PrimaveraConfiguration annotation = clazz.getAnnotation(PrimaveraConfiguration.class);
            if (!annotation.value().isEmpty()) {
                return annotation.value();
            }
        }

        String className = clazz.getSimpleName();
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    private void createBeanInstances() throws Exception {
        for (Map.Entry<String, Class<?>> entry : beanDefinitions.entrySet()) {
            String beanName = entry.getKey();
            Class<?> beanClass = entry.getValue();

            if (!beanContainer.containsKey(beanName)) {
                Object beanInstance = createBeanInstance(beanClass);
                beanContainer.put(beanName, beanInstance);
                log.debug("Bean file creation: {} = {}", beanName, beanInstance.getClass().getSimpleName());

                if (beanClass.isAnnotationPresent(PrimaveraConfiguration.class)) {
                    processBeanMethods(beanInstance);
                }
            }
        }
    }

    private Object createBeanInstance(Class<?> beanClass) throws Exception {
        return beanClass.getDeclaredConstructor().newInstance();
    }

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
                log.debug("@PrimaveraBean file Bean creation: {} = {}", beanName, beanInstance.getClass().getSimpleName());
            }
        }
    }

    private void injectDependencies() throws Exception {
        for (Object bean : beanContainer.values()) {
            injectFieldDependencies(bean);
        }
    }

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
                    log.debug("dependency test: {}.{} = {}",
                            beanClass.getSimpleName(),
                            field.getName(),
                            dependency.getClass().getSimpleName());
                } else if (autowired.required()) {
                    throw new RuntimeException(String.format(
                            "should dependencytest should file: %s.%s (test: %s)",
                            beanClass.getSimpleName(),
                            field.getName(),
                            field.getType().getSimpleName()));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(String beanName) {
        Object bean = beanContainer.get(beanName);
        if (bean == null) {
            throw new RuntimeException("Beantest should file: " + beanName);
        }
        return (T) bean;
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> beanType) {
        for (Object bean : beanContainer.values()) {
            if (beanType.isAssignableFrom(bean.getClass())) {
                return (T) bean;
            }
        }
        return null;
    }

    public Set<String> getBeanNames() {
        return new HashSet<>(beanContainer.keySet());
    }

    public boolean containsBean(String beanName) {
        return beanContainer.containsKey(beanName);
    }

    private void invokePostConstructMethods() throws Exception {
        log.debug(" @PostConstruct connection called test...");

        for (Object bean : beanContainer.values()) {
            invokePostConstructMethod(bean);
        }

        log.debug(" @PostConstruct connection called completed");
    }

    private void invokePostConstructMethod(Object bean) throws Exception {
        Class<?> beanClass = bean.getClass();
        Method[] methods = beanClass.getDeclaredMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(PrimaveraPostConstruct.class)) {

                validateLifecycleMethod(method, PrimaveraPostConstruct.class.getSimpleName());

                method.setAccessible(true);
                method.invoke(bean);
                log.debug("@PostConstruct connection execution: {}.{}",
                        beanClass.getSimpleName(), method.getName());
            }
        }

        if (hasPreDestroyMethod(beanClass)) {
            beansWithPreDestroy.add(bean);
        }
    }

    private void validateLifecycleMethod(Method method, String annotationName) {

        if (method.getParameterCount() > 0) {
            throw new RuntimeException(String.format("%s connection should connection: %s.%s", annotationName, method.getDeclaringClass().getSimpleName(), method.getName()));
        }

        if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
            throw new RuntimeException(String.format("%s connection staticshould file connection: %s.%s", annotationName, method.getDeclaringClass().getSimpleName(), method.getName()));
        }

        if (java.lang.reflect.Modifier.isFinal(method.getModifiers())) {
            throw new RuntimeException(String.format("%s connection finalshould file connection: %s.%s", annotationName, method.getDeclaringClass().getSimpleName(), method.getName()));
        }
    }

    private boolean hasPreDestroyMethod(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(PrimaveraPreDestroy.class)) {
                return true;
            }
        }
        return false;
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info(" Primavera ApplicationContext test should...");
            invokePreDestroyMethods();
            log.info(" Primavera ApplicationContext test completed");
        }, "PrimaveraShutdownHook"));

        log.debug(" Shutdown Hook registration completed");
    }

    private void invokePreDestroyMethods() {
        log.debug(" @PreDestroy connection called test...");

        for (int i = beansWithPreDestroy.size() - 1; i >= 0; i--) {
            Object bean = beansWithPreDestroy.get(i);
            try {
                invokePreDestroyMethod(bean);
            } catch (Exception e) {
                log.error("@PreDestroy connection execution failed with error: {}", bean.getClass().getSimpleName(), e);
            }
        }

        log.debug(" @PreDestroy connection called completed");
    }

    private void invokePreDestroyMethod(Object bean) throws Exception {
        Class<?> beanClass = bean.getClass();
        Method[] methods = beanClass.getDeclaredMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(PrimaveraPreDestroy.class)) {

                validateLifecycleMethod(method, PrimaveraPreDestroy.class.getSimpleName());

                method.setAccessible(true);
                method.invoke(bean);
                log.debug("@PreDestroy connection execution: {}.{}",
                        beanClass.getSimpleName(), method.getName());
            }
        }
    }

    public void close() {
        log.info(" ApplicationContext should test");
        invokePreDestroyMethods();
        beanContainer.clear();
        beanDefinitions.clear();
        beansWithPreDestroy.clear();
        log.info(" ApplicationContext should test completed");
    }

    private void logRegisteredBeans() {
        log.info("=== created successfully Bean test ===");
        beanContainer.forEach((name, bean) ->
                log.info("  • {} -> {}", name, bean.getClass().getSimpleName()));
        log.info("========================");
    }
}