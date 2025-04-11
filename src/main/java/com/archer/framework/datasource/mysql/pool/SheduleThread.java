package com.archer.framework.datasource.mysql.pool;

import java.util.function.Consumer;

public class SheduleThread extends Thread {
	
	private static final String namePrefix = "ArcherConnectPool-";
	private boolean running;
	private long scheduleTime = 2000;
	Consumer<Void> task;
	
	public SheduleThread(Consumer<Void> task) {
		super(namePrefix);
		this.task = task;
	}

	public void exit() {
		running = false;
		super.interrupt();
	}
	
	@Override
	public void start() {
		running = true;
		super.start();
	}
	
	
	
	@Override
	public void run() {
		long start, sleep = scheduleTime;
		while(running) {
			start = System.currentTimeMillis();
			try {
				Thread.sleep(sleep);
			} catch (Exception ignore) {
				sleep = sleep - (System.currentTimeMillis() - start); 
				continue;
			}
			sleep = scheduleTime;
			this.task.accept(null);
		}
	}
}
