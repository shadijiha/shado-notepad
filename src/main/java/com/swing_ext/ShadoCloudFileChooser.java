package com.swing_ext;

import com.utils.*;

import javax.swing.*;
import java.io.*;

public class ShadoCloudFileChooser extends FileChooser {

	public ShadoCloudFileChooser(String description, String... extensions) {
		super(description, extensions);
		chooser = new JFileChooser(new ShadoFile(""), new ShadoCloudFileView());
		chooser.setCurrentDirectory(new ShadoFile(""));

		chooser.addPropertyChangeListener(e -> {
			System.out.printf("%s\t%s --> %s\n", e.getPropertyName(), e.getOldValue(), e.getNewValue());
			chooser.rescanCurrentDirectory();
		});
	}

	@Override
	public File openDialog() {
		chooser.setDialogTitle("Open file");

		int userSelection = chooser.showOpenDialog(Actions.getAppInstance().getFrame());
		if (userSelection == JFileChooser.APPROVE_OPTION) {
			var file = chooser.getSelectedFile();
			return file;
		}
		return null;
	}
}
