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
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.lc.common.AppMessageService;
import com.lc.common.CopySettings;
import com.lc.common.Util;
import com.lc.core.DstExistence;

public class OverwriteStrategyDlg {

	private JDialog dialog;
	private JButton okButton = null;
	private Font font;
	private Font bfont;
	private Font hfont;
	private JFrame jframe;
	JPanel strategyPanel;
	TripleCheck c1;
	TripleCheck c2;
	TripleCheck c3;
	TripleCheck c4;
	TripleCheck c5;
	TripleCheck c6;
	JCheckBox rename;

	JLabel dsizeLabel;
	JLabel ssizeLabel;
	JLabel olderLabel;
	JLabel newerLabel;
	JLabel sameLabel;
	JComboBox<String> dstStrategyCombo;

	private static final String SKIP_ALL = "Skip all";
	private static final String OVERWRITE_ALL = "Overwrite all";
	private static final String OVERWRITE_DIFFERENT_SIZED = "Overwrite different sized files";
	private static final String OVERWRITE_OLDER = "Overwrite older files";
	private static final String OVERWRITE_DIFFERENT_DATED = "Overwrite different dated files (both older and latest)";
	private static final String SKIP_SIMILAR = "Skip similar files, overwrite all others";
	private static final String OVERWRITE_CUSTOM = "Custom strategy";
	private static final String OVERWRITE_BY_ASKING_SEPARATELY = "Ask seaparately for each files";

	private static final Color SafestBorderColor = new Color(230, 255, 247);// cyan + grey
	private static final Color SaferBorderColor = new Color(245, 245, 204);// yellow + grey
	private static final Color SafeBorderColor = new Color(255, 235, 204);// orange + grey
	private static final Color deadBorderColor = new Color(245, 245, 245);// grey

	int width = 600, height = 500;
	private boolean isStrategyPanelEnabled = true;

	public OverwriteStrategyDlg() {

		CopySettings.FILES_TO_OVERWRITE.clear();
		CopySettings.RENAME_SKIPPED_FILES = false;
		CopySettings.ASK_FOR_EACH_FILE = true;

		jframe = LCUiCreator.getUiFrame();
		font = new Font("Arial", Font.PLAIN, 12);
		bfont = new Font("Arial", Font.BOLD, 13);
		hfont = new Font("Arial", Font.PLAIN, 16);
		displayDlg();
	}

	public void displayDlg() {
		if (null == dialog) {
			dialog = new JDialog(jframe, "Select overwrite strategy", Dialog.ModalityType.DOCUMENT_MODAL);
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

			dialog.add(buildDstSection());

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

	public JPanel buildDstSection() {

		List<DstExistence> dstExistenceList1 = CopySettings.overwriteTypeToFilesMap
				.get(CopySettings.DIFFERENT_SIZE_OLDER_DATE);
		Integer count1 = dstExistenceList1 != null ? dstExistenceList1.size() : null;
		List<DstExistence> dstExistenceList2 = CopySettings.overwriteTypeToFilesMap
				.get(CopySettings.SAME_SIZE_OLDER_DATE);
		Integer count2 = dstExistenceList2 != null ? dstExistenceList2.size() : null;
		List<DstExistence> dstExistenceList3 = CopySettings.overwriteTypeToFilesMap
				.get(CopySettings.DIFFERENT_SIZE_LATEST_DATE);
		Integer count3 = dstExistenceList3 != null ? dstExistenceList3.size() : null;
		List<DstExistence> dstExistenceList4 = CopySettings.overwriteTypeToFilesMap
				.get(CopySettings.SAME_SIZE_LATEST_DATE);
		Integer count4 = dstExistenceList4 != null ? dstExistenceList4.size() : null;
		List<DstExistence> dstExistenceList5 = CopySettings.overwriteTypeToFilesMap
				.get(CopySettings.DIFFERENT_SIZE_SAME_DATE);
		Integer count5 = dstExistenceList5 != null ? dstExistenceList5.size() : null;
		List<DstExistence> dstExistenceList6 = CopySettings.overwriteTypeToFilesMap
				.get(CopySettings.SAME_SIZE_SAME_DATE);
		Integer count6 = dstExistenceList6 != null ? dstExistenceList6.size() : null;

		boolean c1Alive = count1 != null;
		boolean c2Alive = count2 != null;
		boolean c3Alive = count3 != null;
		boolean c4Alive = count4 != null;
		boolean c5Alive = count5 != null;
		boolean c6Alive = count6 != null;

		int totalOverwrites = 0;
		totalOverwrites += c1Alive ? count1 : 0;
		totalOverwrites += c2Alive ? count2 : 0;
		totalOverwrites += c3Alive ? count3 : 0;
		totalOverwrites += c4Alive ? count4 : 0;
		totalOverwrites += c5Alive ? count5 : 0;
		totalOverwrites += c6Alive ? count6 : 0;

		JPanel panel = new JPanel(null);
		panel.setBounds(0, 25, width, 375);

		JLabel header = new JLabel("Copying strategy for destination files with same names:");
		header.setFont(hfont);
		header.setForeground(new Color(0, 0, 102));
		header.setBounds(10, 0, width - 20, 30);
		panel.add(header);

		JLabel nameLabel = new JLabel(
				"There are existing files with same names in the destination folder. Please select a overwrite strategy.");
		nameLabel.setFont(font);
		nameLabel.setBounds(20, 40, width - 20, 25);
		panel.add(nameLabel);

		JLabel srcLabel = new JLabel("Strategy:", JLabel.RIGHT);
		srcLabel.setFont(bfont);
		srcLabel.setBounds(50, 80, 110, 25);
		panel.add(srcLabel);

		dstStrategyCombo = new JComboBox<String>();
		dstStrategyCombo.addItem(SKIP_ALL);
		dstStrategyCombo.addItem(OVERWRITE_ALL);
		if ((c1Alive || c3Alive || c5Alive) && (c2Alive || c4Alive || c6Alive)) {
			dstStrategyCombo.addItem(OVERWRITE_DIFFERENT_SIZED);
		}
		if ((c1Alive || c2Alive) && (c3Alive || c4Alive || c5Alive || c6Alive)) {
			dstStrategyCombo.addItem(OVERWRITE_OLDER);
		}
		if ((c5Alive || c6Alive) && (c1Alive || c2Alive || c3Alive || c4Alive)) {
			dstStrategyCombo.addItem(OVERWRITE_DIFFERENT_DATED);
		}
		if (c6Alive && (c1Alive || c2Alive || c3Alive || c4Alive || c5Alive)) {
			dstStrategyCombo.addItem(SKIP_SIMILAR);
		}
		dstStrategyCombo.addItem(OVERWRITE_CUSTOM);
		if (totalOverwrites > 1) {
			dstStrategyCombo.addItem(OVERWRITE_BY_ASKING_SEPARATELY);
		}
		dstStrategyCombo.setBounds(180, 80, 320, 25);
		dstStrategyCombo.setFont(font);
		dstStrategyCombo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setCheckStates(dstStrategyCombo.getSelectedItem().toString());

			}
		});
		panel.add(dstStrategyCombo);

		strategyPanel = new JPanel(null);
		strategyPanel.setBounds(0, 130, width, 220);
		panel.add(strategyPanel);

		// -----------------------------------------------

		JPanel sizeNamePanel = new JPanel(null);
		sizeNamePanel.setBounds(20, 0, width, 30);
		strategyPanel.add(sizeNamePanel);

		dsizeLabel = new JLabel("Different file size");
		dsizeLabel.setToolTipText(
				"Destination file's last modified date is different than source file's last modified date");
		dsizeLabel.setFont(bfont);
		dsizeLabel.setBounds(180, 5, 150, 25);
		sizeNamePanel.add(dsizeLabel);

		ssizeLabel = new JLabel("Same file size");
		ssizeLabel.setToolTipText("Destination file's last modified date is same as source file's last modified date");
		ssizeLabel.setFont(bfont);
		ssizeLabel.setBounds(330, 5, 150, 25);
		sizeNamePanel.add(ssizeLabel);

		// ---------------------------------------------

		JPanel olderPanel = new JPanel(null);
		olderPanel.setBounds(0, 30, width, 40);
		strategyPanel.add(olderPanel);

		olderLabel = new JLabel("Replace older files", JLabel.RIGHT);
		olderLabel.setToolTipText("Replace only if the destination file is older");
		olderLabel.setFont(bfont);
		olderLabel.setBounds(50, 5, 120, 25);
		olderPanel.add(olderLabel);

		JPanel oPanel = new JPanel(null);
		oPanel.setBounds(180, 0, 320, 40);
		oPanel.setBorder(c1Alive || c2Alive ? BorderFactory.createLineBorder(SaferBorderColor)
				: BorderFactory.createLineBorder(deadBorderColor));
		olderPanel.add(oPanel);

		c1 = new TripleCheck(CopySettings.DIFFERENT_SIZE_OLDER_DATE);
		c1.setBounds(20, 5, 140, 30);
		oPanel.add(c1);

		c2 = new TripleCheck(CopySettings.SAME_SIZE_OLDER_DATE);
		c2.setBounds(170, 5, 140, 30);
		oPanel.add(c2);

		// ---------------------------------------------

		JPanel newerPanel = new JPanel(null);
		newerPanel.setBounds(0, 80, width, 50);
		strategyPanel.add(newerPanel);

		newerLabel = new JLabel("Replace latest files", JLabel.RIGHT);
		newerLabel.setToolTipText(
				"Revert destination file with an older version copy (I hope you are sure about doing this)");
		newerLabel.setFont(bfont);
		newerLabel.setBounds(50, 5, 120, 25);
		newerPanel.add(newerLabel);

		JPanel nPanel = new JPanel(null);
		nPanel.setBounds(180, 0, 320, 40);
		nPanel.setBorder(c3Alive || c4Alive ? BorderFactory.createLineBorder(SafeBorderColor)
				: BorderFactory.createLineBorder(deadBorderColor));
		newerPanel.add(nPanel);

		c3 = new TripleCheck(CopySettings.DIFFERENT_SIZE_LATEST_DATE);
		c3.setBounds(20, 5, 140, 30);
		nPanel.add(c3);

		c4 = new TripleCheck(CopySettings.SAME_SIZE_LATEST_DATE);
		c4.setBounds(170, 5, 140, 30);
		nPanel.add(c4);

		// ---------------------------------------------

		JPanel samePanel = new JPanel(null);
		samePanel.setBounds(0, 130, width, 50);
		strategyPanel.add(samePanel);

		sameLabel = new JLabel("Replace twins files", JLabel.RIGHT);
		sameLabel.setToolTipText("Replace same dated files");
		sameLabel.setFont(bfont);
		sameLabel.setBounds(50, 5, 120, 25);
		samePanel.add(sameLabel);

		JPanel sPanel = new JPanel(null);
		sPanel.setBounds(180, 0, 320, 40);
		sPanel.setBorder(c5Alive || c6Alive ? BorderFactory.createLineBorder(SafestBorderColor)
				: BorderFactory.createLineBorder(deadBorderColor));
		samePanel.add(sPanel);

		c5 = new TripleCheck(CopySettings.DIFFERENT_SIZE_SAME_DATE);
		c5.setBounds(20, 5, 140, 30);
		sPanel.add(c5);

		c6 = new TripleCheck(CopySettings.SAME_SIZE_SAME_DATE);
		c6.setBounds(170, 5, 140, 30);
		sPanel.add(c6);

		// ---------------------------------------------

		rename = new JCheckBox();
		rename.setText("Copy skipped files with a new name");
		rename.setFont(font);
		rename.setBounds(180, 185, 250, 25);
		strategyPanel.add(rename);

		// -------------------------

		setCheckStates(dstStrategyCombo.getSelectedItem().toString());

		setStrategyLabelStates();

		return panel;
	}

	private void setStrategyPanelEnabled(boolean enabled) {
		isStrategyPanelEnabled = enabled;

		c1.setDormant(!enabled);
		c6.setDormant(!enabled);

		if (enabled) {
			rename.setEnabled(false);
		}
	}

	private void setStrategyLabelStates() {

		Color deadColor = Color.LIGHT_GRAY;
		Color inactiveColor = Color.GRAY;
		Color activeColor = Color.DARK_GRAY;

		if ((c1.isDead() && c2.isDead()) || !isStrategyPanelEnabled) {
			olderLabel.setForeground(deadColor);
		} else if (c1.isEnabled() || c2.isEnabled()) {
			olderLabel.setForeground(activeColor);
		} else {
			olderLabel.setForeground(inactiveColor);
		}

		if ((c3.isDead() && c4.isDead()) || !isStrategyPanelEnabled) {
			newerLabel.setForeground(deadColor);
		} else if (c3.isEnabled() || c4.isEnabled()) {
			newerLabel.setForeground(activeColor);
		} else {
			newerLabel.setForeground(inactiveColor);
		}

		if ((c5.isDead() && c6.isDead()) || !isStrategyPanelEnabled) {
			sameLabel.setForeground(deadColor);
		} else if (c5.isEnabled() || c6.isEnabled()) {
			sameLabel.setForeground(activeColor);
		} else {
			sameLabel.setForeground(inactiveColor);
		}

		if ((c1.isDead() && c3.isDead() && c5.isDead()) || !isStrategyPanelEnabled) {
			dsizeLabel.setForeground(deadColor);
		} else if (c1.isEnabled() || c3.isEnabled() || c5.isEnabled()) {
			dsizeLabel.setForeground(activeColor);
		} else {
			dsizeLabel.setForeground(inactiveColor);
		}

		if ((c2.isDead() && c4.isDead() && c6.isDead()) || !isStrategyPanelEnabled) {
			ssizeLabel.setForeground(deadColor);
		} else if (c2.isEnabled() || c4.isEnabled() || c6.isEnabled()) {
			ssizeLabel.setForeground(activeColor);
		} else {
			ssizeLabel.setForeground(inactiveColor);
		}

	}

	private boolean isStrategyPanelEnabled() {
		return isStrategyPanelEnabled;
	}

	private void setCheckStates(String strategyName) {
		if (c1 == null || c2 == null || c3 == null || c4 == null || c5 == null || c6 == null) {
			return;
		}

		setStrategyPanelEnabled(true);
		c1.setEnabled(false);
		c2.setEnabled(false);
		c3.setEnabled(false);
		c4.setEnabled(false);
		c5.setEnabled(false);
		c6.setEnabled(false);
		rename.setEnabled(true);

		if (strategyName.equals(SKIP_ALL)) {
			c1.setSelected(false);
			c2.setSelected(false);
			c3.setSelected(false);
			c4.setSelected(false);
			c5.setSelected(false);
			c6.setSelected(false);
		} else if (strategyName.equals(OVERWRITE_ALL)) {
			c1.setSelected(true);
			c2.setSelected(true);
			c3.setSelected(true);
			c4.setSelected(true);
			c5.setSelected(true);
			c6.setSelected(true);
		} else if (strategyName.equals(OVERWRITE_DIFFERENT_SIZED)) {
			c1.setSelected(true);
			c2.setSelected(false);
			c3.setSelected(true);
			c4.setSelected(false);
			c5.setSelected(true);
			c6.setSelected(false);
		} else if (strategyName.equals(OVERWRITE_OLDER)) {
			c1.setSelected(true);
			c2.setSelected(true);
			c3.setSelected(false);
			c4.setSelected(false);
			c5.setSelected(false);
			c6.setSelected(false);
		} else if (strategyName.equals(OVERWRITE_DIFFERENT_DATED)) {
			c1.setSelected(true);
			c2.setSelected(true);
			c3.setSelected(true);
			c4.setSelected(true);
			c5.setSelected(false);
			c6.setSelected(false);
		} else if (strategyName.equals(SKIP_SIMILAR)) {
			c1.setSelected(true);
			c2.setSelected(true);
			c3.setSelected(true);
			c4.setSelected(true);
			c5.setSelected(true);
			c6.setSelected(false);
		} else if (strategyName.equals(OVERWRITE_CUSTOM)) {
			c1.setEnabled(true);
			c2.setEnabled(true);
			c3.setEnabled(true);
			c4.setEnabled(true);
			c5.setEnabled(true);
			c6.setEnabled(true);
		} else if (strategyName.equals(OVERWRITE_BY_ASKING_SEPARATELY)) {
			setStrategyPanelEnabled(false);

		}
		setCheckStatesBasedOnCount();
		setStrategyLabelStates();
	}

	private void setCheckStatesBasedOnCount() {
		if (CopySettings.overwriteTypeToFilesMap.get(CopySettings.DIFFERENT_SIZE_OLDER_DATE) == null) {
			c1.setEnabled(false);
		}
		if (CopySettings.overwriteTypeToFilesMap.get(CopySettings.SAME_SIZE_OLDER_DATE) == null) {
			c2.setEnabled(false);
		}
		if (CopySettings.overwriteTypeToFilesMap.get(CopySettings.DIFFERENT_SIZE_LATEST_DATE) == null) {
			c3.setEnabled(false);
		}
		if (CopySettings.overwriteTypeToFilesMap.get(CopySettings.SAME_SIZE_LATEST_DATE) == null) {
			c4.setEnabled(false);
		}
		if (CopySettings.overwriteTypeToFilesMap.get(CopySettings.DIFFERENT_SIZE_SAME_DATE) == null) {
			c5.setEnabled(false);
		}
		if (CopySettings.overwriteTypeToFilesMap.get(CopySettings.SAME_SIZE_SAME_DATE) == null) {
			c6.setEnabled(false);
		}
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

		if (isStrategyPanelEnabled()) {

			CopySettings.FILES_TO_OVERWRITE.clear();
			CopySettings.FILES_TO_OVERWRITE.addAll(c1.getDstsToOverwrite());
			CopySettings.FILES_TO_OVERWRITE.addAll(c2.getDstsToOverwrite());
			CopySettings.FILES_TO_OVERWRITE.addAll(c3.getDstsToOverwrite());
			CopySettings.FILES_TO_OVERWRITE.addAll(c4.getDstsToOverwrite());
			CopySettings.FILES_TO_OVERWRITE.addAll(c5.getDstsToOverwrite());
			CopySettings.FILES_TO_OVERWRITE.addAll(c6.getDstsToOverwrite());

			CopySettings.RENAME_SKIPPED_FILES = rename.isSelected();
		}

		CopySettings.ASK_FOR_EACH_FILE = !isStrategyPanelEnabled();

		dialog.dispose();

	}

}
