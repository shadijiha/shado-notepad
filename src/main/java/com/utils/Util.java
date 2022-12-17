package com.utils;

import java.util.concurrent.*;

public abstract class Util {

	private static final Util instance = new Util() {
	};

	private ExecutorService executors = Executors.newFixedThreadPool(
			Math.max(Runtime.getRuntime().availableProcessors() / 2, 2)
	);

	private Util()	{}

	/**
	 * Execute task on another thread
	 * @param e
	 */
	public static void execute(Runnable e)	{
		instance.executors.execute(e);
	}

	public static <T> Future<T> execute(Callable<T> task)	{
		return instance.executors.submit(task);
	}

	public static void shutdown()	{
		instance.executors.shutdown();
	}
}
