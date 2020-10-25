package com.genius.primavera;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import javax.sql.DataSource;

@SpringBootApplication
@MapperScan("com.genius.primavera.domain.mapper")
public class PrimaveraApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrimaveraApplication.class, args);
	}
}