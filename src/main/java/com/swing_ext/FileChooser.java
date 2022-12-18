package com.swing_ext;

import com.utils.*;

import javax.swing.*;
import javax.swing.filechooser.*;
import java.io.*;

public class FileChooser {
	public static final String[] TextFiles = new String[]{"txt", "text", "srtf", "js", "json", "yaml", "cpp", "c", "h", "ts", "py", "md", "java", "cs"};
	public static final String[] ImageFiles = new String[]{"png", "jpg", "jpeg", "tif", "gif"};

	private static File lastDir = null;
	private final JFileChooser chooser;

	public FileChooser(String description, String... extensions) {
		if (lastDir == null) {
			String chooser_dir = AppSettings.get("chooser_dir");
			var file = new File(chooser_dir);
			lastDir = chooser_dir.isEmpty() || !file.exists() ? null : file;
		}

		chooser = new JFileChooser(lastDir);

		if (extensions != null && extensions.length > 0) {
			FileNameExtensionFilter filter = new FileNameExtensionFilter(description,
					extensions);
			chooser.setFileFilter(filter);
		}
	}

	public void dirOnly() {
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	}

	public File openDialog() {
		chooser.setDialogTitle("Open file");

		int userSelection = chooser.showOpenDialog(Actions.getAppInstance().getFrame());
		if (userSelection == JFileChooser.APPROVE_OPTION) {
			var file = chooser.getSelectedFile();
			lastDir = file.getParentFile();
			AppSettings.set("chooser_dir", lastDir.getAbsolutePath());
			return file;
		}

		return null;
	}

	public File saveDialog(String title) {
		chooser.setDialogTitle(title);

		int userSelection = chooser.showSaveDialog(Actions.getAppInstance().getFrame());
		if (userSelection == JFileChooser.APPROVE_OPTION) {
			var file = chooser.getSelectedFile();
			lastDir = file.getParentFile();
			return file;
		}
		return null;
	}

	public File saveDialog() {
		return saveDialog("Save file as");
	}

	public JFileChooser getNative() {
		return chooser;
	}
}
