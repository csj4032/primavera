package com.genius.primavera.application;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
public class HelloAspect {

	private Map<Integer, Integer> indexCounts = new HashMap<>();

	@Pointcut("execution(* com.genius.primavera.application.HelloService.getArticle(int)) && args(id)")
	public void getArticlePointcut(int id) {
	}

	@Pointcut("execution(* com.genius.primavera.application.HelloService.getArticles())")
	public void getArticlesPointcut() {
	}

	@Before("getArticlePointcut(id)")
	public void helloBefore(int id) {
		log.info("helloBefore : " + id);
		indexCounts.putIfAbsent(id, 0);
		indexCounts.computeIfPresent(id, (k, v) -> v + 1);
	}

	@Around("getArticlesPointcut()")
	public Object helloAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		log.info("helloAround : " + proceedingJoinPoint.toString());
		return proceedingJoinPoint.proceed();
	}

	@After("getArticlesPointcut()")
	public void helloAfter(JoinPoint joinPoint) {
		log.info("helloAfter : " + joinPoint.toString());
	}

	@AfterReturning("getArticlesPointcut()")
	public void helloAfterReturning() {
		log.info("helloAfterReturning : ");
	}

	@AfterThrowing("getArticlesPointcut()")
	public void helloAfterThrowing() {
		log.info("helloAfterThrowing : ");
	}

	public int getArticleCount(int index) {
		return indexCounts.computeIfAbsent(index, k -> 0);
	}
}