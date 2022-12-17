package com.utils;

import com.google.gson.*;
import com.google.gson.annotations.*;
import com.observer.Observable;

import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.text.*;
import java.util.*;

public class AppSettings extends Observable<AppSettings> {

	private static AppSettings instance = new AppSettings();
	private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

	@Expose
	private Map<String, String> data = new HashMap<>();
	@Expose
	private final Map<String, String> defaultValues = Map.of(
			"theme", "Flat dark",
			"sync_enabled", "false",
			"font_family", "Helvetica Neue",
			"font_size", "24"
	);

	private Set<String> changedFields = new HashSet<>();
	public boolean hasInit;

	public static AppSettings instance() {
		if (!instance.hasInit) {
			try {
				deserialize();
			} catch (IOException e) {
				Actions.assertDialog(false, "Unable to load settings!\n\n" + e.getMessage());
			}
			instance.hasInit = true;
		}

		return instance;
	}

	public static void set(String key, String data) {
		instance.data.put(key, data);
		serialize();

		// Call all observers
		instance.changedFields.add(key);
		instance.updateAll();
		instance.changedFields.clear();
	}

	public static String get(String key) {
		var val = instance.data.get(key);
		if (val == null) {
			val = instance.defaultValues.get(key);
			if (val == null) {
				System.out.println("Unable to get settings key for " + key);
				val = "";
			}
		}
		return val;
	}

	public static boolean getBool(String key) {
		var val = get(key);
		return Boolean.parseBoolean(val);
	}

	public static double getNum(String key) {
		return Double.parseDouble(get(key));
	}

	public static void serialize() {
		Util.execute(() -> {
			synchronized (gson){

				String json = gson.toJson(instance, instance.getClass());

				File file = new File(Actions.getAppDataDir(), "settings.json");
				try (PrintWriter writer = new PrintWriter(new FileOutputStream(file))) {
					writer.println(json);
					writer.close();
				} catch (FileNotFoundException e) {
					Actions.assertDialog(false, "Unable to save settings\n\n" + e.getMessage());
				}
			}
		});
	}

	public static void deserialize() throws IOException {
		File file = new File(Actions.getAppDataDir(), "settings.json");
		if (!file.exists())
			return;

		String raw = Files.readString(file.toPath());
		instance = gson.fromJson(raw, instance.getClass());
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
		return getDate("MM-dd-yyyy HH-mm-ss");
	}

	/**
	 * Checks if a settings field has changed
	 *
	 * @param key The settings field key
	 * @return
	 */
	public static boolean hasChanged(String key) {
		return instance.changedFields.contains(key);
	}

	@Override
	public AppSettings getData() {
		return this;
	}
}
