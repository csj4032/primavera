package com.genius.primavera;

import com.genius.primavera.lifecycle.InterfaceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.GenericXmlApplicationContext;

@Slf4j
@SpringBootApplication
//Application.yml 우선순위
//@EnableAspectJAutoProxy(proxyTargetClass = true)
@ImportResource("classpath:configuration.xml")
public class PrimaveraApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(PrimaveraApplication.class).build().run(args);
	}
}