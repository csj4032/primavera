package com.genius.primavera.commend;

public class StopWatch {

	private long startTime;
	private long endTime;

	public StopWatch() {
		startTime = System.currentTimeMillis();
	}

	private Time elapsed() {
		endTime = System.currentTimeMillis();
		return new Time(endTime - startTime);
	}

	public static void main(String[] args) {
		StopWatch stopWatch = new StopWatch();
		greeting();
		Time time = stopWatch.elapsed();
		System.out.print("경과 : " + time.getSecond());
	}

	private static void greeting() {
		for (int i = 0; i < 10000; i++) {
			System.out.print("@@@@@@@@@@@@@@@@@@@");
		}
		System.out.println("");
	}
}

class Time {
	private long elapsed;

	public Time(long elapsed) {
		this.elapsed = elapsed;
	}

	public long getSecond() {
		return elapsed / 1000;
	}
}