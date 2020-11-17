package com.genius.primavera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import nz.net.ultraq.thymeleaf.LayoutDialect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//@SpringBootApplication
public class PrimaveraApplication {

	public static void main(String[] args) {
		//SpringApplication.run(PrimaveraApplication.class, args);

		//java.util.ArrayList;
		ArrayList<String> arrayList = new ArrayList<>();
		arrayList.add("A");

		//java.util.Arrays.ArrayList
		List<String> arrayListArray = Arrays.asList("A", "B", "C");
		// 멀쩡해보이지만...구현이 안됨
		arrayListArray.add("D");
	}
}