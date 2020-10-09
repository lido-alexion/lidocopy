package com.lc.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import com.lc.common.CopySettings;
import com.lc.common.Settings;
import com.lc.common.Util;
import com.lc.service.CopyServices;
import com.lc.service.LCServiceProvider;
import com.lc.service.Logger;
import com.lc.service.WriteFileThread;

public class MainPanel {

	private JPanel panel;

	JTextField destField;
	JButton startCopyButton = null;
	Font font;
	Font bfont;
	JLabel currentFileNameLabel;
	JLabel currentFileTimeLeftLabel;
	JLabel currentSpeedLabel;
	JLabel totalTimeLeftLabel;
	JLabel progressLabel;
	JPanel progressBar;
	JPanel fileProgressBar;
	JPanel commonProgressBar;
	JPanel srcPanel;
	JPanel destPanel;
	JPanel progressPane;
	JLabel clearButton;
	JLabel resumeButton;
	JLabel cancelButton;
	JLabel failedLabel;
	JCheckBox skipSameFileCheck;
	JCheckBox retainSrcFileCheck;
	JScrollPane srcFilesScrollPane;
	JPanel commonPanel;

	Border errorBorder = BorderFactory.createLineBorder(Color.RED);
	Border normalBorder;
	JButton srcDirBrowseBtn;
	JButton srcDirBrowseBtn2;
	JButton dstDirBrowseBtn;
	JLabel srcFilesCountLabel;
	JLabel srcFilesSelectedIcon;
	JTextArea srcFilesArea;
	JLabel srcFilesClearBtn;

	JPanel copySummaryLinksPanel;
	JLabel copiedLabel;
	JLabel skippedLabel;
	JLabel failedFilesLabel;

	Timer exitTimer;
	Timer updateTimer;

	JLabel warningLabel;
	JPanel lowSpaceWarningPane;

	Border selectedBorder = BorderFactory.createSoftBevelBorder(BevelBorder.RAISED, Color.GRAY, Color.DARK_GRAY);

	List<String> srcFiles = new ArrayList<String>();
	List<String> localSrcFiles = new ArrayList<String>();
	List<String> scheduledSrcFiles = new ArrayList<String>();
	List<String> scheduledSrcFileNames = new ArrayList<String>();
	Border emptyBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
	Border raisedBorder = BorderFactory.createRaisedBevelBorder();
	Border refreshBorder = BorderFactory.createRaisedBevelBorder();
	long estCompletionTime = 0;
	long estCurrentFileCompletionTime = 0;
	long currentFileSize = 0;
	long speed = 0;
	long totalBytes = 0;
	long logicallyCopiedBytes = 0;
	long actuallyCopiedBytes = 0;
	long approxCopiedBytes = 0;
	long approxCopiedBytesOfFile = 0;
	int pctComplete = 0;
	int copiedFilesCount = 0;
	int totalFilesCount = 0;
	double approxCopiedPct = 0.0;
	long totalTimeLeft = 0;
	long spaceToBeFreedUp = 0;

	String copiedText;
	int totalCopiedCount = 0;
	int totalSkippedCount = 0;
	String skippedText;
	int failedCount = 0;
	String failedText;

	String currentlyCopyingFileName;

	public MainPanel() {

		font = new Font("Arial", Font.PLAIN, 15);
		bfont = new Font("Arial", Font.BOLD, 15);
		panel = new JPanel(null);
		panel.setBackground(Color.WHITE);

		buildSource();
		buildDest();
		buildCommonPanel();
		updateCopyButtonState();
	}

	private void buildSource() {
		srcPanel = new JPanel(null);
		srcPanel.setBounds(0, 0, 510, 300);

		JLabel sourceLabel = new JLabel("Source");
		sourceLabel.setBounds(225, 20, 55, 30);
		sourceLabel.setFont(bfont);
		sourceLabel.setForeground(Color.DARK_GRAY);
		srcPanel.add(sourceLabel);

		if (!CopySettings.isPasteMode()) {
			JLabel refreshSrcBtn = new JLabel();
			refreshSrcBtn.setBounds(280, 15, 25, 40);
			refreshSrcBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
			refreshSrcBtn.setIcon(new ImageIcon(Util.getImageDirPath() + "refresh.png"));
			refreshSrcBtn.setBorder(emptyBorder);
			refreshSrcBtn.setToolTipText("Refresh");
			refreshSrcBtn.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseReleased(MouseEvent e) {
					scheduledSrcFileNames.clear();
					scheduledSrcFiles.clear();
					srcFiles.clear();
					LCServiceProvider.runScan(false);
					updateSrcPanel();
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					refreshSrcBtn.setIcon(new ImageIcon(Util.getImageDirPath() + "refresh_active.png"));
				}

				@Override
				public void mouseExited(MouseEvent e) {
					refreshSrcBtn.setIcon(new ImageIcon(Util.getImageDirPath() + "refresh.png"));
				}
			});
			srcPanel.add(refreshSrcBtn);
		}

		srcDirBrowseBtn = new JButton();
		srcDirBrowseBtn.setBounds(100, 60, 300, 180);
		srcDirBrowseBtn.setEnabled(!CopySettings.isPasteMode());
		srcDirBrowseBtn.setIcon(new ImageIcon(Util.getImageDirPath() + "plus.png"));
		if (!CopySettings.isPasteMode()) {
			srcDirBrowseBtn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					File[] selectedFiles = MultiFileChooser.browse();
					if (selectedFiles != null) {
						addLocalSource(Arrays.asList(selectedFiles));
					}
				}
			});
		}
		srcPanel.add(srcDirBrowseBtn);

		srcDirBrowseBtn2 = new JButton("Add files");
		srcDirBrowseBtn2.setFont(font);
		srcDirBrowseBtn2.setBounds(50, 235, 400, 40);
		srcDirBrowseBtn2.setEnabled(!CopySettings.isPasteMode());
		srcDirBrowseBtn2.setVisible(false);
		if (!CopySettings.isPasteMode()) {
			srcDirBrowseBtn2.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					File[] selectedFiles = MultiFileChooser.browse();
					if (selectedFiles != null) {
						addLocalSource(Arrays.asList(selectedFiles));
					}
				}
			});

			srcPanel.setDropTarget(new DropTarget() {
				private static final long serialVersionUID = 1L;

				public synchronized void drop(DropTargetDropEvent evt) {
					onSrcDrop(evt);
				}
			});
		}
		srcPanel.add(srcDirBrowseBtn2);

		srcFilesSelectedIcon = new JLabel(new ImageIcon(Util.getImageDirPath() + "basket.png"));
		srcFilesSelectedIcon.setBounds(50, 60, 25, 25);
		srcFilesSelectedIcon.setVisible(false);
		srcPanel.add(srcFilesSelectedIcon);

		srcFilesCountLabel = new JLabel();
		srcFilesCountLabel.setFont(bfont);
		srcFilesCountLabel.setForeground(new Color(0, 90, 0));
		srcFilesCountLabel.setBounds(80, 65, 200, 25);
		srcFilesCountLabel.setVisible(false);
		srcPanel.add(srcFilesCountLabel);

		srcFilesClearBtn = new JLabel();
		srcFilesClearBtn.setBounds(420, 58, 30, 30);
		srcFilesClearBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		srcFilesClearBtn.setIcon(new ImageIcon(Util.getImageDirPath() + "clear.png"));
		srcFilesClearBtn.setVisible(false);
		srcFilesClearBtn.setBorder(emptyBorder);
		srcFilesClearBtn.setToolTipText("Clear selected files");
		srcFilesClearBtn.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				if (srcFilesClearBtn.isEnabled()) {
					srcFiles.clear();
					localSrcFiles.clear();
					scheduledSrcFiles.clear();
					LCServiceProvider.emptyBucket(scheduledSrcFileNames);
					scheduledSrcFileNames.clear();
					updateSrcPanel();
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				srcFilesClearBtn.setBorder(raisedBorder);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				srcFilesClearBtn.setBorder(emptyBorder);
			}
		});
		if (!CopySettings.isPasteMode()) {
			srcPanel.add(srcFilesClearBtn);
		}

		srcFilesArea = new JTextArea();
		srcFilesArea.setEditable(false);

		srcFilesArea.setAutoscrolls(true);
		srcFilesArea.setLineWrap(true);
		srcFilesArea.setWrapStyleWord(true);
		srcFilesArea.setFont(new Font("Arial", Font.PLAIN, 12));
		srcFilesArea.setMargin(new Insets(0, 5, 0, 5));
		if (!CopySettings.isPasteMode()) {
			srcFilesArea.setDropTarget(new DropTarget() {
				private static final long serialVersionUID = 1L;

				public synchronized void drop(DropTargetDropEvent evt) {
					onSrcDrop(evt);
				}
			});
		}

		srcFilesScrollPane = new JScrollPane(srcFilesArea);
		srcFilesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		srcFilesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		srcFilesScrollPane.setBounds(50, 90, 400, 135);
		srcFilesScrollPane.setVisible(false);
		srcPanel.add(srcFilesScrollPane);

		panel.add(srcPanel);
	}

	private void onSrcDrop(DropTargetDropEvent evt) {
		try {
			evt.acceptDrop(DnDConstants.ACTION_COPY);
			@SuppressWarnings("unchecked")
			List<File> droppedFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
			if (droppedFiles != null && !droppedFiles.isEmpty() && droppedFiles.size() > 0) {
				addLocalSource(droppedFiles);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void addLocalSource(List<File> files) {
		if (files != null && files.size() > 0) {
			for (File f : files) {
				localSrcFiles.add(f.getAbsolutePath());
			}

			if (Settings.REPLACE_ALL_FILES_SELECTION_WITH_WILDCARD) {
				File[] children = files.get(0).getParentFile().listFiles();
				boolean allFilesSelected = false;
				if (files.size() == children.length) {
					allFilesSelected = true;
					for (File f : children) {
						if (!files.contains(f)) {
							allFilesSelected = false;
							break;
						}
					}
				}
				if (allFilesSelected) {
					for (File f : files) {
						localSrcFiles.remove(f.getAbsolutePath());
					}
					localSrcFiles.add(files.get(0).getParent() + File.separator + "*");
				}
			}

		}
		updateSrcPanel();
	}

	private void updateSrcPanel() {
		srcFiles.clear();
		for (String f : localSrcFiles) {
			if (!srcFiles.contains(f)) {
				srcFiles.add(f);
			}
		}
		for (String f : scheduledSrcFiles) {
			if (!srcFiles.contains(f)) {
				srcFiles.add(f);
			}
		}

		if (srcFiles.size() > 0) {
			Logger.debug("Setting UI for files in bucket...");
			srcFilesCountLabel.setText(srcFiles.size() + " files");
			srcFilesCountLabel.setVisible(true);
			srcFilesSelectedIcon.setVisible(true);
			srcFilesClearBtn.setVisible(true);
			srcDirBrowseBtn.setVisible(false);
			srcDirBrowseBtn2.setVisible(true);
			srcFilesScrollPane.setVisible(true);
			String s = "";
			for (String path : srcFiles) {
				s += path + "\n";
			}
			srcFilesArea.setText(s);
		} else {
			srcFilesClearBtn.setVisible(false);
			srcFilesCountLabel.setVisible(false);
			srcFilesSelectedIcon.setVisible(false);
			srcDirBrowseBtn2.setVisible(false);
			srcDirBrowseBtn.setVisible(true);
			srcFilesScrollPane.setVisible(false);
			srcFilesArea.setText("");
		}
		updateCopyButtonState();
	}

	private void updateCopyButtonState() {
		if (srcFiles != null && srcFiles.size() > 0 && destField.getText() != null
				&& destField.getText().trim().length() > 0) {
			startCopyButton.setEnabled(true);
		} else {
			startCopyButton.setEnabled(false);
		}
	}

	private void buildDest() {
		destPanel = new JPanel(null);
		destPanel.setBounds(511, 0, 550, 300);

		JLabel destLabel = new JLabel("Destination");
		destLabel.setBounds(215, 20, 130, 30);
		destLabel.setFont(bfont);
		destLabel.setForeground(Color.DARK_GRAY);
		destPanel.add(destLabel);

		dstDirBrowseBtn = new JButton();
		dstDirBrowseBtn.setIcon(new ImageIcon(Util.getImageDirPath() + "plus.png"));
		dstDirBrowseBtn.setBounds(100, 60, 300, 180);
		dstDirBrowseBtn.setEnabled(!CopySettings.isPasteMode());
		dstDirBrowseBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String s = destField.getText();
				if (s == null || s.trim().length() == 0) {
					if (MultiFileChooser.lastFile != null) {
						s = MultiFileChooser.lastFile.getAbsolutePath();
					}
				}
				File selectedDir = DirectoryChooser.browse(s, true);
				if (selectedDir != null) {
					String filePath = selectedDir.getAbsolutePath();
					destField.setText(filePath);
					destField.setBorder(normalBorder);
					updateCopyButtonState();
				}
			}
		});
		destPanel.add(dstDirBrowseBtn);

		destField = new JTextField();
		destField.setEnabled(!CopySettings.isPasteMode());
		destField.setBounds(50, 250, 400, 25);
		destField.addKeyListener(new DBUKeyListener(destField));
		destPanel.setDropTarget(new DropTarget() {
			private static final long serialVersionUID = 1L;

			public synchronized void drop(DropTargetDropEvent evt) {
				try {
					evt.acceptDrop(DnDConstants.ACTION_COPY);
					@SuppressWarnings("unchecked")
					List<File> droppedFiles = (List<File>) evt.getTransferable()
							.getTransferData(DataFlavor.javaFileListFlavor);
					if (droppedFiles != null && !droppedFiles.isEmpty() && droppedFiles.size() == 1
							&& droppedFiles.get(0).isDirectory()) {
						destField.setText(droppedFiles.get(0).getAbsolutePath());
						destField.setBorder(normalBorder);
						updateCopyButtonState();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		destPanel.add(destField);
		normalBorder = destField.getBorder();

		panel.add(destPanel);
	}

	private void buildCommonPanel() {
		commonPanel = new JPanel(null);
		commonPanel.setBounds(new Rectangle(0, 290, 1025, 300));

		JPanel optionsPanel = new JPanel(null);
		optionsPanel.setBounds(new Rectangle(0, 10, 1020, 60));
		commonPanel.add(optionsPanel);

		skipSameFileCheck = new JCheckBox();
		skipSameFileCheck.setText("Do not copy if files already exist with same date, size and hash");
		skipSameFileCheck.setSelected(true);
		skipSameFileCheck.setBounds(85, 0, 340, 25);
		skipSameFileCheck.setEnabled(!CopySettings.isPasteMode());
		optionsPanel.add(skipSameFileCheck);

		retainSrcFileCheck = new JCheckBox();
		retainSrcFileCheck.setText("Do not delete source files once copied");
		retainSrcFileCheck.setSelected(true);
		retainSrcFileCheck.setEnabled(!CopySettings.isPasteMode());
		retainSrcFileCheck.setBounds(640, 0, 300, 25);
		optionsPanel.add(retainSrcFileCheck);

		progressPane = new JPanel(null);
		progressPane.setBorder(BorderFactory.createEtchedBorder());
		progressPane.setBounds(150, 100, 715, 30);
		progressPane.setBackground(Color.WHITE);
		progressPane.setVisible(false);
		commonPanel.add(progressPane);

		progressBar = new JPanel();
		progressBar.setBackground(new Color(120, 235, 160));
		progressBar.setBounds(2, 2, 0, 25);
		progressPane.add(progressBar);

		fileProgressBar = new JPanel(null);
		fileProgressBar.setBackground(new Color(180, 255, 210));
		fileProgressBar.setBounds(2, 2, 0, 25);
		progressPane.add(fileProgressBar);

		commonProgressBar = new JPanel(null);
		commonProgressBar.setBackground(new Color(80, 209, 130));
		commonProgressBar.setBounds(2, 2, 0, 25);
		progressPane.add(commonProgressBar);

		progressLabel = new JLabel(getProgressStateText(), SwingConstants.CENTER);
		progressLabel.setBounds(120, 2, 475, 25);
		progressLabel.setFont(font);
		progressPane.add(progressLabel);

		currentSpeedLabel = new JLabel("", SwingConstants.RIGHT);
		currentSpeedLabel.setBounds(605, 2, 100, 25);
		currentSpeedLabel.setFont(font);
		progressPane.add(currentSpeedLabel);

		totalTimeLeftLabel = new JLabel("", SwingConstants.LEFT);
		totalTimeLeftLabel.setBounds(10, 2, 100, 25);
		totalTimeLeftLabel.setFont(font);
		progressPane.add(totalTimeLeftLabel);

		progressPane.setComponentZOrder(commonProgressBar, 5);
		progressPane.setComponentZOrder(progressBar, 4);
		progressPane.setComponentZOrder(fileProgressBar, 3);
		progressPane.setComponentZOrder(progressLabel, 2);
		progressPane.setComponentZOrder(currentSpeedLabel, 1);
		progressPane.setComponentZOrder(totalTimeLeftLabel, 0);

		clearButton = new JLabel("Clear", JLabel.CENTER);
		clearButton.setForeground(Color.WHITE);
		clearButton.setOpaque(true);
		clearButton.setBackground(Color.DARK_GRAY);
		clearButton.setBorder(BorderFactory.createEtchedBorder());
		clearButton.setFont(font);
		clearButton.setBounds(864, 100, 80, 30);
		clearButton.setVisible(false);
		commonPanel.add(clearButton);
		clearButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				clearButton.setBorder(BorderFactory.createRaisedBevelBorder());
			}

			@Override
			public void mouseExited(MouseEvent e) {
				clearButton.setBorder(BorderFactory.createEtchedBorder());
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (CopySettings.status == CopySettings.State.COPYING
						|| CopySettings.status == CopySettings.State.STARTING) {
					CopySettings.status = CopySettings.State.PAUSING;
				} else if (CopySettings.status == CopySettings.State.CANCELLED
						|| CopySettings.status == CopySettings.State.COMPLETE
						|| CopySettings.status == CopySettings.State.IDLE) {
					resetUi();
				}
				refreshGuiElements();
			}

		});

		resumeButton = new JLabel(">", JLabel.CENTER);
		resumeButton.setForeground(Color.CYAN);
		resumeButton.setToolTipText("Resume");
		resumeButton.setOpaque(true);
		resumeButton.setBackground(Color.GRAY);
		resumeButton.setBorder(BorderFactory.createEtchedBorder());
		resumeButton.setFont(new Font("Arial", Font.BOLD, 20));
		resumeButton.setBounds(864, 100, 40, 30);
		resumeButton.setVisible(false);
		commonPanel.add(resumeButton);
		resumeButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				resumeButton.setBorder(selectedBorder);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				resumeButton.setBorder(BorderFactory.createEtchedBorder());
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (CopySettings.status == CopySettings.State.PAUSED) {
					CopySettings.status = CopySettings.State.COPYING;
					spaceToBeFreedUp = 0;
					new WriteFileThread().start();
				}
				refreshGuiElements();
			}

		});

		cancelButton = new JLabel("X", JLabel.CENTER);
		cancelButton.setToolTipText("Cancel");
		cancelButton.setForeground(Color.RED);
		cancelButton.setOpaque(true);
		cancelButton.setBackground(Color.GRAY);
		cancelButton.setBorder(BorderFactory.createEtchedBorder());
		cancelButton.setFont(new Font("Arial", Font.BOLD, 20));
		cancelButton.setBounds(904, 100, 40, 30);
		cancelButton.setVisible(false);
		commonPanel.add(cancelButton);
		cancelButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				cancelButton.setBorder(selectedBorder);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				cancelButton.setBorder(BorderFactory.createEtchedBorder());
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (CopySettings.status == CopySettings.State.COPYING) {
					CopySettings.status = CopySettings.State.CANCELING;
				} else {
					CopySettings.status = CopySettings.State.CANCELLED;
				}
				cancelButton.setVisible(false);
				if (CopySettings.copyNotifications.size() > 0) {
					failedLabel.setText("<html><u>" + CopySettings.copyNotifications.size()
							+ " file copy notifications</u></html>");
					failedLabel.setVisible(true);
				}
				refreshGuiElements();
			}

		});

		// -------------------------------------------------------------------------------

		currentFileNameLabel = new JLabel();
		currentFileNameLabel.setBounds(50, 150, 700, 20);
		currentFileNameLabel.setFont(font);
		currentFileNameLabel.setVisible(false);
		commonPanel.add(currentFileNameLabel);

		currentFileTimeLeftLabel = new JLabel();
		currentFileTimeLeftLabel.setBounds(50, 180, 700, 20);
		currentFileTimeLeftLabel.setFont(font);
		currentFileTimeLeftLabel.setVisible(false);
		commonPanel.add(currentFileTimeLeftLabel);

		startCopyButton = new JButton();
		startCopyButton.setFont(font);
		startCopyButton.setBounds(new Rectangle(410, 110, 200, 40));
		startCopyButton.setText("Start Copy");
		startCopyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okButtonActionPerformed();
			}
		});

		commonPanel.add(startCopyButton);

		failedLabel = new JLabel("", JLabel.CENTER);
		failedLabel.setVisible(false);
		failedLabel.setFont(font);
		failedLabel.setForeground(Color.RED);
		failedLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		failedLabel.setBounds(400, 140, 200, 40);
		failedLabel.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				cancelExitTimer();
				new CopyNotificationsDlg();
			}
		});
		commonPanel.add(failedLabel);

		copySummaryLinksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
		copySummaryLinksPanel.setBounds(0, 180, 1020, 30);

		copiedLabel = new JLabel("", JLabel.CENTER);
		copiedLabel.setVisible(false);
		copiedLabel.setFont(font);
		copiedLabel.setForeground(new Color(0, 100, 40));
		copiedLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		copiedLabel.setPreferredSize(new Dimension(300, 30));
		copiedLabel.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				cancelExitTimer();
				new CopyFileStatusDlg(CopyFileStatusDlg.DialogType.COPIED);
			}
		});
		copySummaryLinksPanel.add(copiedLabel);

		skippedLabel = new JLabel("", JLabel.CENTER);
		skippedLabel.setVisible(false);
		skippedLabel.setFont(font);
		skippedLabel.setForeground(new Color(180, 90, 0));
		skippedLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		skippedLabel.setPreferredSize(new Dimension(300, 30));
		skippedLabel.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				cancelExitTimer();
				new CopyFileStatusDlg(CopyFileStatusDlg.DialogType.SKIPPED);
			}
		});
		copySummaryLinksPanel.add(skippedLabel);

		failedFilesLabel = new JLabel("", JLabel.CENTER);
		failedFilesLabel.setVisible(false);
		failedFilesLabel.setFont(font);
		failedFilesLabel.setForeground(Color.RED);
		failedFilesLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		failedFilesLabel.setPreferredSize(new Dimension(300, 30));
		failedFilesLabel.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				new CopyFileStatusDlg(CopyFileStatusDlg.DialogType.FAILED);
			}
		});
		copySummaryLinksPanel.add(failedFilesLabel);

		commonPanel.add(copySummaryLinksPanel);

		commonPanel.add(getLowSpaceWarningPane());

		panel.add(commonPanel);

	}

	public void cancelExitTimer() {
		if (exitTimer != null) {
			exitTimer.cancel();
			exitTimer = null;
		}
	}

	private JPanel getLowSpaceWarningPane() {
		lowSpaceWarningPane = new JPanel();
		lowSpaceWarningPane.setBounds(0, 140, 1020, 30);

		lowSpaceWarningPane.setVisible(false);

		JLabel warningIcon = new JLabel(new ImageIcon(Util.getImageDirPath() + "alert24.png"));
		warningIcon.setPreferredSize(new Dimension(30, 30));
		lowSpaceWarningPane.add(warningIcon);

		warningLabel = new JLabel();
		warningLabel.setFont(font);
		warningLabel.setForeground(Color.RED);
		warningLabel.setPreferredSize(new Dimension(600, 30));
		lowSpaceWarningPane.add(warningLabel);

		return lowSpaceWarningPane;
	}

	private void refreshGuiElements() {

		boolean whenIdle = CopySettings.status == CopySettings.State.IDLE;
		boolean whenCopying = !whenIdle;

		if (whenCopying) {
			// clear/pause/exit button
			if (CopySettings.status == CopySettings.State.CANCELLED
					|| CopySettings.status == CopySettings.State.COMPLETE) {
				clearButton.setText(CopySettings.isPasteMode() ? "Exit" : "Clear");
			} else {
				clearButton.setText("Pause");
			}
		}

		if (CopySettings.status == CopySettings.State.COPYING || CopySettings.status == CopySettings.State.COMPLETE
				|| CopySettings.status == CopySettings.State.CANCELLED) {
			clearButton.setVisible(true);
		} else {
			clearButton.setVisible(false);
		}

		resumeButton.setVisible(CopySettings.status == CopySettings.State.PAUSED);
		cancelButton.setVisible(CopySettings.status == CopySettings.State.PAUSED);

		if (this.speed > 0 && (CopySettings.status == CopySettings.State.COPYING
				|| CopySettings.status == CopySettings.State.PAUSING
				|| CopySettings.status == CopySettings.State.CANCELING)) {
			currentSpeedLabel.setVisible(true);
			currentSpeedLabel.setText(Util.getFormattedCopySpeed(speed));

			currentFileNameLabel.setVisible(currentlyCopyingFileName != null);
			if (currentlyCopyingFileName != null) {
				currentFileNameLabel.setText("Currently copying: " + currentlyCopyingFileName);
			}

			currentFileTimeLeftLabel.setVisible(currentlyCopyingFileName != null);

			if (currentlyCopyingFileName != null && estCurrentFileCompletionTime > 0) {
				long millisNeeded = estCurrentFileCompletionTime > System.currentTimeMillis()
						? estCurrentFileCompletionTime - System.currentTimeMillis()
						: 0;
				String timeLeftForCurrentFile = "Estimated time left: " + Util.getFormattedDuration(millisNeeded);
				currentFileTimeLeftLabel.setText(timeLeftForCurrentFile);
			} else {
				currentFileTimeLeftLabel.setText("Estimated time left: Calculating...");
			}
		} else {
			currentSpeedLabel.setVisible(false);
			currentFileTimeLeftLabel.setVisible(false);
			currentFileNameLabel.setVisible(currentlyCopyingFileName != null);
			if (currentlyCopyingFileName != null) {
				currentFileNameLabel.setText("Currently copying: " + currentlyCopyingFileName);
			}
		}

		if (CopySettings.isCopyBlocked && currentlyCopyingFileName != null) {
			currentFileNameLabel.setText("Currently copying: " + currentlyCopyingFileName);
			currentFileNameLabel.setVisible(true);
			if (speed > 0) {
				long millisNeeded = currentFileSize / speed;
				String timeLeftForCurrentFile = "Estimated time left: " + Util.getFormattedDuration(millisNeeded);
				currentFileTimeLeftLabel.setText(timeLeftForCurrentFile);
				currentFileTimeLeftLabel.setVisible(true);
			}
		}

		startCopyButton.setVisible(whenIdle);
		updateCopyButtonState();

		srcPanel.setEnabled(whenIdle);
		destPanel.setEnabled(whenIdle);
		progressPane.setVisible(whenCopying);
		// fileProgressBar.setVisible(whenCopying);

		srcDirBrowseBtn.setEnabled(whenIdle);
		srcFilesClearBtn.setEnabled(whenIdle);
		srcDirBrowseBtn2.setEnabled(whenIdle);

		dstDirBrowseBtn.setEnabled(whenIdle);
		destField.setEnabled(whenIdle);

		skipSameFileCheck.setEnabled(whenIdle);
		retainSrcFileCheck.setEnabled(whenIdle);

		String progressText = getProgressStateText();
		progressLabel.setText(progressText);

		if (!CopySettings.isCopyBlocked && estCompletionTime > System.currentTimeMillis()) {
			totalTimeLeft = estCompletionTime - System.currentTimeMillis();
		}
		totalTimeLeftLabel.setText(Util.getColonFormattedDuration(totalTimeLeft));
		boolean showTotalTimeLeftLabel = CopySettings.status == CopySettings.State.COPYING
				|| CopySettings.status == CopySettings.State.PAUSING || CopySettings.status == CopySettings.State.PAUSED
				|| CopySettings.status == CopySettings.State.CANCELING;

		if (CopySettings.isCopyBlocked) {
			// when blocked initially (totalTimeLeft = 0), calculate time left based on
			// previously archived speed
			if (speed > 0 && totalTimeLeft == 0) {
				totalTimeLeft = (totalBytes - logicallyCopiedBytes) / speed;
			}
			if (totalTimeLeft > 0) {
				totalTimeLeftLabel.setText(Util.getColonFormattedDuration(totalTimeLeft));
				showTotalTimeLeftLabel = true;
			}
		}

		totalTimeLeftLabel.setVisible(showTotalTimeLeftLabel);

		if (speed > 0 && this.totalBytes > this.actuallyCopiedBytes) {
			currentSpeedLabel.setText(Util.getFormattedCopySpeed(speed));
		} else {
			currentSpeedLabel.setText("");
		}

		double progress = (711.0 * approxCopiedPct) / 100; // 711 is progress bar length in pixels
		int totalProgressPixels = (int) Math.round(progress);

		long approxCopiedPctOfFile = currentFileSize > 0 ? (long) ((approxCopiedBytesOfFile * 100.0) / currentFileSize)
				: 0;
		if (approxCopiedPctOfFile > 100) {
			approxCopiedPctOfFile = 100;
		} else if (approxCopiedPctOfFile < 0) {
			approxCopiedPctOfFile = 0;
		}
		double fileProgress = (711.0 * approxCopiedPctOfFile) / 100;
		int fileProgressPixels = (int) Math.round(fileProgress);

		int commonProgressPixels = 0;
		if (totalProgressPixels >= fileProgressPixels) {
			
			// total progress > file progress
			commonProgressPixels = fileProgressPixels;
			fileProgressPixels = 0;
			commonProgressBar.setBounds(2, 2, commonProgressPixels, 25);
			progressBar.setBounds(commonProgressPixels + 1, 2, totalProgressPixels - commonProgressPixels, 25);
			fileProgressBar.setBounds(2, 2, 0, 25);
		} else {
			System.out.println(totalProgressPixels+" :: "+fileProgressPixels);
			commonProgressBar.setBounds(2, 2, totalProgressPixels, 25);
			progressBar.setBounds(2, 2, 0, 25);
			fileProgressBar.setBounds(totalProgressPixels + 1, 2, fileProgressPixels - totalProgressPixels, 25);
		}

		if (CopySettings.status == CopySettings.State.CANCELLED || CopySettings.status == CopySettings.State.COMPLETE) {
			failedLabel.setVisible(false);
			if (CopySettings.copyNotifications.size() > 0) {
				failedLabel.setText(
						"<html><u>" + CopySettings.copyNotifications.size() + " file copy notifications</u></html>");
				failedLabel.setVisible(true);
				copySummaryLinksPanel.setBounds(0, 180, copySummaryLinksPanel.getSize().width,
						copySummaryLinksPanel.getSize().height);
			} else {
				copySummaryLinksPanel.setBounds(0, 150, copySummaryLinksPanel.getSize().width,
						copySummaryLinksPanel.getSize().height);
			}

			copySummaryLinksPanel.setVisible(true);
			if (totalCopiedCount > 0) {
				copiedLabel.setText(copiedText);
				copiedLabel.setVisible(true);
			} else {
				copiedLabel.setVisible(false);
			}

			if (totalSkippedCount > 0) {
				skippedLabel.setText(skippedText);
				skippedLabel.setVisible(true);
			} else {
				skippedLabel.setVisible(false);
			}

			if (failedCount > 0) {
				failedFilesLabel.setText(failedText);
				failedFilesLabel.setVisible(true);
			} else {
				failedFilesLabel.setVisible(false);
			}
		} else {
			copySummaryLinksPanel.setVisible(false);
			failedLabel.setVisible(false);
			copiedLabel.setVisible(false);
			skippedLabel.setVisible(false);
			failedFilesLabel.setVisible(false);
		}

		if (spaceToBeFreedUp > 0 && CopySettings.status != CopySettings.State.PAUSED) {
			spaceToBeFreedUp = 0;
		}

		if (spaceToBeFreedUp > 0) {
			lowSpaceWarningPane.setVisible(true);
			warningLabel.setText("File copy has been paused as there is not enough space. Please free up "
					+ Util.getFormatedStringForByte(spaceToBeFreedUp) + ".");
		} else {
			lowSpaceWarningPane.setVisible(false);
		}

		updateSrcPanel();
		commonPanel.revalidate();
	}

	private void commit() {

		String dest = destField.getText().trim();

		File dstFile = new File(dest);

		if (srcFiles != null && srcFiles.size() > 0) {
			if (srcFiles.size() > 0) {
				List<File> srcs = new ArrayList<File>();
				for (String s : srcFiles) {
					if (s.endsWith(File.separator + "*")) {
						String ss = s.substring(0, s.length() - 2);
						File f = new File(ss);
						File[] files = f.listFiles();
						if (files != null && files.length > 0) {
							for (File cf : files) {
								srcs.add(cf);
							}
						}
					} else {
						srcs.add(new File(s));
					}
				}
				setUiForCopy();
				new WriteFileThread(srcs, dstFile).start();
			}
		}

	}

	private void setUiForCopy() {
		CopySettings.status = CopySettings.State.STARTING;

		LCServiceProvider.emptyBucket(scheduledSrcFileNames);

		CopySettings.SKIP_SAME_FILE_IF_EXISTS = skipSameFileCheck.isSelected();
		CopySettings.RETAIN_SOURCE_FILE_AFTER_COPY = retainSrcFileCheck.isSelected();

		startUpdateTimer();
	}

	private void startUpdateTimer() {
		updateTimer = new Timer();
		updateTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				updateEstimatedTime();
				if (CopySettings.status == CopySettings.State.COMPLETE
						|| CopySettings.status == CopySettings.State.CANCELLED
						|| CopySettings.status == CopySettings.State.IDLE) {
					updateTimer.cancel();
					updateTimer.purge();
				}
			}

		}, CopySettings.PROGRESS_UPDATE_INTERVAL, CopySettings.PROGRESS_UPDATE_INTERVAL);
	}

	private void updateEstimatedTime() {
		this.refreshGuiElements();
	}

	private void resetUi() {
		if (CopySettings.isPasteMode()) {
			LCServiceProvider.exitApp();
		}
		CopySettings.status = CopySettings.State.IDLE;
		estCompletionTime = 0;
		speed = 0;
		estCurrentFileCompletionTime = 0;
		totalBytes = 0;
		logicallyCopiedBytes = 0;
		pctComplete = 0;
		approxCopiedPct = 0;
		approxCopiedBytes = 0;
		approxCopiedBytesOfFile = 0;
		copiedFilesCount = 0;
		totalFilesCount = 0;
		spaceToBeFreedUp = 0;

		srcFiles.clear();
		localSrcFiles.clear();
		scheduledSrcFiles.clear();
		scheduledSrcFileNames.clear();

		refreshGuiElements();
	}

	private void setUiAfterCopyComplete() {
		if (CopySettings.isPasteMode()) {
			CopySettings.READY_FOR_EXIT = true;
		}

		totalCopiedCount = CopyServices.copiedFilesList.size();
		int replacedCount = CopyServices.overwrittenFilesList.size();
		int renamedAsPlannedCount = CopyServices.renamedFilesList.size();
		int renamedAfterFailedCount = CopyServices.renamedAfterFailedFilesList.size();
		int totalRenamedCount = renamedAsPlannedCount + renamedAfterFailedCount;

		int skippedDiffHashCount = CopyServices.skippedFilesList.size();
		int skippedSameHashFileCount = CopyServices.sameFilesSkippedList.size()
				+ CopyServices.srcDstSameFilesSkippedList.size();
		int copiedAndOcverwrittenCount = CopyServices.copiedAndReplacedFilesList.size();
		totalSkippedCount = skippedDiffHashCount + skippedSameHashFileCount + copiedAndOcverwrittenCount;

		failedCount = CopyServices.failedFilesList.size();

		if (totalCopiedCount > 0) {
			copiedText = "Copied: " + totalCopiedCount;
			if (replacedCount > 0 || totalRenamedCount > 0) {
				copiedText += " (";
				if (replacedCount > 0) {
					copiedText += "Replaced: " + replacedCount;
				}
				if (replacedCount > 0 && totalRenamedCount > 0) {
					copiedText += ", ";
				}
				if (totalRenamedCount > 0) {
					copiedText += "Renamed: " + totalRenamedCount;
				}
				copiedText += ")";
			}
		}

		if (totalSkippedCount > 0) {
			skippedText = "Skipped: " + totalSkippedCount;

			if (skippedDiffHashCount > 0 || copiedAndOcverwrittenCount > 0) {
				skippedText += " (";
			}

			if (skippedDiffHashCount > 0) {
				skippedText += skippedDiffHashCount + " different hash files";
			}
			if (skippedDiffHashCount > 0 && copiedAndOcverwrittenCount > 0) {
				skippedText += ", ";
			}
			if (copiedAndOcverwrittenCount > 0) {
				skippedText += copiedAndOcverwrittenCount + " older version files";
			}
			if (skippedDiffHashCount > 0 || copiedAndOcverwrittenCount > 0) {
				skippedText += ")";
			}
		}

		if (failedCount > 0) {
			failedText = "Failed: " + failedCount;
		}

		// automatically exit after 5 seconds, if pasted cleanly without issues after
		// paste complete
		if (CopySettings.isPasteMode() && failedCount == 0) {
			exitTimer = new Timer();
			exitTimer.schedule(new TimerTask() {

				@Override
				public void run() {
					resetUi();
				}

			}, 5000L);
		}

		if (updateTimer != null) {
			updateTimer.cancel();
			updateTimer.purge();
		}

		refreshGuiElements();
	}

	public void onFileCopyStarting(File file, boolean hint) {
		if (file != null) {
			currentlyCopyingFileName = file.getName();
			approxCopiedBytesOfFile = 0;
			currentFileSize = file.length();

			if (speed == 0 && estCompletionTime == 0) {
				Long oldSpeed = Settings.DRIVE_TO_COPY_SPEED_MAP
						.get(file.getAbsolutePath().substring(0, 1) + destField.getText().trim().substring(0, 1));
				if (oldSpeed != null && oldSpeed > 0) {
					speed = oldSpeed;
				}else {
					speed = 5000; // a hardcoded value just to show some progress...
				}
			}

			if (!hint) {
				if (speed > 0) {
					estCompletionTime = System.currentTimeMillis() + ((totalBytes - logicallyCopiedBytes) / speed);
					long millisNeededForThisFile = currentFileSize / speed;
					estCurrentFileCompletionTime = System.currentTimeMillis() + millisNeededForThisFile;

				}
			}
		}
		refreshGuiElements();
	}

	public void onAllCopyComplete() {
		currentlyCopyingFileName = null;
		approxCopiedBytesOfFile = 0;
		currentFileSize = 0;
		setUiAfterCopyComplete();
		refreshGuiElements();
	}

	/**
	 * 
	 * @param copied
	 *            no. of files copied
	 * @param total
	 *            total no. of files to be copied
	 * @param logicallyCopiedBytes
	 *            bytes that can be considered copied for user(actually copied +
	 *            skipped file sizes)
	 * @param actuallyTransferredBytes
	 *            bytes actually copied. This should be used for any calculation.
	 * @param totalBytes
	 *            totalBytes to be copied
	 * @param timeTakenMillis
	 *            Actual time spent on copying (ignores time when user provides many
	 *            options, or while paused)
	 */
	public void onFileCopyComplete(int copied, int total, long logicallyCopiedBytes, long actuallyTransferredBytes,
			long totalBytes, long timeTakenMillis) {
		Logger.info(copied + " of " + total + " copied");
		this.totalBytes = totalBytes;
		this.logicallyCopiedBytes = logicallyCopiedBytes;
		this.actuallyCopiedBytes = actuallyTransferredBytes;
		approxCopiedBytes = this.logicallyCopiedBytes;
		copiedFilesCount = copied;
		totalFilesCount = total;
		approxCopiedBytesOfFile = currentFileSize;
		estCurrentFileCompletionTime = System.currentTimeMillis();

		if (timeTakenMillis > 0) {
			double pct = totalBytes > 0 ? (logicallyCopiedBytes * 100.0) / totalBytes : 0;
			pctComplete = (int) pct;

			if (copied > 0) {
				long leftBytes = this.totalBytes - this.logicallyCopiedBytes;
				speed = actuallyTransferredBytes / timeTakenMillis; // bytes per milli-sec
				totalTimeLeft = (long) leftBytes / speed;
				estCompletionTime = System.currentTimeMillis() + totalTimeLeft;
			}
		}

		this.refreshGuiElements();
	}

	public void onAllCopyStart(int total, long totalBytes) {
		this.totalBytes = totalBytes;
		this.logicallyCopiedBytes = 0;
		this.actuallyCopiedBytes = 0;
		approxCopiedBytes = 0;
		copiedFilesCount = 0;
		totalFilesCount = total;
		approxCopiedBytesOfFile = 0;
		estCurrentFileCompletionTime = System.currentTimeMillis();
	}

	private String getProgressStateText() {
		if (CopySettings.isCopyBlocked) {
			return "Waiting...";
		}
		switch (CopySettings.status) {
		case COPYING:
			// calculate approx. %age copied
			calcApproxCopiedInfo();
			return Util.DOUBLE_NUMBER_FORMAT.format(approxCopiedPct) + "% complete (" + (copiedFilesCount + 1) + " of "
					+ totalFilesCount + " files, " + Util.getFormatedStringForByte((approxCopiedBytes), totalBytes)
					+ " of " + Util.getFormatedStringForByte(totalBytes) + ")";
		case STARTING:
			return "Copy starting";
		case PAUSED:
			return "Paused (" + copiedFilesCount + " of " + totalFilesCount + " files copied)";
		case PAUSING:
			calcApproxCopiedInfo();
			return "Pausing after copying current file (" + (copiedFilesCount + 1) + " of " + totalFilesCount + ")";
		case CANCELING:
			calcApproxCopiedInfo();
			return "Cancelling after copying current file (" + (copiedFilesCount + 1) + " of " + totalFilesCount + ")";
		case CANCELLED:
			return "Copy cancelled (" + copiedFilesCount + " of " + totalFilesCount + " files copied)";
		case COMPLETE:
			return "Copy complete (" + totalFilesCount + " files copied)";
		case IDLE:
		default:
			return "Waiting...";
		}
	}

	private void calcApproxCopiedInfo() {
		if (speed > 0) {
			if (estCurrentFileCompletionTime > System.currentTimeMillis()) {
				long bytesLeft = speed * (estCurrentFileCompletionTime - System.currentTimeMillis());
				if (bytesLeft > currentFileSize) {
					bytesLeft = currentFileSize;
				} else if (bytesLeft < 0) {
					bytesLeft = 0;
				}
				approxCopiedBytesOfFile = currentFileSize > bytesLeft ? currentFileSize - bytesLeft
						: approxCopiedBytesOfFile;
				if (approxCopiedBytesOfFile > currentFileSize) {
					approxCopiedBytesOfFile = currentFileSize;
				}
				if (approxCopiedBytesOfFile < 0) {
					approxCopiedBytesOfFile = 0;
				}
				approxCopiedBytes = logicallyCopiedBytes + approxCopiedBytesOfFile;
			}
			approxCopiedPct = approxCopiedBytes * 100.0 / totalBytes;
			
			if (approxCopiedPct > 100) {
				approxCopiedPct = pctComplete > 0 ? pctComplete : 0;
			}
		} else {
			approxCopiedBytes = logicallyCopiedBytes;
		}
	}

	private void okButtonActionPerformed() {
		if (validated()) {
			commit();
		}
	}

	private boolean validated() {

		if (srcFiles == null || srcFiles.size() == 0) {
			return false;
		}

		boolean validated = true;

		String dst = destField.getText().trim();
		File dstFile = new File(dst);
		if (dst == null || !dstFile.exists() || !dstFile.isDirectory()) {
			destField.setBorder(errorBorder);
			destField.setToolTipText("Invalid destination folder");
			return false;
		}

		return validated;
	}

	public JPanel getPanel() {
		return panel;
	}

	public void addSrcFile(String file, String fileName) {
		Logger.debug("Got file for copy: " + file);
		if (!scheduledSrcFiles.contains(file)) {
			scheduledSrcFiles.add(file);
			Logger.debug("Added file for copy to srcFiles: " + scheduledSrcFiles.size());

		}
		scheduledSrcFileNames.add(fileName);
		updateSrcPanel();
	}

	public void pasteFiles(String file) {
		if (file != null) {
			destField.setText(file);
			if (srcFiles.size() > 0) {
				if (Settings.START_COPY_ON_PASTE_AUTOMATICALLY) {
					okButtonActionPerformed();
				}
			} else {
				CopySettings.setPastePath(null);
			}
		}
	}

	public class DBUKeyListener implements KeyListener {

		private JTextField fld;
		private JScrollPane spane;

		public DBUKeyListener(JTextField fld) {
			this.fld = fld;
		}

		public DBUKeyListener(JScrollPane fld) {
			this.spane = fld;
		}

		public void keyPressed(KeyEvent e) {
		}

		public void keyReleased(KeyEvent e) {
		}

		public void keyTyped(KeyEvent e) {
			if (fld != null) {
				fld.setBorder(normalBorder);
				fld.setToolTipText(null);
				updateCopyButtonState();
			}
			if (spane != null) {
				spane.setBorder(normalBorder);
				spane.setToolTipText(null);
			}
		}
	}

	public void refreshSourcePanel() {
		updateSrcPanel();
	}

	public void updateWhenPaused() {
		estCurrentFileCompletionTime = 0;
		estCompletionTime = 0;
		refreshGuiElements();
	}

	public void showLowSpaceWarning(long spaceNeeded) {
		spaceToBeFreedUp = spaceNeeded;
		refreshGuiElements();
	}

}
