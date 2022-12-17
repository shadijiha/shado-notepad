package com;

import com.editor.*;
import com.observer.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class NotepadTab extends JPanel implements Observer<AppSettings> {
	private String title;
	private File file;
	private final RichHTMLEditor markdownPane;

	public NotepadTab(String name, String text, Notepad notepad, File file) {
		super(new BorderLayout());
		this.file = file;
		title = name;
		setBackground(Color.WHITE);

		markdownPane = new RichHTMLEditor(notepad, this, text);

		this.add(new JScrollPane(markdownPane), BorderLayout.CENTER);

		// Add the text editor to a new tab in the JTabbedPane
		notepad.tabs.addTab(name, this);
	}

	public String getTabTitle() {
		return title;
	}

	public File getFile() {
		return file;
	}

	public void write(PrintWriter stream) throws Exception {
		markdownPane.serialize(stream);
	}

	public void save() {
		// Show save as
		if (file == null) {
			var chooser = new FileChooser("Custom Shado Rich text format (*.srtf)", "srtf");
			var file = chooser.saveDialog("Save " + title + " as");

			if (file != null) {
				this.file = file;
			} else {
				return;
			}
		}

		// Otherwise save file
		try (PrintWriter writer = new PrintWriter(new FileOutputStream(file))) {
			markdownPane.serialize(writer);
			writer.close();
		} catch (Exception e) {
			Actions.assertDialog(false, e.getMessage());
		}
	}

	@Override
	public void update(AppSettings data) {
		if (AppSettings.hasChanged("font_family") || AppSettings.hasChanged("font_size"))
			markdownPane.setFont(new Font(AppSettings.get("font_family"), Font.PLAIN, (int) AppSettings.getNum("font_size")));
	}
}
