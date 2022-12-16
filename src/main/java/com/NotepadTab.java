package com;

import com.filters.*;
import com.observer.Observer;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.rtf.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.font.*;
import java.io.*;
import java.util.*;

public class NotepadTab extends JPanel implements Observer<AppSettings> {
	private String title;
	private File file;
	private JTextPane markdownPane;
	private Notepad notepad;

	public NotepadTab(String name, String text, Notepad notepad, File file) {
		super(new BorderLayout());
		this.notepad = notepad;
		this.file = file;
		title = name;
		setBackground(Color.WHITE);

		// Create a new MarkdownPane
		//var iosFont = new Font(AppSettings.get("font_family"), Font.PLAIN, (int) AppSettings.getNum("font_size"));
		markdownPane = new JTextPane();
		markdownPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		//markdownPane.setFont(iosFont);
		markdownPane.setFocusable(true);
		markdownPane.setContentType("text/rtf");

		// IF it is a file, then need to parse the RTF
		if (file != null) {
			try (InputStream in = new FileInputStream(file)) {
				RTFEditorKit rtfEditorKit = new RTFEditorKit();
				rtfEditorKit.read(in, markdownPane.getStyledDocument(), 0);
			} catch (Exception ex) {
				Actions.assertDialog(false, "Failed to parse file " + file.getAbsolutePath() + "\n\n" + ex.getMessage());
			}
		} else
			markdownPane.setText(text);

		this.add(new JScrollPane(markdownPane), BorderLayout.CENTER);

		// Add the text editor to a new tab in the JTabbedPane
		notepad.tabs.addTab(name, this);

		// Setup drag and drop
		markdownPane.setDropTarget(onDrop());

		// TODO: move this shit from here
		{
			JPanel toolbar = new JPanel(new FlowLayout());

			{
				Action boldAction = new StyledEditorKit.BoldAction();
				// Add a button to the frame that triggers the bold action when clicked
				JButton boldButton = new JButton("B");
				var font = boldButton.getFont();
				boldButton.setFont(new Font(font.getFontName(), Font.BOLD, font.getSize()));
				boldButton.addActionListener(e -> boldAction.actionPerformed(new ActionEvent(markdownPane, ActionEvent.ACTION_PERFORMED, null)));
				toolbar.add(boldButton);
			}
			{
				Action italicAction = new StyledEditorKit.ItalicAction();
				// Add a button to the frame that triggers the bold action when clicked
				JButton italicButton = new JButton("I");
				var font = italicButton.getFont();
				italicButton.setFont(new Font(font.getFontName(), Font.ITALIC, font.getSize()));
				italicButton.addActionListener(e -> italicAction.actionPerformed(new ActionEvent(markdownPane, ActionEvent.ACTION_PERFORMED, null)));
				toolbar.add(italicButton);
			}
			{
				Action action = new StyledEditorKit.UnderlineAction();
				// Add a button to the frame that triggers the bold action when clicked
				JButton button = new JButton("U");
				var font = button.getFont();
				button.setFont(
						new Font(font.getFontName(), Font.PLAIN, font.getSize()).deriveFont(Map.of(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON))
				);
				button.addActionListener(e -> action.actionPerformed(new ActionEvent(markdownPane, ActionEvent.ACTION_PERFORMED, null)));
				toolbar.add(button);
			}
			{
				JTextField field = new JTextField(10);
				PlainDocument doc = (PlainDocument) field.getDocument();
				doc.setDocumentFilter(new IntFilter());
				field.setText(AppSettings.get("font_size"));
				field.addKeyListener(new KeyListener() {
					public void keyTyped(KeyEvent e) {
					}

					public void keyPressed(KeyEvent e) {
					}

					public void keyReleased(KeyEvent e) {
						int size = Integer.parseInt(field.getText());
						Action action = new StyledEditorKit.FontSizeAction("font-size-" + size, size);
						action.actionPerformed(new ActionEvent(markdownPane, ActionEvent.ACTION_PERFORMED, null));
					}
				});
				toolbar.add(field);
			}
			{
				JComboBox<String> field = new JComboBox(
						GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()
				);
				final var currentFont = markdownPane.getFont();
				field.setSelectedItem(currentFont.getFamily());
				field.addActionListener(e -> {
					String fontStr = field.getSelectedItem().toString();
					System.out.println(fontStr);
					Action action = new StyledEditorKit.FontFamilyAction("font-family-" + currentFont.getFamily(), currentFont.getFamily());
					action.actionPerformed(new ActionEvent(markdownPane, ActionEvent.ACTION_PERFORMED, null));
				});
				toolbar.add(field);
			}
			{
				JButton insertImageButton = new JButton("Insert Image");
				insertImageButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						// Show a file chooser to let the user select an image file
						JFileChooser fileChooser = new JFileChooser();
						int result = fileChooser.showOpenDialog(notepad.getFrame());
						if (result == JFileChooser.APPROVE_OPTION) {
							File file = fileChooser.getSelectedFile();
							// Load the image into an Icon object
							ImageIcon icon = new ImageIcon(file.getAbsolutePath());
							int newWidth = 200;
							int newHeight = (int) (icon.getIconHeight() * ((float) newWidth / icon.getIconWidth()));

							icon.setImage(icon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH));

							// Create a style that includes the Icon object
							Style style = markdownPane.addStyle("image", null);
							StyleConstants.setIcon(style, icon);
							// Insert the image into the text pane
							try {
								markdownPane.getStyledDocument().insertString(markdownPane.getCaretPosition(), "ignored text", style);
							} catch (BadLocationException ex) {
								ex.printStackTrace();
							}
						}
					}
				});
				toolbar.add(insertImageButton);
			}
			add(toolbar, BorderLayout.NORTH);
		}
	}

	public String getTabTitle() {
		return title;
	}

	public File getFile() {
		return file;
	}

	public void write(PrintWriter stream) throws Exception {
		stream.println(getContentAsU8());
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
			writer.println(getContentAsU8());
			writer.close();
		} catch (Exception e) {
			Actions.assertDialog(false, e.getMessage());
		}
	}

	@Override
	public void update(AppSettings data) {
		if (AppSettings.hasChanged("font_family") || AppSettings.hasChanged("font_size"))
			markdownPane.setFont(new Font(AppSettings.get("font_family"), Font.PLAIN, (int) AppSettings.getNum("font_size")));
	}

	private DropTarget onDrop() {
		return new DropTarget() {
			public synchronized void drop(DropTargetDropEvent evt) {
				try {
					evt.acceptDrop(DnDConstants.ACTION_COPY);
					java.util.List<File> droppedFiles = (java.util.List<File>)
							evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
					for (File file : droppedFiles) {
						// process files
						notepad.openTab(file);

						// Focus last openned
						notepad.tabs.setSelectedIndex(notepad.tabs.getTabCount() - 1);
					}
				} catch (Exception ex) {
					Actions.assertDialog(false, ex.getMessage());
				}
			}
		};
	}

	private String getContentAsU8() {
		RTFEditorKit editorKit = new RTFEditorKit();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		var document = markdownPane.getStyledDocument();
		try {
			editorKit.write(bos, document, 0, document.getLength());
			bos.flush();
		} catch (Exception e) {
			Actions.assertDialog(false, e.getMessage());
		}
		return bos.toString();
	}

}
