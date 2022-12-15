package com;

import javax.swing.*;

public class Toolbar {
	private JToolBar toolbar;

	public Toolbar(JFrame frame) {
		// Create a new JToolBar
		toolbar = new JToolBar();

// Set the floating property of the toolbar to false
		toolbar.setFloatable(false);

		// Create a new JButton for the bold button
		JButton boldButton = new JButton("Bold");

// Add the bold button to the toolbar
		toolbar.add(boldButton);

// Create a new JButton for the italic button
		JButton italicButton = new JButton("Italic");

// Add the italic button to the toolbar
		toolbar.add(italicButton);

// Create a new JButton for the underline button
		JButton underlineButton = new JButton("Underline");

// Add the underline button to the toolbar
		toolbar.add(underlineButton);

// Create a new JButton for the color button
		JButton colorButton = new JButton("Color");

// Add the color button to the toolbar
		toolbar.add(colorButton);

// Add the toolbar to the JWindow
		frame.add(toolbar);

		setVisible(false);
	}

	public void setVisible(boolean b) {
		toolbar.setVisible(b);
	}

	public JToolBar getNative() {
		return toolbar;
	}
}
