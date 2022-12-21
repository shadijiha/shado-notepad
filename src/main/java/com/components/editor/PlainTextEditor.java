package com.components.editor;

import com.*;
import com.filters.*;
import com.utils.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;

public class PlainTextEditor extends AbstractEditor {

	public PlainTextEditor(Notepad notepad, JPanel tab, String text) {
		super(notepad, tab, text);
	}

	@Override
	protected void buildUI(String text) {
		setContentType("text/plain");

		var iosFont = new Font(AppSettings.get("font_family"), Font.PLAIN, (int) AppSettings.getNum("font_size"));
		setFont(iosFont);
		setFocusable(true);

		AbstractDocument doc = (AbstractDocument) getDocument();
		bindChangeEventToDoc(doc);

		try {
			deserialize(text);
		} catch (IOException e) {
			Actions.assertDialog(e);
		}

		//new Toolbar(notepad, tab, this);
	}

}
