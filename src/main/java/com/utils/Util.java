package com.utils;

import com.components.*;

import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;

public abstract class Util {

	private static final Util instance = new Util() {
	};

	private final ExecutorService executors = Executors.newFixedThreadPool(
			Math.max(Runtime.getRuntime().availableProcessors() - 1, 1)
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
			instance.executors.awaitTermination(1, TimeUnit.MINUTES);
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

	public static void progress(String msg, int currentTask, int maxTasks) {
		SwingUtilities.invokeLater(() -> {
			Actions.getAppInstance()
					.showProgress()
					.setProgressMsg(msg)
					.setProgress((int) ((float) currentTask / (float) maxTasks * 100));
		});
	}

	public static String getDate(String pattern) {
		// Create an instance of SimpleDateFormat used for formatting
		// the string representation of date according to the chosen pattern
		DateFormat df = new SimpleDateFormat(pattern);

		// Get the today date using Calendar object.
		Date today = Calendar.getInstance().getTime();
		// Using DateFormat format method we can create a string
		// representation of a date with the defined format.
		String todayAsString = df.format(today);

		// Print the result!
		return todayAsString;
	}

	public static String getDate() {
		return getDate("dd-MM-yyyy HH-mm-ss");
	}

	public static String formatDate(Date date) {
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date today = Calendar.getInstance().getTime();
		String todayAsString = df.format(today);
		return todayAsString;
	}

	public static String getAnIncrementVersion() throws IOException {
		final String path = "out/artifacts/shado_notepad_jar/version";

		// Read the version file
		File file = new File(path);

		// If it doesn't exist, then create it
		if (!file.exists()) {
			PrintWriter writer = new PrintWriter(new FileOutputStream(file));
			writer.print("1.0.0");
			writer.close();
			return "1.0.0";
		}

		String versionRaw = Files.readString(file.toPath()).trim();
		String[] tokens = versionRaw.split("\\.");
		int minorVersion = Integer.parseInt(tokens[tokens.length - 1]);

		PrintWriter writer = new PrintWriter(new FileOutputStream(file));
		for (int i = 0; i < tokens.length - 1; i++) {
			writer.print(tokens[i] + ".");
		}
		writer.print(++minorVersion);
		writer.close();
		return versionRaw;
	}

	public static <K, V> SortedMap<K, V> sortedMap(Object... keysValues) {
		SortedMap<K, V> sortedMap = new TreeMap<>();

		for (int i = 0; i < keysValues.length - 1; i += 2) {
			sortedMap.put((K) keysValues[i], (V) keysValues[i + 1]);
		}

		return sortedMap;
	}
}
