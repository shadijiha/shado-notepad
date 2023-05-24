package com.swing_ext;

import com.utils.*;

import javax.swing.filechooser.*;
import java.io.*;
import java.util.*;

public class ShadoCloudFileView extends FileSystemView {
	@Override
	public boolean isRoot(File f) {
		return f.getName().length() <= 1;
	}

	@Override
	public ShadoFile[] getFiles(File dir, boolean useFileHiding) {
		try {
			ShadoFile file = dir instanceof ShadoFile ? (ShadoFile) dir : new ShadoFile(dir.getPath());
			var client = AppSettings.getClient();
			System.out.println("------>" + file.getPath());
			var temp = client.directories.list(file.getPath());
			return Arrays.stream(temp).map(ShadoFile::new).toArray(ShadoFile[]::new);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getSystemDisplayName(File f) {
		return f.getName();
	}

	@Override
	public File createNewFolder(File containingDir) throws IOException {
		try {
			AppSettings.getClient().directories.newDirectory("New Folder");
			return new ShadoFile(AppSettings.getClient().files.info("New Folder"));
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}

	@Override
	public File getHomeDirectory() {
		var client = AppSettings.getClient();
		try {
			//var info = client.files.info("");
			//System.out.println(info);
			return new ShadoFile("");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public File[] getRoots() {
		return new ShadoFile[]{
				new ShadoFile("")
		};
	}

	@Override
	public File getDefaultDirectory() {
		return new ShadoFile("");
	}

	@Override
	public Boolean isTraversable(File f) {
		if (!(f instanceof ShadoFile))
			return new ShadoFile(f.getPath()).isDirectory();
		return f.isDirectory();
	}

	@Override
	public File getChild(File parent, String fileName) {
		return new ShadoFile(parent.getPath() + "/" + fileName);
	}

	@Override
	public boolean isFileSystem(File f) {
		return true;
	}

	@Override
	public String getSystemTypeDescription(File f) {
		return f.getName();
	}

	@Override
	public File getParentDirectory(File dir) {
		return dir.getParentFile();
	}
}


