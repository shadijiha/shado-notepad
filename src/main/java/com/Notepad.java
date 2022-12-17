package com;

// Import the necessary classes

import com.formdev.flatlaf.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.*;


public class Notepad {

	private List<NotepadTab> openTabs = new ArrayList<>();
	protected JTabbedPane tabs;
	private Titlebar titleBar;
	private JFrame frame;

	public static void main(String[] args) {
		Notepad notepad = new Notepad();
		//notepad.openTab("tab1", "# This is a heading\n"
		//		+ "This is a paragraph with *italic* and **bold** text.\n"
		//		+ "Here is a list:\n"
		//		+ "* Item 1\n"
		//		+ "* Item 2\n"
		//		+ "    * Nested item 1\n"
		//		+ "    * Nested item 2\n"
		//		+ "Here is a [link](https://github.com/johanneskropf/MarkdownPane) to the MarkdownPane library.\n"
		//		+ "```" +
		//		"public static int x = 10;" +
		//		"```", null);
		//notepad.openTab("Queen", "Cait my Queen <3 <3 💓", null);
	}

	public Notepad() {

		// Set the Darcula LAF as the default look and feel for the application
		try {
			//UIManager.setLookAndFeel("com.bulenkov.darcula.DarculaLaf");
			//UIManager.setLookAndFeel(new MaterialLookAndFeel(new MaterialOceanicTheme()));
			UIManager.setLookAndFeel(new FlatDarkLaf());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Create a new JFrame for the notepad
		frame = new JFrame("Shado notepad");
		frame.setUndecorated(true);
		frame.setPreferredSize(new Dimension(1280, 720));

		// Add your code here to create the notepad user interface,
		// such as text editor, menus, and buttons
		tabs = new DraggableTabbedPane();
		// Add a ChangeListener to the JTabbedPane to detect when the user switches tabs
		tabs.addChangeListener(e -> {
			// Get the index of the selected tab
			int index = tabs.getSelectedIndex();
			// Get the component for the selected tab
			Component component = tabs.getComponentAt(index);
			// Update the notepad UI based on the selected tab
			updateUI(component);
		});
		tabs.addMouseListener(new NotepadTabContextMenu.PopClickListener(this));

		// Add the JTabbedPane to the JFrame
		frame.add(tabs);

		/**
		 * Add Title bar and menus
		 */
		// Add the title bar to the top of the JWindow
		titleBar = new Titlebar(this);
		frame.add(titleBar, BorderLayout.NORTH);

		Actions.setNotepadSingleton(this);

		// Show the notepad window
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public void openTab(String name, String text, File file) {
		var tab = new NotepadTab(name, text, this, file);
		openTabs.add(tab);
		AppSettings.instance().addObserver(tab);
	}

	public void openTab(String name, String text) {
		openTab(name, text, null);
	}

	public void openTab(File file) {
		try {
			var content = Files.readString(file.toPath());
			openTab(file.getName(), content, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void save() {
		// Get current tab
		int index = tabs.getSelectedIndex();
		NotepadTab tab = openTabs.get(index);
		tab.save();
	}

	private void updateUI(Component component) {
		// Get the text editor for the selected tab
		NotepadTab tab = (NotepadTab) component;

		// Update the text editor UI
		//textArea.setFont(new Font("Monaco", Font.PLAIN, 16));
		//textArea.setLineWrap(true);
		//textArea.setWrapStyleWord(true);

		// Add your code here to update other UI elements of the notepad,
		// such as menus and buttons, based on the selected tab
		titleBar.setTitle(tab.getTabTitle() + " - Shado Notepad");
	}

	public JFrame getFrame() {
		return frame;
	}

	public List<NotepadTab> getOpenTabs() {
		return openTabs;
	}

	public JTabbedPane getTabs() {
		return tabs;
	}
}