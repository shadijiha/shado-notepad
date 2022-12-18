package com.editor;

import com.shadocloud.nest.*;

import javax.swing.*;
import javax.swing.filechooser.*;
import java.io.*;
import java.util.*;

public class ShadoCloudFileChooser {

	public ShadoCloudFileChooser() throws Exception {

		JFileChooser chooser = new JFileChooser();
		ShadoCloudClient client = new ShadoCloudClient("shadi@shado.com", "shadi1234");
		client.auth.login();
		chooser.setFileSystemView(new ShadoCloudFileSystemView());
		chooser.showOpenDialog(null);
	}

	private static class ShadoCloudFileSystemView extends FileSystemView {

		@Override
		public File createNewFolder(File containingDir) throws IOException {
			return null;
		}

		@Override
		public File[] getFiles(File dir, boolean useFileHiding) {
//			try {
//				var temp = client.directories.list("");
//				return Arrays.stream(temp).map(e -> new File(e.path)).toArray(File[]::new);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			return null;
		}
	}
}
