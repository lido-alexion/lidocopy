package com.lc.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUnzipUtil {

	static final int BUFFER = 2048;
	static List<String> fileList = new ArrayList<String>();

	public static void zip(String parentDirectory, String destPath, boolean encrypt) {

		fileList.clear();

		try {

			File destinationZip = new File(destPath);
			File parentDir = destinationZip.getParentFile();

			if (!parentDir.exists()) {
				parentDir.mkdirs();
			}

			BufferedInputStream origin = null;
			FileOutputStream dest = new FileOutputStream(destPath);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
			byte data[] = new byte[BUFFER];

			// get a list of files from current directory
			File f = new File(parentDirectory);
			buildFilesList(f);

			String entryName = "";

			for (int i = 0; i < fileList.size(); i++) {

				entryName = fileList.get(i);

				FileInputStream fi = new FileInputStream(parentDirectory + File.separator + entryName);
				origin = new BufferedInputStream(fi, BUFFER);

				ZipEntry entry = new ZipEntry(ZipUnzipUtil.getRandomized(entryName, encrypt));
				out.putNextEntry(entry);
				int count;
				while ((count = origin.read(data, 0, BUFFER)) != -1) {
					out.write(ZipUnzipUtil.getRandomized(data, encrypt), 0, count);
				}
				origin.close();
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void buildFilesList(File file) {
		buildFilesList(file, "");
	}

	public static void buildFilesList(File file, String subpath) {

		if (file.isFile()) {
			fileList.add(subpath + file.getName());
			return;
		}

		File[] files = file.listFiles();
		if (files != null && files.length > 0) {
			for (File f : files) {

				if (f.isFile()) {
					fileList.add(subpath + f.getName());
				} else {
					buildFilesList(f, subpath + f.getName() + File.separator);
				}
			}
		}
	}

	public static byte[] getRandomized(byte[] bytes, boolean isCrypted) {

		if (isCrypted) {
			byte b = new Byte("1").byteValue();

			for (int i = 0; i < bytes.length; i++) {
				byte temp = (byte) (bytes[i] ^ b);
				bytes[i] = temp;
			}
		}
		return bytes;
	}

	public static String getRandomized(String str, boolean isCrypted) {
		return new String(getRandomized(str.getBytes(), isCrypted));
	}

	@SuppressWarnings("rawtypes")
	public static void unzip(String inputZip, String destinationDirectory, boolean decrypt) throws IOException {

		fileList.clear();

		List<String> zipFiles = new ArrayList<String>();
		File sourceZipFile = new File(inputZip);
		File unzipDestinationDirectory = new File(destinationDirectory);
		unzipDestinationDirectory.mkdir();

		ZipFile zipFile;
		// Open Zip file for reading
		zipFile = new ZipFile(sourceZipFile, ZipFile.OPEN_READ);

		// Create an enumeration of the entries in the zip file
		Enumeration zipFileEntries = zipFile.entries();

		// Process each entry
		while (zipFileEntries.hasMoreElements()) {
			// grab a zip file entry
			ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();

			String currentEntry = getRandomized(entry.getName(), decrypt);

			File destFile = new File(unzipDestinationDirectory, currentEntry);
			// destFile = new File(unzipDestinationDirectory,

			if (currentEntry.endsWith(".zip")) {
				zipFiles.add(destFile.getAbsolutePath());
			}

			// grab file's parent directory structure
			File destinationParent = destFile.getParentFile();

			// create the parent directory structure if needed
			destinationParent.mkdirs();

			try {
				// extract file if not a directory
				if (!entry.isDirectory()) {
					BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(entry));
					int currentByte;
					// establish buffer for writing file
					byte data[] = new byte[BUFFER];

					// write the current file to disk
					FileOutputStream fos = new FileOutputStream(destFile);
					BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

					// read and write until last byte is encountered
					while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
						dest.write(getRandomized(data, decrypt), 0, currentByte);
					}
					dest.flush();
					dest.close();
					is.close();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		zipFile.close();

		for (Iterator iter = zipFiles.iterator(); iter.hasNext();) {
			String zipName = (String) iter.next();
			File tempZipFile = new File(zipName);
			String name = tempZipFile.getName();
			int lastIndex = name.lastIndexOf(".zip");
			if (lastIndex == -1) {
				continue;
			}
			unzip(zipName, destinationDirectory + File.separator + name.substring(0, lastIndex), decrypt);

			tempZipFile.delete();
		}

	}

}