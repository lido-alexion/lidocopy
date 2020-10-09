package com.lc.service;

import java.io.File;
import java.util.List;

public class WriteFileThread extends Thread {

	List<File> srcList;
	File dst;

	public WriteFileThread(List<File> srcList, File dst) {
		super();

		this.srcList = srcList;
		this.dst = dst;
	}

	public WriteFileThread() {
		super();
	}

	public void run() {
		if (srcList != null && dst != null) {
			CopyServices.copyFiles(srcList, dst);
		} else {
			CopyServices.resumeCopyFiles();
		}
	}

}
