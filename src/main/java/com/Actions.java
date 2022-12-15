package com;

import javax.swing.*;
import java.io.*;
import java.nio.file.*;

public abstract class Actions {

	private static Notepad notepad = null;
	private static File appDataDir = null;

	public static void setNotepadSingleton(Notepad frame) {
		try {
			Actions.notepad = frame;
			ThemeManager.notepad = frame;
			boolean exists = verifyAppdataDir();

			if (exists)
				loadWorkSpace();

			// Init settings
			AppSettings.instance();

			// Set the theme that is in the settings
			ThemeManager.setTheme(AppSettings.get("theme"));
		} catch (IOException e) {
			assertDialog(false, e.getMessage());
		}
	}

	public static void exit() {
		assert notepad != null : "You need to call setFrame at least once";

		try {
			saveWorkSpace();
			AppSettings.serialize();
		} catch (FileNotFoundException e) {
			assertDialog(false, e.getMessage());
		}
		notepad.getFrame().dispose();
	}

	public static void minimize() {
		notepad.getFrame().setState(JFrame.ICONIFIED);
	}

	public static void assertDialog(boolean b, String message) {
		if (!b)
			JOptionPane.showMessageDialog(notepad.getFrame(), message);
	}

	public static File getAppDataDir() {
		return appDataDir;
	}

	public static Notepad getAppInstance() {
		return notepad;
	}

	/**
	 * Helper functions
	 */
	private static boolean verifyAppdataDir() {
		var appdata = System.getenv("LOCALAPPDATA") + "/shado-notepad";
		appDataDir = new File(appdata);

		if (!appDataDir.exists()) {
			var b = appDataDir.mkdirs();
			assertDialog(b, "Failed to access Local AppData directory. App may not work correctly!");
			if (!b) return false;
		}

		return new File(appDataDir, "workspace.txt").exists();
	}

	private static void loadWorkSpace() throws IOException {
		File workdspace = new File(appDataDir, "workspace.txt");
		String[] filecontent = Files.readString(workdspace.toPath()).split("\n");

		// Parse the file
		int lineCount = 1;
		for (var line : filecontent) {
			// token[0]: Type
			// token[1]: filepath
			try {
				String tokens[] = line.split("\t");

				if (tokens[0].trim().equalsIgnoreCase("local")) {
					notepad.openTab(new File(tokens[1].trim()));
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				throw new IOException("An error occured while parsing workspace file at line " + line + ". " + workdspace.getAbsolutePath());
			}
			lineCount++;
		}
	}

	private static void saveWorkSpace() throws FileNotFoundException {
		File workdspace = new File(appDataDir, "workspace.txt");
		var tabs = notepad.getOpenTabs();

		// Dump workspace
		PrintWriter writer = new PrintWriter(new FileOutputStream(workdspace));
		for (var tab : tabs) {
			// Save the content
			var file = saveTab(tab);
			writer.println("local\t" + file.getAbsolutePath());
		}
		writer.close();
	}

	private static File saveTab(NotepadTab tab) throws FileNotFoundException {
		// Check if it is a file
		if (tab.getFile() != null && tab.getFile().exists()) {
			PrintWriter writer = new PrintWriter(new FileOutputStream(tab.getFile()));
			writer.println(tab.getFilter().getText());
			writer.close();
			return tab.getFile();
		}

		// Otherwise save it to temp
		File file = new File(appDataDir, tab.getTabTitle());
		PrintWriter writer = new PrintWriter(new FileOutputStream(file));
		writer.println(tab.getFilter().getText());
		writer.close();
		return file;
	}

}
