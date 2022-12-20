package com.utils;

import com.*;
import com.shadocloud.nest.*;

import javax.swing.*;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public abstract class Actions {

	private static Notepad notepad = null;
	static File appDataDir = new File(System.getenv("LOCALAPPDATA") + "/shado-notepad");
	;
	private static Date workspaceDate = null;

	/**
	 * CAlled on app start
	 *
	 * @param frame
	 */
	public static void setNotepadSingleton(Notepad frame) {
		try {
			Actions.notepad = frame;
			ThemeManager.notepad = frame;

			// Init settings
			AppSettings.instance();

			Workspace.LocalPath = new File(Actions.appDataDir, "workspace.txt");
			loadWorkSpace();

			// Set the theme that is in the settings
			ThemeManager.setTheme(AppSettings.get("theme"));

			AppSettings.instance().addObserver(new ShadoCloudFieldsObserver());
		} catch (Exception e) {
			assertDialog(e);
		}
	}

	public static void exit() {
		assert notepad != null : "You need to call setFrame at least once";

		try {
			saveWorkSpace();
			AppSettings.serialize();

			// Logout from Shado cloud
			Util.execute(() -> {
				try {
					AppSettings.client.auth.logout();
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		} catch (Exception e) {
			assertDialog(false, e.getMessage());
		}
		Util.shutdown();
		notepad.getFrame().dispose();
	}

	public static void minimize() {
		notepad.getFrame().setState(JFrame.ICONIFIED);
	}

	public static void assertDialog(boolean b, String message) {
		if (!b) {
			System.out.println(message);
			SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(notepad.getFrame(), message));
		}
	}

	public static File getAppDataDir() {
		return appDataDir;
	}

	public static Notepad getAppInstance() {
		return notepad;
	}

	public static File getWorkspaceFileDir() {
		return Workspace.LocalPath;
	}

	/**
	 * Helper functions
	 */
	private static boolean verifyAppdataDir() {
		if (!appDataDir.exists()) {
			var b = appDataDir.mkdirs();
			assertDialog(b, "Failed to access Local AppData directory. App may not work correctly!");
			if (!b) return false;
		}

		return getWorkspaceFileDir().exists();
	}

	private static void loadWorkSpace() throws Exception {
		File workdspace = getWorkspaceFileDir();

		// Apply the local workspace
		//if (verifyAppdataDir()) {
		final var local = Workspace.parseWorkspaceFile(
				Files.readString(workdspace.toPath()), workdspace.getAbsolutePath());
		Workspace.apply(notepad, local);
		//}

		// Load Cloud
		// get the cloud file text
		if (AppSettings.getBool("sync_enabled")) {
			Util.execute(() -> {
				try {
					final var client = AppSettings.client;
					final var urlPath = Workspace.CloudPathFile;
					client.auth.login();

					if (client.files.exists(urlPath)) {
						var is = client.files.get(urlPath);
						String content = new BufferedReader(
								new InputStreamReader(is, StandardCharsets.UTF_8))
								.lines()
								.collect(Collectors.joining("\n"));

						Workspace cloud = Workspace.parseWorkspaceFile(content, Workspace.CloudPathFile);
						if (!Workspace.isNewer(cloud, local))
							return;

						// If workspace was synced, then sync the files too
						// Get all files in cloud notepad dir
						var cloudFiles = client.directories.list("auto/shado-notepad");

						var tabsToMerge = Arrays.stream(cloudFiles)
								.filter(fileInfo -> cloud.contains(fileInfo.name))
								.map(file -> {
									try {
										var input = client.files.get(Workspace.CloudPath + file.name);
										String rawContent = new BufferedReader(
												new InputStreamReader(input, StandardCharsets.UTF_8))
												.lines()
												.collect(Collectors.joining("\n"));

										// Save it locally
										File saved = new File(Actions.appDataDir, file.name);
										PrintWriter writer = new PrintWriter(new FileOutputStream(saved));
										writer.println(rawContent);
										writer.close();

										return saved;
									} catch (Exception e) {
										Actions.assertDialog(e);
									}
									return null;
								})
								.filter(Objects::nonNull)
								.toList();

						notepad.mergeTabs(tabsToMerge);
					}
				} catch (Exception e) {
					Actions.assertDialog("Unable to load Shado Cloud workspace file", e);
				}
			});
		}
	}

	private static void saveWorkSpace() {
		Util.execute(() -> {
			File workdspace = getWorkspaceFileDir();
			var tabs = notepad.getOpenTabs();
			var currentTab = notepad.getSelectedTab();

			// construct the file workspace
			StringBuilder builder = new StringBuilder();
			builder.append("date\t" + new Date().getTime()).append("\n");
			for (var tab : tabs) {
				// Save the content
				var file = saveTab(tab);
				builder.append("local\t" + file.getAbsolutePath()).append("\n");
			}

			builder.append("open\t" + notepad.getOpenTabs().indexOf(notepad.getSelectedTab())).append("\n");

			// Write it locally
			try (PrintWriter writer = new PrintWriter(new FileOutputStream(workdspace))) {
				writer.println(builder.toString());
				writer.close();
			} catch (Exception e) {
				Actions.assertDialog(e);
			}

			// Now save it to cloud
			if (AppSettings.getBool("sync_enabled")) {
				//ProgressDialog dialog = new ProgressDialog();

				final int totalTasks = notepad.getOpenTabs().size() + 1 + 1;

				ShadoCloudClient client = AppSettings.client;
				try {
					client.auth.login();

					// Check if file exists
					// Then upload the workspace file
					if (!client.files.exists(Workspace.CloudPathFile)) {
						client.directories.newDirectory(Workspace.CloudPath);
						client.files.newFile(Workspace.CloudPathFile);
					}
					client.files.save(Workspace.CloudPathFile, builder.toString(), false);

					// Save it to AppData then
					// Upload it files to cloud
					for (var tab : notepad.getOpenTabs()) {
						File file = new File(appDataDir, tab.getFileName());
						PrintWriter writer = new PrintWriter(new FileOutputStream(file));
						tab.write(writer);
						writer.close();

						// Then upload the file to cloud
						var destName = Workspace.CloudPath + tab.getFileName();
						if (client.files.exists(destName))
							client.files.delete(destName);

						client.files.newFile(destName);
						client.files.save(destName, tab.getContent(), false);
					}

				} catch (Exception e) {
					Actions.assertDialog("Unable to sync workspace file ", e);
				}
			}
		});
	}

	private static File saveTab(NotepadTab tab) {
		// Check if it is a file
		try {
			if (tab.getFile() != null && tab.getFile().exists()) {
				PrintWriter writer = new PrintWriter(new FileOutputStream(tab.getFile()));
				tab.write(writer);
				writer.close();
				return tab.getFile();
			}

			// Otherwise save it to temp
			File file = new File(appDataDir, tab.getTabTitle());
			PrintWriter writer = new PrintWriter(new FileOutputStream(file));
			tab.write(writer);
			tab.setFile(file);
			writer.close();
			return file;
		} catch (Exception e) {
			Actions.assertDialog(e);

		}
		return null;
	}

	public static void assertDialog(String customMessage, Exception ex) {
		Actions.assertDialog(false, customMessage + "\n" + ex.getMessage());
		throw new RuntimeException(ex);
	}

	public static void assertDialog(Exception e) {
		assertDialog("", e);
	}
}
