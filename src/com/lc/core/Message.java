package com.lc.core;

import java.io.File;

public class Message implements Comparable<Message> {

	public enum TYPE {
		ERROR, WARNING, INFO
	};

	private String fileName;
	private String message;
	private TYPE type;
	private String correctiveAction;
	private File actionFile;
	private String reason;

	public Message(String fileName, TYPE type, String message, String reason, String action, File actionFile) {
		this.fileName = fileName;
		this.type = type;
		this.message = message;
		this.correctiveAction = action;
		this.actionFile = actionFile;
		this.reason = reason;
	}

	public String getMessage() {
		return message;
	}

	public TYPE getType() {
		return type;
	}

	public String getFileName() {
		return fileName;
	}

	public String getCorrectiveAction() {
		return correctiveAction;
	}

	public File getActionFile() {
		return actionFile;
	}

	public String getReason() {
		return reason;
	}

	@Override
	public int compareTo(Message msg) {
		int val = this.fileName.compareTo(msg.fileName);
		if (val == 0) {
			TYPE type = this.getType();
			TYPE msgType = msg.getType();
			if (type == TYPE.ERROR) {
				val = -1;
			} else if (type == TYPE.INFO) {
				val = 1;
			} else {
				if (msgType == TYPE.ERROR) {
					val = 1;
				} else if (msgType == TYPE.INFO) {
					val = -1;
				}
			}
		}

		return val;
	}

}
