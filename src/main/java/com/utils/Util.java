package com.utils;

import java.io.*;
import java.util.concurrent.*;

public abstract class Util {

	private static final Util instance = new Util() {
	};

	private final ExecutorService executors = Executors.newFixedThreadPool(
			Math.max(Runtime.getRuntime().availableProcessors() / 2, 2)
	);

	private Util() {
	}

	/**
	 * Execute task on another thread
	 *
	 * @param e
	 */
	public static void execute(Runnable e) {
		instance.executors.execute(e);
	}

	public static <T> Future<T> execute(Callable<T> task) {
		return instance.executors.submit(task);
	}

	public static void shutdown() {
		try {
			instance.executors.shutdown();
			instance.executors.awaitTermination(2, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			Actions.assertDialog(e);
		}
	}

	public static String getExtension(File file) {
		// convert the file name into string
		String fileName = file.toString();

		int index = fileName.lastIndexOf('.');
		if (index > 0) {
			return fileName.substring(index + 1);
		}
		return "";
	}
}
