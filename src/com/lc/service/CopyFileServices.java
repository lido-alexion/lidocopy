package com.lc.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.lc.common.CopySettings;
import com.lc.common.Settings;
import com.lc.core.DstExistence;
import com.lc.core.Message;

class CopyFileServices {

	public static boolean isSameFile(File srcFile, File dstFile) {
		if (!srcFile.exists() || !dstFile.exists()) {
			return false;
		}
		long sizeDiff = getFileSize(srcFile) - getFileSize(dstFile);
		long dateDiff = getFileDate(srcFile) - getFileDate(dstFile);
		if (sizeDiff == 0 && dateDiff == 0) {
			String hash1 = getFileHash(srcFile);
			String hash2 = getFileHash(dstFile);
			if (hash1 == null) {
				Logger.warn(srcFile.getName() + " : file hash could not be calculated for source file.");
				return false;
			} else {
				Logger.debug("File hash (Source: " + srcFile.getName() + ") :: " + hash1);
			}
			if (hash2 == null) {
				Logger.warn(dstFile.getName() + " : file hash could not be calculated for destination file.");
				return false;
			} else {
				Logger.debug("File hash (Destination: " + srcFile.getName() + ") :: " + hash2);
			}
			return hash1.equals(hash2);
		}
		return false;
	}

	/**
	 * True if first file is latest
	 * 
	 * @param file
	 * @param compareFile
	 * @return
	 */
	protected static boolean isLatestFile(File file, File compareFile) {
		return getFileDate(file) > getFileDate(compareFile);
	}

	protected static void updateDstConflicts() {
		CopySettings.overwriteTypeToFilesMap.clear();
		Set<String> dsts = DstExistence.dstPathMap.keySet();
		for (String dst : dsts) {
			DstExistence dstExistence = DstExistence.dstPathMap.get(dst);
			if (dstExistence.getPreferredSrc() != null) {
				updateConflictCount(new File(dstExistence.getPreferredSrc()), new File(dstExistence.getDstPath()));
			}
		}
	}

	protected static void updateConflictCount(File srcFile, File dstFile) {
		long dateDiff = getFileDate(srcFile) - getFileDate(dstFile); // +ve if src is latest, -ve if dest is latest,
		// 0 if same dated
		boolean isSameSize = getFileSize(srcFile) == getFileSize(dstFile);

		String srcPath = srcFile.getAbsolutePath();
		String dstPath = dstFile.getAbsolutePath();

		if (!isSameSize) {
			if (dateDiff > 0) {
				updateCount(CopySettings.DIFFERENT_SIZE_OLDER_DATE, srcPath, dstPath);
			} else if (dateDiff == 0) {
				updateCount(CopySettings.DIFFERENT_SIZE_SAME_DATE, srcPath, dstPath);
			} else {
				updateCount(CopySettings.DIFFERENT_SIZE_LATEST_DATE, srcPath, dstPath);
			}
		} else {
			if (dateDiff > 0) {
				updateCount(CopySettings.SAME_SIZE_OLDER_DATE, srcPath, dstPath);
			} else if (dateDiff == 0) {
				if (!CopySettings.SKIP_SAME_FILE_IF_EXISTS || !isSameFile(srcFile, dstFile)) {
					// only if user has chosen not to skip same files and it's not same file, even
					// though same date and size
					updateCount(CopySettings.SAME_SIZE_SAME_DATE, srcPath, dstPath);
				}
			} else {
				updateCount(CopySettings.SAME_SIZE_LATEST_DATE, srcPath, dstPath);
			}
		}
	}

	private static void updateCount(String type, String srcPath, String dstPath) {
		List<DstExistence> dstExistenceList = CopySettings.overwriteTypeToFilesMap.get(type);
		if (dstExistenceList == null) {
			dstExistenceList = new ArrayList<DstExistence>();
		}
		DstExistence dstExistence = null;
		if (DstExistence.isDstExists(dstPath)) {
			dstExistence = DstExistence.getDst(dstPath);
			dstExistenceList.remove(dstExistence);
			dstExistence.addSource(srcPath);
			dstExistenceList.add(dstExistence);
		} else {
			dstExistence = new DstExistence(dstPath, srcPath);
			dstExistenceList.add(dstExistence);
		}

		CopySettings.overwriteTypeToFilesMap.put(type, dstExistenceList);
	}

	protected static boolean doOverwrite(File srcFile, File dstFile) {

		return CopySettings.FILES_TO_OVERWRITE.contains(dstFile.getAbsolutePath());
	}

	protected static boolean deleteEmptyFolder(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files == null || files.length == 0) {
				try {
					Logger.info(file.getName() + " : Trying to delete empty directory in destination.");
					return file.delete();
				} catch (Exception e) {
					return false;
				}
			}
		}
		return false;
	}

	protected static void rollbackCopiedFile(File fileToDelete, String userRefFileName) {
		boolean deleted = false;
		String reason = null;
		try {
			Files.delete(Paths.get(fileToDelete.getAbsolutePath()));
			if (!fileToDelete.exists()) {
				deleted = true;
			}
		} catch (Exception e) {
			reason = e.getMessage();
		}
		if (deleted) {
			Logger.warn("Rollbacked file (delete file) : " + fileToDelete.getAbsolutePath());
		} else {
			Logger.error("Failed to rollback (delete file) : " + fileToDelete.getAbsolutePath());
			notify(userRefFileName, Message.TYPE.WARNING,
					"Failed to delete temporary file created in destination folder.", reason,
					"Delete the file manually : " + fileToDelete.getName(), fileToDelete);
		}
	}

	protected static File getRenamedFile(File srcFile, File file) {
		return getRenamedFile(srcFile, file, 1);
	}

	private static File getRenamedFile(File srcFile, File file, int renameIndex) {
		String parentPath = file.getParent();
		String nameWIthExtn = file.getName();
		if (nameWIthExtn.lastIndexOf(".") > -1) {
			String extn = nameWIthExtn.substring(nameWIthExtn.lastIndexOf("."));
			String name = nameWIthExtn.substring(0, nameWIthExtn.lastIndexOf("."));
			nameWIthExtn = name + "_" + renameIndex + extn;
		} else {
			nameWIthExtn = nameWIthExtn + "_" + renameIndex;
		}

		File rnmFile = new File(parentPath + File.separator + nameWIthExtn);
		if (rnmFile.exists()) {
			renameIndex++;
			int limit = 10;
			if (renameIndex > limit) {
				Logger.error("Failed to obtain a renamed filename for : " + file.getName());
				notify(file, Message.TYPE.ERROR, "Failed to copy the file with a different name.",
						"Could not find a different filename.", "Try to rename the file first and then copy.", srcFile);
				return null;
			} else {
				return getRenamedFile(srcFile, file, renameIndex);
			}
		} else {
			return rnmFile;
		}
	}

	protected static void notify(File file, Message.TYPE type, String message, String reason, String action,
			File actionFile) {
		String fileName = file.getName();
		notify(fileName, type, message, reason, action, actionFile);
	}

	protected static void notify(String fileName, Message.TYPE type, String message, String reason, String action,
			File actionFile) {
		notify(new Message(fileName, type, message, reason, action, actionFile));
	}

	protected static void notify(Message message) {
		CopySettings.copyNotifications.add(message);
	}

	protected static String getFileHash(File file) {

		if (!Settings.USE_HASH_FOR_FILE_IDENTITY || file.length() > Settings.FILE_SIZE_LIMIT_FOR_HASH_CALC) {
			return "#";
		}
		String fileName = file.getName();
		String fileExtn = fileName.lastIndexOf(".") > -1 ? fileName.substring(fileName.lastIndexOf(".")) : "";
		if (!Settings.HASH_CHECK_ALLOWED_FILE_TYPES.contains(fileExtn)) {
			return "#";
		}

		String path = file.getAbsolutePath();
		String checksum = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(path);
			MessageDigest md = MessageDigest.getInstance("MD5");

			byte[] buffer = new byte[8192];
			int numOfBytesRead;
			while ((numOfBytesRead = fis.read(buffer)) > 0) {
				md.update(buffer, 0, numOfBytesRead);
			}
			byte[] hash = md.digest();
			checksum = new BigInteger(1, hash).toString(16);
			fis.close();
			fis = null;
		} catch (Exception ex) {
			if (fis != null) {
				try {
					fis.close();
					fis = null;
				} catch (IOException e) {
				}
			}
			Logger.error("Exception in finding checksum (" + file.getName() + "): " + ex.getMessage());
		} finally {
			if (fis != null) {
				try {
					fis.close();
					fis = null;
				} catch (IOException e) {
				}
			}
		}
		if (fis != null) {
			Logger.debug(
					"Could not close filestream : there may be additional problems in copying. Let's try and see.");
		}
		return checksum;
	}

	public static long getFileSize(File file) {
		return file.isDirectory() ? 0 : file.length();
	}

	public static long getFileDate(File file) {
		return file.lastModified();
	}

	protected static String getFileDriveLetter(File file) {
		return file.getAbsolutePath().substring(0, 1).toUpperCase();
	}

}
