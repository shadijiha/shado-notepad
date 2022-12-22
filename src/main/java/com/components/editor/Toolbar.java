package com.components.editor;

import com.*;
import com.filters.*;
import com.swing_ext.*;
import com.utils.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.util.*;
import java.util.function.*;

public class Toolbar {

	private volatile Consumer<Object> changeEvent;

	public Toolbar(Notepad notepad, JPanel tab, RichHTMLEditor markdownPane) {
		// TODO: move this shit from here

		JPanel toolbar = new JPanel(new FlowLayout());

		{
			Action boldAction = new StyledEditorKit.BoldAction();
			// Add a button to the frame that triggers the bold action when clicked
			JButton boldButton = new JButton("B");
			var font = boldButton.getFont();
			boldButton.setFont(new Font(font.getFontName(), Font.BOLD, font.getSize()));
			boldButton.addActionListener(e -> {
				boldAction.actionPerformed(new ActionEvent(markdownPane, ActionEvent.ACTION_PERFORMED, null));
				callChangeEvent(e);
			});
			toolbar.add(boldButton);
		}
		{
			Action italicAction = new StyledEditorKit.ItalicAction();
			// Add a button to the frame that triggers the bold action when clicked
			JButton italicButton = new JButton("I");
			var font = italicButton.getFont();
			italicButton.setFont(new Font(font.getFontName(), Font.ITALIC, font.getSize()));
			italicButton.addActionListener(e -> {
				italicAction.actionPerformed(new ActionEvent(markdownPane, ActionEvent.ACTION_PERFORMED, null));
				callChangeEvent(e);
			});
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
			button.addActionListener(e -> {
				action.actionPerformed(new ActionEvent(markdownPane, ActionEvent.ACTION_PERFORMED, null));
				callChangeEvent(e);
			});
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
					callChangeEvent(e);
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
				Action action = new StyledEditorKit.FontFamilyAction("font-family-" + fontStr, fontStr);
				action.actionPerformed(new ActionEvent(markdownPane, ActionEvent.ACTION_PERFORMED, null));
				callChangeEvent(e);
			});
			toolbar.add(field);
		}

		{
			JButton listBtn = new JButton("List");
			listBtn.addActionListener(e -> {
				insertList(markdownPane);
			});
		}

		{
			JButton insertImageButton = new JButton("Insert Image");
			insertImageButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// Show a file chooser to let the user select an image file
					var chooser = new FileChooser("Image", FileChooser.ImageFiles);
					chooser.getNative().setAcceptAllFileFilterUsed(false);

					//fileChooser.addChoosableFileFilter(filter);
					var file = chooser.openDialog();
					if (file != null) {
						try {
							markdownPane.insertImageAsIcon(file, 200);
						} catch (Exception ex) {
							Actions.assertDialog(ex);
						}
					}

					callChangeEvent(e);
				}
			});
			toolbar.add(insertImageButton);
		}
		tab.add(toolbar, BorderLayout.NORTH);
	}

	private void insertList(RichHTMLEditor editor) {
		HTMLDocument doc = (HTMLDocument) editor.getStyledDocument();
		
	public void onChange(Consumer<Object> changeEvent) {
		this.changeEvent = changeEvent;
	}

	private void callChangeEvent(AWTEvent e) {
		if (changeEvent != null)
			changeEvent.accept(e);
	}
}
