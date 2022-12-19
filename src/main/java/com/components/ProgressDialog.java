package com.components;

import com.utils.*;

import javax.swing.*;
import java.awt.event.*;

public class ProgressDialog extends JDialog {
	private JPanel contentPane;
	//private JButton buttonOK;
	//private JButton buttonCancel;
	private JProgressBar progressBar;
	private JLabel messageField;

	public ProgressDialog() {
		setContentPane(contentPane);
		setModal(true);
//		getRootPane().setDefaultButton(buttonOK);
//
//		buttonOK.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				onOK();
//			}
//		});
//
//		buttonCancel.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				onCancel();
//			}
//		});

		// call onCancel() when cross is clicked
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});

		// call onCancel() on ESCAPE
		contentPane.registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

		pack();
		setLocationRelativeTo(Actions.getAppInstance().getFrame());
		setVisible(true);
	}

	public void setPercent(int percent) {
		progressBar.setValue(percent);
	}

	public void setLabel(String message) {
		messageField.setText(message);
	}

	public void update(int percent, String message) {
		if (message != null)
			setLabel(message);
		setPercent(percent);
	}

	public void update(float current, float total, String message) {
		if (message != null)
			setLabel(message);
		setPercent((int) ((current / total) * 100));
	}

	public void dispose() {
		super.dispose();
	}

	private void onOK() {
		// add your code here
		super.dispose();
	}

	private void onCancel() {
		// add your code here if necessary
		super.dispose();
	}
}
