package com.lc.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.lc.common.CopySettings;
import com.lc.common.CopySettings.MultiSourceCopyActionEnum;
import com.lc.common.DBUMessage;
import com.lc.common.Settings;
import com.lc.common.Util;
import com.lc.core.CopyFileUnit;
import com.lc.core.DstExistence;
import com.lc.core.Message;
import com.lc.ui.LCUiCreator;
import com.lc.ui.OverwriteStrategyDlg;
import com.lc.ui.OverwriteWarningDlg;
import com.lc.ui.SrcCopyStrategyDlg;

public class CopyServices extends CopyFileServices {

	private static int copied = 0;
	private static long copiedBytes = 0;
	private static long actuallyTransferredBytes = 0;
	private static long totalBytes = 0;
	private static int totalFiles = 0;
	private static long actuallyConsumedTime = 0;

	public static List<String> skippedFilesList = new ArrayList<String>(); // unique count, not including same hash
																			// files count
	public static List<String> sameFilesSkippedList = new ArrayList<String>();

	public static List<String> srcDstSameFilesSkippedList = new ArrayList<String>();

	public static List<String> copiedFilesList = new ArrayList<String>(); // including overwritten, renamed,
																			// renamedAfterFailed

	public static List<String> copiedAndReplacedFilesList = new ArrayList<String>();

	public static List<String> overwrittenFilesList = new ArrayList<String>();
	public static List<String> renamedFilesList = new ArrayList<String>(); // unique count, not including
																			// renamedAfterFailed count
	public static List<String> renamedAfterFailedFilesList = new ArrayList<String>();

	public static List<String> failedFilesList = new ArrayList<String>();

	public static List<String> processedFilesList = new ArrayList<String>();

	private static HashMap<String, String> reverseFilesCopyMap = new HashMap<String, String>(); // dst path to source
																								// path map

	public static HashMap<String, ArrayList<String>> dstToMultiSrcMap = new HashMap<String, ArrayList<String>>();

	public static List<CopyFileUnit> copyFileUnits = new ArrayList<CopyFileUnit>();

	public static void copyFiles(List<File> srcFiles, File dstFolder) {

		// srcToDstFilesMap.clear();
		skippedFilesList.clear();
		overwrittenFilesList.clear();
		copiedFilesList.clear();
		copiedAndReplacedFilesList.clear();
		sameFilesSkippedList.clear();
		srcDstSameFilesSkippedList.clear();
		renamedFilesList.clear();
		failedFilesList.clear();
		renamedAfterFailedFilesList.clear();
		dstToMultiSrcMap.clear();
		reverseFilesCopyMap.clear();
		processedFilesList.clear();
		copyFileUnits.clear();
		CopySettings.FILES_TO_OVERWRITE.clear();

		prepareFiles(srcFiles, dstFolder, null);
		prepareCopy(dstFolder);
		if (CopySettings.status == CopySettings.State.CANCELING
				|| CopySettings.status == CopySettings.State.CANCELLED) {
			CopySettings.status = CopySettings.State.CANCELLED;
			postCopyComplete();
			return;
		}
		resumeCopyFiles();
	}

	public static void resumeCopyFiles() {
		if (initCopy()) {
			CopySettings.status = CopySettings.State.COMPLETE;
			postCopyComplete();
		} else if (CopySettings.status == CopySettings.State.CANCELLED) {
			postCopyComplete();
		}
	}

	private static void postCopyComplete() {
		LCUiCreator.onAllCopyComplete();
		System.gc();
	}

	/**
	 * 
	 * @param srcFiles
	 * @param dstFolder
	 * @param relativePath
	 *            in format "/folder"
	 */
	private static void prepareFiles(List<File> srcFiles, File dstFolder, String relativePath) {
		if (relativePath == null) {
			relativePath = "";
		}

		for (int i = 0; i < srcFiles.size(); i++) {
			File srcFile = srcFiles.get(i);
			if (srcFile.exists() && srcFile.isFile()) {
				String name = srcFile.getName();
				String destPath = dstFolder.getAbsolutePath() + relativePath + File.separator + name;
				// srcToDstFilesMap.put(srcFile.getAbsolutePath(), destPath);

				copyFileUnits.add(new CopyFileUnit(srcFile.getAbsolutePath(), destPath, relativePath));

				ArrayList<String> srcs = dstToMultiSrcMap.get(destPath);
				if (srcs == null) {
					srcs = new ArrayList<String>();
				}
				srcs.add(srcFile.getAbsolutePath());
				dstToMultiSrcMap.put(destPath, srcs);

			}
		}

		for (int i = 0; i < srcFiles.size(); i++) {
			File srcFile = srcFiles.get(i);
			if (srcFile.isDirectory()) {
				File[] files = srcFile.listFiles();
				if (files != null && files.length > 0) {
					String newRelativePath = relativePath + File.separator + srcFile.getName();
					prepareFiles(Arrays.asList(files), dstFolder, newRelativePath);
				}
			}
		}

		Set<String> keys = dstToMultiSrcMap.keySet();
		if (keys != null && keys.size() > 0) {
			List<String> singleSrcKeysList = new ArrayList<String>();
			for (String key : keys) {
				ArrayList<String> srcs = dstToMultiSrcMap.get(key);
				if (srcs.size() == 1) {
					singleSrcKeysList.add(key);
				}
			}
			// remove entries with non-conflicting sources
			if (singleSrcKeysList.size() > 0) {
				for (String key : singleSrcKeysList) {
					dstToMultiSrcMap.remove(key);
				}
			}
		}
	}

	private static void prepareCopy(File dstFolder) {

		totalBytes = 0;
		copiedBytes = 0;
		actuallyTransferredBytes = 0;
		copied = 0;
		actuallyConsumedTime = 0;
		totalFiles = copyFileUnits.size();

		CopySettings.ASK_FOR_EACH_FILE = true;
		CopySettings.copyNotifications.clear();
		CopySettings.overwriteTypeToFilesMap.clear();

		for (CopyFileUnit copyUnit : copyFileUnits) {
			String srcPath = copyUnit.srcPath;
			File srcFile = new File(srcPath);

			totalBytes += getFileSize(srcFile);

			String destPath = copyUnit.dstPath;
			File dstFile = new File(destPath);
			if (dstFile.exists() && !srcPath.equals(destPath)) {
				updateConflictCount(srcFile, dstFile);
			}
		}

		if (Settings.CHECK_AND_WARN_DISK_SPACE_BEFORE_COPY_START && totalBytes > dstFolder.getFreeSpace()) {
			if (!DBUMessage.showQMessage(
					"\nThere is not sufficient space to copy all the files ("
							+ Util.getFormatedStringForByte(totalBytes) + ").\n\nDo you want to ignore and copy?\n\n",
					"Free up " + Util.getFormatedStringForByte(totalBytes - dstFolder.getFreeSpace()) + " (Available: "
							+ Util.getFormatedStringForByte(dstFolder.getFreeSpace()) + ")")) {
				CopySettings.status = CopySettings.State.CANCELLED;
				return;
			}
		}

		if(dstToMultiSrcMap.size() > 0) {
			Logger.info(
					"Files with same names and relative paths copied together from different sources. Will show src copy strategy dialog.");
			CopySettings.isCopyBlocked = true;
			new SrcCopyStrategyDlg();
			CopySettings.isCopyBlocked = false;
			
			if (CopySettings.status == CopySettings.State.CANCELING
					|| CopySettings.status == CopySettings.State.CANCELLED) {
				CopySettings.status = CopySettings.State.CANCELLED;
				System.gc();
				return;
			}
			
			// TODO sanitize dstExistence as per src selected
			Set<String> dsts = DstExistence.dstPathMap.keySet();
			for(String dst: dsts) {
				DstExistence dstExistence = DstExistence.dstPathMap.get(dst);
				dstExistence.cook();
			}
			updateDstConflicts();
			
			findSrcNonCopyFiles();			
		}
		
		
		
		if (CopySettings.overwriteTypeToFilesMap.size() > 0) {
			if (CopySettings.overwriteTypeToFilesMap.size() > 0) {
				Logger.info("Files with same names (but not same hash) found. Will show overwrite strategy dialog.");
			}

			CopySettings.isCopyBlocked = true;
			new OverwriteStrategyDlg();
			CopySettings.isCopyBlocked = false;

			if (CopySettings.status == CopySettings.State.CANCELING
					|| CopySettings.status == CopySettings.State.CANCELLED) {
				CopySettings.status = CopySettings.State.CANCELLED;
				System.gc();
				return;
			}
		}

		Logger.debug("Overwrite strategy decided : ");
		Logger.debug("RENAME_SKIPPED_FILES : " + CopySettings.RENAME_SKIPPED_FILES);
		Logger.debug("ASK_FOR_EACH_FILE : " + CopySettings.ASK_FOR_EACH_FILE);

//		Logger.debug("COPY_MULTIPLE_FILES_TO_SAME_PATH__ASK_FOR_EACH_FILE : "
//				+ CopySettings.COPY_MULTIPLE_FILES_TO_SAME_PATH__ASK_FOR_EACH_FILE);
		

		// remove older dated files if decided to copy only latest files, in case of
		// multiple sources with same destination
//		findIgnoreNonLatestFiles();
		
		// remove non-distinct files
//		findIgnoreNonDistinctFiles();

//		dstToMultiSrcMap.clear();
		totalFiles = copyFileUnits.size();
		totalBytes = 0;
		for (CopyFileUnit copyFileUnit : copyFileUnits) {
			totalBytes += copyFileUnit.size;
		}

		Logger.info("Copying " + totalFiles + " files.");
		Logger.log("--------------------------------------------------------------------------------");
		LCUiCreator.onFileCopyComplete(0, totalFiles, 0, 0, totalBytes, 0);

	}
	
	/**
	 * Find and list files that are causing src conflict but will not be copied (as
	 * per selected src copy conflict resolution strategy)
	 */
	private static void findSrcNonCopyFiles() {
		
		if (CopySettings.COPY_MULTIPLE_FILES_TO_SAME_PATH__ACTION_SELECTED == MultiSourceCopyActionEnum.CUSTOM_SELECTION) {
			Set<String> dstPaths = dstToMultiSrcMap.keySet();
			for (String dstPath : dstPaths) {
				ArrayList<String> srcs = dstToMultiSrcMap.get(dstPath);
				for (String src : srcs) {
					if (!SrcCopyStrategyDlg.selectedSrcsWithSrcConflict.contains(src)) {
						CopySettings.IGNORE_SRC_FILES.add(src);
					}
				}
			}
		} else if (CopySettings.COPY_MULTIPLE_FILES_TO_SAME_PATH__ACTION_SELECTED == MultiSourceCopyActionEnum.COPY_IF_LATEST) {
			Set<String> dstPaths = dstToMultiSrcMap.keySet();
			for (String dstPath : dstPaths) {
				ArrayList<String> srcs = dstToMultiSrcMap.get(dstPath);
				String latestFile = null;
				// find latest file
				for (String src : srcs) {
					if (latestFile == null) {
						latestFile = src;
					} else {
						if (getFileDate(new File(latestFile)) < getFileDate(new File(src))) {
							latestFile = src;
						}
					}
				}
				// remove non-latest files
				for (String src : srcs) {
					if (!latestFile.equals(src)) {
						CopySettings.IGNORE_SRC_FILES.add(src);
					}
				}
			}
		} else if (CopySettings.COPY_MULTIPLE_FILES_TO_SAME_PATH__ACTION_SELECTED == MultiSourceCopyActionEnum.RENAME_AND_COPY_IF_DISTINCT) {
			Set<String> dstPaths = dstToMultiSrcMap.keySet();
			for (String dstPath : dstPaths) {
				ArrayList<String> srcs = dstToMultiSrcMap.get(dstPath);
				HashMap<String, String> distinctMap = new HashMap<String, String>();
				
				for (String src : srcs) {
					File f = new File(src);
					String uniqueId = getFileSize(f)+"#"+getFileDate(f)+"#"+getFileHash(f);
					if(distinctMap.get(uniqueId) == null) {
						distinctMap.put(uniqueId, src);
					}
				}
				
				// remove non-latest files
				for (String src : srcs) {
					if (!distinctMap.containsValue(src)) {
						CopySettings.IGNORE_SRC_FILES.add(src);
					}
				}
			}
		}
		
	}

	/**
	 * 
	 * @return true if copy processed all the files
	 */
	private static boolean initCopy() {
		CopySettings.status = CopySettings.State.COPYING;

		sortFileCopyOrder();

		for (CopyFileUnit copyFileUnit : copyFileUnits) {
			String srcPath = copyFileUnit.srcPath;

			if (processedFilesList.contains(srcPath)) {
				// after resuming, no need to copy if the file is already copied
				continue;
			}

			Logger.log("");
			Logger.log("----------------------------------------------");
			Logger.log("");
			
			System.out.println(srcPath);

			if (CopySettings.status == CopySettings.State.CANCELING
					|| CopySettings.status == CopySettings.State.CANCELLED) {
				CopySettings.status = CopySettings.State.CANCELLED;
				System.gc();
				return false;
			}

			if (CopySettings.status == CopySettings.State.PAUSING) {
				CopySettings.status = CopySettings.State.PAUSED;
				LCUiCreator.updateWhenPaused();
				System.gc();
				return false;
			}

			File srcFile = new File(srcPath);
			LCUiCreator.onFileCopyStarting(srcFile, true);
			String destPath = CopyFileUnit.getDstGivenSrc(srcPath, copyFileUnits);
			File dstFile = new File(destPath);
			boolean copyStatus = false;

			if (srcPath.equals(destPath)) {
				// skip if src is same as destination
				Logger.info("Skipping the file as same file already exists in destination.");
				srcDstSameFilesSkippedList.add(srcPath);
			} else if(CopySettings.IGNORE_SRC_FILES.contains(srcPath)){
				// skip if src is marked to be ignored
				Logger.info("Skipping the file as the file has been marked to be ignored.");
				srcDstSameFilesSkippedList.add(srcPath);
			} else {

				if (dstFile.exists()) {
					Logger.info("The file already exists in destination : " + srcPath);

					// first check if it is already existing destination file or recently copied
					// destination file
					if (dstToMultiSrcMap.containsKey(destPath) && reverseFilesCopyMap.containsKey(destPath)) {
						// it is src duplicate file
						Logger.debug("The file has been recently copied here. So apply duplicate source file rules.");

//						if (CopySettings.COPY_MULTIPLE_FILES_TO_SAME_PATH__ASK_FOR_EACH_FILE) {
//							Logger.info("Ask user what to do about this particular file: " + srcFile.getName());
//
//							CopySettings.isCopyBlocked = true;
//							new MultiSrcOverwriteWarningDlg(srcFile);
//							CopySettings.isCopyBlocked = false;
//						}

						if (CopySettings.COPY_MULTIPLE_FILES_TO_SAME_PATH__ACTION_SELECTED == MultiSourceCopyActionEnum.RENAME_AND_COPY_ALL) {
							Logger.info("Renaming and copying file i.e. copying file with a different name");
							copyStatus = copy(srcFile, dstFile, true, false);
							if (copyStatus) {
								renamedFilesList.add(srcPath);
							}
						} else if (CopySettings.COPY_MULTIPLE_FILES_TO_SAME_PATH__ACTION_SELECTED == MultiSourceCopyActionEnum.RENAME_AND_COPY_IF_DISTINCT) {
							if (!isSameFile(srcFile, dstFile)) {
								Logger.info(
										"Renaming and copying file as they are distinct files i.e. copying file with a different name");
								copyStatus = copy(srcFile, dstFile, true, false);
								if (copyStatus) {
									renamedFilesList.add(srcPath);
								}
							} else {
								Logger.info("Skipping the file as same file has just been copied from another source.");
								sameFilesSkippedList.add(srcPath);
							}
						} else {
							if (isLatestFile(srcFile, dstFile)) {
								Logger.info(
										"Overwriting file as this a latest copy i.e. earlier a file was copied, but it was not latest, so replacing with this latest copy.");
								copyStatus = copy(srcFile, dstFile, false, true);
							} else {
								Logger.info(
										"Skipping the file as a latest copy of the file has already been copied from another source.");
								sameFilesSkippedList.add(srcPath);
							}
						}

					} else {
						// The file has been existing here from before. So overwrite rules will apply.

						// same named file exists
						if (CopySettings.SKIP_SAME_FILE_IF_EXISTS && isSameFile(srcFile, dstFile)) {
							// the destination file is an exact same file
							Logger.info("Skipping the file as same file already exists in destination.");
							sameFilesSkippedList.add(srcPath);
						} else {
							Logger.warn("Same named file exists : " + srcFile.getName());
							// warning about overwrite
							if (CopySettings.ASK_FOR_EACH_FILE) {
								Logger.info("Ask user what to do about this particular file: " + srcFile.getName());

								CopySettings.isCopyBlocked = true;
								new OverwriteWarningDlg(srcFile, dstFile);
								CopySettings.isCopyBlocked = false;

								if (CopySettings.status == CopySettings.State.CANCELING
										|| CopySettings.status == CopySettings.State.CANCELLED) {
									CopySettings.status = CopySettings.State.CANCELLED;
									Logger.info("Copy cancelled. Go home.");
									System.gc();
									return false;
								}
							}

							if (doOverwrite(srcFile, dstFile)) {
								// user has asked to overwrite
								Logger.warn("Overwriting existing file");
								copyStatus = copy(srcFile, dstFile, false, true);
								if (copyStatus) {
									overwrittenFilesList.add(srcFile.getAbsolutePath());
								}
							} else if (CopySettings.RENAME_SKIPPED_FILES) {
								Logger.info("Renaming skipped file i.e. copying file to a different name");
								copyStatus = copy(srcFile, dstFile, true, false);
								if (copyStatus) {
									renamedFilesList.add(srcPath);
								}
							} else {
								Logger.info("Skipping the file (not even renaming)");
								skippedFilesList.add(srcPath);
							}
						}
					}
				} else {
					Logger.debug("Same named file do not exist, so copy simply : " + srcFile.getName());
					copyStatus = copy(srcFile, dstFile, false, false);
				}
			}

			if (!copyStatus && CopySettings.status == CopySettings.State.PAUSED) {
				// copy failed and got paused, that means space issue
				LCUiCreator.updateWhenPaused();
				System.gc();
				return false;
			}

			doneWithFile(srcFile);
		}

		return true;
	}

	public static void cookFileCopies() {
		for (CopyFileUnit unit : copyFileUnits) {
			unit.cook(copyFileUnits);
		}

	}

	private static void sortFileCopyOrder() {

		cookFileCopies();

		Collections.sort(copyFileUnits);
	}

	private static void doneWithFile(File file) {
		copied++;
		copiedBytes += getFileSize(file);
		processedFilesList.add(file.getAbsolutePath());
		LCUiCreator.onFileCopyComplete(copied, totalFiles, copiedBytes, actuallyTransferredBytes, totalBytes,
				actuallyConsumedTime);
		System.gc();
	}

	/**
	 * 
	 * @param srcFile
	 * @param dstFile
	 * @param rename
	 *            user wants to copy file with a different name, and also keep
	 *            original dst file
	 */
	private static boolean copy(File srcFile, File dstFile, boolean rename, boolean overwrite) {

		if (rename) {
			File tempFile = getRenamedFile(srcFile, dstFile);
			if (tempFile == null) {
				Logger.error("Could not obtain a new name to rename. Go back home.");
				notify(srcFile, Message.TYPE.ERROR, "Failed to copy the file.",
						"Could not obtain a new name to rename the file and copy.",
						"Rename the file to a non-conflicting name first and then copy.", srcFile);
				failedFilesList.add(srcFile.getAbsolutePath());
				return false;
			}
			dstFile = tempFile;
		}

		long freeBytes = getFreeBytes(dstFile.getParentFile());
		if (!CopySettings.IGNORE_FREE_SPACE_WARNING && getFileSize(srcFile) > freeBytes) {

			CopySettings.status = CopySettings.State.PAUSED;

			long spaceNeeded = (totalBytes - copiedBytes) - freeBytes;
			if (spaceNeeded < 0) {
				spaceNeeded = getFileSize(srcFile) - freeBytes;
			}
			LCUiCreator.showLowSpaceWarning(spaceNeeded);

			Logger.error(srcFile.getName()
					+ " : Did not copy the file as there is not enough space in destination drive. (Needed: "
					+ Util.getFormatedStringForByte(getFileSize(srcFile)) + ", Available: "
					+ Util.getFormatedStringForByte(freeBytes) + ")");
			notify(srcFile, Message.TYPE.ERROR, "Failed to copy the file.",
					"There is not enough space to copy the file. (Needed: "
							+ Util.getFormatedStringForByte(getFileSize(srcFile)) + ", Available: "
							+ Util.getFormatedStringForByte(freeBytes) + ")",
					"Free up enough space before copying the file.", srcFile);
			failedFilesList.add(srcFile.getAbsolutePath());
			return false;
		}

		LCUiCreator.onFileCopyStarting(srcFile, false);
		long copyStartTime = System.currentTimeMillis();
		boolean copyStatus = copyFile(srcFile, dstFile, overwrite);

		if (copyStatus) {
			long transferredBytes = getFileSize(srcFile);
			actuallyTransferredBytes += transferredBytes;

			long copyEndTime = System.currentTimeMillis();

			long actuallyConsumedTimeForFile = copyEndTime - copyStartTime;
			actuallyConsumedTime += actuallyConsumedTimeForFile;

			if (actuallyConsumedTimeForFile > 100) { // 100 milliseconds is the threshhold.. any faster copy means not
														// really copied
				long speed = transferredBytes / actuallyConsumedTimeForFile;
				String drives = getFileDriveLetter(srcFile) + getFileDriveLetter(dstFile);
				Settings.DRIVE_TO_COPY_SPEED_MAP.put(drives, speed);
			}

			copiedFilesList.add(srcFile.getAbsolutePath());

			if (overwrite && reverseFilesCopyMap.get(dstFile.getAbsolutePath()) != null) {
				copiedFilesList.remove(reverseFilesCopyMap.get(dstFile.getAbsolutePath()));
				actuallyTransferredBytes -= getFileSize(new File(reverseFilesCopyMap.get(dstFile.getAbsolutePath())));
				copiedAndReplacedFilesList.add(reverseFilesCopyMap.get(dstFile.getAbsolutePath()));
			}
			reverseFilesCopyMap.put(dstFile.getAbsolutePath(), srcFile.getAbsolutePath());
		} else {
			failedFilesList.add(srcFile.getAbsolutePath());
		}

		if (copyStatus && !CopySettings.RETAIN_SOURCE_FILE_AFTER_COPY) {
			boolean deleted = false;
			String reason = null;
			try {
				Files.delete(Paths.get(srcFile.getAbsolutePath()));
				deleted = !srcFile.exists();
			} catch (Exception e) {
				Logger.error(srcFile.getName() + " : Error in deleting file : " + e.getMessage());
				reason = e.getMessage();
			}

			if (deleted) {
				Logger.info(srcFile.getName() + " : Deleted source file after copy is completed.");
			} else {
				Logger.warn(srcFile.getName() + " : Failed to delete source file after copy is completed.");
				notify(srcFile, Message.TYPE.WARNING, "Failed to delete source file after copying.", reason,
						"Delete the source file manually.", srcFile);
			}
		}

		if (overwrite && !copyStatus && CopySettings.RENAME_SKIPPED_FILES) {
			Logger.info(srcFile.getName()
					+ " : Copying the file with a new name since overwrite failed and user chose to rename skipped files.");
			File tempFile = getRenamedFile(srcFile, dstFile);
			if (tempFile == null) {
				Logger.error("Could not obtain a new name to rename. Go back home.");
				return false;
			}
			if (copy(srcFile, tempFile, false, false)) {
				failedFilesList.remove(srcFile.getAbsolutePath());
				notify(srcFile, Message.TYPE.INFO, "Copied the file with a new name : '" + tempFile.getName() + "'",
						null,
						"Delete the destination file, and rename the copied file to destination filename manually.",
						tempFile);
				renamedAfterFailedFilesList.add(srcFile.getAbsolutePath());
			}
		}
		return copyStatus;
	}

	private static long getFreeBytes(File file) {
		if (file == null) {
			return 0;
		}
		if (file.exists()) {
			return file.getUsableSpace();
		} else {
			return getFreeBytes(file.getParentFile());
		}
	}

	private static boolean copyFile(File srcFile, File dstFile, boolean overwrite) {
		Logger.debug("Copying the file using nio.Files.copy() method.");

		if (dstFile.exists() && dstFile.isDirectory()) {
			if (!deleteEmptyFolder(dstFile)) {
				Logger.error(srcFile.getName()
						+ " : Failed to copy the file : A directory (probably non-empty) with same name already exists.");
				notify(srcFile, Message.TYPE.ERROR,
						"Failed to copy the file : A directory (probably non-empty) with same name already exists.",
						null, "Rename the directory and copy the file. Or rename the source file and then copy.",
						dstFile);
				return false;
			}
		}

		File parent = dstFile.getParentFile();
		if (!parent.exists()) {
			try {
				parent.mkdirs();
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error(
						srcFile.getName() + " : Failed to copy the file : could not create the destination folder.");
				notify(srcFile, Message.TYPE.INFO, "Creating destination directory failed before copying.",
						e.getMessage(), "Copy the file again or manually.", srcFile);
				return false;
			}
		}

		try {

			if (Settings.SLOW_COPY_FOR_DEV_TESTING) {
				try {
					long sleepTime = srcFile.length() / 10000;
					while (sleepTime > 100000) {
						sleepTime = sleepTime / 10; // making it <100sec
					}
					if (sleepTime < 10000) {
						sleepTime = sleepTime * 10; // making it >10sec
					}
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			// actual copying happens here
			Files.copy(Paths.get(srcFile.getAbsolutePath()), Paths.get(dstFile.getAbsolutePath()),
					StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING, LinkOption.NOFOLLOW_LINKS);

			Logger.debug("Copy completed: " + srcFile.getName());
			if (Settings.TEST_FILE_INTEGRITY_USING_HASH) {
				// data integrity check
				if (getFileHash(srcFile).equals(getFileHash(dstFile))) {
					Logger.debug(srcFile.getName() + " : Data integrity check passed.");
					return true;
				} else {
					// integrity not maintained. delete copied file instead (rollback)
					Logger.error(srcFile.getName()
							+ " : Failed to copy the file : data integrity check failed after copying.");
					notify(srcFile, Message.TYPE.INFO, "Data integrity check failed after copying.", null,
							"Copy the file again or manually.", srcFile);
					rollbackCopiedFile(dstFile, srcFile.getName());
					return false;
				}
			} else {
				return true;
			}
		} catch (Exception e) {
			if (overwrite) {
				Logger.error(srcFile.getName() + " : Failed to copy the file : " + e.getMessage());
				if (tryDeleteAndCopy(srcFile, dstFile)) {
					return true;
				}
			}
			Logger.error(srcFile.getName() + " : Failed to copy the file : " + e.getMessage());
			notify(srcFile.getName(), Message.TYPE.ERROR, "Failed to copy file.", e.getMessage(),
					"Try copying the file manually.", srcFile);
		}

		Logger.error("Failed copying file: " + srcFile.getName());
		return false;
	}

	private static boolean tryDeleteAndCopy(File srcFile, File dstFile) {
		Logger.debug("Trying to copy the file using nio.Files.copy() method after deleting the dst file.");

		try {
			if (!dstFile.delete()) {
				Logger.error("Failed to delete the existing file: " + dstFile.getName());
				return false;
			}
		} catch (Exception e) {
			Logger.error("Failed to delete the existing file: " + dstFile.getName() + " : " + e.getMessage());
			return false;
		}

		// existing file deleted, now copy src file
		try {
			// actual copying happens here
			Files.copy(Paths.get(srcFile.getAbsolutePath()), Paths.get(dstFile.getAbsolutePath()),
					StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING, LinkOption.NOFOLLOW_LINKS);

			Logger.debug("Copy completed: " + srcFile.getName());
			if (Settings.TEST_FILE_INTEGRITY_USING_HASH) {
				// data integrity check
				if (getFileHash(srcFile).equals(getFileHash(dstFile))) {
					Logger.debug(srcFile.getName() + " : Data integrity check passed.");
					return true;
				} else {
					// integrity not maintained. delete copied file instead (rollback)
					Logger.error(srcFile.getName()
							+ " : Failed to copy the file : data integrity check failed after copying.");
					notify(srcFile, Message.TYPE.INFO, "Data integrity check failed after copying.", null,
							"Copy the file again or manually.", srcFile);
					rollbackCopiedFile(dstFile, srcFile.getName());
					return false;
				}
			} else {
				return true;
			}
		} catch (IOException e) {
			Logger.error(srcFile.getName() + " : Failed to copy the file : " + e.getMessage());
			notify(srcFile.getName(), Message.TYPE.ERROR, "Failed to copy file.", e.getMessage(),
					"Try copying the file manually.", srcFile);
		}

		Logger.error("Failed copying file: " + srcFile.getName());
		return false;
	}

}
