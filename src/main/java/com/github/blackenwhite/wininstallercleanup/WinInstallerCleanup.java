package com.github.blackenwhite.wininstallercleanup;

/**
 * Created on 15.09.2015.
 */
import javax.swing.*;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.prefs.Preferences;

/**
 * Created on 14.09.2015.
 */
public class WinInstallerCleanup {
	public static final String SCRIPT_RUNNER 			= "cscript";
	public static final String VBSCRIPT 				= "/script.vbs";
	public static final String TEMP_SCRIPT_NAME 		= "tmpscript";
	public static final String TEMP_SCRIPT_EXTENSION 	= ".vbs";
	public static final String BEGIN_OF_OUTPUT 			= "###begin###";
	public static final String END_OF_OUTPUT 			= "###end###";
	public static final String INSTALLER_FOLDER 		= "C:\\Windows\\Installer";
//	public static final String INSTALLER_FOLDER 		= "E:\\tmp";
//	public static final String ERROR_PERMISSIONS_TITLE	= "Not enough permissions";
//	public static final String ERROR_PERMISSIONS_MESAGE = "Please run start.cmd as administrator";

	private String tempScript;
	private ArrayList<String> registeredPatches;
	private ArrayList<String> filesToDelete;
	private int totalSize;
	private int filesCount;

	public void run() {
		if (copyScriptContents()) {
			System.out.println("Script init succeeded.");
			execScript();
			listFilesForDelete(new File(INSTALLER_FOLDER));
			System.out.println("Total size: " + getTotalSize() + " mb. Files: " + getFilesCount());
		} else {
			System.out.println("Something went wrong.");
		}
	}

	public boolean backup(String path) {
		Path backupDir = Paths.get(path);
		if (Files.exists(backupDir)) {
			for (String file : filesToDelete) {
				final File sourceFile = new File(file);
				final File destFile = new File(backupDir.toString() + "\\" + sourceFile.getName());
				try {
					copyFile(sourceFile, destFile);
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
				System.out.println(destFile);
			}
			return true;
		}
		return false;
	}

	public boolean delete() {
		try {
			for (String file : filesToDelete) {
				File fileToDelete = new File(file);
				if (!fileToDelete.delete()) {
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void setFilesCount(int filesCount) {
		this.filesCount = filesCount;
	}

	public Integer getFilesCount() {
		return filesCount;
	}

	public Integer getTotalSize() {
		return totalSize;
	}

	public void setTotalSize(int totalSize) {
		this.totalSize = totalSize;
	}

	public ArrayList<String> getRegisteredPatches() {
		return registeredPatches;
	}

	private static void copyFile(File sourceFile, File destFile) throws IOException {
		if(!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;

		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		}
		finally {
			if(source != null) {
				source.close();
			}
			if(destination != null) {
				destination.close();
			}
		}
	}

	private boolean copyScriptContents() {
		// First, create empty script file
		File temp = null;
		try {
			temp = File.createTempFile(TEMP_SCRIPT_NAME, TEMP_SCRIPT_EXTENSION,
					new File(System.getProperty("user.dir")));
			temp.deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		// Then copy script.vbs contents into temp script
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			inputStream = getClass().getResourceAsStream(VBSCRIPT);
			outputStream = new FileOutputStream(temp);

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			inputStream.close();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		tempScript = temp.getPath();
		return true;
	}

	private void execScript() {
		ProcessBuilder ps = new ProcessBuilder(SCRIPT_RUNNER, tempScript);
		ps.redirectErrorStream(true);

		try {
			Process pr = ps.start();
			BufferedReader in = new BufferedReader(new
					InputStreamReader(pr.getInputStream()));
			registeredPatches = new ArrayList<String>();
			String line;
			boolean beginRead = false;
			while ((line = in.readLine()) != null) {
				if (line.equals(BEGIN_OF_OUTPUT)) {
					beginRead = true;
					continue;
				}
				if (line.equals(END_OF_OUTPUT)) {
					break;
				}
				if (!beginRead) {
					continue;
				}
				registeredPatches.add(line);
			}
			pr.waitFor();

			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	private void listFilesForDelete(File folder) {
		filesToDelete = new ArrayList<String>();
		for (File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				continue;
			}
			if (isRegisteredPatch(fileEntry)) {
				continue;
			}
			double fileSize = Math.round((double)fileEntry.length() / 1000000.0);
			setTotalSize(getTotalSize() + (int)fileSize);
			setFilesCount(getFilesCount() + 1);
			filesToDelete.add(fileEntry.getAbsolutePath());
			System.out.println(fileEntry.getAbsolutePath() + " " + fileSize + " mb");
		}
	}

	private boolean isRegisteredPatch(File file) {
		for (String registered : registeredPatches) {
			if (file.getAbsolutePath().equals(registered)) {
				return true;
			}
		}
		return false;
	}
}
