package com.swing_ext;

import com.shadocloud.nest.models.*;

import java.io.*;

public class ShadoFile extends File {
	private FileInfo info;
	private String pathname;
	private boolean isDir;

	public ShadoFile(String pathname) {
		this(pathname, true);
	}

	public ShadoFile(String pathname, boolean isDir) {
		super(pathname);
		this.pathname = pathname;
		this.isDir = isDir;
	}

	public ShadoFile(FileInfo cloudInfo) {
		this(cloudInfo.path, cloudInfo.mime == null);
		this.info = cloudInfo;

		if (isDir)
			pathname = pathname.length() <= 1 ? cloudInfo.name : cloudInfo.path + "/" + cloudInfo.name;
	}

	@Override
	public boolean isDirectory() {
		return isDir;
	}

	@Override
	public String getName() {
		return info == null ? pathname : info.name;
	}

	@Override
	public boolean isHidden() {
		if (info == null)
			return super.isHidden();
		return info.name.startsWith(".");
	}

	@Override
	public String getPath() {
		if (info == null)
			if (pathname.length() <= 1)
				return "";
			else
				return pathname;

		if (isDir)
			return info.path.length() <= 0 ? info.name : info.path + "/" + info.name;
		return info.path;
	}

	@Override
	public String getAbsolutePath() {
		return getPath();
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public File getParentFile() {
		String[] tokens = pathname.split("/");
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < tokens.length - 1; i++)
			builder.append(tokens[i]).append("/");
		return new ShadoFile(builder.toString());
	}
}
