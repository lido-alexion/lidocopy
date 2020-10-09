package com.lc.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.lc.core.DstExistence;
import com.lc.core.Message;

public class CopySettings {

	public static enum State {
		STARTING, COPYING, PAUSED, PAUSING, CANCELING, CANCELLED, COMPLETE, IDLE
	};
	
	public static enum MultiSourceCopyActionEnum {
		RENAME_AND_COPY_ALL, RENAME_AND_COPY_IF_DISTINCT, COPY_IF_LATEST, CUSTOM_SELECTION
	};

	public static final boolean IGNORE_FREE_SPACE_WARNING = false;

	public static boolean RENAME_SKIPPED_FILES = false;
	public static boolean ASK_FOR_EACH_FILE = true;

	public static MultiSourceCopyActionEnum COPY_MULTIPLE_FILES_TO_SAME_PATH__ACTION_SELECTED = MultiSourceCopyActionEnum.RENAME_AND_COPY_ALL;
//	public static boolean COPY_MULTIPLE_FILES_TO_SAME_PATH__ASK_FOR_EACH_FILE = true;

	public static boolean SKIP_SAME_FILE_IF_EXISTS = true;
	public static boolean RETAIN_SOURCE_FILE_AFTER_COPY = true;

	public static final String SAME_SIZE_OLDER_DATE = "SAME_SIZE_OLDER_DATE";
	public static final String SAME_SIZE_LATEST_DATE = "SAME_SIZE_LATEST_DATE";
	public static final String SAME_SIZE_SAME_DATE = "SAME_SIZE_SAME_DATE";
	public static final String DIFFERENT_SIZE_OLDER_DATE = "DIFFERENT_SIZE_OLDER_DATE";
	public static final String DIFFERENT_SIZE_LATEST_DATE = "DIFFERENT_SIZE_LATEST_DATE";
	public static final String DIFFERENT_SIZE_SAME_DATE = "DIFFERENT_SIZE_SAME_DATE";

	public static final List<String> IGNORE_SRC_FILES = new ArrayList<String>();

	private static String PASTE_INVOKED_PATH;
	private static boolean isPasteMode = false;
	public static int DROPPED_FILE_COUNT = 0;
	public static boolean pasted = false;
	public static boolean isCopyBlocked = false;
	
	public static String SESSION_ID = "#";

	public static State status = State.IDLE;

	public static HashMap<String, List<DstExistence>> overwriteTypeToFilesMap = new HashMap<String, List<DstExistence>>();
	public static List<String> FILES_TO_OVERWRITE = new ArrayList<String>();
	
	public static List<Message> copyNotifications = new ArrayList<Message>();

	public static boolean READY_FOR_EXIT = false;
	
	public static long PROGRESS_UPDATE_INTERVAL = 500; // 0.5 sec

	public static String getPastePath() {
		return PASTE_INVOKED_PATH;
	}

	public static void setPastePath(String path) {
		PASTE_INVOKED_PATH = path;
	}

	public static void setPasteMode(boolean pasteMode) {
		isPasteMode = pasteMode;
	}

	public static boolean isPasteMode() {
		return isPasteMode;
	}

}
