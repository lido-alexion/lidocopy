package com.lc.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.lc.common.Util;
import com.lc.service.Logger;

public class LoggerPanelDlg {

	private JDialog dialog;
	private JButton okButton = null;
	private Font font;
	private Font lfont;
	int y = 0;
	int width = 800;
	int height = 500;
	JTextPane logField;
	JScrollPane pane;
	static LoggerPanelDlg logPanelDlg;
	SimpleAttributeSet errorAttributeSet;
	SimpleAttributeSet warnAttributeSet;
	SimpleAttributeSet infoAttributeSet;
	SimpleAttributeSet debugAttributeSet;
	SimpleAttributeSet logAttributeSet;
	int logFilterLevel = 0;
	boolean isUpdatePaused;

	public LoggerPanelDlg() {
		logPanelDlg = this;
		
		LCUiCreator.cancelExitTimer();
		
		errorAttributeSet = new SimpleAttributeSet();  
        StyleConstants.setForeground(errorAttributeSet, Color.red);
        
        warnAttributeSet = new SimpleAttributeSet();  
        StyleConstants.setForeground(warnAttributeSet, Color.magenta);
        
        infoAttributeSet = new SimpleAttributeSet();  
        StyleConstants.setForeground(infoAttributeSet, Color.blue);
        
        debugAttributeSet = new SimpleAttributeSet();  
        StyleConstants.setForeground(debugAttributeSet, Color.black);
        
        logAttributeSet = new SimpleAttributeSet();  
        StyleConstants.setForeground(logAttributeSet, Color.gray);
		
		font = new Font("Arial", Font.PLAIN, 13);
		lfont = new Font("Consolas", Font.PLAIN, 13);
		displayDlg();
	}

	public void displayDlg() {
		if (null == dialog) {
			String title = "Logs";
			dialog = new JDialog(LCUiCreator.getUiFrame(), title, Dialog.ModalityType.MODELESS);
			Dimension dimension = new Dimension(width, height);
			dialog.pack();
			dialog.setLocationRelativeTo(null);
			dialog.setResizable(true);

			Rectangle bounds = new Rectangle(0, 0, width, height);
			Dimension screensz = Toolkit.getDefaultToolkit().getScreenSize();
			bounds.x = Math.max(0, (screensz.width - width) / 2);
			bounds.y = Math.max(0, (screensz.height - height) / 2);
			bounds.width = Math.min(bounds.width, screensz.width);
			bounds.height = Math.min(bounds.height, screensz.height);
			dialog.setBounds(bounds);
			dialog.setIconImage(Util.getAppIconImage());
//			dialog.setLayout(null);
			
			logField = new JTextPane();
			logField.setAutoscrolls(true);
//			logField.setDisabledTextColor(Color.BLUE);
//			logField.setLineWrap(true);
//			logField.setWrapStyleWord(true);
			logField.setFont(lfont);
			logField.setMargin(new Insets(5, 5, 5, 5));
			logField.setEditable(false);
			setLogText(null);
//			int pos = logField.getDocument().getLength() > 0 ? logField.getDocument().getLength() - 1 : 0;
//			logField.setCaretPosition(pos);
//			logField.setEnabled(false);

			pane = new JScrollPane(logField);
			pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//			pane.setBounds(20, 20, width - 50, height - 120);
			pane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20), BorderFactory.createEtchedBorder()));

			dialog.add(pane, BorderLayout.CENTER);

			addButtonPanel();

			dialog.setPreferredSize(dimension);
			dialog.setMaximumSize(dimension);
			dialog.setMinimumSize(dimension);
			dialog.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					logPanelDlg = null;
					e.getWindow().dispose();
				}
			});

			dialog.setVisible(true);

		}
	}

	private void addButtonPanel() {
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setPreferredSize(new Dimension(width, 50));
		dialog.add(buttonPane, BorderLayout.PAGE_END);
		
		JPanel leftPane = new JPanel(null);
		Dimension dl = new Dimension(320, 50);
		leftPane.setPreferredSize(dl);
		leftPane.setMaximumSize(dl);
		leftPane.setMinimumSize(dl);
		buttonPane.add(leftPane);
		buttonPane.add(Box.createHorizontalGlue());
		
		JLabel filterLabel = new JLabel("Filter logs:");
		filterLabel.setFont(font);
		filterLabel.setBounds(20, 10, 70, 25);
		leftPane.add(filterLabel);		
		
		JComboBox<String> logLevelCombo = new JComboBox<String>();
		logLevelCombo.addItem("DEBUG");
		logLevelCombo.addItem("INFO");
		logLevelCombo.addItem("WARN");
		logLevelCombo.addItem("ERROR");
		
		logLevelCombo.setBounds(90, 10, 90, 25);
		logLevelCombo.setFont(font);
		logLevelCombo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				logFilterLevel = logLevelCombo.getSelectedIndex();
				Document doc = logField.getStyledDocument();
				try {
					doc.remove(0, doc.getLength());
				} catch (BadLocationException e1) {
				}
				setLogText(null);
			}
		});
		leftPane.add(logLevelCombo);
		
		JCheckBox pauseCheck = new JCheckBox("Pause updating");
		pauseCheck.setBounds(190, 10, 120, 25);
		pauseCheck.setFont(font);
		pauseCheck.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(pauseCheck.isSelected()) {
					isUpdatePaused = true;
				}else {
					isUpdatePaused = false;
					setLog(null);
				}
				
			}});
		leftPane.add(pauseCheck);
		
		
		JPanel rightPane = new JPanel(null);
		Dimension d = new Dimension(360, 50);
		rightPane.setPreferredSize(d);
		rightPane.setMaximumSize(d);
		rightPane.setMinimumSize(d);
		buttonPane.add(rightPane);
		
		JButton copyButton = new JButton();
		copyButton.setFont(font);
		copyButton.setBounds(new Rectangle(0, 10, 120, 25));
		copyButton.setText("Copy contents");
		copyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(new StringSelection(logField.getText()), null);
			}
		});
		rightPane.add(copyButton);

		JButton flushButton = new JButton();
		flushButton.setFont(font);
		flushButton.setBounds(new Rectangle(140, 10, 90, 25));
		flushButton.setText("Clear");
		flushButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Logger.clearLogs();
				Document doc = logField.getStyledDocument();
				try {
					doc.remove(0, doc.getLength());
				} catch (BadLocationException e1) {
				}
			}
		});
		rightPane.add(flushButton);

		
		okButton = new JButton();
			okButton.setFont(font);
			okButton.setBounds(new Rectangle(250, 10, 90, 25));
			okButton.setText("Close");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					logPanelDlg = null;
					dialog.dispose();
				}
			});
		
			rightPane.add(okButton);
	}

	private void setLog(String newLogs) {
		if (logField != null && !isUpdatePaused) {
			
			setLogText(newLogs);
//			logField.setText(Logger.buffer.toString());
			int pos = logField.getDocument().getLength() > 0 ? logField.getDocument().getLength() - 1 : 0;
			logField.setCaretPosition(pos);	
		}
	}
	
	private void setLogText(String newLogs) {
		if(newLogs == null) {
			newLogs = Logger.readFullBuffer();
			
			Document doc = logField.getStyledDocument();
			try {
				doc.remove(0, doc.getLength());
			} catch (BadLocationException e1) {
			}
			
		}
		String[] lines = newLogs.split("\n");
		Document doc = logField.getStyledDocument();  
        
		SimpleAttributeSet set = null;
		for(String line:lines) {
			if(line.startsWith("ERROR")) {
				set = errorAttributeSet;
			}else if(line.startsWith("WARN")){
				if(logFilterLevel > 2) {
					continue;
				}	
				set = warnAttributeSet;
			}else if(line.startsWith("INFO")){
				if(logFilterLevel > 1) {
					continue;
				}
				set = infoAttributeSet;
			}else if(line.startsWith("DEBUG")){
				if(logFilterLevel > 0) {
					continue;
				}
				set = debugAttributeSet;
			}else {
				if(logFilterLevel > 0) {
					continue;
				}
				set = logAttributeSet;
			}		
			
			try {
				doc.insertString(doc.getLength(), line+"\n", set);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		
	}

	public static void updateLog(String newLogs) {
		if (logPanelDlg != null) {
			logPanelDlg.setLog(newLogs);
		}
	}

}
