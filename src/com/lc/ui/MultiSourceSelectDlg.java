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

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.lc.common.CopySettings;
import com.lc.common.Util;
import com.lc.core.Message;
import com.lc.service.CopyServices;

public class MultiSourceSelectDlg {

	private JDialog dialog;
	private JButton okButton = null;
	private Font font;
	private Font sfont;
	JScrollPane scrollPane;
	int y = 0;
	int width = 800;
	int height = 500;
	JPanel panel;

	HashMap<String, JCheckBox> checksList = new HashMap<String, JCheckBox>();
	protected static boolean isDirty;
	private static List<String> selectedSourceList = new ArrayList<String>();

	public MultiSourceSelectDlg(List<String> srcsWithSrcConflict) {

		selectedSourceList = srcsWithSrcConflict;

		isDirty = false;

		font = new Font("Arial", Font.PLAIN, 13);
		sfont = new Font("Arial", Font.PLAIN, 12);

		displayDlg();
	}

	public void displayDlg() {
		if (null == dialog) {
			String title = "Conflicting source files";
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
		Set<String> keys = CopyServices.dstToMultiSrcMap.keySet();
		for (String dst : keys) {
			addDstFilePath(dst);

			addSrcHeader();

			for (String src : CopyServices.dstToMultiSrcMap.get(dst)) {
				addSrcPath(src);
			}
		}
	}

	private void addSrcHeader() {
		JLabel label = new JLabel("<html><b><i>To be overwritten by:</i></b></html>");
		label.setForeground(Color.BLACK);
		label.setFont(font);
		label.setBounds(30, y, width - 70, 25);
		panel.add(label);
		y += 20;
	}

	private void addDstFilePath(String filePath) {
		y += 5;

		JLabel fileIcon = new JLabel(new ImageIcon(Util.getImageDirPath() + "file.png"));
		fileIcon.setBounds(10, y, 16, 20);
		panel.add(fileIcon);

		JLabel label = new JLabel(filePath);
		label.setToolTipText("<html><b>" + filePath + "</b> (ctrl+click to open)</html>");
		label.setFont(sfont);
		label.setBounds(30, y, width - 230, 20);
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
		sizeLabel.setFont(sfont);
		sizeLabel.setForeground(Color.DARK_GRAY);
		sizeLabel.setBounds(width - 200, y, 60, 20);
		panel.add(sizeLabel);

		JLabel dateLabel = new JLabel(Util.getFullFormattedDateTime(new File(filePath).lastModified()));
		dateLabel.setToolTipText(Util.getFormattedDateTime(new File(filePath).lastModified()));
		dateLabel.setFont(sfont);
		dateLabel.setForeground(Color.DARK_GRAY);
		dateLabel.setBounds(width - 140, y, 110, 20);
		panel.add(dateLabel);

		y += 20;
	}

	private void addSrcPath(String filePath) {
		JCheckBox check = new JCheckBox("", selectedSourceList.contains(filePath));
		check.setBounds(40, y, 20, 20);
		check.setBackground(Color.WHITE);
		panel.add(check);
		checksList.put(filePath, check);

		JLabel label = new JLabel("<html>" + filePath + "</html>");
		label.setForeground(Color.BLUE);
		label.setToolTipText("<html><b>" + filePath + "</b> (ctrl+click to open)</html>");
		label.setFont(font);
		label.setBounds(65, y, width - 265, 20);
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
				}
			}
		});
		panel.add(label);

		JLabel sizeLabel = new JLabel(Util.getFormatedStringForByte(new File(filePath).length()));
		sizeLabel.setToolTipText(Util.getFormatedStringForByte(new File(filePath).length()));
		sizeLabel.setFont(sfont);
		sizeLabel.setForeground(Color.DARK_GRAY);
		sizeLabel.setBounds(width - 200, y, 60, 20);
		panel.add(sizeLabel);

		JLabel dateLabel = new JLabel(Util.getFullFormattedDateTime(new File(filePath).lastModified()));
		dateLabel.setToolTipText(Util.getFormattedDateTime(new File(filePath).lastModified()));
		dateLabel.setFont(sfont);
		dateLabel.setForeground(Color.DARK_GRAY);
		dateLabel.setBounds(width - 140, y, 110, 20);
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

					isDirty = false;

					List<String> tempSrcList = new ArrayList<String>();
					Set<String> paths = checksList.keySet();
					for (String path : paths) {
						JCheckBox check = checksList.get(path);
						if (check.isSelected()) {
							tempSrcList.add(path);
						}
					}
					if (selectedSourceList.size() != tempSrcList.size()) {
						isDirty = true;
						selectedSourceList.clear();
						selectedSourceList.addAll(tempSrcList);
					} else {
						for (String s : tempSrcList) {
							if (!selectedSourceList.contains(s)) {
								selectedSourceList.add(s);
								isDirty = true;
							}
						}
						if (isDirty) {
							for (String s : selectedSourceList) {
								if (!tempSrcList.contains(s)) {
									selectedSourceList.remove(s);
									isDirty = true;
								}
							}
						}
					}

					dialog.dispose();
				}
			});
		}

		return okButton;
	}

	public static List<String> getSelectedSrcPathsToCopy() {
		return selectedSourceList;
	}

	public static boolean isSelectedSrcFilesListDirty() {
		return isDirty;
	}

}
