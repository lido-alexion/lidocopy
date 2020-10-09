package com.lc.ui;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Font;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ImageIcon;

import com.lc.common.AppDetails;
import com.lc.common.AppMessageService;
import com.lc.common.CopySettings;
import com.lc.common.Settings;
import com.lc.common.StartOnLogon;
import com.lc.common.Util;
import com.lc.service.LCServiceProvider;

public class SystemTrayPanel {

    private static TrayIcon trayIcon;
    private static SystemTray tray;

    public SystemTrayPanel() {
        createAndShowGUI();
        if (Settings.LOAD_ON_START) {
            LCUiCreator.startApplication();
        }
    }

    private static void createAndShowGUI() {
        // Check the SystemTray support
        if (!SystemTray.isSupported()) {
            LCUiCreator.startApplication();
            AppMessageService.showMessage("The application could not be loaded in System tray.",
                    "Not Supported");
            return;
        }

        final PopupMenu popup = new PopupMenu();
        trayIcon = new TrayIcon(Util.getAppIconImage());
        tray = SystemTray.getSystemTray();

        // Create a popup menu components
        MenuItem lisaPadItem = new MenuItem(AppDetails.appName);
        lisaPadItem.setFont(new Font("Arial", Font.BOLD, 12));
        CheckboxMenuItem cb1 = new CheckboxMenuItem("Start automatically on login");
        cb1.setFont(new Font("Arial", Font.PLAIN, 12));
        CheckboxMenuItem cb2 = new CheckboxMenuItem("Load interface when started");
        cb2.setFont(new Font("Arial", Font.PLAIN, 12));
        CheckboxMenuItem cb3 = new CheckboxMenuItem("Mute sound");
        cb3.setFont(new Font("Arial", Font.PLAIN, 12));
        CheckboxMenuItem cb5 = new CheckboxMenuItem("Confirm before closing Application");
        cb5.setFont(new Font("Arial", Font.PLAIN, 12));
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setFont(new Font("Arial", Font.PLAIN, 12));
        
        if (StartOnLogon.isStartOnLogonEnabled()) {
            cb1.setState(true);
        }
        cb2.setState(Settings.LOAD_ON_START);
        cb3.setState(Settings.SOUND_MUTED);
        cb5.setState(Settings.CONFIRM_ON_CLOSE);

        cb1.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                int cb1Id = e.getStateChange();
                if (cb1Id == ItemEvent.SELECTED) {
                    StartOnLogon.setStartOnLogon();
                } else {
                    StartOnLogon.setDontStartOnLogon();
                }
            }
        });

        cb2.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                int cb1Id = e.getStateChange();
                if (cb1Id == ItemEvent.SELECTED) {
                    Settings.LOAD_ON_START = true;
                } else {
                    Settings.LOAD_ON_START = false;
                }
                LCServiceProvider.saveSettings();
            }
        });

        cb3.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                int cb1Id = e.getStateChange();
                if (cb1Id == ItemEvent.SELECTED) {
                    Settings.SOUND_MUTED = true;
                } else {
                    Settings.SOUND_MUTED = false;
                }
                LCServiceProvider.saveSettings();
            }
        });

        cb5.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                int cb1Id = e.getStateChange();
                if (cb1Id == ItemEvent.SELECTED) {
                    Settings.CONFIRM_ON_CLOSE = true;
                } else {
                    Settings.CONFIRM_ON_CLOSE = false;
                }
                LCServiceProvider.saveSettings();
            }
        });

        // Add components to popup menu
        popup.add(lisaPadItem);
        popup.addSeparator();
        popup.add(cb1);
        popup.add(cb2);
        popup.add(cb3);
        popup.add(cb5);
        popup.addSeparator();
        popup.add(exitItem);
        
        trayIcon.setPopupMenu(popup);
        trayIcon.setImageAutoSize(true);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            LCUiCreator.startApplication();
            return;
        }

        trayIcon.displayMessage(AppDetails.appName+" "+AppDetails.appVersion, AppDetails.appName+" is running.",
                TrayIcon.MessageType.NONE);

        trayIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LCUiCreator.startApplication();
            }
        });

        lisaPadItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LCUiCreator.startApplication();
            }
        });

        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (LCServiceProvider.closeApplication()) {
                    trayIcon.setPopupMenu(null);
                    trayIcon.setToolTip("<html>"+AppDetails.appName+" is closing.</html>");
                    trayIcon.setImage(new ImageIcon(Util.getImageDirPath() + "closingIcon.png")
                            .getImage());
                }
            }
        });
        
        if(CopySettings.isPasteMode()) {
        	LCUiCreator.startApplication();
        }
        
    }

    public static void removeSystemTray() {
        tray.remove(trayIcon);
    }

    public static void showUserConfirmationTrayMsg() {
        trayIcon.displayMessage("User Intervention Needed",
                "Confirm changes for folder synchronization.", TrayIcon.MessageType.WARNING);
    }

}
