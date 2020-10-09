package com.lc.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.lc.common.CopySettings;
import com.lc.service.CopyServices;
import com.lc.ui.SrcCopyStrategyDlg;

public class DstExistence {

	private String dst;
	private List<String> sources = new ArrayList<>();
	private String firstSrcToReplace;
	private String replaceBucket;
	public static HashMap<String, DstExistence> dstPathMap = new HashMap<String, DstExistence>();

	public DstExistence(String dst) {
		this.dst = dst;
		dstPathMap.put(dst, this);
	}

	public DstExistence(String dst, String src) {
		this.dst = dst;
		sources.add(src);
		dstPathMap.put(dst, this);
	}

	public DstExistence(String dst, List<String> srcs) {
		this.dst = dst;
		for (String src : srcs) {
			sources.add(src);
		}
		dstPathMap.put(dst, this);
	}

	public void addSource(String src) {
		if (!sources.contains(src)) {
			sources.add(src);
		}
	}

	public void addSources(List<String> srcs) {
		for (String src : srcs) {
			if (!sources.contains(src)) {
				sources.add(src);
			}
		}
	}

	public void removeSource(String src) {
		sources.remove(src);
	}

	public String getDstPath() {
		return dst;
	}

	public List<String> getSources() {
		return sources;
	}

	public String getPreferredSrc() {
		return firstSrcToReplace;
	}

	public String getBucketName() {
		return replaceBucket;
	}

	public int getSrcCount() {
		return sources.size();
	}
	
	private void cleanUpUnselectedSrcs() {
		List<String> cleaunUpList = new ArrayList<String>();
		for(String src : sources) {
			if(!SrcCopyStrategyDlg.selectedSrcsWithSrcConflict.contains(src)) {
				cleaunUpList.add(src);
			}
		}
		
		for(String src : cleaunUpList) {
			sources.remove(src);
		}
	}

	public void cook() {
		cleanUpUnselectedSrcs();
		
		if (sources.size() > 1) {
			List<CopyFileUnit> copyFileUnitsList = new ArrayList<CopyFileUnit>();
			for (String src : sources) {
				copyFileUnitsList.add(CopyFileUnit.getCopyFileUnitWithSrc(src, CopyServices.copyFileUnits));
			}

			Collections.sort(copyFileUnitsList);
			firstSrcToReplace = copyFileUnitsList.get(0).srcPath;
		} else {
			firstSrcToReplace = sources.size() > 0 ? sources.get(0) : null;
		}

		replaceBucket = null;
		if (firstSrcToReplace != null) {
			long dateDiff = new File(firstSrcToReplace).lastModified() - new File(dst).lastModified(); // +ve if src is latest, -ve if dest is latest,
			// 0 if same dated
			boolean isSameSize = new File(firstSrcToReplace).length() == new File(dst).length();

			if (!isSameSize) {
				if (dateDiff > 0) {
					replaceBucket = CopySettings.DIFFERENT_SIZE_OLDER_DATE;
				} else if (dateDiff == 0) {
					replaceBucket = CopySettings.DIFFERENT_SIZE_SAME_DATE;
				} else {
					replaceBucket = CopySettings.DIFFERENT_SIZE_LATEST_DATE;
				}
			} else {
				if (dateDiff > 0) {
					replaceBucket = CopySettings.SAME_SIZE_OLDER_DATE;
				} else if (dateDiff == 0) {
					if (!CopySettings.SKIP_SAME_FILE_IF_EXISTS || !CopyServices.isSameFile(new File(firstSrcToReplace), new File(dst))) {
						// only if user has chosen not to skip same files and it's not same file, even
						// though same date and size
						replaceBucket = CopySettings.SAME_SIZE_SAME_DATE;
					}
				} else {
					replaceBucket = CopySettings.SAME_SIZE_LATEST_DATE;
				}
			}
		}
	}

	public static boolean isDstExists(String dstPath) {
		return dstPathMap.containsKey(dstPath);
	}

	public static DstExistence getDst(String dstPath) {
		return dstPathMap.get(dstPath);
	}

}
