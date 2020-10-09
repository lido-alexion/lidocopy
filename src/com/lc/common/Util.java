package com.lc.common;

import java.awt.Image;
import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.swing.ImageIcon;

public class Util {

	public static DecimalFormat DOUBLE_NUMBER_FORMAT = new DecimalFormat("#.##");
	private static DecimalFormat IMPRECISE_DOUBLE_NUMBER_FORMAT = new DecimalFormat("#");
	private static final String SPACE_STRING = " ";
	private static final Double BYTE = 1.0;
	private static final Double KB = 1024 * BYTE;
	private static final Double MB = KB * KB;
	private static final Double GB = MB * KB;
	private static final Double TB = GB * KB;
	private static final Double PB = TB * KB;
	private static final String BYTE_UNIT = "Byte";
	private static final String BYTES_UNIT = "Bytes";
	private static final String KB_UNIT = "KB";
	private static final String MB_UNIT = "MB";
	private static final String GB_UNIT = "GB";
	private static final String TB_UNIT = "TB";
	private static final String PB_UNIT = "PB";

	public static String getCurrentWorkingDir() {
		return System.getProperty("user.dir");
	}

	public static String getConfFilePath() {
		String path = getCurrentWorkingDir() + File.separator + "res" + File.separator + "conf" + File.separator
				+ "config.properties";
		return path;
	}

	public static String getDbConfFilePath() {
		String path = getCurrentWorkingDir() + File.separator + "res" + File.separator + "conf" + File.separator
				+ "dbConfig.properties";
		return path;
	}

	public static String getselfExecutablePath() {
		String path = System.getProperty("user.dir") + File.separator + AppDetails.selfExecutableName;
		return path;
	}

	public static String getAudioPath(String file) {
		String path = System.getProperty("user.dir") + File.separator + "res" + File.separator + "media"
				+ File.separator + file + ".wav";
		return path;
	}

	public static String getHelpFilePath() {
		String path = System.getProperty("user.dir") + File.separator + "res" + File.separator + "help" + File.separator
				+ "help.pdf";
		return path;
	}

	public static int getDayCount(int date, int month) {
		return (month - 1) * 31 + date;
	}

	public static int getDayCount(int date, int month, int year) {
		int dayCnt = (month - 1) * 31 + date;
		return Integer.valueOf(year + "" + dayCnt);
	}

	public static String escapeForDb(String text) {
		if (text == null) {
			return "";
		}

		text = text.trim();
		text = text.replaceAll("\\\\", "\\\\\\\\");
		text = text.replaceAll("'", "\\\\'");
		text = text.replaceAll("\\\\\"", "\\\\\\\\\"");
		return text;
	}

	public static String escapeForApp(String text) {
		if (text == null) {
			return "";
		}

		text = text.replaceAll("\'", "'");
		text = text.replaceAll("\\\"", "\"");
		return text;
	}

	// abc -> bca
	public static String getCrypted(String text) {

		StringBuffer reverse = new StringBuffer(text).reverse();
		char[] chars = reverse.toString().toCharArray();
		if (chars.length > 3) {
			int tail = chars.length % 3;
			for (int i = 0; i < chars.length - 2 - tail; i = i + 3) {
				char temp = chars[i];
				chars[i] = chars[i + 1];
				chars[i + 1] = chars[i + 2];
				chars[i + 2] = temp;
			}
		}

		return new String(chars);
	}

	// bca -> abc
	public static String getDecrypted(String text) {

		char[] chars = text.toString().toCharArray();
		if (chars.length > 3) {
			int tail = chars.length % 3;
			for (int i = 0; i < chars.length - 2 - tail; i = i + 3) {
				char temp = chars[i]; // b bca
				chars[i] = chars[i + 2]; // aca
				chars[i + 2] = chars[i + 1];
				chars[i + 1] = temp;// aba
			}
		}

		return new StringBuffer(new String(chars)).reverse().toString();
	}

	public static String getMD5(String str) {
		MessageDigest m;
		String md5 = null;
		try {
			m = MessageDigest.getInstance("MD5");
			m.update(str.getBytes(), 0, str.length());
			md5 = new BigInteger(1, m.digest()).toString(16).toUpperCase();

			while (md5.length() < 32) {
				md5 = "0" + md5;
			}
		} catch (NoSuchAlgorithmException e) {
			md5 = str;
		}
		return md5;
	}

	public static Image getAppIconImage() {
		String curDir = getCurrentWorkingDir() + File.separator + "res" + File.separator + "img" + File.separator
				+ "icon.PNG";

		return new ImageIcon(curDir).getImage();
	}

	public static Image getStoppedTrayImage() {
		String curDir = getCurrentWorkingDir() + File.separator + "res" + File.separator + "img" + File.separator
				+ "stoppedIcon.PNG";

		return new ImageIcon(curDir).getImage();
	}

	public static String getImageDirPath() {
		String path = getCurrentWorkingDir() + File.separator + "res" + File.separator + "img" + File.separator;
		return path;
	}

	public static String getStartMenuPath() {
		Map<String, String> env = System.getenv();

		String path = env.get("USERPROFILE") + File.separator
				+ "AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup" + File.separator;

		return path;
	}

	/**
	 * 
	 * @param month
	 *            : 1-jan, 2-feb.... (1..12)
	 * @param year
	 *            : 2008, 2009.... etc.
	 * @return day : 1-sun, 2-mon .... (1..7)
	 */
	@SuppressWarnings("deprecation")
	public static int getStartDayOfMonth(int month, int year) {
		year = year - 1900;
		month = month - 1;
		Date dt = new Date();
		dt.setDate(1);
		dt.setMonth(month);
		dt.setYear(year);
		return (dt.getDay() + 1);
	}

	/**
	 * 
	 * @param month
	 *            : 1-jan, 2-feb.... (1..12)
	 * @param year
	 *            : 2008, 2009.... etc.
	 * @return 28..31
	 */
	public static int getTotalDaysOfMonth(int month, int year) {

		if (month % 2 == 1) {
			if (month < 8) {
				return 31;
			} else {
				return 30;
			}
		} else if (month == 2) {
			if (year % 100 == 0) {
				if (year % 400 == 0) {
					return 29;
				} else {
					return 28;
				}
			} else {
				if (year % 4 == 0) {
					return 29;
				} else {
					return 28;
				}
			}
		} else {
			if (month < 7) {
				return 30;
			} else {
				return 31;
			}
		}
	}

	public static String getHour12h(int hour) {
		if (hour == 0) {
			hour = 12;
		} else if (hour > 12) {
			hour = hour - 12;
		}
		return (hour < 10 ? "0" : "") + hour;
	}

	/**
	 * 
	 * @param hour
	 *            0-23 (24h format)
	 * @param min
	 *            0-59
	 * @return AM or PM
	 */
	public static String getAmPm(int hour, int min) {
		String amPm = "AM";

		if (hour == 12) {
			if (min > 0) {
				amPm = "PM";
			}
		} else if (hour > 12) {
			amPm = "PM";
		}
		return amPm;
	}

	public static int getHour24h(int hour, String amPm) {

		if (hour == 12 && amPm.equals("AM")) {
			hour = 0;
		} else if (amPm.equals("PM") && hour < 12) {
			hour = hour + 12;
		}
		return hour;
	}

	public static String getFormattedTime(long millis) {
		SimpleDateFormat ft = new SimpleDateFormat("hh:mm a (EEEE)");
		return ft.format(new Date(millis));
	}

	public static String getFormattedDuration(long millis) {
		if (millis <= 1000) {
			return "Hold on tiger, almost done.";
		} else {
			long sec = millis / 1000;
			if (sec < 60) {
				return sec > 1 ? sec + " seconds" : sec + " second";
			} else {
				long min = sec / 60;
				if (min < 60) {
					String minsStr = min > 1 ? min + " minutes" : min + " minute";
					if (min > 5) {
						return minsStr;
					} else {
						sec = sec % 60;
						if (sec > 0) {
							String secStr = sec > 1 ? sec + " seconds" : sec + " second";
							return minsStr + " " + secStr;
						} else {
							return minsStr;
						}
					}
				} else {
					long hours = ((long) (min / 60));
					if (hours > 24) {
						long day = hours / 24;
						return day > 1 ? day + " days" : day + " day";
					} else {
						return hours > 1 ? hours + " hours" : hours + " hour";
					}
				}
			}
		}
	}

	public static String getFormattedDateTime(long millis) {
		SimpleDateFormat ft = new SimpleDateFormat("hh:mm a, MMM d, yyyy");
		return ft.format(new Date(millis));
	}

	public static String getFullFormattedDateTime(long millis) {
		SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
		return ft.format(new Date(millis));
	}

	public static String getFormatedStringForByte(long sizeInByte, long precisionMarkSize) {
		boolean precisionNeeded = false;
		String unit = getSizeUnit(sizeInByte);
		String precisionUnit = getSizeUnit(precisionMarkSize);
		if (unit.equals(precisionUnit) || precisionUnit.equals(BYTES_UNIT) || precisionUnit.equals(BYTE_UNIT)
				|| precisionUnit.equals(KB_UNIT)) {
			precisionNeeded = true;
		}
		return getFormatedBytesString(sizeInByte, precisionNeeded);
	}

	private static String getSizeUnit(long sizeInByte) {
		if (sizeInByte >= PB) {
			return PB_UNIT;
		} else if (sizeInByte >= TB) {
			return TB_UNIT;
		} else if (sizeInByte >= GB) {
			return GB_UNIT;
		} else if (sizeInByte >= MB) {
			return MB_UNIT;
		} else if (sizeInByte >= KB) {
			return KB_UNIT;
		} else {
			if (sizeInByte > 1) {
				return BYTES_UNIT;
			} else if (sizeInByte == 1) {
				return BYTE_UNIT;
			} else {
				return KB_UNIT;
			}
		}
	}

	public static String getFormatedStringForByte(long sizeInByte) {
		return getFormatedBytesString(sizeInByte, true);
	}

	public static String getFormatedBytesString(long sizeInByte, boolean needPrecision) {
		if (sizeInByte >= PB) {
			return getRoundedDouble((sizeInByte / PB), needPrecision) + SPACE_STRING + PB_UNIT;
		} else if (sizeInByte >= TB) {
			return getRoundedDouble((sizeInByte / TB), needPrecision) + SPACE_STRING + TB_UNIT;
		} else if (sizeInByte >= GB) {
			return getRoundedDouble((sizeInByte / GB), needPrecision) + SPACE_STRING + GB_UNIT;
		} else if (sizeInByte >= MB) {
			return getRoundedDouble((sizeInByte / MB), needPrecision) + SPACE_STRING + MB_UNIT;
		} else if (sizeInByte >= KB) {
			return Math.round(sizeInByte / KB) + SPACE_STRING + KB_UNIT;
		} else {
			if (sizeInByte > 1) {
				return sizeInByte + SPACE_STRING + BYTES_UNIT;
			} else if (sizeInByte == 1) {
				return sizeInByte + SPACE_STRING + BYTE_UNIT;
			} else {
				return 0 + SPACE_STRING + KB_UNIT;
			}
		}
	}

	private static String getRoundedDouble(double input, boolean needPrecision) {
		return needPrecision ? DOUBLE_NUMBER_FORMAT.format(input) : IMPRECISE_DOUBLE_NUMBER_FORMAT.format(input);
	}

	public static String getResourceDirPath() {
		String path = getCurrentWorkingDir() + File.separator + "res";
		return path;
	}

	public static Image getPendingTrayImage() {
		String curDir = getCurrentWorkingDir() + File.separator + "res" + File.separator + "img" + File.separator
				+ "qIcon.png";

		return new ImageIcon(curDir).getImage();
	}

	public static String getFormattedCopySpeed(long speedinBytesPerMillis) {

		if (speedinBytesPerMillis > KB) {
			long speed = speedinBytesPerMillis*1000;
			String speedps = "";
			if (speed > GB) {
				speedps = getRoundedDouble((speed / GB), true) + SPACE_STRING + GB_UNIT.toLowerCase() + "/s";
			} else if (speed > MB){
				speedps = getRoundedDouble((speed / MB), true) + SPACE_STRING + MB_UNIT.toLowerCase() + "/s";
			}else {
				speedps = getRoundedDouble((speed / KB), true) + SPACE_STRING + KB_UNIT.toLowerCase() + "/s";
			}
			return speedps;
		}

		return "";
	}

	public static String getColonFormattedDuration(long millis) {
		long secs = millis / 1000;
		if (secs < 0) {
			return "Calculating...";
		}else if (secs == 0) {
			return "00:00:00";
		} else if (secs < 60) {
			return secs > 9 ? "00:00:" + secs : "00:00:0" + secs;
		} else {
			long mins = secs / 60;
			secs = secs % 60;
			long hours = mins / 60;
			mins = mins % 60;
			String sex = secs > 9 ? "" + secs : "0" + secs;
			String mix = mins > 9 ? "" + mins : "0" + mins;
			String hex = hours > 9 ? "" + hours : "0" + hours;
			return hex + ":" + mix + ":" + sex;
		}
	}

}
