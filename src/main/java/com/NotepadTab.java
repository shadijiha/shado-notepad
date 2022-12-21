package com;

import com.components.editor.*;
import com.swing_ext.*;
import com.utils.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class NotepadTab extends JPanel {
	private String title;
	private File file;
	private AbstractEditor markdownPane;
	private final JScrollPane scrollPane;
	private final Notepad notepad;

	protected Date lastSave;
	private boolean hasChanged;

	public NotepadTab(String name, String text, Notepad notepad, File file) {
		super(new BorderLayout());
		this.notepad = notepad;
		this.file = file;
		title = name;
		setBackground(Color.WHITE);

		markdownPane = AbstractEditor.factory(notepad, this, text, file);
		markdownPane.onChange(e -> {
			if (hasChanged) return;
			hasChanged = true;
			notepad.tabs.setTitleAt(notepad.getOpenTabs().indexOf(this), "*" + title);
			if (notepad.progressUIUpdater != null) notepad.progressUIUpdater.accept(this);
		});
		AppSettings.instance().addObserver(markdownPane);

		scrollPane = new JScrollPane(markdownPane);
		this.add(scrollPane, BorderLayout.CENTER);

		// Add the text editor to a new tab in the JTabbedPane
		notepad.tabs.addTab(name, this);
	}

	public String getTabTitle() {
		return title;
	}

	public File getFile() {
		return file;
	}

	public String getContent() {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		markdownPane.serialize(out);
		out.close();
		return writer.toString();
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
			hasChanged = false;

			lastSave = new Date();
			notepad.tabs.setTitleAt(notepad.getOpenTabs().indexOf(this), title);
			notepad.progressUIUpdater.accept(this);
		} catch (Exception e) {
			Actions.assertDialog(false, e.getMessage());
		}
	}

	public void setFile(File file) {
		this.file = file;
	}

	public boolean hasChanged() {
		return hasChanged;
	}

	/**
	 * If it is a file, then returns the name of the file. Otherwise, return the title
	 *
	 * @return
	 */
	public String getFileName() {
		if (file != null)
			return file.getName();
		return title;
	}
}
