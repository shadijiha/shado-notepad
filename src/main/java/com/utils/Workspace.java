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

	public static Workspace parseWorkspaceFile(String rawContent, String filepath) throws Exception {
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

	public static boolean apply(Notepad notepad, Workspace workspace) {
		// If the last Applied is newer than the current one,
		// then don't apply it
		// ALso if it is the same date don't apply (means that it has been synced before)
		if (lastApplied != null && (lastApplied.date.after(workspace.date) || lastApplied.date.equals(workspace.date))) {
			return false;
		}

		notepad.mergeTabs(
				workspace.tabs.stream()
						.filter(tab -> tab.type == WorkspaceTab.Type.Local)
						.map(tab -> new File(tab.path))
						.filter(File::exists)
						.toList()
		);

		notepad.setSelectedTab(workspace.openTabIndex);
		lastApplied = workspace;

		return true;
	}

	public static boolean isNewer(Workspace workspace1, Workspace workspace2) {
		assert workspace1 != null;
		assert workspace2 != null;
		return (workspace1.date.after(workspace2.date) || workspace1.date.equals(workspace2.date));
	}

	public static Workspace getCurrentWorkspace() {
		return lastApplied;
	}

	public boolean contains(String filename) {
		return tabs.stream()
				.map(tab -> new File(tab.path).getName())
				.anyMatch(name -> name.equals(filename));
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
