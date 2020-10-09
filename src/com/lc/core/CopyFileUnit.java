package com.lc.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.lc.common.CopySettings;

public class CopyFileUnit implements Comparable<CopyFileUnit> {

	public String dstPath;
	public String srcPath;

	public String relativePath;
	public int depth = 0;
	public boolean exists;
	public long size;
	public boolean userActionNeeded;
	public int siblings = 1;
	public String name;

	public CopyFileUnit(String srcPath, String dstPath, String relativePath) {
		this.srcPath = srcPath;
		this.dstPath = dstPath;
		this.name = new File(srcPath).getName();
		this.relativePath = relativePath;

		exists = new File(dstPath).exists();
		size = new File(srcPath).length();
		depth = relativePath.split("\\\\").length - 1;
	}

	public void cook(List<CopyFileUnit> list) {
		if (CopySettings.ASK_FOR_EACH_FILE && exists) {
			userActionNeeded = true;
		} else {
			userActionNeeded = false;
		}

		if (list != null) {
			siblings = getCopyFileUnitsWithDst(dstPath, list).size();
			if (!userActionNeeded) {
//				if (CopySettings.COPY_MULTIPLE_FILES_TO_SAME_PATH__ASK_FOR_EACH_FILE && siblings > 1) {
//					userActionNeeded = true;
//				}
			}
		} else {
			siblings = 1;
		}
	}

	public static CopyFileUnit getCopyFileUnitWithSrc(String srcPath, List<CopyFileUnit> list) {
		for (CopyFileUnit copyUnit : list) {
			if (copyUnit.srcPath.equals(srcPath)) {
				return copyUnit;
			}
		}
		return null;
	}

	public static List<CopyFileUnit> getCopyFileUnitsWithDst(String dstPath, List<CopyFileUnit> list) {
		List<CopyFileUnit> prunedList = new ArrayList<CopyFileUnit>();
		for (CopyFileUnit copyUnit : list) {
			if (copyUnit.dstPath.equals(dstPath)) {
				prunedList.add(copyUnit);
			}
		}
		return prunedList;
	}

	public static String getDstGivenSrc(String srcPath, List<CopyFileUnit> list) {
		CopyFileUnit unit = getCopyFileUnitWithSrc(srcPath, list);
		return unit != null ? unit.dstPath : null;
	}

	@Override
	public int compareTo(CopyFileUnit unit2) {
		// sort by depth first (less depth first)
		if (depth < unit2.depth) {
			return -1;
		} else if (depth > unit2.depth) {
			return 1;
		} else {
			// sort based on whether user action needed or not (files without user action
			// needed come first)
			if (!userActionNeeded && unit2.userActionNeeded) {
				return -1;
			} else if (userActionNeeded && !unit2.userActionNeeded) {
				return 1;
			} else {
				// sort by file size (small files first)
				if (size < unit2.size) {
					return -1;
				} else if (size > unit2.size) {
					return 1;
				} else {
					// sort by file name (alphabetical)
					return name.compareTo(unit2.name);
				}
			}
		}
	}

}
