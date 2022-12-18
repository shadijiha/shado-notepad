package com.components.settings;

import javax.swing.*;

public class GeneralPane {
	private JPanel panel;
	private JTextField workspaceField;
	private JTextField settingsField;
	private JTextField autoSaveField;
	private JButton browseBtn;


	public JPanel getPanel() {
		return panel;
	}

	public JTextField getWorkspaceField() {
		return workspaceField;
	}

	public JTextField getSettingsField() {
		return settingsField;
	}

	public JTextField getAutoSaveField() {
		return autoSaveField;
	}

	public JButton getBrowseBtn() {
		return browseBtn;
	}
}
