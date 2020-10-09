package com.lc.common;

import java.io.File;
import java.io.FileOutputStream;

public class StartOnLogon {

	public static void setStartOnLogon() {

		String targetPath = Util.getselfExecutablePath();
		String userDirPath = Util.getCurrentWorkingDir();
		String startMenuPath = Util.getStartMenuPath();
		String script = "Set sh = CreateObject(\"WScript.Shell\")" + "\nSet shortcut = sh.CreateShortcut(\""
				+ startMenuPath + AppDetails.selfExecutableShortName + ".lnk\")" + "\nshortcut.TargetPath = \""
				+ targetPath + "\"" + "\nshortcut.WorkingDirectory = \"" + userDirPath + "\"" + "\nshortcut.Save";

		File file = new File(startMenuPath + "temp.vbs");
		FileOutputStream fo;
		try {
			fo = new FileOutputStream(file);
			fo.write(script.getBytes());
			fo.close();
			Process p = Runtime.getRuntime().exec("wscript.exe \"" + file.getAbsolutePath() + "\"");
			p.waitFor();
			file.delete();
		} catch (Exception e) {

		}
	}

	public static void setDontStartOnLogon() {
		String autoStartLinkPath = Util.getStartMenuPath() + AppDetails.selfExecutableShortName + ".lnk";
		File file = new File(autoStartLinkPath);
		file.delete();
	}

	public static boolean isStartOnLogonEnabled() {
		String autoStartLinkPath = Util.getStartMenuPath() + AppDetails.selfExecutableShortName + ".lnk";
		File file = new File(autoStartLinkPath);
		return file.exists();
	}

}
