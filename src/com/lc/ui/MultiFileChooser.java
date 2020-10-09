package com.lc.ui;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class MultiFileChooser {

	public static File lastFile;

	public static File[] browse() {

		File[] files = null;
		try {
			JFileChooser chooser = new JFileChooser() {
				private static final long serialVersionUID = 1L;

				@Override
				protected JDialog createDialog(Component parent) throws HeadlessException {
					JDialog dialog = super.createDialog(LCUiCreator.getUiFrame());
					return dialog;
				}
			};

			chooser.setDialogTitle("Select files");
			chooser.setFileFilter(new FileFilter() {

				public boolean accept(File f) {
					return true;
				}

				public String getDescription() {
					return "All files";
				}
			});

			if (lastFile != null) {
				chooser.setSelectedFile(lastFile);
			}

			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setMultiSelectionEnabled(true);

			int select = chooser.showDialog(null, "Select");

			if (select == JFileChooser.APPROVE_OPTION) {
				files = chooser.getSelectedFiles();
				if (files != null && files.length > 0) {
					lastFile = files[0];
				}
			}
		} catch (Exception e) {

		}

		return files;
	}
}
