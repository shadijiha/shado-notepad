package com;

// Import the necessary classes

import com.components.*;
import com.components.editor.*;
import com.formdev.flatlaf.*;
import com.utils.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.*;
import java.util.function.*;

import static com.utils.Util.*;


public class Notepad {

	private List<NotepadTab> openTabs = new ArrayList<>();
	protected JTabbedPane tabs;
	private Titlebar titleBar;
	private JFrame frame;

	protected volatile Consumer<NotepadTab> progressUIUpdater;
	private volatile int progress = 0;
	private volatile boolean showProgress = true;
	private volatile String currentMessage = "Test message";

	public static void main(String[] args) {
		try {
			System.out.printf("[Info]\tStarting v%s\n", getAnIncrementVersion());
		} catch (IOException e) {
			e.printStackTrace();
		}

		SwingUtilities.invokeLater(() -> {
			Notepad notepad = new Notepad();
		});
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
			try {
				// Get the index of the selected tab
				int index = tabs.getSelectedIndex();
				// Get the component for the selected tab
				Component component = tabs.getComponentAt(index);
				// Update the notepad UI based on the selected tab
				updateUI(component);
			} catch (Exception ex) {
			}
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

		// This bar indicates current application status
		progressUIUpdater = setupInfoBar(frame);

		// Show the notepad window
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public void openTab(String name, String text, File file) {

		// Check if the tab is already open
		Optional<NotepadTab> isOpen = openTabs.stream().filter(e -> e.getFile() != null && e.getFile().equals(file)).findFirst();
		if (isOpen.isPresent()) {
			tabs.setSelectedIndex(openTabs.indexOf(isOpen.get()));
			return;
		}

		var tab = new NotepadTab(name, text, this, file);
		openTabs.add(tab);

		setSelectedTab(tabs.getTabCount() - 1);
	}

	public void openTab(String name, String text) {
		openTab(name, text, null);
	}

	public void openTab(File file) {
		try {
			progress("Reading " + file.getAbsolutePath() + " content...", 0, 2);
			var content = Files.readString(file.toPath());

			progress("Opening tab " + file.getName() + "...", 1, 2);
			openTab(file.getName(), content, file);

			SwingUtilities.invokeLater(this::hideProgress);
		} catch (IOException e) {
			Actions.assertDialog(e);
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

		// Add your code here to update other UI elements of the notepad,
		// such as menus and buttons, based on the selected tab
		titleBar.setTitle(tab.getTabTitle() + " - Shado Notepad");

		if (progressUIUpdater != null)
			progressUIUpdater.accept(tab);
	}

	public JFrame getFrame() {
		return frame;
	}

	public List<NotepadTab> getOpenTabs() {
		return openTabs;
	}

	public NotepadTab getSelectedTab() {
		if (getOpenTabs().size() == 0)
			return null;
		return getOpenTabs().get(tabs.getSelectedIndex());
	}

	public void setSelectedTab(int index) {
		try {
			tabs.setSelectedIndex(index);
		} catch (Exception e) {
		}
	}

	public void close(NotepadTab selectedTab) {
		int i = openTabs.indexOf(selectedTab);

		try {
			openTabs.remove(i);
			tabs.remove(i);
		} catch (Exception e) {

		}
	}

	public void mergeTabs(List<File> incoming) {
		final var temp = openTabs.stream().map(NotepadTab::getTabTitle).toList();
		for (var file : incoming) {
			if (temp.contains(file.getName())) {
				int i = temp.indexOf(file.getName());
				close(openTabs.get(i));
				openTab(file);
			} else
				openTab(file);
		}
	}

	public Notepad setProgress(int n) {
		progress = Math.min(n, 100);
		showProgress();
		try {
			progressUIUpdater.accept(getSelectedTab());
		} catch (Exception ex) {
		}
		return this;
	}

	public Notepad showProgress() {
		showProgress = true;
		try {
			progressUIUpdater.accept(getSelectedTab());
		} catch (Exception ex) {
		}
		return this;
	}

	public Notepad hideProgress() {
		showProgress = false;
		try {
			progressUIUpdater.accept(getSelectedTab());
		} catch (Exception ex) {
		}
		return this;
	}

	public Notepad setProgressMsg(String s) {
		currentMessage = s;
		showProgress();
		try {
			progressUIUpdater.accept(getSelectedTab());
		} catch (Exception ex) {
		}
		return this;
	}

	/**
	 * Setups the UI for info bar
	 *
	 * @param frame
	 * @return Returns a function that should be called to update the info bar (call on Tab change event)
	 */
	private Consumer<NotepadTab> setupInfoBar(JFrame frame) {
		final var selectedTab = getSelectedTab();
		final var file = selectedTab != null ? getSelectedTab().getFile() : null;

		final JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		final var saveStatus = new JLabel(
				file == null ? "Unsaved file" : file.getAbsolutePath()
		);

		panel.add(saveStatus, BorderLayout.WEST);

		final JPanel container = new JPanel();

		final var progressBar = new JProgressBar();
		progressBar.setVisible(true);
		progressBar.setValue(45);

		final var progressLabel = new JLabel(progressBar.getValue() + "%");
		final var messageLabel = new JLabel(currentMessage);

		container.add(messageLabel);
		container.add(progressBar);
		container.add(progressLabel);

		panel.add(container, BorderLayout.EAST);

		frame.add(panel, BorderLayout.SOUTH);

		return (tab) -> {
			final var updatedFile = tab.getFile();

			saveStatus.setText(String.format("%s   %s",
					tab.lastSave == null ? "" : "[Last save " + Util.formatDate(tab.lastSave) + "]",
					updatedFile == null ? "Unsaved file" : updatedFile.getAbsolutePath()
			));
			progressBar.setValue(progress);
			progressBar.setVisible(showProgress);
			progressLabel.setText(progressBar.getValue() + "%");
			progressLabel.setVisible(showProgress);

			messageLabel.setText(currentMessage);
			messageLabel.setVisible(showProgress);
		};
	}
}