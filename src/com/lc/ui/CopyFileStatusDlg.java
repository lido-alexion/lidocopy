package com.lc.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.lc.common.Util;
import com.lc.core.Message;
import com.lc.service.CopyServices;

public class CopyFileStatusDlg {

	private JDialog dialog;
	private JButton okButton = null;
	private Font font;
	JScrollPane scrollPane;
	int y = 0;
	int width = 800;
	int height = 500;
	JPanel panel;
	List<Message> notifs;
	Color LINK_COLOR = new Color(0, 0, 102);
	DialogType dlgType;

	enum DialogType {
		COPIED, SKIPPED, FAILED
	};

	public CopyFileStatusDlg(DialogType dlgType) {
		this.dlgType = dlgType;

		if (dlgType == DialogType.COPIED) {
			int totalCopiedCount = CopyServices.copiedFilesList.size();
			if (totalCopiedCount == 0) {
				return;
			}
		} else if (dlgType == DialogType.SKIPPED) {
			int skippedDiffHashCount = CopyServices.skippedFilesList.size();
			int skippedSameHashFileCount = CopyServices.sameFilesSkippedList.size();
			int skippedSameSrcDstFileCount = CopyServices.srcDstSameFilesSkippedList.size();
			int skippedAfterCopying = CopyServices.copiedAndReplacedFilesList.size();
			if (skippedDiffHashCount == 0 && skippedSameHashFileCount == 0 && skippedAfterCopying == 0
					&& skippedSameSrcDstFileCount == 0) {
				return;
			}
		} else {
			int failedCount = CopyServices.failedFilesList.size();
			if (failedCount == 0) {
				return;
			}
		}

		font = new Font("Arial", Font.PLAIN, 12);

		displayDlg();
	}

	public void displayDlg() {
		if (null == dialog) {
			String title = dlgType == DialogType.COPIED ? "Files copied"
					: (dlgType == DialogType.SKIPPED ? "Files skipped" : "Files failed copying");
			dialog = new JDialog(LCUiCreator.getUiFrame(), title, Dialog.ModalityType.DOCUMENT_MODAL);
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

			panel = new JPanel(null);
			panel.setAutoscrolls(true);
			panel.setBackground(Color.WHITE);

			scrollPane = new JScrollPane(panel);
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setBounds(10, 10, width - 25, height - 100);

			dialog.add(scrollPane);

			addContents();

			y += 20;

			panel.setPreferredSize(new Dimension(width - 50, y));

			dialog.setPreferredSize(dimension);
			dialog.setMaximumSize(dimension);
			dialog.setMinimumSize(dimension);
			dialog.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					e.getWindow().dispose();
				}
			});

			dialog.setVisible(true);

		}
	}

	private void addContents() {
		if (dlgType == DialogType.COPIED) {
			int totalCopiedCount = CopyServices.copiedFilesList.size();
			int replacedCount = CopyServices.overwrittenFilesList.size();
			int renamedAsPlannedCount = CopyServices.renamedFilesList.size();
			int renamedAfterFailedCount = CopyServices.renamedAfterFailedFilesList.size();
			int totalRenamedCount = renamedAsPlannedCount + renamedAfterFailedCount;
			int copiedAsPlanned = totalCopiedCount - replacedCount - totalRenamedCount;

			if (replacedCount > 0) {
				addHeader(replacedCount + " files replaced:");
				for (String filePath : CopyServices.overwrittenFilesList) {
					addFilePath(filePath);
				}
			}

			if (renamedAsPlannedCount > 0) {
				addHeader(renamedAsPlannedCount + " files renamed and copied:");
				for (String filePath : CopyServices.renamedFilesList) {
					addFilePath(filePath);
				}
			}

			if (renamedAfterFailedCount > 0) {
				addHeader(renamedAfterFailedCount + " files which failed replacing, and hence renamed and copied:");
				for (String filePath : CopyServices.renamedAfterFailedFilesList) {
					addFilePath(filePath);
				}
			}

			if (copiedAsPlanned > 0) {
				addHeader(copiedAsPlanned + " files copied:");
				for (String filePath : CopyServices.copiedFilesList) {
					if (!CopyServices.overwrittenFilesList.contains(filePath)
							&& !CopyServices.renamedFilesList.contains(filePath)
							&& !CopyServices.renamedAfterFailedFilesList.contains(filePath)) {
						addFilePath(filePath);
					}
				}
			}

		} else if (dlgType == DialogType.SKIPPED) {
			int skippedDiffHashCount = CopyServices.skippedFilesList.size();
			int skippedSameHashFileCount = CopyServices.sameFilesSkippedList.size();
			int skippedSameSrcDstFileCount = CopyServices.srcDstSameFilesSkippedList.size();
			int skippedAfterCopying = CopyServices.copiedAndReplacedFilesList.size();

			if (skippedDiffHashCount > 0) {
				addHeader(skippedDiffHashCount + " files skipped due to name conflict:");
				for (String filePath : CopyServices.skippedFilesList) {
					addFilePath(filePath);
				}
			}

			if (skippedSameHashFileCount > 0) {
				addHeader(
						skippedSameHashFileCount + " files skipped as exact same files already exist in destination:");
				for (String filePath : CopyServices.sameFilesSkippedList) {
					addFilePath(filePath);
				}
			}

			if (skippedSameSrcDstFileCount > 0) {
				addHeader(skippedSameSrcDstFileCount
						+ " files skipped as the files are copied to same location (source and destination same):");
				for (String filePath : CopyServices.srcDstSameFilesSkippedList) {
					addFilePath(filePath);
				}
			}

			if (skippedAfterCopying > 0) {
				addHeader(
						skippedAfterCopying + " files skipped as latest copy of same file copied from another source:");
				for (String filePath : CopyServices.copiedAndReplacedFilesList) {
					addFilePath(filePath);
				}
			}

		} else {
			int failedCount = CopyServices.failedFilesList.size();
			if (failedCount > 0) {
				for (String filePath : CopyServices.failedFilesList) {
					addFilePath(filePath);
				}
			}
		}
	}

	private void addHeader(String title) {
		y += 10;
		JLabel label = new JLabel("<html><b>" + title + "</b></html>");
		label.setForeground(Color.BLACK);
		label.setFont(font);
		label.setBounds(10, y, width - 50, 25);
		panel.add(label);
		y += 25;
	}

	private void addFilePath(String filePath) {
		File file = new File(filePath);
		String fileName = file.getName();
		String parentPath = file.getParent();
		
		parentPath = parentPath == null ? "" : parentPath + File.separator;
		
		JLabel label = new JLabel("<html>" + parentPath+" " + "<b><span style='color:blue;'>"+ fileName +"</span></b></html>");
		label.setForeground(Color.DARK_GRAY);
		label.setToolTipText(filePath);
		label.setFont(font);
		label.setBounds(15, y, width - 50, 20);
		label.setCursor(new Cursor(Cursor.HAND_CURSOR));
		label.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				try {
					Runtime.getRuntime().exec("explorer.exe /select," + filePath);
				} catch (IOException e1) {
				}
			}
		});
		panel.add(label);
		y += 25;
	}

	private JButton getOkButton() {
		if (null == okButton) {
			okButton = new JButton();
			okButton.setFont(font);
			okButton.setBounds(new Rectangle(width - 120, height - 70, 90, 25));
			okButton.setText("Close");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dialog.dispose();
				}
			});
		}
		return okButton;
	}

}
