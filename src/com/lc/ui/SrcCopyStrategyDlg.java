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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.lc.common.AppMessageService;
import com.lc.common.CopySettings;
import com.lc.common.CopySettings.MultiSourceCopyActionEnum;
import com.lc.common.Util;
import com.lc.service.CopyServices;

public class SrcCopyStrategyDlg {

	private JDialog dialog;
	private JButton okButton = null;
	private Font font;
	private Font bfont;
	private Font hfont;
	private JFrame jframe;
	
	JPanel srcPanel;
	JLabel srcSecHeader;
	JLabel srcSecDescLabel;
	JLabel srcSecAddlDescLabel;
	JLabel srcSecStrategyLabel;
	JLabel viewSrcLabel;

	private static final String RENAME_AND_COPY_EVERY_FILE = "Rename and copy every file";
	private static final String RENAME_AND_COPY_DISTINCT_FILES = "Rename and copy distinct files only";
	private static final String COPY_LATEST_VERSION_OF_FILES = "Copy only latest version of files";
	private static final String CUSTOM_SELECTION = "Custom selection";

	JComboBox<String> srcStrategyCombo;
	int width = 600, height = 310;
//	List<String> selectedDstsWithSrcConflict = new ArrayList<String>();
	public static List<String> selectedSrcsWithSrcConflict = new ArrayList<String>();

	public SrcCopyStrategyDlg() {

//		CopySettings.COPY_MULTIPLE_FILES_TO_SAME_PATH__ASK_FOR_EACH_FILE = false;
		CopySettings.COPY_MULTIPLE_FILES_TO_SAME_PATH__ACTION_SELECTED = MultiSourceCopyActionEnum.RENAME_AND_COPY_ALL;
		updateSrcConflictList();

		jframe = LCUiCreator.getUiFrame();
		font = new Font("Arial", Font.PLAIN, 12);
		bfont = new Font("Arial", Font.BOLD, 13);
		hfont = new Font("Arial", Font.PLAIN, 16);
		displayDlg();
	}

	public void displayDlg() {
		if (null == dialog) {
			dialog = new JDialog(jframe, "Resolve source file name conflicts", Dialog.ModalityType.DOCUMENT_MODAL);
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

			dialog.add(buildSrcSection());

			dialog.add(getOkButton());
			dialog.add(getCancelLabelButton());

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

	public JPanel buildSrcSection() {

		srcPanel = new JPanel(null);
		int y = 25;
		srcPanel.setBounds(0, y, width, 170);

		srcSecHeader = new JLabel("Copying strategy for source files with name conflicts:");
		srcSecHeader.setFont(hfont);
		srcSecHeader.setForeground(new Color(0, 0, 102));
		srcSecHeader.setBounds(10, 0, width - 20, 30);
		srcPanel.add(srcSecHeader);

		srcSecDescLabel = new JLabel(
				"Files with same name and relative path have been selected from different sources.");
		srcSecDescLabel.setFont(font);
		srcSecDescLabel.setBounds(30, 50, width - 20, 25);
		srcPanel.add(srcSecDescLabel);

		srcSecAddlDescLabel = new JLabel("Cannot copy them together to same path. What would you like to do?");
		srcSecAddlDescLabel.setFont(font);
		srcSecAddlDescLabel.setBounds(30, 75, width - 20, 25);
		srcPanel.add(srcSecAddlDescLabel);

		srcSecStrategyLabel = new JLabel("Strategy:", JLabel.RIGHT);
		srcSecStrategyLabel.setFont(bfont);
		srcSecStrategyLabel.setBounds(50, 120, 110, 25);
		srcPanel.add(srcSecStrategyLabel);

		srcStrategyCombo = new JComboBox<String>();
		srcStrategyCombo.addItem(RENAME_AND_COPY_EVERY_FILE);
		srcStrategyCombo.addItem(RENAME_AND_COPY_DISTINCT_FILES);
		srcStrategyCombo.addItem(COPY_LATEST_VERSION_OF_FILES);
		srcStrategyCombo.addItem(CUSTOM_SELECTION);

		srcStrategyCombo.setBounds(180, 120, 225, 25);
		srcStrategyCombo.setFont(font);
		srcStrategyCombo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateSrcConflictList();
			}
		});
		srcPanel.add(srcStrategyCombo);

		viewSrcLabel = new JLabel("View files");
		viewSrcLabel.setFont(bfont);
		viewSrcLabel.setBounds(425, 120, 110, 25);
		viewSrcLabel.setForeground(Color.blue);
		viewSrcLabel.setFont(font);
		viewSrcLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		viewSrcLabel.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				
					
				
					new MultiSourceSelectDlg(selectedSrcsWithSrcConflict);
					if (MultiSourceSelectDlg.isSelectedSrcFilesListDirty()) {
						selectedSrcsWithSrcConflict = MultiSourceSelectDlg.getSelectedSrcPathsToCopy();
						srcStrategyCombo.setSelectedItem(CUSTOM_SELECTION);
					}
			}
		});
		srcPanel.add(viewSrcLabel);

		return srcPanel;
	}

	protected void updateSrcConflictList() {
		if (srcStrategyCombo == null || srcStrategyCombo.getSelectedItem().equals(RENAME_AND_COPY_EVERY_FILE)) {
			selectedSrcsWithSrcConflict.clear();
			Set<String> keys = CopyServices.dstToMultiSrcMap.keySet();
			for (String key : keys) {
				selectedSrcsWithSrcConflict.addAll(CopyServices.dstToMultiSrcMap.get(key));
			}
		} else if (srcStrategyCombo.getSelectedItem().equals(RENAME_AND_COPY_DISTINCT_FILES)) {
			selectedSrcsWithSrcConflict.clear();
			Set<String> keys = CopyServices.dstToMultiSrcMap.keySet();
			for (String key : keys) {
				selectedSrcsWithSrcConflict.addAll(getDistinctFiles(CopyServices.dstToMultiSrcMap.get(key)));
			}
		} else if (srcStrategyCombo.getSelectedItem().equals(COPY_LATEST_VERSION_OF_FILES)) {
			selectedSrcsWithSrcConflict.clear();
			Set<String> keys = CopyServices.dstToMultiSrcMap.keySet();
			for (String key : keys) {
				selectedSrcsWithSrcConflict.add(getLatestFile(CopyServices.dstToMultiSrcMap.get(key)));
			}
		}
	}

	private List<String> getDistinctFiles(List<String> paths) {
		HashMap<Long, String> distinctMap = new HashMap<Long, String>();
		for (String path : paths) {
			if (distinctMap.get(new File(path).length()) == null) {
				distinctMap.put(new File(path).length(), path);
			}
		}
		return new ArrayList<String>(distinctMap.values());
	}

	private String getLatestFile(List<String> paths) {
		File latestFile = null;
		for (String path : paths) {
			File f = new File(path);
			if (latestFile == null) {
				latestFile = f;
			} else if (f.lastModified() > latestFile.lastModified()) {
				latestFile = f;
			}
		}
		return latestFile.getAbsolutePath();
	}

	private JButton getOkButton() {
		if (null == okButton) {
			okButton = new JButton();
			okButton.setFont(hfont);
			okButton.setBounds(new Rectangle(230, height - 100, 140, 35));
			okButton.setText("Save");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					okButtonActionPerformed();
				}
			});
		}
		return okButton;
	}

	public JLabel getCancelLabelButton() {
		JLabel cancelLabel = new JLabel("Cancel Copy", JLabel.CENTER);
		cancelLabel.setBounds(width - 100, 0, 100, 25);
		cancelLabel.setBackground(Color.DARK_GRAY);
		cancelLabel.setOpaque(true);
		cancelLabel.setForeground(Color.WHITE);
		cancelLabel.setFont(font);
		cancelLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		cancelLabel.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (AppMessageService.showQMessage("Are you sure, you want to cancel the operation?", "Cancel copy")) {
					CopySettings.status = CopySettings.State.CANCELLED;
					dialog.dispose();
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				cancelLabel.setForeground(Color.YELLOW);
				cancelLabel.setFont(bfont);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				cancelLabel.setForeground(Color.WHITE);
				cancelLabel.setFont(font);
			}
		});
		return cancelLabel;
	}

	private void okButtonActionPerformed() {

		if (srcStrategyCombo.getSelectedItem().equals(RENAME_AND_COPY_EVERY_FILE)) {
//			CopySettings.COPY_MULTIPLE_FILES_TO_SAME_PATH__ASK_FOR_EACH_FILE = false;
			CopySettings.COPY_MULTIPLE_FILES_TO_SAME_PATH__ACTION_SELECTED = MultiSourceCopyActionEnum.RENAME_AND_COPY_ALL;
		} else if (srcStrategyCombo.getSelectedItem().equals(RENAME_AND_COPY_DISTINCT_FILES)) {
//			CopySettings.COPY_MULTIPLE_FILES_TO_SAME_PATH__ASK_FOR_EACH_FILE = false;
			CopySettings.COPY_MULTIPLE_FILES_TO_SAME_PATH__ACTION_SELECTED = MultiSourceCopyActionEnum.RENAME_AND_COPY_IF_DISTINCT;
		} else if (srcStrategyCombo.getSelectedItem().equals(COPY_LATEST_VERSION_OF_FILES)) {
//			CopySettings.COPY_MULTIPLE_FILES_TO_SAME_PATH__ASK_FOR_EACH_FILE = false;
			CopySettings.COPY_MULTIPLE_FILES_TO_SAME_PATH__ACTION_SELECTED = MultiSourceCopyActionEnum.COPY_IF_LATEST;
		}else {
			CopySettings.COPY_MULTIPLE_FILES_TO_SAME_PATH__ACTION_SELECTED = MultiSourceCopyActionEnum.CUSTOM_SELECTION;
		}

		dialog.dispose();

	}

}
