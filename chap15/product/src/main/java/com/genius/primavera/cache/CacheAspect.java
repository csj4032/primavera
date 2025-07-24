package com.genius.primavera.cache;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.join;

@Slf4j
@Aspect
@Component
public class CacheAspect {

	private static final String KEY_DELIMITER = "::";

	@Around("@annotation(cacheGet)")
	public Object cacheGet(ProceedingJoinPoint joinPoint, CacheGet cacheGet) throws Throwable {
		Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
		CacheKeyGenerator cacheGenerator = cacheGet.keyPrefixType().getCacheGenerator();
		String base = getBase(joinPoint.getTarget(), method.getName());
		String suffix = getSuffix(method.getParameterAnnotations(), joinPoint.getArgs());
		if (!suffix.isEmpty()) {
			String key = cacheGenerator.generator(join(KEY_DELIMITER, base, suffix));
			joinPoint.getArgs()[2] = key;
			return joinPoint.proceed(joinPoint.getArgs());
		}
		return joinPoint.proceed();
	}

	private String getSuffix(Annotation[][] annotations, Object[] arguments) {
		Map<Long, String> cacheKeyMap = new LinkedHashMap<>();
		for (int i = 0; i < annotations.length; i++) {
			CacheKey cacheKey = getCacheKey(annotations[i]);
			if (cacheKey != null) cacheKeyMap.put(cacheKey.order(), arguments[i].toString());
		}
		return joiningSuffixes(cacheKeyMap);
	}

	private CacheKey getCacheKey(Annotation[] annotations) {
		for (int j = 0; j < annotations.length; j++) {
			Annotation annotation = annotations[j];
			if (annotation instanceof CacheKey) {
				return (CacheKey) annotation;
			}
		}
		return null;
	}

	private String getBase(Object target, String methodName) {
		return join(KEY_DELIMITER, target.getClass().getCanonicalName(), methodName);
	}

	private String joiningSuffixes(Map<Long, String> cacheKeyMap) {
		return cacheKeyMap.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).map(e -> e.getValue()).collect(Collectors.joining(KEY_DELIMITER));
	}
}