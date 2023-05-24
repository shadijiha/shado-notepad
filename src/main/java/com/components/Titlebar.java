package com.components;

import com.*;
import com.components.settings.*;
import com.swing_ext.*;
import com.utils.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.function.*;

public class Titlebar extends JPanel {

	// Store the initial position of the mouse when it is pressed
	private Point initialMousePosition = null;

	// Store the initial position of the JWindow when the mouse is pressed
	private Point initialWindowPosition = null;

	private JLabel title;
	private Notepad notepad;

	public Titlebar(Notepad notepad) {
		this.notepad = notepad;
		JFrame frame = notepad.getFrame();

		// Set the background color of the title bar to the default title bar color
		setBackground(UIManager.getColor("InternalFrame.inactiveTitleBackground"));
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(frame.getWidth(), 40));
		setBorder(new EmptyBorder(0, 0, 0, 0));

		// Set the layout of the title bar to BorderLayout
		setLayout(new BorderLayout());


		// Create a new JLabel for the title
		title = new JLabel("Shado Notepad");
		title.setFont(title.getFont().deriveFont(Font.BOLD, 16));

		// Add the title to the WEST side of the title bar
		JPanel menuTitleContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));
		menuTitleContainer.setBorder(new EmptyBorder(0, 0, 0, 0));
		menuTitleContainer.setBackground(getBackground());
		setupMenus(menuTitleContainer, frame);
		var menuTitleContainerEmptySpace = new JLabel();
		menuTitleContainerEmptySpace.setPreferredSize(new Dimension(500, 10));
		menuTitleContainer.add(menuTitleContainerEmptySpace);
		menuTitleContainer.add(title);
		add(menuTitleContainer, BorderLayout.CENTER);

		// Create a new JLabel for the empty space
		JLabel emptySpace = new JLabel();

		// Set the preferred size of the empty space label
		emptySpace.setPreferredSize(new Dimension(0, 0));

		// Add the empty space label to the WEST side of the title bar
		add(emptySpace, BorderLayout.WEST);

		// Create a new JPanel for the buttons
		JPanel buttons = new JPanel();

		// Set the layout of the buttons panel to FlowLayout
		buttons.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));

		// Create a new JButton for the close button
		JButton close = new JButton("×");
		close.setFont(new Font("Arial", Font.BOLD, 14));

		// Create a new JButton for the minimize button
		JButton minimize = new JButton("-");
		minimize.setFont(new Font("Arial", Font.BOLD, 14));

		// Add the minimize button to the buttons panel
		buttons.add(minimize);
		buttons.add(close);

		// Add the buttons panel to the EAST side of the title bar
		add(buttons, BorderLayout.EAST);

		close.addActionListener(e -> Actions.exit());
		minimize.addActionListener(e -> Actions.minimize());

		makeFrameDraggable(frame);
	}

	private void makeFrameDraggable(JFrame frame) {

		// Add a MouseListener and MouseMotionListener to the title bar
		addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				// Set the initial position of the mouse
				initialMousePosition = e.getPoint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// Reset the initialMousePosition field
				initialMousePosition = null;
			}

		});
		addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {
				// Set the initial position of the mouse
				initialMousePosition = e.getPoint();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				// Check if the initialMousePosition field is not null
				if (initialMousePosition != null) {
					// Get the current position of the mouse
					Point currentMousePosition = e.getPoint();

					// Set the initial position of the JFrame
					initialWindowPosition = frame.getLocation();

					// Calculate the difference between the initial and current positions of the JFrame
					int dx = currentMousePosition.x - initialMousePosition.x;
					int dy = currentMousePosition.y - initialMousePosition.y;

					// Calculate the new position of the JWindow
					int newX = initialWindowPosition.x + dx;
					int newY = initialWindowPosition.y + dy;

					// Set the new position of the JWindow
					frame.setLocation(newX, newY);
				}
			}

		});
	}

	private void setupMenus(JPanel panel, JFrame frame) {
		final Consumer<ActionEvent> Empty = (e) -> {
		};

		/**
		 * All menu
		 */
		SortedMap<String, MenuData[]> menus = Util.<String, MenuData[]>sortedMap(
				"File", new MenuData[]{
						MenuData.of("New", e -> notepad.openTab("Untitled " + Util.getDate() + ".srtf", ""), "control N"),
						MenuData.of("Open", this::openFile, "control O"),
						MenuData.of("Open from cloud", this::openCouldFile, "control shift O"),
						MenuData.of("Save", e -> notepad.save(), "control S"),
						MenuData.separator(),
						MenuData.of("Settings", e -> new SettingsFrame(frame)),
						MenuData.separator(),
						MenuData.of("Exit", e -> Actions.exit(), "control W")
				},
				"Sync", new MenuData[]{
						MenuData.of("Sync now", e -> {
							Actions.getAppInstance().getOpenTabs().forEach(tab -> tab.setHasChanged(true));
							Actions.saveWorkSpace();
						}),
						MenuData.of("Force load local workspace", e -> {
							try {
								Actions.loadWorkSpace(false);
							} catch (Exception ex) {
								Actions.assertDialog(ex);
							}
						}),
						MenuData.separator(),
						MenuData.of("Sync settings", e -> new SettingsFrame(frame, "sync"))
				}
		);

		JMenuBar menuBar = new JMenuBar();

		for (var entry : menus.entrySet()) {
			JMenu menu = new JMenu(entry.getKey());
			menuBar.add(menu);

			for (var menuData : entry.getValue()) {
				if (menuData == null) {
					menu.addSeparator();
					continue;
				}
				JMenuItem item = new JMenuItem(menuData.label);
				item.addActionListener(menuData.action::accept);

				if (menuData.shortcut != null) {
					item.setAccelerator(KeyStroke.getKeyStroke(menuData.shortcut));
				}
				menu.add(item);
			}
		}

		// Set the menu bar for the frame
		panel.add(menuBar);
	}

	public void setTitle(String s) {
		title.setText(s);
	}

	private void openFile(ActionEvent actionEvent) {
		FileChooser chooser = new FileChooser("Text files", FileChooser.TextFiles);
		var file = chooser.openDialog();
		if (file != null) {
			notepad.openTab(file);
		}
	}

	private void openCouldFile(ActionEvent actionEvent) {
		ShadoCloudFileChooser chooser = new ShadoCloudFileChooser("");
		chooser.openDialog();
	}

	/**
	 * Data classes
	 */
	private static class MenuData {
		public final String label;
		public final Consumer<ActionEvent> action;
		public final String shortcut;

		public MenuData(String label, Consumer<ActionEvent> action, String shortcut) {
			this.label = label;
			this.action = action;
			this.shortcut = shortcut;
		}

		protected static MenuData of(String label, Consumer<ActionEvent> action, String shortcut) {
			return new MenuData(label, action, shortcut);
		}

		protected static MenuData of(String label, Consumer<ActionEvent> action) {
			return of(label, action, null);
		}

		protected static MenuData separator() {
			return null;
		}
	}
}
