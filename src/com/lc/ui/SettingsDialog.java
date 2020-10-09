package com.lc.ui;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.lc.common.DBUMessage;
import com.lc.common.Settings;
import com.lc.common.Settings.LogLevel;
import com.lc.common.StartOnLogon;
import com.lc.common.Util;
import com.lc.service.LCServiceProvider;

public class SettingsDialog {

	private JDialog dialog;
	private JButton okButton = null;
	private JButton cancelButton = null;
	private Font font;
	private Font bfont;
	private JFrame jframe;
	JCheckBox autoLoginOnStartup;
	JCheckBox showDlgOnStartup;
	JCheckBox muteCheck;
	JComboBox<String> logLevelCombo;
	JCheckBox confirmClosing;

	JCheckBox checkDiskSpaceBeforeCopy;
	JCheckBox autoStartCopyOnPaste;

	JCheckBox useHashForFileCompare;
	JCheckBox testFileIntegrityAfterCopy;
	JTextField hashFileSizeLimit;
	JTextField hashFileTypes;
	JLabel hashLimitLabel;
	JLabel sizeUnitLabel;
	JLabel hashFileTypesLabel;

	boolean isStartOnLogonEnabled;

	int width = 600, height = 530;

	public SettingsDialog() {

		LCUiCreator.cancelExitTimer();
		isStartOnLogonEnabled = StartOnLogon.isStartOnLogonEnabled();

		jframe = LCUiCreator.getUiFrame();
		font = new Font("Arial", Font.PLAIN, 13);
		bfont = new Font("Arial", Font.BOLD, 13);

		displayDlg();
	}

	public void displayDlg() {
		if (null == dialog) {
			dialog = new JDialog(jframe, "Preferences", Dialog.ModalityType.DOCUMENT_MODAL);
			Dimension dimension = new Dimension(width, height);
			dialog.pack();
			dialog.setLocationRelativeTo(null);
			dialog.setResizable(false);

			Rectangle bounds = new Rectangle(0, 0, width, height);
			Dimension screensz = Toolkit.getDefaultToolkit().getScreenSize();
			bounds.x = Math.max(0, (screensz.width - width) / 2);
			bounds.y = Math.max(0, (screensz.height - height) / 2);
			bounds.width = Math.min(bounds.width, screensz.width);
			bounds.height = Math.min(bounds.height, screensz.height);
			dialog.setBounds(bounds);
			dialog.setIconImage(Util.getAppIconImage());
			dialog.setLayout(null);
			dialog.add(getOkButton());
			dialog.add(getCancelButton());

			dialog.add(getAppSettingsPanel());
			dialog.add(getCopySettingsPanel());

			dialog.setPreferredSize(dimension);
			dialog.setMaximumSize(dimension);
			dialog.setMinimumSize(dimension);
			dialog.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					e.getWindow().dispose();
				}
			});

			onHashChange();

			dialog.setVisible(true);
		}
	}

	private JPanel getAppSettingsPanel() {
		JPanel appSettingsPane = new JPanel(null);
		TitledBorder b1 = BorderFactory.createTitledBorder("Application Preferences");
		b1.setTitleColor(Color.BLACK);
		b1.setTitleFont(bfont);
		appSettingsPane.setBorder(b1);
		appSettingsPane.setBounds(10, 10, width - 25, 190);

		autoLoginOnStartup = new JCheckBox("Automatically start after Windows boot", isStartOnLogonEnabled);
		autoLoginOnStartup.setFont(font);
		autoLoginOnStartup.setBounds(10, 30, 280, 25);
		appSettingsPane.add(autoLoginOnStartup);

		showDlgOnStartup = new JCheckBox("Show dialog on startup", Settings.LOAD_ON_START);
		showDlgOnStartup.setFont(font);
		showDlgOnStartup.setBounds(10, 60, 280, 25);
		appSettingsPane.add(showDlgOnStartup);

		muteCheck = new JCheckBox("Mute sound", Settings.SOUND_MUTED);
		muteCheck.setFont(font);
		muteCheck.setBounds(10, 90, 280, 25);
		appSettingsPane.add(muteCheck);

		confirmClosing = new JCheckBox("Confirm before closing", Settings.CONFIRM_ON_CLOSE);
		confirmClosing.setFont(font);
		confirmClosing.setBounds(10, 120, 280, 25);
		appSettingsPane.add(confirmClosing);

		JLabel filterLabel = new JLabel("Filter logs:");
		filterLabel.setFont(font);
		filterLabel.setBounds(15, 150, 70, 25);
		appSettingsPane.add(filterLabel);

		logLevelCombo = new JComboBox<String>();
		logLevelCombo.addItem("ERROR");
		logLevelCombo.addItem("WARN");
		logLevelCombo.addItem("INFO");
		logLevelCombo.addItem("DEBUG");
		logLevelCombo.addItem("OFF");

		if (Settings.LOG_LEVEL == LogLevel.ERROR) {
			logLevelCombo.setSelectedItem("ERROR");
		} else if (Settings.LOG_LEVEL == LogLevel.WARN) {
			logLevelCombo.setSelectedItem("WARN");
		} else if (Settings.LOG_LEVEL == LogLevel.INFO) {
			logLevelCombo.setSelectedItem("INFO");
		} else if (Settings.LOG_LEVEL == LogLevel.DEBUG) {
			logLevelCombo.setSelectedItem("DEBUG");
		} else {
			logLevelCombo.setSelectedItem("OFF");
		}

		logLevelCombo.setBounds(85, 150, 90, 25);
		logLevelCombo.setFont(font);
		appSettingsPane.add(logLevelCombo);

		return appSettingsPane;
	}

	private JPanel getCopySettingsPanel() {
		JPanel copySettingsPane = new JPanel(null);
		TitledBorder b2 = BorderFactory.createTitledBorder("Copy Preferences");
		b2.setTitleColor(Color.BLACK);
		b2.setTitleFont(bfont);
		copySettingsPane.setBorder(b2);
		copySettingsPane.setBounds(10, 220, width - 20, 220);

		checkDiskSpaceBeforeCopy = new JCheckBox("Check space availability before copy",
				Settings.CHECK_AND_WARN_DISK_SPACE_BEFORE_COPY_START);
		checkDiskSpaceBeforeCopy.setFont(font);
		checkDiskSpaceBeforeCopy.setBounds(10, 30, 280, 25);
		copySettingsPane.add(checkDiskSpaceBeforeCopy);

		autoStartCopyOnPaste = new JCheckBox("Start copying automatically when pasted",
				Settings.START_COPY_ON_PASTE_AUTOMATICALLY);
		autoStartCopyOnPaste.setFont(font);
		autoStartCopyOnPaste.setBounds(10, 60, 280, 25);
		copySettingsPane.add(autoStartCopyOnPaste);

		useHashForFileCompare = new JCheckBox("Use file hashing for file comparison",
				Settings.USE_HASH_FOR_FILE_IDENTITY);
		useHashForFileCompare.setFont(font);
		useHashForFileCompare.setBounds(10, 90, 280, 25);
		copySettingsPane.add(useHashForFileCompare);
		useHashForFileCompare.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				onHashChange();
			}
		});

		hashLimitLabel = new JLabel("Use hash for files with size upto:");
		hashLimitLabel.setBounds(52, 120, 190, 25);
		hashLimitLabel.setFont(font);
		copySettingsPane.add(hashLimitLabel);

		hashFileSizeLimit = new JTextField();
		hashFileSizeLimit.setText((Settings.FILE_SIZE_LIMIT_FOR_HASH_CALC / (1024 * 1024)) + "");
		hashFileSizeLimit.setFont(font);
		hashFileSizeLimit.setBounds(250, 120, 50, 25);
		hashFileSizeLimit.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				char caracter = e.getKeyChar();
				if (((caracter < '0') || (caracter > '9')) && (caracter != '\b')) {
					e.consume();
				}
			}
		});
		copySettingsPane.add(hashFileSizeLimit);

		sizeUnitLabel = new JLabel("MB");
		sizeUnitLabel.setBounds(305, 120, 100, 25);
		sizeUnitLabel.setFont(font);
		copySettingsPane.add(sizeUnitLabel);

		hashFileTypesLabel = new JLabel("File types (comma-separated) to be hashed:");
		hashFileTypesLabel.setBounds(52, 150, 260, 25);
		hashFileTypesLabel.setFont(font);
		copySettingsPane.add(hashFileTypesLabel);

		hashFileTypes = new JTextField();
		hashFileTypes.setBounds(315, 150, 200, 25);
		hashFileTypes.setFont(font);
		hashFileTypes.setToolTipText("Comma-separated file extensions");
		hashFileTypes.setText(Settings.getFileTypesStringalised());
		copySettingsPane.add(hashFileTypes);

		testFileIntegrityAfterCopy = new JCheckBox("Test file integrity after copy",
				Settings.TEST_FILE_INTEGRITY_USING_HASH);
		testFileIntegrityAfterCopy.setFont(font);
		testFileIntegrityAfterCopy.setBounds(30, 180, 280, 25);
		copySettingsPane.add(testFileIntegrityAfterCopy);

		return copySettingsPane;
	}

	private void onHashChange() {
		hashFileSizeLimit.setEnabled(useHashForFileCompare.isSelected());
		hashFileTypes.setEnabled(useHashForFileCompare.isSelected());
		testFileIntegrityAfterCopy.setEnabled(useHashForFileCompare.isSelected());
		hashLimitLabel
				.setForeground(useHashForFileCompare.isSelected() ? autoLoginOnStartup.getForeground() : Color.gray);
		sizeUnitLabel
				.setForeground(useHashForFileCompare.isSelected() ? autoLoginOnStartup.getForeground() : Color.gray);
		hashFileTypesLabel
				.setForeground(useHashForFileCompare.isSelected() ? autoLoginOnStartup.getForeground() : Color.gray);

	}

	private void commit() {

		if (autoLoginOnStartup.isSelected()) {
			StartOnLogon.setStartOnLogon();
		} else {
			StartOnLogon.setDontStartOnLogon();
		}

		Settings.LOAD_ON_START = showDlgOnStartup.isSelected();
		Settings.SOUND_MUTED = muteCheck.isSelected();
		Settings.LOG_LEVEL = getSelectedLogLevel();
		Settings.CONFIRM_ON_CLOSE = confirmClosing.isSelected();

		Settings.CHECK_AND_WARN_DISK_SPACE_BEFORE_COPY_START = checkDiskSpaceBeforeCopy.isSelected();
		Settings.START_COPY_ON_PASTE_AUTOMATICALLY = autoStartCopyOnPaste.isSelected();
		Settings.USE_HASH_FOR_FILE_IDENTITY = useHashForFileCompare.isSelected();
		Settings.TEST_FILE_INTEGRITY_USING_HASH = testFileIntegrityAfterCopy.isSelected();

		int mbs = 0;
		try {
			String mbsStr = hashFileSizeLimit.getText();
			mbs = mbsStr == null || mbsStr.trim().equals("") ? 0 : Integer.valueOf(mbsStr);
			Settings.FILE_SIZE_LIMIT_FOR_HASH_CALC = Integer.valueOf(mbs) * 1024 * 1024;
		} catch (Exception e) {
		}

		Settings.HASH_CHECK_ALLOWED_FILE_TYPES.clear();
		String extns = hashFileTypes.getText();
		String[] extnsArr = extns != null ? extns.split(",") : null;
		if (extnsArr != null && extnsArr.length > 0) {
			for (String s : extnsArr) {
				Settings.HASH_CHECK_ALLOWED_FILE_TYPES.add(s.trim());
			}
		}

		try {
			LCServiceProvider.saveSettings();
		} catch (Exception e) {
			DBUMessage.showError("Failed to save the changes permanently: " + e.getMessage()
					+ " However, the settings are applied for the current session.");
		}
	}

	private LogLevel getSelectedLogLevel() {

		if (logLevelCombo.getSelectedItem().equals("ERROR")) {
			return LogLevel.ERROR;
		} else if (logLevelCombo.getSelectedItem().equals("WARN")) {
			return LogLevel.WARN;
		} else if (logLevelCombo.getSelectedItem().equals("INFO")) {
			return LogLevel.INFO;
		} else if (logLevelCombo.getSelectedItem().equals("DEBUG")) {
			return LogLevel.DEBUG;
		}

		return LogLevel.OFF;
	}

	private JButton getOkButton() {
		if (null == okButton) {
			okButton = new JButton();
			okButton.setFont(font);
			okButton.setBounds(new Rectangle(width / 2 - 10 - 90, height - 70, 90, 25));
			okButton.setText("Save");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dialog.dispose();
					commit();
				}
			});
		}
		return okButton;
	}

	private JButton getCancelButton() {
		if (null == cancelButton) {
			cancelButton = new JButton();
			cancelButton.setFont(font);
			cancelButton.setBounds(new Rectangle(width / 2 + 10, height - 70, 90, 25));
			cancelButton.setText("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					dialog.dispose();
				}
			});
		}
		return cancelButton;
	}

}
