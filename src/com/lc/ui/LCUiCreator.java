package com.lc.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.lc.audio.LCAudio;
import com.lc.common.AppDetails;
import com.lc.common.AppMessageService;
import com.lc.common.CopySettings;
import com.lc.common.Util;
import com.lc.service.LCServiceProvider;

public class LCUiCreator {

	private static JFrame jFrame = null;
	public static int height = 650;
	public static int width = 1050;
	private static MainPanel backUpPanel;

	public LCUiCreator() {
		getJFrame();
	}

	/**
	 * This method initializes jFrame
	 * 
	 * @return javax.swing.JFrame
	 */
	public JFrame getJFrame() {
		if (jFrame == null) {

			try {
				UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(-1, 1, 0, 0));
				UIManager.getDefaults().put("TabbedPane.tabsOverlapBorder", false);
				UIManager.getDefaults().put("TabbedPane.contentOpaque", true);
			} catch (Exception ee) {
				ee.printStackTrace();
			}

			jFrame = new JFrame();
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

			jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			jFrame.setContentPane(getBasePanel());
			jFrame.setBackground(Color.WHITE);
			jFrame.setBounds((screenSize.width - width) / 2, (screenSize.height - height) / 2, width, height);
			jFrame.setPreferredSize(new Dimension(width, height));
			jFrame.setMinimumSize(new Dimension(width, height));
			jFrame.setMaximumSize(new Dimension(width, height));
			jFrame.setResizable(false);
			jFrame.setIconImage(Util.getAppIconImage());
			jFrame.setTitle(AppDetails.appName + " " + AppDetails.appVersion);
			
			jFrame.setJMenuBar(new GuiMenuBar().getJJMenuBar());
			
			
			jFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			jFrame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent arg0) {
					if (CopySettings.isPasteMode()) {
						LCServiceProvider.closeApplication(true);
					} else {
						LCServiceProvider.hideAppGui();
					}
				}
			});
			jFrame.addWindowStateListener(new WindowStateListener() {

				@Override
				public void windowStateChanged(WindowEvent e) {
					if (e.getNewState() == JFrame.ICONIFIED) {
						LCServiceProvider.hideAppGui();
					}
				}

			});

		}

		return jFrame;
	}

	public static JFrame getUiFrame() {
		return jFrame;
	}

	private JPanel getBasePanel() {

		JPanel baseLowerPanel = new JPanel(new BorderLayout());
		baseLowerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		backUpPanel = new MainPanel();

		baseLowerPanel.add(backUpPanel.getPanel(), BorderLayout.CENTER);
		baseLowerPanel.add(new VersionPanel().getPanel(), BorderLayout.PAGE_END);

		baseLowerPanel.setBackground(new Color(180, 180, 180));

		return baseLowerPanel;
	}

	/*
	 * @hint if it is a hint, approximation won't start
	 */
	public static void onFileCopyStarting(File file, boolean hint) {
		if (backUpPanel != null) {
			backUpPanel.onFileCopyStarting(file, hint);
		}
	}

	public static void onAllCopyComplete() {
		if (backUpPanel != null) {
			backUpPanel.onAllCopyComplete();
		}
	}

	public static void onAllCopyStart(int total, long totalBytes) {
		if (backUpPanel != null) {
			backUpPanel.onAllCopyStart(total, totalBytes);
		}
	}

	public static void onFileCopyComplete(int copied, int total, long copiedBytes, long actuallyTransferredBytes,
			long totalBytes, long timeTakenMillis) {
		if (backUpPanel != null) {
			backUpPanel.onFileCopyComplete(copied, total, copiedBytes, actuallyTransferredBytes, totalBytes,
					timeTakenMillis);
		}
	}

	public static void updateWhenPaused() {
		if (backUpPanel != null) {
			backUpPanel.updateWhenPaused();
		}
	}
	
	public static void cancelExitTimer() {
		if (backUpPanel != null) {
			backUpPanel.cancelExitTimer();
		}
	}

	public static MainPanel getBackPanel() {
		return backUpPanel;
	}

	public static void startApplication() {
		new LCAudio(Util.getAudioPath("start"));
		if (jFrame == null) {
			new LCUiCreator();
		}
		AppMessageService.setJFrame(jFrame);
		getUiFrame().setVisible(true);
		if (backUpPanel != null) {
			backUpPanel.refreshSourcePanel();
		}
	}

	public static void hideApplication() {
		new LCAudio(Util.getAudioPath("start"));
		getUiFrame().setVisible(false);
	}

	public static void showLowSpaceWarning(long spaceNeeded) {
		if (backUpPanel != null) {
			backUpPanel.showLowSpaceWarning(spaceNeeded);
		}
		startApplication();
	}

}
