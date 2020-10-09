package com.lc.common;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class AppMessageService {

	private static JFrame jFrame = null;

	public static void setJFrame(JFrame jframe) {
		jFrame = jframe;
	}

	public static void showMessage(String msg, String header, int msgType) {
		JOptionPane.showMessageDialog(jFrame, msg, header, msgType);
	}

	public static void showInfoMessage(String msg, String header) {
		JOptionPane.showMessageDialog(jFrame, msg, header,
				JOptionPane.INFORMATION_MESSAGE);
	}

	public static void showError(String msg) {
		JOptionPane.showMessageDialog(jFrame, msg, "Error",
				JOptionPane.ERROR_MESSAGE);
	}

	public static void showErrorMessage(String msg, String header) {
		JOptionPane.showMessageDialog(jFrame, msg, header,
				JOptionPane.ERROR_MESSAGE);
	}

	public static void showMessage(String msg, String header) {
		JOptionPane.showMessageDialog(jFrame, msg, header,
				JOptionPane.PLAIN_MESSAGE);
	}

	public static boolean showQMessage(String msg, String header) {
		if (JOptionPane.showConfirmDialog(jFrame, msg, header,
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
			return true;
		return false;
	}

	public static boolean showConfirmMessage(String msg, String header) {
		if (JOptionPane.showConfirmDialog(jFrame, msg, header,
				JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
			return true;
		return false;
	}
	
	public static String showInputMessage(String msg, String header) {
		return JOptionPane.showInputDialog(jFrame, msg, header,
				JOptionPane.PLAIN_MESSAGE);
	}

}
