package com.lc.service;

import java.io.File;
import java.util.List;

import com.lc.common.LogUtil;

public class LoggerThread extends Thread {

	List<File> srcList;
	String logs;
	boolean flushToDlg;

	public LoggerThread(String logs, boolean flushToDlg) {
		super();
		this.logs = logs;		
	}

	public void run() {
		if(flushToDlg) {
			LogUtil.flushToDlg(logs);
		}else {
			LogUtil.flush(logs);
		}
	}

}
