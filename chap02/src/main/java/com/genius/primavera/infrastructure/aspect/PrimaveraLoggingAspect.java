package com.genius.primavera.infrastructure.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class PrimaveraLoggingAspect {

	/*
		execution([접근제한자 패턴] 타입패턴 [타입패턴.]이름패턴 (타입패턴| "..", ...)[throws 예외패턴])
		접근제한자 패턴 : public, private 같은 접근제한자, 생략 가능
		타입패턴 : 리턴 값의 타입 패턴
		타입패턴. : 패키지와 클래스 이름에 따라 패턴. 생략 가능
		이름패턴 : 메서드 이름
		타입패턴| : 파라미터 타입 패턴을 순서대로 넣을 수 있음, 와일드카드를 이용한 파라미터 개수에 상관없는 패턴을 만들 수 있음
		예외패턴 : 예외 이름 패턴
	 */
	@Pointcut("execution(* com.genius.primavera.applicaiton.HelloService.getUserById(..)) && target(object) && args(id) && @annotation(primaveraLogging)")
	private void helloServicePointcut(Object object, long id, PrimaveraLogging primaveraLogging) {
	}

	@Before(value = "helloServicePointcut(object, id, primaveraLogging)", argNames = "object, id, primaveraLogging")
	public void helloServiceBefore(Object object, long id, PrimaveraLogging primaveraLogging) {
		log.info("HelloServiceBefore target : {}, args : {}, type : {}", object, id, primaveraLogging.type());
	}

	@Around(value = "@annotation(primaveraLogging) && args(id)")
	public Object helloServiceAround(ProceedingJoinPoint pjp, long id, PrimaveraLogging primaveraLogging) throws Throwable {
		Object retVal = pjp.proceed();
		log.info("HelloServiceAround, retVal : {}, id : {}, type : {}", retVal, id, primaveraLogging.type());
		return retVal;
	}

	@Around(value = "@annotation(primaveraLogging)")
	public Object aroundHandler(ProceedingJoinPoint pjp, PrimaveraLogging primaveraLogging) throws Throwable {
		log.info("Around Before type : {}", primaveraLogging.type());
		Object retVal = pjp.proceed();
		log.info("Around After retVal : {}, type : {}", retVal, primaveraLogging.type());
		return retVal;
	}

	@Before(value = "@annotation(primaveraLogging)", argNames = "joinPoint, primaveraLogging")
	public void beforeHandler(JoinPoint joinPoint, PrimaveraLogging primaveraLogging) {
		log.info("Before jointPoint : {}, type : {}", joinPoint, primaveraLogging.type());
	}

	@AfterReturning(value = "@annotation(primaveraLogging)", returning = "str")
	public void afterReturningHandler(JoinPoint joinPoint, Object str, PrimaveraLogging primaveraLogging) {
		log.info("AfterReturning : {}, str : {}, type : {}", joinPoint, str, primaveraLogging.type());
	}

	@AfterThrowing(value = "@annotation(primaveraLogging)", throwing = "exception")
	public void afterThrowingHandler(JoinPoint joinPoint, Exception exception, PrimaveraLogging primaveraLogging) throws RuntimeException {
		log.info("AfterThrowing : {}, exception : {}, type : {}", joinPoint, exception.getMessage(), primaveraLogging.type());
	}

	@After(value = "@annotation(primaveraLogging)", argNames = "joinPoint, primaveraLogging")
	public void afterHandler(JoinPoint joinPoint, PrimaveraLogging primaveraLogging) {
		log.info("After joinPoint {}, type : {}", joinPoint, primaveraLogging.type());
	}
}