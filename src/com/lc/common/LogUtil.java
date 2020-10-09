package com.lc.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.lc.ui.LoggerPanelDlg;

public class LogUtil {

	private static int index = 1;

	public static void flushToDlg(String logs) {
		LoggerPanelDlg.updateLog(logs);
	}

	public static void flush(String logs) {
		flushToLogFile(logs);
	}

	private static void flushToLogFile(String line) {

		String logFileRelativePath = "res/conf/logs/log-" + CopySettings.SESSION_ID + ".log";
		String logFilePath = Util.getCurrentWorkingDir() + File.separator + logFileRelativePath;

		File f = new File(logFilePath);
		if (f.exists() && f.length() > 512 * 1024) {
			f.renameTo(new File(Util.getCurrentWorkingDir() + File.separator + "res/conf/logs/log-"
					+ CopySettings.SESSION_ID + "-" + index + ".txt"));
			index++;
		}

		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(logFileRelativePath, true)));
			if (line != null && line.length() > 0) {
				out.println(line);
			} else {
				out.println("");
			}
			out.close();
		} catch (IOException e) {
			// exception handling left
		}
	}

	public static void delete() {
		File f = new File(Util.getCurrentWorkingDir() + File.separator + "res/conf/logs");
		if (f.exists()) {
			if (Settings.DELETE_OLD_LOGS_ON_STARTUP) {
				File[] logs = f.listFiles();
				if (logs != null && logs.length > 0) {
					for (File file : logs) {
						file.delete();
					}
				}
			}
		} else {
			f.mkdir();
		}
	}

}
