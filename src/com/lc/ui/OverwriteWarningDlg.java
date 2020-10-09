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
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.lc.common.AppMessageService;
import com.lc.common.CopySettings;
import com.lc.common.Util;
import com.lc.core.DstExistence;
import com.lc.service.CopyServices;

public class OverwriteWarningDlg {

	private JDialog dialog;
	private Font font;
	private Font bfont;
	private JFrame jframe;

	File srcFile;
	File dstFile;

	public OverwriteWarningDlg(File srcFile, File dstFile) {
		this.srcFile = srcFile;
		this.dstFile = dstFile;
		jframe = LCUiCreator.getUiFrame();
		font = new Font("Arial", Font.PLAIN, 12);
		bfont = new Font("Arial", Font.BOLD, 13);
		displayDlg();
	}

	public void displayDlg() {
		if (null == dialog) {
			dialog = new JDialog(jframe, "File already exists", Dialog.ModalityType.DOCUMENT_MODAL);
			int width = 500, height = 300;
			Dimension dimension = new Dimension(width, height);
			dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
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

			JLabel cancelLabel = new JLabel("Cancel Copy", JLabel.CENTER);
			cancelLabel.setBounds(width - 110, 0, 100, 25);
			cancelLabel.setBackground(Color.DARK_GRAY);
			cancelLabel.setOpaque(true);
			cancelLabel.setForeground(Color.WHITE);
			cancelLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			cancelLabel.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					if (AppMessageService.showQMessage("Are you sure, you want to cancel the operation?",
			                "Cancel copy")) {
						CopySettings.status = CopySettings.State.CANCELLED;
						dialog.dispose();
			        }
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					cancelLabel.setForeground(Color.YELLOW);
				}

				@Override
				public void mouseExited(MouseEvent e) {
					cancelLabel.setForeground(Color.WHITE);
				}
			});
			dialog.add(cancelLabel);

			JLabel nameLabel = new JLabel(srcFile.getName());
			nameLabel.setFont(bfont);
			nameLabel.setBounds(20, 20, width - 20, 25);
			dialog.add(nameLabel);

			JLabel descLabel = new JLabel("The file already exists. Would you like to overwrite the existing file?");
			descLabel.setFont(font);
			descLabel.setBounds(30, 50, width - 20, 25);
			dialog.add(descLabel);

			JLabel srcLabel = new JLabel("Source:          " + Util.getFormattedDateTime(srcFile.lastModified())
					+ ";   " + NumberFormat.getNumberInstance(Locale.getDefault()).format(srcFile.length()) + " bytes");
			srcLabel.setFont(font);
			srcLabel.setBounds(30, 90, width - 40, 25);
			dialog.add(srcLabel);

			JLabel dstLabel = new JLabel("Destination:  " + Util.getFormattedDateTime(dstFile.lastModified()) + ";   "
					+ NumberFormat.getNumberInstance(Locale.getDefault()).format(dstFile.length()) + " bytes");
			dstLabel.setFont(font);
			dstLabel.setBounds(30, 120, width - 40, 25);
			dialog.add(dstLabel);

			// -------------------------

			JButton replaceButton = new JButton();
			replaceButton.setFont(font);
			replaceButton.setBounds(new Rectangle(50, 170, 100, 30));
			replaceButton.setText("Overwrite");
			replaceButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					CopySettings.ASK_FOR_EACH_FILE = true;
					CopySettings.FILES_TO_OVERWRITE.add(dstFile.getAbsolutePath());
					
					CopySettings.RENAME_SKIPPED_FILES = false;
					
					dialog.dispose();
				}
			});
			dialog.add(replaceButton);

			JButton skipButton = new JButton();
			skipButton.setFont(font);
			skipButton.setBounds(new Rectangle(200, 170, 100, 30));
			skipButton.setText("Skip");
			skipButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					CopySettings.ASK_FOR_EACH_FILE = true;
					
					dialog.dispose();
				}
			});
			dialog.add(skipButton);

			JButton renameButton = new JButton();
			renameButton.setFont(font);
			renameButton.setBounds(new Rectangle(350, 170, 100, 30));
			renameButton.setText("Rename");
			renameButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					CopySettings.ASK_FOR_EACH_FILE = true;
					CopySettings.RENAME_SKIPPED_FILES = true;

					dialog.dispose();
				}
			});
			dialog.add(renameButton);

			// -------------------------

			JButton replaceAllButton = new JButton();
			replaceAllButton.setFont(font);
			replaceAllButton.setBounds(new Rectangle(50, 210, 100, 23));
			replaceAllButton.setText("Overwrite All");
			replaceAllButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					CopySettings.ASK_FOR_EACH_FILE = false;
					CopySettings.RENAME_SKIPPED_FILES = false;
					
					CopySettings.FILES_TO_OVERWRITE.add(dstFile.getAbsolutePath());
					
					Set<String> keys = CopySettings.overwriteTypeToFilesMap.keySet();
					for(String key:keys) {
						List<DstExistence> files = CopySettings.overwriteTypeToFilesMap.get(key);
						if(files != null) {
						for(DstExistence file:files) {
							if(!CopySettings.FILES_TO_OVERWRITE.contains(file.getDstPath())) {
								CopySettings.FILES_TO_OVERWRITE.add(file.getDstPath());
							}
						}
						}
					}
					
					
					CopyServices.cookFileCopies();
					
					dialog.dispose();
				}
			});
			dialog.add(replaceAllButton);

			JButton skipAllButton = new JButton();
			skipAllButton.setFont(font);
			skipAllButton.setBounds(new Rectangle(200, 210, 100, 23));
			skipAllButton.setText("Skip All");
			skipAllButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					CopySettings.ASK_FOR_EACH_FILE = false;
					CopySettings.RENAME_SKIPPED_FILES = false;

					CopyServices.cookFileCopies();
					
					dialog.dispose();
				}
			});
			dialog.add(skipAllButton);

			JButton renameAllButton = new JButton();
			renameAllButton.setFont(font);
			renameAllButton.setBounds(new Rectangle(350, 210, 100, 23));
			renameAllButton.setText("Rename All");
			renameAllButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					CopySettings.ASK_FOR_EACH_FILE = false;
					CopySettings.RENAME_SKIPPED_FILES = true;

					CopyServices.cookFileCopies();
					
					dialog.dispose();
				}
			});
			dialog.add(renameAllButton);

			// -------------------------

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

}
