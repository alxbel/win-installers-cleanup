package com.github.blackenwhite.wininstallercleanup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.prefs.Preferences;


/**
 * Created on 16.09.2015.
 */
public class MainWindow extends JPanel {
	private static final String APP_NAME = "Windows Installers Cleaner";
	private static final String DEFAULT_BACKUP_FOLDER = "E:\\tmp\\backup";
	private static final String FOUND_FILES_LABEL = "Found files:";
	private static final String DISK_SPACE_TO_FREE_LABEL = "Disk space to free:";
	private static final String BROWSE_BUTTON_LABEL = "Browse";
	private static final String SCAN_BUTTON_LABEL = "Scan";
	private static final String DELETE_BUTTON_LABEL = "Delete";
	private static final String MEGABYTES_LABEL = " mb";
	private static final String BACKUP_LABEL = "Backup";
	private static final String ERROR_PERMISSIONS_TITLE	= "Not enough permissions";
	private static final String ERROR_PERMISSIONS_MESAGE = "Please run Start.cmd as administrator";

	private static final int DEFAULT_WIDTH = 600;
	private static final int DEFAULT_HEIGHT = 200;

	private final JLabel foundFilesLabel;
	private final JLabel foundFilesCount;
	private final JLabel diskSpaceToFreeLabel;
	private final JLabel diskSpaceToFree;
	private JCheckBox checkBox;
	private JTextField backupPath;
	private JButton browseBtn;
	private JButton scanBtn;
	private JButton delBtn;
	private Integer filesCount;
	private Integer spaceToFree;

	private boolean backupEnabled = false;
	WinInstallerCleanup cleaner;

	public MainWindow() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		foundFilesLabel = new JLabel(FOUND_FILES_LABEL);
		filesCount = new Integer(0);
		foundFilesCount = new JLabel(filesCount.toString());
		diskSpaceToFreeLabel = new JLabel(DISK_SPACE_TO_FREE_LABEL);
		spaceToFree = new Integer(0);
		diskSpaceToFree = new JLabel(spaceToFree.toString() + MEGABYTES_LABEL);
		checkBox = new JCheckBox();
		checkBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					backupPath.setEnabled(true);
					browseBtn.setEnabled(true);
					backupEnabled = true;
				} else {
					backupPath.setEnabled(false);
					browseBtn.setEnabled(false);
					backupEnabled = false;
				}
			}
		});

		backupPath = new JTextField(30);
		backupPath.setText(DEFAULT_BACKUP_FOLDER);
		backupPath.setPreferredSize(new Dimension(0, 25));
		backupPath.setEnabled(false);

		browseBtn = new JButton(BROWSE_BUTTON_LABEL);
		browseBtn.setEnabled(false);
		browseBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setCurrentDirectory(new File(backupPath.getText()));
				int returnVal = chooser.showOpenDialog(null);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					backupPath.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});

		scanBtn = new JButton(SCAN_BUTTON_LABEL);
		scanBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cleaner = new WinInstallerCleanup();
				cleaner.run();
				foundFilesCount.setText(cleaner.getFilesCount().toString());
				diskSpaceToFree.setText(cleaner.getTotalSize().toString() + MEGABYTES_LABEL);
				if (cleaner.getTotalSize() > 0 && cleaner != null) {
					delBtn.setEnabled(true);
				}
			}
		});
		delBtn = new JButton(DELETE_BUTTON_LABEL);
		delBtn.setEnabled(false);

		delBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (backupEnabled) {
					if (!cleaner.backup(getBackupPath())) {
						System.out.println("Backup failed");
						return;
					}
					System.out.println("Backup succeeded. Deleting...");
					delBtn.setEnabled(false);
					if (!cleaner.delete()) {
						System.out.println("Delete failed");
						return;
					}
					System.out.println("Delete succeeded.");
				}
			}
		});

		JPanel firstRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
		JPanel backupCheck = new JPanel();
		backupCheck.add(new JLabel(BACKUP_LABEL));
		backupCheck.add(checkBox);
		JPanel path = new JPanel();
		path.add(backupPath);
		path.add(browseBtn);
		firstRow.add(backupCheck);
		firstRow.add(path);
		add(firstRow);

		JPanel secondRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 10));
		JPanel found = new JPanel();
		found.add(foundFilesLabel);
		found.add(foundFilesCount);
		JPanel diskSpace = new JPanel();
		diskSpace.add(diskSpaceToFreeLabel);
		diskSpace.add(diskSpaceToFree);
		secondRow.add(found);
		secondRow.add(diskSpace);
		add(secondRow);

		JPanel thirdRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 10));
		thirdRow.add(scanBtn);
		thirdRow.add(delBtn);
		add(thirdRow);
	}

	public void setFoundFilesCount(int count) {
		foundFilesCount.setText(new Integer(count).toString());
	}

	private String getBackupPath() {
		return backupPath.getText();
	}

	private static void createAndShowGUI() {
		JFrame frame = new JFrame(APP_NAME);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		MainWindow contentPane = new MainWindow();
		contentPane.setOpaque(true);
		frame.setContentPane(contentPane);
		frame.setResizable(false);

		centreWindow(frame);
		frame.pack();
		frame.setVisible(true);
	}

	private static void centreWindow(Window frame) {
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
		int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
		frame.setLocation(x, y);
	}

	private static void checkIfAdmin(){
		Preferences prefs = Preferences.systemRoot();
		try {
			prefs.put("foo", "bar"); // SecurityException on Windows
			prefs.remove("foo");
			prefs.flush(); // BackingStoreException on Linux
		} catch(Exception e) {
			JOptionPane.showMessageDialog(null, ERROR_PERMISSIONS_MESAGE,
					ERROR_PERMISSIONS_TITLE, JOptionPane.WARNING_MESSAGE);
			System.exit(0);
		}
	}

	public static void main(String[] args) {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				checkIfAdmin();
				createAndShowGUI();
			}
		});
	}
}
