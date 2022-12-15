package com;

import com.filters.*;
import com.observer.*;

import javax.swing.*;
import javax.swing.text.*;
import javax.xml.parsers.*;
import java.awt.*;
import java.io.*;

public class NotepadTab extends JPanel implements Observer<AppSettings> {
	private String title;
	private MarkdownDocumentFilter filter;
	private File file;
	private JTextPane markdownPane;

	public NotepadTab(String name, String text, JTabbedPane tabs, File file) {
		super(new BorderLayout());
		this.file = file;
		title = name;
		setBackground(Color.WHITE);

		// Create a new MarkdownPane
		var iosFont = new Font(AppSettings.get("font_family"), Font.PLAIN, (int) AppSettings.getNum("font_size"));
		markdownPane = new JTextPane();
		markdownPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		markdownPane.setFont(iosFont);
		markdownPane.setFocusable(true);

		try {
			filter = new MarkdownDocumentFilter(markdownPane);
			filter.setup(text);
			((AbstractDocument) markdownPane.getDocument()).setDocumentFilter(filter);
			this.add(new JScrollPane(markdownPane), BorderLayout.CENTER);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		// Add the text editor to a new tab in the JTabbedPane
		tabs.addTab(name, this);
	}

	public String getTabTitle() {
		return title;
	}

	public File getFile() {
		return file;
	}

	public MarkdownDocumentFilter getFilter() {
		return filter;
	}

	public void save() {
		// Show save as
		if (file == null) {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Save " + title + " as");

			int userSelection = chooser.showSaveDialog(Actions.getAppInstance().getFrame());
			if (userSelection == JFileChooser.APPROVE_OPTION) {
				this.file = chooser.getSelectedFile();
			} else {
				return;
			}
		}

		// Otherwise save file
		try (PrintWriter writer = new PrintWriter(new FileOutputStream(file))) {
			writer.println(filter.getText());
			writer.close();
		} catch (FileNotFoundException e) {
			Actions.assertDialog(false, e.getMessage());
		}
	}

	@Override
	public void update(AppSettings data) {
		if (AppSettings.hasChanged("font_family") || AppSettings.hasChanged("font_size"))
			markdownPane.setFont(new Font(AppSettings.get("font_family"), Font.PLAIN, (int) AppSettings.getNum("font_size")));
	}
}
