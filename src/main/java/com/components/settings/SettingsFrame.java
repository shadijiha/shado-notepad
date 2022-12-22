package com.components.settings;

import com.filters.*;
import com.swing_ext.*;
import com.utils.*;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class SettingsFrame extends JDialog {
	private JFrame frame;
	private JTabbedPane tabbedPane;

	public SettingsFrame(JFrame parent) {
		this.frame = parent;
		setTitle("Settings");
		//setAlwaysOnTop(true);
		setup();
		setPreferredSize(new Dimension(500, 500));
		pack();
		setLocationRelativeTo(parent);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	public SettingsFrame(JFrame parent, String selectedTab) {
		this(parent);

		int index = SettingsTab.SettingsTabsIdx.get(selectedTab.toLowerCase());
		tabbedPane.setSelectedIndex(index);
	}

	private void setup() {
		// Clear some metadata
		SettingsTab.SettingsTabsIdx.clear();
		SettingsTab.CurrentPos = 0;

		final List<SettingsTab> tabs = List.of(
				generalTab(),
				syncTab(),
				themeTab()
		);
		
		// Create a JTabbedPane and set it to be vertically oriented
		tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
		for (var tab : tabs) {
			tabbedPane.addTab(tab.label, tab.panel);
		}

		// Add the JTabbedPane to the JFrame
		add(tabbedPane);
	}

	private SettingsTab generalTab() {
		SettingsTab tab = SettingsTab.of("General");
		var general = new GeneralPane();
		tab.panel = general.getPanel();

		general.getWorkspaceField().setText(Actions.getWorkspaceFileDir().getAbsolutePath());
		general.getSettingsField().setText(AppSettings.getSettingsFileDir().getAbsolutePath());
		general.getAutoSaveField().setText(Actions.getAppDataDir().getAbsolutePath());

		general.getBrowseBtn().addActionListener(e -> {
			FileChooser chooser = new FileChooser("Folder");
			chooser.dirOnly();
			var file = chooser.openDialog();
			System.out.println(file);
		});

		return tab;
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
		var syncPane = new SyncPane();
		tab.panel = syncPane.getPanel();        // Check the designer

		syncPane.getShadoEmailField().setText(AppSettings.get("shado_cloud_email"));
		syncPane.getShadoPassField().setText(AppSettings.get("shado_cloud_pass"));

		syncPane.getShadoEmailField().addFocusListener(createFocusListener(syncPane.getShadoEmailField(), "shado_cloud_email"));
		syncPane.getShadoPassField().addFocusListener(createFocusListener(syncPane.getShadoPassField(), "shado_cloud_pass"));

		syncPane.getSyncCheckbox().setSelected(AppSettings.getBool("sync_enabled"));
		syncPane.getSyncCheckbox().addChangeListener(e ->
				AppSettings.set("sync_enabled", syncPane.getSyncCheckbox().isSelected() + ""));

		return tab;
	}

	private FocusListener createFocusListener(JTextField field, String app_settings_label) {
		return new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
			}

			@Override
			public void focusLost(FocusEvent e) {
				// Check if it has changed
				boolean hasChanged = !AppSettings.get(app_settings_label).equals(field.getText());
				if (hasChanged) {
					System.out.println(field.getText());
					AppSettings.set(app_settings_label, field.getText());
				}
			}
		};
	}

	/**
	 * Helper class
	 */
	private static class SettingsTab {
		private static final Map<String, Integer> SettingsTabsIdx = new HashMap<>();
		private static int CurrentPos = 0;

		public final String label;
		public JPanel panel;


		private SettingsTab(String label, JPanel panel) {
			this.label = label;
			this.panel = panel;

			SettingsTabsIdx.put(label.toLowerCase(), CurrentPos++);
		}

		private static SettingsTab of(String label, JPanel panel) {
			return new SettingsTab(label, panel);
		}

		private static SettingsTab of(String label) {
			return new SettingsTab(label, new JPanel());
		}
	}

}
