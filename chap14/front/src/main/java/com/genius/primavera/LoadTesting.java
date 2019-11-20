package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class LoadTesting {

	static AtomicInteger counter = new AtomicInteger(0);
	static String url = "http://localhost:8080/router/users/{userId}/orders";
	static String urlMvc = "http://localhost:8080/mvc/users/{userId}/orders";

	public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
		ExecutorService executorService = Executors.newFixedThreadPool(100);
		RestTemplate restTemplate = new RestTemplate();
		CyclicBarrier barrier = new CyclicBarrier(101);

		StopWatch main = new StopWatch();
		main.start();

		for (int i = 0; i < 100; i++) {
			executorService.submit(() -> {
				int idx = counter.addAndGet(1);
				try {
					barrier.await();
				} catch (InterruptedException | BrokenBarrierException e) {
					e.printStackTrace();
				}
				log.info("Thread {}", idx);
				StopWatch thread = new StopWatch();
				thread.start();
				String result = restTemplate.getForObject(urlMvc, String.class, idx);
				thread.stop();
				log.info("Elapsed : {} {}", idx, thread.getTotalTimeSeconds());
				return (Void) null;
			});
		}
		barrier.await();
		executorService.shutdown();
		executorService.awaitTermination(10, TimeUnit.SECONDS);
		main.stop();
		log.info("Total : {}", main.getTotalTimeSeconds());
	}
}
