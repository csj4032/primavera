package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toList;
import static org.jooq.lambda.Seq.zip;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HelloController {

	private final HelloService helloService;

	@GetMapping("/hello/{name}")
	public String hello(@PathVariable(value = "name") String name) {
		return "Hello World " + helloService.getGreetingName(name);
	}

	@GetMapping("/hello/params")
	public Seq<Person> params(Params params, List<Person> persons) {
		System.out.println(persons);
		return zip(params.names, params.ages, params.enumTypes).map(Person::of);
	}
}