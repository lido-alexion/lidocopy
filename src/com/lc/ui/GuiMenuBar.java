package com.lc.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Event;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.plaf.MenuBarUI;
import javax.swing.plaf.metal.MetalMenuBarUI;

import com.lc.service.LCServiceProvider;

public class GuiMenuBar {
    private static JMenuBar jJMenuBar = null;
    private static JMenu fileMenu = null;
    private static JMenuItem quitMenuItem = null;
    private static JMenuItem hideMenuItem = null;
    private static JMenuItem settingsMenuItem = null;
    private static JMenuItem logsMenuItem = null;
    private static Font bigfont = new Font("Arial", Font.PLAIN, 15);
    private static Font font = new Font("Arial", Font.PLAIN, 14);
    private static Insets bigInsets = new Insets(4, 10, 4, 10);
    private static Insets insets = new Insets(2, 3, 2, 3);
    
    
    public JMenuBar getJJMenuBar() {
        if (jJMenuBar == null) {
            jJMenuBar = new JMenuBar();
            MenuBarUI ui = (MenuBarUI) MetalMenuBarUI.createUI(jJMenuBar);
			jJMenuBar.setUI(ui );
            jJMenuBar.setBackground(new Color(230, 230, 230));
            jJMenuBar.setOpaque(true);
            jJMenuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
            jJMenuBar.add(getFileMenu());
        }
        return jJMenuBar;
    }

    public static JMenu getFileMenu() {
        fileMenu = new JMenu();
        fileMenu.setCursor(new Cursor(12));
        fileMenu.setBackground(Color.WHITE);
        fileMenu.setForeground(Color.BLACK);
        fileMenu.setFont(bigfont);
        fileMenu.setMargin(bigInsets);
        fileMenu.setText("Options");
        fileMenu.add(getHideMenuItem());
        fileMenu.add(getSettingsMenuItem());
        fileMenu.add(getLogsMenuItem());
        fileMenu.add(getQuitMenuItem());
        fileMenu.setMnemonic(KeyEvent.VK_O);

        return fileMenu;
    }

    private static JMenuItem getQuitMenuItem() {
        if (quitMenuItem == null) {
            quitMenuItem = new JMenuItem();
            quitMenuItem.setFont(font);
            quitMenuItem.setMargin(insets);
            quitMenuItem.setText("Exit");
            quitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Event.ALT_MASK, true));
            quitMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	LCServiceProvider.closeApplication();
                }
            });
        }
        return quitMenuItem;
    }

    

    private static JMenuItem getHideMenuItem() {
        if (hideMenuItem == null) {
            hideMenuItem = new JMenuItem();
            hideMenuItem.setFont(font);
            hideMenuItem.setMargin(insets);
            hideMenuItem.setText("Minimize");
            hideMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, Event.ALT_MASK, true));
            hideMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	LCServiceProvider.hideAppGui();
                }
            });
        }
        return hideMenuItem;
    }

    private static JMenuItem getSettingsMenuItem() {
        if (settingsMenuItem == null) {
            settingsMenuItem = new JMenuItem();
            settingsMenuItem.setFont(font);
            settingsMenuItem.setMargin(insets);
            settingsMenuItem.setText("Preferences");
            settingsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.ALT_MASK, true));
            settingsMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new SettingsDialog();
                }
            });
        }
        return settingsMenuItem;
    }
    
    private static JMenuItem getLogsMenuItem() {
        if (logsMenuItem == null) {
        	logsMenuItem = new JMenuItem();
        	logsMenuItem.setFont(font);
        	logsMenuItem.setMargin(insets);
        	logsMenuItem.setText("Logs");
        	logsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.ALT_MASK, true));
        	logsMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new LoggerPanelDlg();
                }
            });
        }
        return logsMenuItem;
    }

}
