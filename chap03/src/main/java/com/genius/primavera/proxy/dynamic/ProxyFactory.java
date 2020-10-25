package com.genius.primavera.proxy.dynamic;

import com.genius.primavera.PrimaveraApplication;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class ProxyFactory {

	private List<PrimaveraProxy> beanRegistry;

	public ProxyFactory(Package packageToLookup) {
		log.info("package : {}", packageToLookup.getName());
		Reflections reflections = new Reflections(packageToLookup.getName());
		log.info("reflections : {}", reflections);
		Set<Class<?>> primaveraAnnotationClasses = reflections.getTypesAnnotatedWith(ProxyAnnotation.class);
		log.info("transactionalServiceClasses : {}", primaveraAnnotationClasses.size());
		List<?> beans = instantiateBeans(primaveraAnnotationClasses);
		beanRegistry = createProxies(beans);
	}

	private PrimaveraProxy createProxy(Object bean) {
		InvocationHandler handler = new DynamicInvocationHandler(bean);
		Object proxyObj = Proxy.newProxyInstance(PrimaveraApplication.class.getClassLoader(), bean.getClass().getInterfaces(), handler);
		return new PrimaveraProxy(Arrays.asList(bean.getClass().getInterfaces()), proxyObj);
	}

	private List<PrimaveraProxy> createProxies(List<?> beans) {
		return beans.stream().map(this::createProxy).collect(Collectors.toList());
	}

	private Object instantiateClass(Class<?> aClass) {
		try {
			return aClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("Could not instantiate class " + aClass);
		}
	}

	private List<?> instantiateBeans(Set<Class<?>> annotated) {
		return annotated.stream().map(this::instantiateClass).collect(Collectors.toList());
	}

	public <T> T getBean(Class<T> clazz) {
		Object proxy = beanRegistry.stream()
				.filter(p -> p.hasInterface(clazz))
				.findFirst()
				.map(PrimaveraProxy::getJdkProxy)
				.orElseThrow(() -> new RuntimeException("No Bean found for class " + clazz));
		return clazz.cast(proxy);
	}
}