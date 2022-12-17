package com;

import com.utils.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;

public class NotepadTabContextMenu extends JPopupMenu {
	public NotepadTabContextMenu(JTabbedPane tabsPane, List<NotepadTab> tabs) {
		var anItem = new JMenuItem("Show in explorer");
		anItem.addActionListener(e -> {
			try {
				Desktop desktop = Desktop.getDesktop();
				desktop.open(tabs.get(tabsPane.getSelectedIndex()).getFile().getParentFile());
			} catch (IOException ex) {
				Actions.assertDialog(false, ex.getMessage());
			}
		});
		add(anItem);

		var close = new JMenuItem("Close");
		close.addActionListener(e -> {
			int i = tabsPane.getSelectedIndex();
			tabsPane.remove(i);
			tabs.remove(i);
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
			NotepadTabContextMenu menu = new NotepadTabContextMenu(notepad.tabs, notepad.getOpenTabs());
			menu.show(e.getComponent(), e.getX(), e.getY());
		}
	}
}
