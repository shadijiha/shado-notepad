package com.utils;

import com.*;

import java.io.*;
import java.util.*;

public class Workspace {

	protected final List<WorkspaceTab> tabs = new ArrayList<>();
	protected Date date;
	protected int openTabIndex;

	private static Date lastApplied = null;
	public static File LocalPath = null;
	public static final String CloudPath = "auto/shado-notepad/workspace.txt";

	protected Workspace() {
	}

	public static final Workspace parseWorkspaceFile(String rawContent, String filepath) throws Exception	{
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
				}
				else if (cmd.equalsIgnoreCase("open"))	{
					workspace.openTabIndex = Integer.parseInt(tokens[1].trim());
				}
				else if (cmd.equalsIgnoreCase("date"))	{
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

	public static final void apply(Notepad notepad, Workspace workspace)	{
		// If the last Applied is newer than the current one,
		// then don't apply it
		// ALso if it is the same date don't apply (means that it has been synced before)
		if (lastApplied != null && (lastApplied.after(workspace.date) || lastApplied.equals(workspace.date))) {
			System.out.println("Did not apply " + workspace.date);
			return;
		}

		for (var tab : workspace.tabs)	{
			if (tab.type == WorkspaceTab.Type.Local)
				notepad.openTab(new File(tab.path));
		}

		notepad.setSelectedTab(workspace.openTabIndex);
		lastApplied = workspace.date;
	}


	private static final class WorkspaceTab	{
		public final String path;
		public final Type type;

		private WorkspaceTab(String path, Type type) {
			this.path = path;
			this.type = type;
		}

		enum Type	{
			Local, Cloud
		}
	}
}
