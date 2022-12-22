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
import java.io.*;
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
				try {
					insertList(markdownPane, "ul");
				} catch (Exception ex) {
					Actions.assertDialog("Unable to insert a List item! ", ex);
				}
			});
			toolbar.add(listBtn);
		}

		{
			JButton listBtn = new JButton("Ordered");
			listBtn.addActionListener(e -> {
				try {
					insertList(markdownPane, "ol");
				} catch (Exception ex) {
					Actions.assertDialog("Unable to insert a List item! ", ex);
				}
			});
			toolbar.add(listBtn);
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

	private void insertList(RichHTMLEditor editor, String type) throws Exception {
		HTMLDocument doc = (HTMLDocument) editor.getStyledDocument();

		var elementAtPos = getElementAtCaret(doc, editor);

		int caretPos = editor.getCaretPosition();

		String id = "ul_" + (int) (Math.random() * 1e9);
		String htmlBegin = String.format("<%s id=\"%s\" style=\"text-align: left;\"><li style=\"text-align: left;\">", type, id);
		String html = String.format("%s</li></%s>", htmlBegin, type);

		doc.insertAfterEnd(elementAtPos, html);
		editor.setCaretPosition(caretPos + 1);
	}

	public void onChange(Consumer<Object> changeEvent) {
		this.changeEvent = changeEvent;
	}

	private void callChangeEvent(AWTEvent e) {
		if (changeEvent != null)
			changeEvent.accept(e);
	}

	private Element getBodyElement(HTMLDocument htmlDoc) {
		Element[] roots = htmlDoc.getRootElements(); // #0 is the HTML element, #1 the bidi-root
		Element body = null;
		for (int i = 0; i < roots[0].getElementCount(); i++) {
			Element element = roots[0].getElement(i);
			if (element.getAttributes().getAttribute(StyleConstants.NameAttribute) == HTML.Tag.BODY) {
				body = element;
				break;
			}
		}
		return body;
	}

	private Element getElementAtCaret(HTMLDocument document, JTextPane myJTextPane) {
		int p = myJTextPane.getCaretPosition();
		Element el = document.getCharacterElement(p);
		System.out.println(el.getName());
		return el;
	}

	private void setAttribute(Element element, HTML.Attribute attr,
							  Object value) {
		HTMLDocument doc = (HTMLDocument) element.getDocument();
		int startOffset = element.getStartOffset();
		int endOffset = element.getEndOffset();
		MutableAttributeSet attributeSet = new SimpleAttributeSet();
		attributeSet.addAttribute(attr, value);
		doc.setCharacterAttributes(startOffset, endOffset - startOffset,
				attributeSet, false);
	}
}
