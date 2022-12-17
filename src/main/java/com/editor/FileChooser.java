package com.editor;

import com.*;

import javax.swing.*;
import javax.swing.filechooser.*;
import java.io.*;

public class FileChooser {
	public static final String[] TextFiles = new String[]{"txt", "text", "srtf", "js", "json", "yaml", "cpp", "c", "h", "ts", "py", "md", "java", "cs"};
	public static final String[] ImageFiles = new String[]{"png", "jpg", "jpeg", "tif", "gif"};

	private static File lastDir = null;
	private final JFileChooser chooser;

	public FileChooser(String description, String... extensions) {
		chooser = new JFileChooser(lastDir);

		FileNameExtensionFilter filter = new FileNameExtensionFilter(description,
				extensions);
		chooser.setFileFilter(filter);
	}

	public File openDialog() {
		chooser.setDialogTitle("Open file");

		int userSelection = chooser.showOpenDialog(Actions.getAppInstance().getFrame());
		if (userSelection == JFileChooser.APPROVE_OPTION) {
			var file = chooser.getSelectedFile();
			lastDir = file.getParentFile();
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
