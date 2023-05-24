package com.utils;

import com.google.gson.*;
import com.google.gson.annotations.*;
import com.observer.Observable;
import com.shadocloud.nest.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class AppSettings extends Observable<AppSettings> {

	private static AppSettings instance = new AppSettings();
	private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
	protected static ShadoCloudClient client = null;

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
			synchronized (gson) {

				String json = gson.toJson(instance, instance.getClass());

				File file = getSettingsFileDir();
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
		File file = getSettingsFileDir();
		if (!file.exists())
			return;

		String raw = Files.readString(file.toPath());
		instance = gson.fromJson(raw, instance.getClass());

		// Check if email and password are defined
		var email = get("shado_cloud_email");
		var pass = get("shado_cloud_pass");
		if (!email.isEmpty() && !pass.isEmpty()) {
			client = new ShadoCloudClient(email, pass);
			Util.execute(() -> {
				try {
					client.auth.login();
				} catch (Exception e) {
					Actions.assertDialog(e);
				}
			});
		}
	}

	public static File getSettingsFileDir() {
		return new File(Actions.getAppDataDir(), "settings.json");
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

	public static ShadoCloudClient getClient() {
		return client;
	}

	@Override
	public AppSettings getData() {
		return this;
	}
}
