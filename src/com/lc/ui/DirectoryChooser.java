package com.lc.ui;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class DirectoryChooser {

	public static File browse(String currentPath, boolean onlyDirectory) {

		File currentSelectedFile = null;
		if (currentPath != null && currentPath.trim().length() > 0) {
			currentSelectedFile = new File(currentPath);
		}
		File file = null;
		try {			
			JFileChooser chooser = new JFileChooser(){
			    private static final long serialVersionUID = 1L;
				@Override
			    protected JDialog createDialog( Component parent ) throws HeadlessException {
			        JDialog dialog = super.createDialog(LCUiCreator.getUiFrame());
			        return dialog;
			    }
			};
			
			chooser.setDialogTitle("Select Folder");
			
			
			chooser.setFileFilter(new FileFilter() {

				public boolean accept(File f) {
					return true;
				}

				public String getDescription() {
					return "Folders only";
				}
			});

			if (currentSelectedFile != null) {
			    if(onlyDirectory){
			        chooser.setCurrentDirectory(currentSelectedFile.getParentFile());
			    }else{
			        chooser.setSelectedFile(currentSelectedFile);
			    }
			}

			if(onlyDirectory){
			    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			}else{
			    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			}
			chooser.setAcceptAllFileFilterUsed(false);

			int select = chooser.showDialog(null, "Select");

			if (select == JFileChooser.APPROVE_OPTION) {
				file = chooser.getSelectedFile();
			} else {
				file = null;
			}
		} catch (Exception e) {

		}

		return file;
	}
}
