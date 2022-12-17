package com.filters;

import com.utils.*;
import org.markdownj.*;

import javax.swing.*;
import javax.swing.text.*;
import javax.xml.parsers.*;

public class MarkdownDocumentFilter extends DocumentFilter {
	// the JTextPane that we will listen to
	private final JTextPane textPane;

	// the Markdown parser that we will use to convert Markdown to HTML
	private MarkdownProcessor markdownProcessor;

	private String markdownText;

	public MarkdownDocumentFilter(JTextPane pane) throws ParserConfigurationException {
		textPane = pane;
		markdownProcessor = new MarkdownProcessor();
	}

	public void setup(String rawText) {
		this.markdownText = rawText;
		updateMarkdown();
	}

	public String getText() {
		//return markdownText;
		try {
			var doc = textPane.getStyledDocument();
			System.out.println(doc.getText(0, doc.getLength()));
			return doc.getText(0, doc.getLength());
		} catch (BadLocationException e) {
			Actions.assertDialog(false, e.getMessage());
		}
		return null;
	}

	@Override
	public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
		// log the inserted text to the standard output
		System.out.println("Inserted text: " + string);

		// pass the text through to the document unmodified
		//super.insertString(fb, offset, string, attr);

		markdownText += string;
		updateMarkdown();
	}

	@Override
	public void replace(DocumentFilter.FilterBypass fb, int offset,
						int length, String text, AttributeSet attrs)
			throws BadLocationException {


		//String before = markdownText.substring(0, offset);
		//int diff = getDiffBetweenMarkdownAndRaw(before, markdownText);

		//before = markdownText.substring(0, offset + diff);
		//String after = markdownText.substring(offset + diff + length);

		super.replace(fb, offset, length, text, attrs);
		markdownText = fb.getDocument().getText(0, fb.getDocument().getLength());
		//updateMarkdown();

		//textPane.setCaretPosition(offset + 1);
	}

	// convert the Markdown text in the document to HTML and update the text pane
	private void updateMarkdown() {
		// get the Markdown text from the document
		// convert the Markdown text to HTML
		String html = markdownProcessor.markdown(markdownText);

		textPane.setContentType("text/html");
		// set the HTML text in the text pane
		textPane.setText(html.replace("\n", "<br>"));
	}

	private int getDiffBetweenMarkdownAndRaw(String rawBefore, String markdownText) throws BadLocationException {
		String html = markdownProcessor.markdown(markdownText);

		JTextPane dummy = new JTextPane();
		dummy.setContentType("text/html");
		dummy.setText(html);
		StyledDocument doc = dummy.getStyledDocument();
		String text = doc.getText(0, doc.getLength());

		return markdownText.length() - text.length();
	}
}
