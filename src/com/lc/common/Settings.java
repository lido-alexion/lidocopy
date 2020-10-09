package com.lc.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class Settings {

	/**
	 *  Dev Testing control variables 
	 */	
	public static boolean DELETE_OLD_LOGS_ON_STARTUP = false; // XXX false in prod
	public static boolean SLOW_COPY_FOR_DEV_TESTING = false; // XXX false in prod
	
	//---------------------------------------------------------------------------------

	public static final boolean REPLACE_ALL_FILES_SELECTION_WITH_WILDCARD = true;

	public static boolean SOUND_MUTED = false;
	public static boolean LOAD_ON_START = false;
	public static boolean CONFIRM_ON_CLOSE = false;
	public static HashMap<String, Long> DRIVE_TO_COPY_SPEED_MAP = new HashMap<String, Long>();

	public static boolean USE_HASH_FOR_FILE_IDENTITY = true; // if false, below variables bear no meaning
	public static long FILE_SIZE_LIMIT_FOR_HASH_CALC = 15 * 1024 * 1024; // 15 MB
	public static List<String> HASH_CHECK_ALLOWED_FILE_TYPES = new ArrayList<>();
	public static boolean TEST_FILE_INTEGRITY_USING_HASH = true; // test after copy; this can be used only for files
																	// that pass above has hashing criteria

	public static boolean CHECK_AND_WARN_DISK_SPACE_BEFORE_COPY_START = true; // before starting any copy activity
	public static boolean START_COPY_ON_PASTE_AUTOMATICALLY = true;

	public static enum LogLevel {
		ERROR, WARN, INFO, DEBUG, OFF
	}

	public static LogLevel LOG_LEVEL = LogLevel.INFO;

	// -------------------------------------------------------------------------------

	private static String MUTE_KEY = "soundMuted";
	private static String LOG_KEY = "logLevel";
	private static String LOAD_KEY = "loadGui";
	private static String CLOSE_CONFIRM_KEY = "confirmOnClose";
	private static String COPY_SPEED = "copySpeed";

	private static String USE_HASH_KEY = "useHash";
	private static String HASH_FILESIZE_KEY = "hashFilesize";
	private static String HASH_FILETYPES_KEY = "hashFiletypes";
	private static String HASH_TEST_INTEGRITY_KEY = "hashTestIntegrity";
	private static String CHECK_SPACE_KEY = "checkSpaceBeforeCopy";
	private static String COPY_ON_PASTE_KEY = "startCopyOnPaste";
	
	public static void writeAppSettings() throws FileNotFoundException, IOException {

		String curDir = Util.getConfFilePath();

		Properties prop = new Properties();
		prop.setProperty(MUTE_KEY, String.valueOf(SOUND_MUTED));
		prop.setProperty(LOG_KEY, String.valueOf(LOG_LEVEL));
		prop.setProperty(LOAD_KEY, String.valueOf(LOAD_ON_START));
		prop.setProperty(CLOSE_CONFIRM_KEY, String.valueOf(CONFIRM_ON_CLOSE));
		prop.setProperty(COPY_SPEED, getCopySpeedStringalised());

		prop.setProperty(USE_HASH_KEY, String.valueOf(USE_HASH_FOR_FILE_IDENTITY));
		prop.setProperty(HASH_FILESIZE_KEY, String.valueOf(FILE_SIZE_LIMIT_FOR_HASH_CALC));
		prop.setProperty(HASH_FILETYPES_KEY, String.valueOf(getFileTypesStringalised()));
		prop.setProperty(HASH_TEST_INTEGRITY_KEY, String.valueOf(TEST_FILE_INTEGRITY_USING_HASH));
		prop.setProperty(CHECK_SPACE_KEY, String.valueOf(CHECK_AND_WARN_DISK_SPACE_BEFORE_COPY_START));
		prop.setProperty(COPY_ON_PASTE_KEY, String.valueOf(START_COPY_ON_PASTE_AUTOMATICALLY));

		FileOutputStream stream = new FileOutputStream(curDir);
		prop.store(stream, "App settings details");
		stream.close();

	}

	public static void loadAppSettings() throws IOException, Exception {

		String curDir = Util.getCurrentWorkingDir() + File.separator + "res" + File.separator + "conf" + File.separator
				+ "config.properties";

		FileInputStream stream = new FileInputStream(curDir);
		Properties properties = new Properties();
		properties.load(stream);

		SOUND_MUTED = Boolean.valueOf(properties.getProperty(MUTE_KEY));
		LOG_LEVEL = parseLogLevel(properties.getProperty(LOG_KEY));
		LOAD_ON_START = Boolean.valueOf(properties.getProperty(LOAD_KEY));
		CONFIRM_ON_CLOSE = Boolean.valueOf(properties.getProperty(CLOSE_CONFIRM_KEY));
		unstringalizeCopySpeed(properties.getProperty(COPY_SPEED));

		USE_HASH_FOR_FILE_IDENTITY = Boolean.valueOf(properties.getProperty(USE_HASH_KEY));
		try {
			FILE_SIZE_LIMIT_FOR_HASH_CALC = Integer.valueOf(properties.getProperty(HASH_FILESIZE_KEY));
		} catch (Exception e) {
			FILE_SIZE_LIMIT_FOR_HASH_CALC = 0;
		}
		HASH_CHECK_ALLOWED_FILE_TYPES = getFileTypesUnstringalised(properties.getProperty(HASH_FILETYPES_KEY));
		TEST_FILE_INTEGRITY_USING_HASH = Boolean.valueOf(properties.getProperty(HASH_TEST_INTEGRITY_KEY));
		CHECK_AND_WARN_DISK_SPACE_BEFORE_COPY_START = Boolean.valueOf(properties.getProperty(CHECK_SPACE_KEY));
		START_COPY_ON_PASTE_AUTOMATICALLY = Boolean.valueOf(properties.getProperty(COPY_ON_PASTE_KEY));

		if (!HASH_CHECK_ALLOWED_FILE_TYPES.contains(".jpg")) {
			HASH_CHECK_ALLOWED_FILE_TYPES.add(".jpg");
		}
	}

	private static LogLevel parseLogLevel(String logLevel) {	
			if (logLevel.equals("ERROR")) {
				return LogLevel.ERROR;
			} else if (logLevel.equals("WARN")) {
				return LogLevel.WARN;
			} else if (logLevel.equals("INFO")) {
				return LogLevel.INFO;
			} else if (logLevel.equals("DEBUG")) {
				return LogLevel.DEBUG;
			}
			return LogLevel.OFF;		
	}
	
	private static void unstringalizeCopySpeed(String speedString) {
		DRIVE_TO_COPY_SPEED_MAP.clear();
		if (speedString != null) {
			String[] vals = speedString.split(";");
			if (vals != null) {
				for (String val : vals) {
					if (val.length() > 0) {
						String[] parts = val.split("#");
						try {
							if (parts.length > 1) {
								DRIVE_TO_COPY_SPEED_MAP.put(parts[0], Long.valueOf(parts[1]));
							}
						} catch (Exception e) {
						}
					}
				}
			}
		}
	}

	private static String getCopySpeedStringalised() {
		Set<String> keys = DRIVE_TO_COPY_SPEED_MAP.keySet();
		String str = "";
		if (keys.size() > 0) {
			for (String drives : keys) {
				str += drives + "#" + DRIVE_TO_COPY_SPEED_MAP.get(drives) + ";";
			}
			str = str.substring(0, str.length() - 1);
		}
		return str;
	}

	public static String getFileTypesStringalised() {
		String str = "";
		if (HASH_CHECK_ALLOWED_FILE_TYPES != null && HASH_CHECK_ALLOWED_FILE_TYPES.size() > 0) {
			for (String s : HASH_CHECK_ALLOWED_FILE_TYPES) {
				if (str.length() > 0) {
					str += ",";
				}
				str += s;
			}
		}
		return str;
	}

	private static List<String> getFileTypesUnstringalised(String str) {
		List<String> list = new ArrayList<String>();

		if (str != null) {
			String[] strs = str.split(",");
			if (strs != null && strs.length > 0) {
				for (String s : strs) {
					list.add(s);
				}
			}
		}
		return list;
	}

}
