package com.components.editor;

import com.*;
import com.utils.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;

public class NotepadTabContextMenu extends JPopupMenu {
	public NotepadTabContextMenu(Notepad notepad, NotepadTab selectedTab, List<NotepadTab> tabs) {
		var anItem = new JMenuItem("Show in explorer");
		anItem.addActionListener(e -> {
			try {
				Desktop desktop = Desktop.getDesktop();
				desktop.open(selectedTab.getFile().getParentFile());
			} catch (IOException ex) {
				Actions.assertDialog(false, ex.getMessage());
			}
		});
		add(anItem);

		var close = new JMenuItem("Close");
		close.addActionListener(e -> {
			notepad.close(selectedTab);
		});
		add(close);
	}

	public static class PopClickListener extends MouseAdapter {
		private Notepad notepad;

		public PopClickListener(Notepad notepad) {
			super();
			this.notepad = notepad;
		}

		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger())
				doPop(e);
		}

		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger())
				doPop(e);
		}

		private void doPop(MouseEvent e) {
			NotepadTabContextMenu menu = new NotepadTabContextMenu(notepad, notepad.getSelectedTab(), notepad.getOpenTabs());
			menu.show(e.getComponent(), e.getX(), e.getY());
		}
	}
}
