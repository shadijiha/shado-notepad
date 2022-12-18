package com.components.editor;

import com.*;
import com.observer.*;
import com.utils.*;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.*;

public abstract class AbstractEditor extends JTextPane implements Observer<AppSettings> {

	protected Notepad notepad;
	protected JPanel tab;

	public AbstractEditor(Notepad notepad, JPanel tab, String text) {
		this.notepad = notepad;
		this.tab = tab;
		setDropTarget(onDrop());
		buildUI(text);
	}

	public static AbstractEditor factory(Notepad notepad, JPanel panel, String text, File file) {
		if (file == null)
			return new RichHTMLEditor(notepad, panel, text);

		String extension = Util.getExtension(file);

		return switch (extension) {
			case "srtf" -> new RichHTMLEditor(notepad, panel, text);
			default -> new PlainTextEditor(notepad, panel, text);
		};
	}

	protected abstract void buildUI(String text);

	public void serialize(PrintWriter writer) {
		writer.println(this.getText());
	}

	public void deserialize(String rawContent) throws IOException {
		this.setText(rawContent);
	}

	@Override
	public void update(AppSettings data) {
		if (AppSettings.hasChanged("font_family") || AppSettings.hasChanged("font_size"))
			this.setFont(new Font(AppSettings.get("font_family"), Font.PLAIN, (int) AppSettings.getNum("font_size")));
	}

	protected DropTarget onDrop() {
		return new DropTarget() {
			public synchronized void drop(DropTargetDropEvent evt) {
				try {
					evt.acceptDrop(DnDConstants.ACTION_COPY);
					java.util.List<File> droppedFiles = (java.util.List<File>)
							evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
					for (File file : droppedFiles) {
						// process files
						notepad.openTab(file);
					}
				} catch (Exception ex) {
					Actions.assertDialog(false, ex.getMessage());
				}
			}
		};
	}

}
