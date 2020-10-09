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
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.lc.common.CopySettings;
import com.lc.common.Util;
import com.lc.core.Message;

public class CopyNotificationsDlg {

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

	public CopyNotificationsDlg() {

		notifs = CopySettings.copyNotifications;
		if (notifs == null || notifs.size() == 0) {
			return;
		}

		Collections.sort(notifs);

		font = new Font("Arial", Font.PLAIN, 12);
		displayDlg();
	}

	public void displayDlg() {
		if (null == dialog) {
			dialog = new JDialog(LCUiCreator.getUiFrame(), "File copy notifications",
					Dialog.ModalityType.DOCUMENT_MODAL);
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

			boolean addNameLabel = true;
			String fName = null;
			for (Message msg : notifs) {
				addNameLabel = true;
				if(fName != null && fName.equals(msg.getFileName())) {
					addNameLabel = false;
				}
				fName = msg.getFileName();
				addLabel(msg, addNameLabel);
			}
			
			y+= 20;

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

	private void addLabel(Message msg, boolean addNameLabel) {
		y += 15;
		if (addNameLabel) {
			y += 5;
			JLabel label = new JLabel("<html><b><h3>" + msg.getFileName() + ":</h3></b></html>");
			label.setToolTipText(msg.getMessage());
			label.setFont(font);
			label.setBounds(10, y, width - 50, 25);
			panel.add(label);
			y += 30;
		}

		String iconName = "error24.png";
		if (msg.getType() == Message.TYPE.WARNING) {
			iconName = "alert24.png";
		} else if (msg.getType() == Message.TYPE.INFO) {
			iconName = "info24.png";
		}
		String path = Util.getImageDirPath() + iconName;
		ImageIcon icon = new ImageIcon(path);
		JLabel iconLabel = new JLabel(icon);
		iconLabel.setBounds(10, y + 5, 24, 24);
		panel.add(iconLabel);

		
		JLabel label1 = new JLabel("<html>" + msg.getMessage() + "</html>");
		label1.setForeground(Color.DARK_GRAY);
		label1.setToolTipText(msg.getMessage());
		label1.setFont(font);
		label1.setBounds(38, y, width - 50, 25);
		panel.add(label1);
		y += 20;

		if (msg.getReason() != null) {
			JLabel label2 = new JLabel("<html><b>Reason:</b> " + msg.getReason() + "</html>");
			label2.setForeground(Color.DARK_GRAY);
			label2.setToolTipText(msg.getReason());
			label2.setFont(font);
			label2.setBounds(40, y, width - 50, 25);
			panel.add(label2);
			y += 20;
		}

		if (msg.getCorrectiveAction() != null) {
			JLabel label0 = new JLabel("<html><b>"
					+ (msg.getActionFile() != null ? "<u>Corrective action</u>:" : "Corrective action:") + "</html>");
			label0.setForeground(LINK_COLOR);
			label0.setToolTipText(msg.getCorrectiveAction());
			if (msg.getActionFile() != null) {
				label0.setCursor(new Cursor(Cursor.HAND_CURSOR));
				label0.addMouseListener(new MouseAdapter() {

					@Override
					public void mouseClicked(MouseEvent e) {
						try {
							Runtime.getRuntime().exec("explorer.exe /select," + msg.getActionFile().getAbsolutePath());
						} catch (IOException e1) {
						}
					}
				});

			}
			label0.setFont(font);
			label0.setBounds(40, y, 110, 25);
			panel.add(label0);

			JLabel label2 = new JLabel("<html>" + msg.getCorrectiveAction() + "</html>");
			label2.setForeground(Color.DARK_GRAY);
			label2.setToolTipText(msg.getCorrectiveAction());
			label2.setFont(font);
			label2.setBounds(145, y, width - 50, 25);
			panel.add(label2);
			y += 20;
		}

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
