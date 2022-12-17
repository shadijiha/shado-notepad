package com;

import com.filters.*;
import com.utils.*;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class SettingsFrame extends JDialog {
	private JFrame frame;

	public SettingsFrame(JFrame parent) {
		this.frame = parent;
		setTitle("Settings");
		setAlwaysOnTop(true);
		setup();
		setPreferredSize(new Dimension(500, 500));
		pack();
		setLocationRelativeTo(parent);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	private void setup() {

		final List<SettingsTab> tabs = List.of(
				syncTab(),
				themeTab()
		);

		// Create a JTabbedPane and set it to be vertically oriented
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
		for (var tab : tabs) {
			tabbedPane.addTab(tab.label, tab.panel);
		}

		// Add the JTabbedPane to the JFrame
		add(tabbedPane);
	}

	private SettingsTab themeTab() {
		var tab = SettingsTab.of("Theme");
		var panel = tab.panel;

		// Theme drop down
		{
			JLabel lbl = new JLabel("Select theme ");
			panel.add(lbl);

			final JComboBox<String> cb = new JComboBox<String>(
					ThemeManager.getChoices().keySet()
							.stream()
							.sorted().toArray(String[]::new)
			);
			cb.setSelectedItem(AppSettings.get("theme"));
			panel.add(cb);
			cb.addActionListener(e -> {
				String themeStr = cb.getSelectedItem().toString();
				ThemeManager.setTheme(themeStr, this);
			});
		}
		// Font size
		{
			JLabel lbl = new JLabel("Font size");
			panel.add(lbl);

			final JTextField textField = new JTextField(10);
			PlainDocument doc = (PlainDocument) textField.getDocument();
			doc.setDocumentFilter(new IntFilter());

			textField.setText(AppSettings.get("font_size"));
			panel.add(textField);
			textField.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent e) {
				}

				public void focusLost(FocusEvent e) {
					AppSettings.set("font_size", textField.getText());
				}
			});
		}

		return tab;
	}

	private SettingsTab syncTab() {
		var tab = SettingsTab.of("Sync");
		var panel = tab.panel;

		final String[] labels = {"Shado Cloud email", "Shado Cloud password"};
		panel.setLayout(new GridLayout(labels.length + 1, 2, 1, 0));

		JLabel empty = new JLabel();
		JCheckBox enabled = new JCheckBox("Enabled sync");
		enabled.setSelected(AppSettings.getBool("sync_enabled"));
		panel.add(enabled);
		panel.add(empty);
		enabled.addChangeListener(e -> AppSettings.set("sync_enabled", enabled.isSelected() + ""));

		for (int i = 0; i < labels.length; i++) {
			final JLabel l = new JLabel(labels[i], JLabel.TRAILING);
			l.setHorizontalAlignment(JLabel.LEFT);
			panel.add(l);

			final JTextField textField = new JTextField();
			textField.setText(AppSettings.get(labels[i]));
			l.setLabelFor(textField);

			final int finalI = i;
			textField.addFocusListener(new FocusListener() {

				public void focusGained(FocusEvent e) {
				}

				public void focusLost(FocusEvent e) {
					// Check if it has changed
					boolean hasChanged = !AppSettings.get(labels[finalI]).equals(textField.getText());
					if (hasChanged) {
						AppSettings.set(labels[finalI], textField.getText());
					}
				}
			});

			panel.add(textField);
		}

		return tab;
	}

	/**
	 * Helper class
	 */
	private static class SettingsTab {
		public final String label;
		public final JPanel panel;


		private SettingsTab(String label, JPanel panel) {
			this.label = label;
			this.panel = panel;
		}

		private static SettingsTab of(String label, JPanel panel) {
			return new SettingsTab(label, panel);
		}

		private static SettingsTab of(String label) {
			return new SettingsTab(label, new JPanel());
		}
	}

}
