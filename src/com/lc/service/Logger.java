package com.lc.service;

import java.util.Date;

import com.lc.common.Settings;
import com.lc.common.Settings.LogLevel;

public class Logger {

	private static StringBuffer buffer = new StringBuffer(); // buffer with all logs to show in log dialog
	private static StringBuffer unreadBuffer = new StringBuffer(); // buffer for writing to log dialog
	private static StringBuffer tmpBuffer = new StringBuffer(); // buffer for writing to log file

	public static void warn(String msg) {
		log(msg, "WARN ");
	}

	public static void error(String msg) {
		log(msg, "ERROR");
	}

	public static void debug(String msg) {
		log(msg, "DEBUG");
	}

	public static void info(String msg) {
		log(msg, "INFO ");
	}

	public static void log(String msg) {
		log(msg, null);
	}

	public static void log(String msg, String logType) {

		// System.out.println(msg);

		String line = logType != null ? logType + " | " + new Date() + " : " + msg : msg;
		if(logType == null) {
			logType = "DEBUG";
		}

		if (buffer.length() > 0) {
			buffer.append("\n");
		}
		if (unreadBuffer.length() > 0) {
			unreadBuffer.append("\n");
		}
		buffer.append(line);
		unreadBuffer.append(line);

		if (unreadBuffer.length() > 1000) {
			new LoggerThread(unreadBuffer.toString(), true).start();
			unreadBuffer.setLength(0);
		}

		// now process for log file writing

		if (Settings.LOG_LEVEL == LogLevel.ERROR || msg == null || msg.length() == 0) {
			return;
		} else if (Settings.LOG_LEVEL == LogLevel.ERROR && (logType.startsWith("WARN") || logType.startsWith("INFO")
				|| logType.startsWith("DEBUG") || logType.startsWith("LOG"))) {
			return;
		} else if (Settings.LOG_LEVEL == LogLevel.WARN
				&& (logType.startsWith("INFO") || logType.startsWith("DEBUG") || logType.startsWith("LOG"))) {
			return;
		} else if (Settings.LOG_LEVEL == LogLevel.INFO && (logType.startsWith("DEBUG") || logType.startsWith("LOG"))) {
			return;
		}

		if (tmpBuffer.length() > 0) {
			tmpBuffer.append("\n");
		}
		tmpBuffer.append(line);

		if (tmpBuffer.length() > 7000) {
			new LoggerThread(tmpBuffer.toString(), false).start();
			tmpBuffer.setLength(0);
		}

	}

	public static void clearLogs() {
		buffer.setLength(0);
		unreadBuffer = new StringBuffer();
	}

	public static String readFullBuffer() {
		unreadBuffer.setLength(0);
		return buffer.toString();
	}

	public static void clearUnreadBuffer() {
		buffer = new StringBuffer();
	}

	public static String readUnreadBuffer() {
		return unreadBuffer.toString();
	}

	public static void flush() {
		if (tmpBuffer.length() > 0) {
			new LoggerThread(tmpBuffer.toString(), false).start();
			tmpBuffer.setLength(0);
		}
	}

	public static void flushToDlg() {
		if (unreadBuffer.length() > 0) {
			new LoggerThread(unreadBuffer.toString(), true).start();
			unreadBuffer.setLength(0);
		}
	}

}
