package com.lc.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.lc.common.AppDetails;

public class VersionPanel {

	private JPanel panel;
	private JLabel head;

	public VersionPanel() {

		panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		panel.setLayout(new BorderLayout());
		build();
	}

	private void build() {

		head = new JLabel(AppDetails.appName + " " + AppDetails.appVersion, JLabel.CENTER);
		head.setBounds(0, 13, LCUiCreator.width - 25, 25);
		head.setOpaque(false);
		head.setBackground(Color.WHITE);
		head.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

		JPanel innerPanel = new JPanel();
		innerPanel.setBackground(new Color(240, 240, 240));
		innerPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		innerPanel.setPreferredSize(new Dimension(LCUiCreator.width - 25, 40));
		innerPanel.add(head);

		panel.add(innerPanel);
	}

	public JPanel getPanel() {
		panel.setBackground(Color.LIGHT_GRAY);
		return panel;
	}
}
