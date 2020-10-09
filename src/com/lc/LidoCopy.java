package com.lc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.lc.common.CopySettings;
import com.lc.service.LCServiceProvider;
import com.lc.service.Logger;

/**
 * 1. Dialog/setting for waiting for copy settings just before paste starts
 * 2. Setting destination path first and then go on a copying spree from different folders.
 * Dropping into lidobasket should start copying immediately in that case.
 * 
 * 
 * @author Nitty
 *
 *   
 */
public class LidoCopy {

	public static void main(String[] args) {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.put("TabbedPane.focus", Color.WHITE);
					UIManager.put("TabbedPane.contentOpaque", true);
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
				} catch (ClassNotFoundException e) {
				} catch (InstantiationException e) {
				} catch (IllegalAccessException e) {
				} catch (UnsupportedLookAndFeelException e) {
				}

				if (args.length > 2) {
					addForCopy(args);
				} else if (args.length == 2) {
					if (args[1].equals("paste")) {
						Logger.debug("Initialized app for file paste: " + args[0]);
						CopySettings.setPastePath(args[0]);
						CopySettings.setPasteMode(true);
						continueApp();
					} else if (args[1].equals("contents")) {
						Logger.debug("Adding file for copy: " + args[0]);
						LCServiceProvider.addToCopyList(args[0] + File.separator + "*");
						LCServiceProvider.exitApp();
					} else {
						addForCopy(args);
					}
				} else if (args.length == 1) {
					addForCopy(args);
				} else {
					continueApp();
				}

			}
		});

	}

	private static void addForCopy(String[] paths) {
		for (String path : paths) {
			if (new File(path).exists()) {
				Logger.debug("Adding file for copy: " + path);
				LCServiceProvider.addToCopyList(path);
			}
		}
		LCServiceProvider.exitApp();
	}

	private static void continueApp() {
		int step = 0;

		try {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			if (screenSize.width < 900 || screenSize.height < 600) {
				throw new Exception();
			}
			step++;
			LCServiceProvider.startServices();

			LCServiceProvider.startSystemTray();

		} catch (Exception e) {
			JFrame frame = new JFrame();
			frame.getContentPane().setBackground(Color.white);
			String errMsg = e.getMessage();
			if (errMsg == null) {
				switch (step) {
				case 0:
					errMsg = "Your monitor resolution is insufficient to run Map Viewer.\n\nPlease upgrade your screen resolution to a minimum of 1120 X 768 pixels.";
					break;
				case 1:
					errMsg = "Error in starting services.";
					break;
				case 2:
					errMsg = "Error starting the User Interface.";
					break;
				default:
					errMsg = "Error.";
					break;
				}
			}
			JOptionPane.showMessageDialog(frame, "The application failed to Load. " + errMsg, "Start Application",
					JOptionPane.ERROR_MESSAGE);

			System.exit(0);
		}
	}

}
