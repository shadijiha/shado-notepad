package com.utils;

import com.*;
import com.shadocloud.nest.*;

import javax.swing.*;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import static com.utils.Util.*;

public abstract class Actions {

	private static Notepad notepad = null;
	static File appDataDir = new File(System.getenv("LOCALAPPDATA") + "/shado-notepad");

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

		progress("Shutting down...", 1, 1);
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
		progress("Loading Local workspace file...", 0, 2);

		final var local = Workspace.parseWorkspaceFile(
				Files.readString(workdspace.toPath()), workdspace.getAbsolutePath());

		progress("Applying Local workspace file...", 1, 2);
		Workspace.apply(notepad, local);
		progress("", 2, 2);

		//}

		// Load Cloud
		// get the cloud file text
		if (AppSettings.getBool("sync_enabled")) {
			Util.execute(() -> {
				try {
					final int TOTAL_TASKS = 6;

					final var client = AppSettings.client;
					final var urlPath = Workspace.CloudPathFile;

					progress("Starting Sync, Logging in to cloud...", 0, TOTAL_TASKS);
					client.auth.login();

					if (client.files.exists(urlPath)) {

						progress("Getting cloud workspace...", 1, TOTAL_TASKS);

						var is = client.files.get(urlPath);
						String content = new BufferedReader(
								new InputStreamReader(is, StandardCharsets.UTF_8))
								.lines()
								.collect(Collectors.joining("\n"));

						progress("Parsing cloud workspace...", 2, TOTAL_TASKS);

						Workspace cloud = Workspace.parseWorkspaceFile(content, Workspace.CloudPathFile);
						if (!Workspace.isNewer(cloud, local))
							return;

						// If workspace was synced, then sync the files too
						// Get all files in cloud notepad dir
						progress("Pulling files...", 3, TOTAL_TASKS);
						var cloudFiles = client.directories.list("auto/shado-notepad");

						var tabsToMerge = Arrays.stream(cloudFiles)
								.filter(fileInfo -> cloud.contains(fileInfo.name))
								.map(file -> {
									try {
										progress("Pulling " + file.name + "...", 4, TOTAL_TASKS);

										var input = client.files.get(Workspace.CloudPath + file.name);
										String rawContent = new BufferedReader(
												new InputStreamReader(input, StandardCharsets.UTF_8))
												.lines()
												.collect(Collectors.joining("\n"));

										// Save it locally
										progress("Saving " + file.name + " locally...", 5, TOTAL_TASKS);
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

						progress("Merging local and cloud tabs...", 6, TOTAL_TASKS);
						notepad.mergeTabs(tabsToMerge);
					}

					notepad.hideProgress();
				} catch (Exception e) {
					Actions.assertDialog("Unable to load Shado Cloud workspace file", e);
				}
			});
		} else
			notepad.hideProgress();
	}

	private static void saveWorkSpace() {
		Util.execute(() -> {
			File workdspace = getWorkspaceFileDir();
			var tabs = notepad.getOpenTabs();
			var currentTab = notepad.getSelectedTab();

			// construct the file workspace
			final int TOTAL_TASKS = tabs.size() + 1 + 1;
			progress("Building workspace file...", 1, TOTAL_TASKS);

			StringBuilder builder = new StringBuilder();
			builder.append("date\t" + new Date().getTime()).append("\n");

			{
				int i = 1;
				for (var tab : tabs) {
					// Save the content
					if (tab.hasChanged()) {
						progress("Saving " + tab.getTabTitle(), ++i, TOTAL_TASKS);
						var file = saveTab(tab);
					}

					if (tab.getFile() != null)
						builder.append("local\t")
								.append(tab.getFile().getAbsolutePath())
								.append("\n");
				}
			}

			builder.append("open\t")
					.append(notepad.getOpenTabs().indexOf(notepad.getSelectedTab()))
					.append("\n");

			// Write it locally
			try (PrintWriter writer = new PrintWriter(new FileOutputStream(workdspace))) {
				progress("Saving workspace file locally...", TOTAL_TASKS, TOTAL_TASKS);
				writer.println(builder.toString());
				writer.close();
			} catch (Exception e) {
				Actions.assertDialog(e);
			}

			// Now save it to cloud
			if (AppSettings.getBool("sync_enabled")) {
				final int TOTAL_TASKS_SYNC = notepad.getOpenTabs().size() * 2 + 1 + 1;

				ShadoCloudClient client = AppSettings.client;
				try {
					progress("Syncing, Logging in to cloud...", 1, TOTAL_TASKS_SYNC);
					client.auth.login();

					// Check if file exists
					// Then upload the workspace file
					progress("Uploading workspace file...", 2, TOTAL_TASKS_SYNC);
					if (!client.files.exists(Workspace.CloudPathFile)) {
						client.directories.newDirectory(Workspace.CloudPath);
						client.files.newFile(Workspace.CloudPathFile);
					}
					client.files.save(Workspace.CloudPathFile, builder.toString(), false);

					// Save it to AppData then
					// Upload it files to cloud
					int i = 2;
					for (var tab : notepad.getOpenTabs()) {
						if (!tab.hasChanged())
							continue;

						progress("Saving " + tab.getTabTitle() + " locally...", ++i, TOTAL_TASKS_SYNC);

						File file = new File(appDataDir, tab.getFileName());
						PrintWriter writer = new PrintWriter(new FileOutputStream(file));
						tab.write(writer);
						writer.close();

						// Then upload the file to cloud
						progress("Uploading " + tab.getTabTitle() + " to cloud...", ++i, TOTAL_TASKS_SYNC);

						var destName = Workspace.CloudPath + tab.getFileName();
						if (!client.files.exists(destName))
							client.files.newFile(destName);

						client.files.save(destName, tab.getContent(), false);
					}

					notepad.hideProgress();
				} catch (Exception e) {
					Actions.assertDialog("Unable to sync workspace file ", e);
				}
			} else {
				notepad.hideProgress();
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
		//throw new RuntimeException(ex);
	}

	public static void assertDialog(Exception e) {
		assertDialog("", e);
	}
}
