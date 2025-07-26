package com.genius.primavera.proxy.dynamic;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
public class DynamicInvocationHandler implements InvocationHandler {

	private final Object target;
	private final Class<?> targetClass;

	public DynamicInvocationHandler(Object targetObject) {
		this.target = targetObject;
		this.targetClass = targetObject.getClass();
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Method targetMethod = getOverriddenMethod(method);
		return getTransactionalMethod(targetMethod)
				.map(annotation -> handleTransactionalMethod(method, args, annotation))
				.orElseGet(() -> uncheckedInvoke(method, args));
	}

	private Object handleTransactionalMethod(Method method, Object[] args, ProxyPointAnnotation annotation) {
		Object result;
		try {
			result = uncheckedInvoke(method, args);
		} catch (RuntimeException e) {
			throw e;
		}
		return result;
	}

	private Object uncheckedInvoke(Method method, Object[] args) throws RuntimeException {
		try {
			log.info("method : {}, args : {}", method.getName(), Arrays.toString(args));
			Object result = method.invoke(target, args);
			log.info("result : {}", result);
			return result;
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new ProxyInvocationFailedException("Could not invoke method " + method.getName(), e);
		}
	}

	private Optional<ProxyPointAnnotation> getTransactionalMethod(Method method) {
		return Optional.ofNullable(method.getDeclaredAnnotation(ProxyPointAnnotation.class));
	}

	private Method getOverriddenMethod(Method method) throws NoSuchMethodException {
		return targetClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
	}
}
