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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.lc.common.CopySettings;
import com.lc.common.Util;
import com.lc.core.DstExistence;
import com.lc.core.Message;

public class ExistingDstDlg {

	private JDialog dialog;
	private JButton okButton = null;
	private Font font;
	private Font sfont;
	JScrollPane scrollPane;
	int y = 0;
	int width = 800;
	int height = 500;
	JPanel panel;
	List<Message> notifs;
	Color LINK_COLOR = new Color(0, 0, 102);
	List<DstExistence> allDstList;
	String header = "";
	JCheckBox selectAllCheck;
	JCheckBox unselectAllCheck;
	HashMap<String, JCheckBox> checksList = new HashMap<String, JCheckBox>();
	int selectValueState = 0;
	private static List<String> selectedForOverwrite = new ArrayList<String>();
	private boolean refreshChecksOn = false;

	public ExistingDstDlg(String overwriteTypeKey, List<String> selectedList) {

		this.allDstList = CopySettings.overwriteTypeToFilesMap.get(overwriteTypeKey);
		selectedForOverwrite = selectedList;

		header = getHeader(overwriteTypeKey);

		if (allDstList == null || allDstList.size() == 0) {
			return;
		}

		font = new Font("Arial", Font.PLAIN, 13);
		sfont = new Font("Arial", Font.PLAIN, 12);

		displayDlg();
	}

	private String getHeader(String overwriteTypeKey) {

		switch (overwriteTypeKey) {
		case CopySettings.SAME_SIZE_OLDER_DATE:
			return "Existing files in destination have same size, but an older last modified dates";
		case CopySettings.SAME_SIZE_LATEST_DATE:
			return "Existing files in destination have same size, but a later last modified dates";
		case CopySettings.SAME_SIZE_SAME_DATE:
			return "Existing files in destination have same size, as well as same last modified dates";
		case CopySettings.DIFFERENT_SIZE_OLDER_DATE:
			return "Existing files in destination have different size, but an older last modified dates";
		case CopySettings.DIFFERENT_SIZE_LATEST_DATE:
			return "Existing files in destination have different size, but a later last modified dates";
		case CopySettings.DIFFERENT_SIZE_SAME_DATE:
			return "Existing files in destination have different size, but same last modified dates";
		default:
			return "";
		}
	}

	public void displayDlg() {
		if (null == dialog) {
			String title = "Files already existing";
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
		addHeader(header);
		for (DstExistence dst : allDstList) {

			addDstFilePath(dst.getDstPath());

			if (dst.getSrcCount() > 0) {
				addSrcHeader();
			}

			String src = dst.getPreferredSrc() != null ? dst.getPreferredSrc() : dst.getSources().get(0);
			addSrcPath(src);
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

	private void addSrcHeader() {
		String text = "To be overwritten by:";
		JLabel label = new JLabel("<html><b>" + text + "</b></html>");
		label.setForeground(Color.DARK_GRAY);
		label.setFont(sfont);
		label.setBounds(35, y, width - 70, 25);
		panel.add(label);
		y += 20;
	}

	private void addDstFilePath(String filePath) {
		y += 5;
		File file = new File(filePath);
		String fileName = file.getName();
		String parentPath = file.getParent();

		parentPath = parentPath == null ? "" : parentPath + File.separator;

		JCheckBox check = new JCheckBox("", selectedForOverwrite.contains(filePath));
		check.setBounds(10, y, 20, 20);
		check.setBackground(Color.WHITE);
		check.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!refreshChecksOn) {
					selectValueState = 0;
					refreshChecks();
				}
			}
		});
		panel.add(check);
		checksList.put(filePath, check);

		JLabel label = new JLabel(
				"<html>" + parentPath + " " + "<b><span style='color:blue;'>" + fileName + "</span></b></html>");
		label.setForeground(Color.BLUE);
		label.setToolTipText("<html><b>" + filePath + "</b> (ctrl+click to open)</html>");
		label.setFont(font);
		label.setBounds(35, y, width - 235, 20);
		label.setCursor(new Cursor(Cursor.HAND_CURSOR));
		label.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (arg0.isControlDown()) {
					try {
						Runtime.getRuntime().exec("explorer.exe /select," + filePath);
					} catch (IOException e1) {
					}
				} else {
					check.setSelected(!check.isSelected());
					if (!refreshChecksOn) {
						selectValueState = 0;
						refreshChecks();
					}
				}
			}
		});
		panel.add(label);

		JLabel sizeLabel = new JLabel(Util.getFormatedStringForByte(new File(filePath).length()));
		sizeLabel.setToolTipText(Util.getFormatedStringForByte(new File(filePath).length()));
		sizeLabel.setFont(font);
		sizeLabel.setBounds(width - 200, y, 60, 20);
		panel.add(sizeLabel);

		JLabel dateLabel = new JLabel(Util.getFullFormattedDateTime(new File(filePath).lastModified()));
		dateLabel.setToolTipText(Util.getFormattedDateTime(new File(filePath).lastModified()));
		dateLabel.setFont(font);
		dateLabel.setBounds(width - 140, y, 110, 20);
		panel.add(dateLabel);

		y += 20;
	}

	private void addSrcPath(String filePath) {
		JLabel label = new JLabel("<html>" + filePath + "</html>");
		label.setForeground(Color.DARK_GRAY);
		label.setToolTipText("<html><b>" + filePath + "</b> (ctrl+click to open)</html>");
		label.setFont(sfont);
		label.setForeground(Color.gray);
		label.setBounds(55, y, width - 255, 20);
		label.setCursor(new Cursor(Cursor.HAND_CURSOR));
		label.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (arg0.isControlDown()) {
					try {
						Runtime.getRuntime().exec("explorer.exe /select," + filePath);
					} catch (IOException e1) {
					}
				}
			}
		});
		panel.add(label);

		JLabel sizeLabel = new JLabel(Util.getFormatedStringForByte(new File(filePath).length()));
		sizeLabel.setToolTipText(Util.getFormatedStringForByte(new File(filePath).length()));
		sizeLabel.setForeground(Color.gray);
		sizeLabel.setBounds(width - 200, y, 60, 20);
		panel.add(sizeLabel);

		JLabel dateLabel = new JLabel(Util.getFullFormattedDateTime(new File(filePath).lastModified()));
		dateLabel.setToolTipText(Util.getFormattedDateTime(new File(filePath).lastModified()));
		dateLabel.setForeground(Color.gray);
		dateLabel.setBounds(width - 135, y, 100, 20);
		panel.add(dateLabel);

		y += 20;

	}

	private JButton getOkButton() {
		if (null == okButton) {
			okButton = new JButton();
			okButton.setFont(font);
			okButton.setBounds(new Rectangle(width - 120, height - 70, 90, 25));
			okButton.setText("Save");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					selectedForOverwrite.clear();
					Set<String> paths = checksList.keySet();
					for (String path : paths) {
						JCheckBox check = checksList.get(path);
						if (check.isSelected()) {
							selectedForOverwrite.add(path);
						}
					}
					dialog.dispose();
				}
			});
		}

		selectAllCheck = new JCheckBox("Overwrite all", allDstList.size() == selectedForOverwrite.size());
		selectAllCheck.setFont(font);
		selectAllCheck.setBounds(20, height - 70, 100, 25);
		dialog.add(selectAllCheck);
		selectAllCheck.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectAllCheck.isSelected()) {
					selectValueState = 1;
					refreshChecks();
				}
			}
		});

		unselectAllCheck = new JCheckBox("Skip all", selectedForOverwrite.size() == 0);
		unselectAllCheck.setFont(font);
		unselectAllCheck.setBounds(140, height - 70, 100, 25);
		dialog.add(unselectAllCheck);
		unselectAllCheck.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (unselectAllCheck.isSelected()) {
					selectValueState = -1;
					refreshChecks();
				}
			}
		});

		return okButton;
	}

	private void refreshChecks() {
		if (selectValueState == 1) {
			selectAllChecks(true);
			unselectAllCheck.setSelected(false);
		} else if (selectValueState == -1) {
			selectAllChecks(false);
			selectAllCheck.setSelected(false);
		} else {
			int count = 0;
			Set<String> paths = checksList.keySet();
			for (String path : paths) {
				JCheckBox check = checksList.get(path);
				if (check.isSelected()) {
					count++;
				}
			}

			if (count == allDstList.size()) {
				selectValueState = 1;
				selectAllCheck.setSelected(true);
				unselectAllCheck.setSelected(false);
				selectAllChecks(true);
			} else if (count == 0) {
				selectValueState = -1;
				selectAllCheck.setSelected(false);
				unselectAllCheck.setSelected(true);
				selectAllChecks(false);
			} else {
				selectAllCheck.setSelected(false);
				unselectAllCheck.setSelected(false);
			}
		}
	}

	private void selectAllChecks(boolean select) {
		refreshChecksOn = true;
		Set<String> paths = checksList.keySet();
		for (String path : paths) {
			JCheckBox check = checksList.get(path);
			check.setSelected(select);
		}
		refreshChecksOn = false;
	}

	public static List<String> getSelectedDstPathsOverwrite() {
		return selectedForOverwrite;
	}

}
