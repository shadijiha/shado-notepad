package com.utils;

import com.*;

import java.io.*;
import java.util.*;

public class Workspace {

	protected final List<WorkspaceTab> tabs = new ArrayList<>();
	protected Date date;
	protected int openTabIndex;

	private static Workspace lastApplied = null;
	public static File LocalPath = null;
	public static final String CloudPath = "auto/shado-notepad/";
	public static final String CloudPathFile = CloudPath + "workspace.txt";


	protected Workspace() {
	}

	public static final Workspace parseWorkspaceFile(String rawContent, String filepath) throws Exception {
		String[] filecontent = rawContent.split("\n");

		Workspace workspace = new Workspace();

		// Parse the file
		int lineCount = 1;
		for (var line : filecontent) {
			// token[0]: Type
			// token[1]: filepath
			try {
				String tokens[] = line.split("\t");

				var cmd = tokens[0].trim();
				if (cmd.equalsIgnoreCase("local")) {
					workspace.tabs.add(new WorkspaceTab(tokens[1].trim(), WorkspaceTab.Type.Local));
				} else if (cmd.equalsIgnoreCase("open")) {
					workspace.openTabIndex = Integer.parseInt(tokens[1].trim());
				} else if (cmd.equalsIgnoreCase("date")) {
					long time = Long.parseLong(tokens[1].trim());
					workspace.date = new Date(time);
				}
			} catch (Exception e) {
				throw new Exception("An error occured while parsing workspace file at line " + line + ". \n\n" + filepath);
			}
			lineCount++;
		}

		return workspace;
	}

	public static final boolean apply(Notepad notepad, Workspace workspace) {
		// If the last Applied is newer than the current one,
		// then don't apply it
		// ALso if it is the same date don't apply (means that it has been synced before)
		if (lastApplied != null && (lastApplied.date.after(workspace.date) || lastApplied.date.equals(workspace.date))) {
			return false;
		}

		for (var tab : workspace.tabs) {
			if (tab.type == WorkspaceTab.Type.Local)
				notepad.openTab(new File(tab.path));
		}

		notepad.setSelectedTab(workspace.openTabIndex);
		lastApplied = workspace;

		return true;
	}

	public static Workspace getCurrentWorkspace() {
		return lastApplied;
	}

	public boolean containsLocal(String filename) {
		return getLocalPathFromName(filename) != null;
	}

	public File getLocalPathFromName(String filename) {
		for (var tab : tabs) {
			var file = new File(tab.path);
			if (tab.type == WorkspaceTab.Type.Local && file.getName().equals(filename))
				return file;
		}
		return null;
	}

	static final class WorkspaceTab {
		public final String path;
		public final Type type;

		private WorkspaceTab(String path, Type type) {
			this.path = path;
			this.type = type;
		}

		public String getName() {
			return new File(path).getName();
		}

		enum Type {
			Local, Cloud
		}
	}
}
