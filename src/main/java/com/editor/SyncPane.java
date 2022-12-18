package com.editor;

import javax.swing.*;

public class SyncPane {
	private JTextField shadoEmailField;
	private JTextField shadoPassField;
	private JCheckBox syncCheckbox;
	private JPanel panel;

	public JPanel getPanel()	{
		return panel;
	}

	public JTextField getShadoEmailField() {
		return shadoEmailField;
	}

	public JTextField getShadoPassField() {
		return shadoPassField;
	}

	public JCheckBox getSyncCheckbox() {
		return syncCheckbox;
	}
}
