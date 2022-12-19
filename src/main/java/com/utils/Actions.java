package com.utils;

import com.*;
import com.components.*;
import com.shadocloud.nest.*;

import javax.swing.*;
import java.io.*;
import java.net.*;
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
			assertDialog(false, e.getMessage());
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
		if (verifyAppdataDir()) {
			var local = Workspace.parseWorkspaceFile(Files.readString(workdspace.toPath()), workdspace.getAbsolutePath());
			Workspace.apply(notepad, local);
		}

		// Load Cloud
		// get the cloud file text
		if (AppSettings.getBool("sync_enabled")) {
			Util.execute(() -> {
				try {
					final var client = AppSettings.client;
					final var urlPath = encode(Workspace.CloudPathFile);
					client.auth.login();
					if (client.files.exists(urlPath)) {
						var is = client.files.get(urlPath);
						String content = new BufferedReader(
								new InputStreamReader(is, StandardCharsets.UTF_8))
								.lines()
								.collect(Collectors.joining("\n"));

						Workspace cloud = Workspace.parseWorkspaceFile(content, Workspace.CloudPathFile);
						boolean hasApplied = Workspace.apply(notepad, cloud);

						// If workspace was synced, then sync the files too
						// Get all files in cloud notepad dir
						var cloudFiles = client.directories.list(encode("auto/shado-notepad"));
						for (var file : cloudFiles) {
							//Actions.appDataDir
							// Only open the cloud file if it is open in the local workspace?
							if (Workspace.getCurrentWorkspace().containsLocal(file.name)) {
								var input = client.files.get(encode(Workspace.CloudPath + file.name));
								String rawContent = new BufferedReader(
										new InputStreamReader(input, StandardCharsets.UTF_8))
										.lines()
										.collect(Collectors.joining("\n"));

								notepad.openTab("[Cloud] " + file.name, rawContent, null);
							}
						}
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
					//dialog.update(0, totalTasks, "Logging in to " + AppSettings.get("shado_cloud_email") + " 's Shado cloud account...");
					client.auth.login();


					// Check if file exists
					// Then upload the workspace file
					//dialog.update(1, totalTasks, "Creating workspace file...");
					if (!client.files.exists(Workspace.CloudPathFile)) {
						client.directories.newDirectory(Workspace.CloudPath);
						client.files.newFile(Workspace.CloudPathFile);
					}
					client.files.save(Workspace.CloudPathFile, builder.toString(), false);

					// Upload opened files to cloud
					//dialog.update(2, totalTasks, "Uploading files...");
					int i = 0;
					for (var tab : notepad.getOpenTabs()) {

						// TODO: For now ignore files marked with [Cloud]. Change this
						if (tab.getTabTitle().startsWith("[Cloud]"))
							continue;

						final var file = tab.getFile();
						final var fileName = Workspace.CloudPath + file.getName();

						//dialog.update(i++, totalTasks, "Uploading " + file.getName() + " ...");

						if (!client.files.exists(fileName))
							client.files.newFile(fileName);
						client.files.save(fileName, Files.readString(file.toPath()), false);
					}

				} catch (Exception e) {
					Actions.assertDialog(false, "Unable to sync workspace file " + e.getMessage());
				} finally {
					//dialog.dispose();
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
	}

	public static void assertDialog(Exception e) {
		assertDialog("", e);
	}

	private static String encode(String s) {
		return URLEncoder.encode(s, Charset.defaultCharset());
	}
}
