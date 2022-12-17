package com.filters;

import com.editor.*;

import javax.swing.text.*;

public class ImageDeleteFilter extends DocumentFilter {
	private RichHTMLEditor editor;

	public ImageDeleteFilter(RichHTMLEditor editor) {
		this.editor = editor;
	}

	@Override
	public void insertString(FilterBypass fb, int offset, String text, AttributeSet attrs) throws BadLocationException {
		//String t = text.replaceAll("\n", "<br>");
		super.insertString(fb, offset, text, attrs);
		//System.out.println("Insert --> " + text);
	}

	@Override
	public void replace(DocumentFilter.FilterBypass fb, int offset,
						int length, String text, AttributeSet attrs)
			throws BadLocationException {
		//System.out.println(text);
		super.replace(fb, offset, length, text, attrs);
	}

	@Override
	public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
		//System.out.println("Deleted Offset: " + offset);
		editor.deleteImageAtOffset(offset);
		super.remove(fb, offset, length);
	}
}
