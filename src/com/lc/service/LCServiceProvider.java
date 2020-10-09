package com.lc.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;

import com.lc.audio.LCAudio;
import com.lc.common.AppDetails;
import com.lc.common.AppMessageService;
import com.lc.common.CopySettings;
import com.lc.common.LogUtil;
import com.lc.common.Settings;
import com.lc.common.Settings.LogLevel;
import com.lc.common.Util;
import com.lc.common.ZipUnzipUtil;
import com.lc.ui.LCUiCreator;
import com.lc.ui.MainPanel;
import com.lc.ui.SystemTrayPanel;

public class LCServiceProvider {

	private static Timer timer;
	private static int timerIndex = 0;
	private static final char[] symbols = new String("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ")
			.toCharArray();

	/**
	 * This method is used to start the services required by the application, like
	 * logger, database, property reader, networking
	 * 
	 * @throws IOException
	 * 
	 */
	public static void startServices() throws IOException {
		performStartupJobs();
		unzipResources();
		loadAppSettings();
		startTimer();
		setDevModeParams();
		Logger.log("Session Id: " + CopySettings.SESSION_ID);
		Logger.debug("Services initiated");
	}

	private static void setDevModeParams() {
		File f = new File(Util.getselfExecutablePath());
		if(!f.exists()) {
			Settings.DELETE_OLD_LOGS_ON_STARTUP = true;
			Settings.SLOW_COPY_FOR_DEV_TESTING = true;
			Settings.LOG_LEVEL = LogLevel.DEBUG;
		}
		
	}

	private static boolean pasteFileService() {
		if (LCUiCreator.getBackPanel() != null) {
			LCUiCreator.getBackPanel().pasteFiles(CopySettings.getPastePath());
			return true;
		}
		return false;
	}

	private static void performStartupJobs() {
		InitializeSessionId();
		LogUtil.delete();
	}

	private static void InitializeSessionId() {
		Calendar cal = Calendar.getInstance();
		int y = cal.get(Calendar.YEAR) - 2000;
		int d = cal.get(Calendar.DAY_OF_YEAR);
		String h = cal.get(Calendar.HOUR_OF_DAY) > 9 ? "" + cal.get(Calendar.HOUR_OF_DAY)
				: "0" + cal.get(Calendar.HOUR_OF_DAY);
		String mn = cal.get(Calendar.MINUTE) > 9 ? "" + cal.get(Calendar.MINUTE) : "0" + cal.get(Calendar.MINUTE);
		String s = cal.get(Calendar.SECOND) > 9 ? "" + cal.get(Calendar.SECOND) : "0" + cal.get(Calendar.SECOND);

		CopySettings.SESSION_ID = y + "" + d + "-" + h + mn + s;
	}

	public static void startSystemTray() {
		new SystemTrayPanel();
	}

	public static void unzipResources() {
		try {
			String resourcePath = Util.getResourceDirPath();
			String inputZip = Util.getCurrentWorkingDir() + File.separator + "res.zip";
			ZipUnzipUtil.unzip(inputZip, resourcePath, true);
		} catch (Exception e) {
			Logger.warn("Failed to unzip the resources: " + e.getMessage());
			System.out.println(e.getMessage());
		}
	}

	private static void startTimer() {
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				timerIndex++;
				if (timerIndex >= 1000) {
					timerIndex = 100; // iterate between 100 & 1000... 0 to 100 is reserved for initial operations
										// only
				}
				if (timerIndex < 10 && !CopySettings.pasted && CopySettings.isPasteMode()) {
					runScan(true);
				}

				if (timerIndex % 10 == 0) {
					Logger.flushToDlg();
				}
				if (timerIndex % 30 == 0) {
					Logger.flush();
				}
			}

		}, 1000L, 1000L);
	}

	public static void runScan(boolean fromTimer) {
		File folder = new File(Util.getCurrentWorkingDir() + File.separator + "files");
		File[] files = folder.listFiles();

		boolean success = true;
		if (fromTimer) {
			LCUiCreator.startApplication();
		}
		if (files != null && files.length > 0) {
			for (File f : files) {
				Logger.debug("Add file for copy: " + f);
				success = loadFile(f);
				if (!success) {
					break;
				}
			}

			if (success) {
				if(pasteFileService()) {
					CopySettings.pasted = true;
				}
			}
		} else {
			if (CopySettings.isPasteMode()) {
				Logger.info("Exiting since session is in paste mode and no files found for pasting.");
				exitApp();
			}
		}

	}

	private static boolean loadFile(File f) {
		boolean success = false;
		MainPanel backPanel = LCUiCreator.getBackPanel();
		if (backPanel != null) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(f.getAbsolutePath()));
				String line;

				while ((line = br.readLine()) != null) {

					String file = line.trim();
					Logger.debug("passing to backpanel >> " + file);
					backPanel.addSrcFile(file, f.getName());
					success = true;

				}
				br.close();

			} catch (Exception e) {
				Logger.error("Exception: " + e.getMessage());
				System.out.println(e.getMessage());
			}
		} else {
			Logger.debug("Back panel not ready..");
		}
		return success;
	}

	private static void loadAppSettings() {

		try {
			String curDir = Util.getCurrentWorkingDir() + File.separator + "res" + File.separator + "conf"
					+ File.separator + "config.properties";

			File file = new File(curDir);
			if (!file.exists()) {
				Settings.writeAppSettings();
			}

			Settings.loadAppSettings();
		} catch (Exception e) {
			AppMessageService.showError(e.getMessage());
		}
	}

	public static void restartApp() {
		String path = Util.getselfExecutablePath();
		try {
			Runtime.getRuntime().exec(path + " 1");
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(null, AppDetails.appName + " failed to restart.", "Restart Error",
					JOptionPane.OK_OPTION);
		}
		exitApp();
	}

	public static void hideAppGui() {
		new LCAudio(Util.getAudioPath("hide"));
		LCUiCreator.getUiFrame().dispose();
		if (CopySettings.READY_FOR_EXIT) {
			exitApp();
		}
	}

	public static boolean closeApplication() {
		return closeApplication(false);
	}

	public static boolean closeApplication(boolean forceConfirm) {
		if (!forceConfirm && !Settings.CONFIRM_ON_CLOSE) {
			exitApp();
			return false;
		}

		if (AppMessageService.showQMessage("Are you sure, you want to close " + AppDetails.appName + " ? ",
				"Close " + AppDetails.appName)) {
			return exitApp();
		}
		return false;

	}

	public static boolean exitApp() {
		new LCAudio(Util.getAudioPath("stop"));
		try {
			LCUiCreator.hideApplication();
			runExitOperations();
		} catch (Exception e) {
			System.exit(0);
		}
		return false;
	}

	public static void addToCopyList(String path) {
		Logger.debug("Add to copy List: " + path);
		OutputStreamWriter fw = null;
		BufferedWriter bw = null;
		PrintWriter out = null;
		try {
			String fName = Util.getCurrentWorkingDir() + File.separator + "files" + File.separator + getRandomName();
			for (int loopCounter = 0; new File(fName).exists() && loopCounter < 20; ++loopCounter) {
				fName = Util.getCurrentWorkingDir() + File.separator + "files" + File.separator + getRandomName();
			}
			fw = new FileWriter(fName, true);
			bw = new BufferedWriter(fw);
			out = new PrintWriter(bw);
			out.println(path);
			out.close();
			Logger.debug("Added to copy List: " + path);

		} catch (Exception e) {
			Logger.error("Exception: " + e.getMessage());
		} finally {
			if (out != null) {
				out.close();
			}
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (IOException iOException) {
			}
			try {
				if (fw != null) {
					fw.close();
				}
			} catch (IOException iOException) {
			}
		}
	}

	private static String getRandomName() {
		Random random = new Random();
		char[] buf = new char[10];
		for (int idx = 0; idx < buf.length; ++idx) {
			buf[idx] = symbols[random.nextInt(symbols.length)];
		}
		return String.valueOf(new String(buf)) + System.currentTimeMillis();
	}

	public static void runExitOperations() throws FileNotFoundException, IOException, SQLException {
		stopServices();
		SystemTrayPanel.removeSystemTray();
		System.exit(0);
	}

	/**
	 * This method is used to stop all services before closing the application. It
	 * included closing of database connection, network connection.
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws SQLException
	 */
	public static void stopServices() throws FileNotFoundException, IOException, SQLException {
		stopTimer();
		saveSettings();
		zipResources();
	}

	private static void stopTimer() {
		if (timer != null) {
			timer.cancel();
			timer.purge();
		}
	}

	public static void saveSettings() {
		try {
			Settings.writeAppSettings();
			Logger.flush();
			zipResources();
		} catch (Exception e) {
		}
	}

	private static void zipResources() {
		try {
			String resourcePath = Util.getResourceDirPath();
			String outputZip = Util.getCurrentWorkingDir() + File.separator + "res.zip";

			File file = new File(outputZip);
			file.delete();

			ZipUnzipUtil.zip(resourcePath, outputZip, true);
		} catch (Exception e) {
			Logger.warn("Failed to zip the resources: " + e.getMessage());
		}

	}

	public static void emptyBucket(List<String> fileNames) {
		File folder = new File(Util.getCurrentWorkingDir() + File.separator + "files");
		File[] files = folder.listFiles();
		if (files != null && files.length > 0) {
			for (File f : files) {
				if (fileNames.contains(f.getName())) {
					f.delete();
				}
			}
		}
	}

}
