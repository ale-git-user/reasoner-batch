package com.termmed.util;

import java.io.File;

import org.apache.log4j.Logger;

public class ConvertionSnapshotDelta {

	private static final Logger log = Logger.getLogger(ConvertionSnapshotDelta.class);

	public static boolean snapshotFile(File file, File tempSortingFolder,File tempSortedFinalfolder, File resultFile, String date) {
		try {
			if (!tempSortingFolder.exists()) {
				tempSortingFolder.mkdirs();
			}
			boolean sorted = false;
			int[] sortColumns = new int[] { 0, 1 };
			sorted = isSorted(file, sortColumns);
			if (!sorted) {
				File sortedFile = new File(tempSortedFinalfolder, "Sorted" + file.getName());
				FileSorter fsc = new FileSorter(file, sortedFile, tempSortingFolder, sortColumns);
				fsc.execute();
				fsc = null;
				System.gc();
				SnapshotGenerator sg = new SnapshotGenerator(sortedFile, date, 0, 1, resultFile, null, null);
				sg.execute();
				sg=null;
				System.gc();
				if (sortedFile.exists()){
					sortedFile.delete();
				}
			}else{

				SnapshotGenerator sg = new SnapshotGenerator(file, date, 0, 1, resultFile, null, null);
				sg.execute();
				sg=null;
				System.gc();
			}
		} catch (Exception e) {
			log.error(e);
			return false;
		} finally {
			FileHelper.emptyFolder(tempSortingFolder);
		}
		return true;
	}

	public static boolean deltaFile(File file, File deltaFile, File tempSortingFolder,File tempSortedFinalFolder, String initDate, String endDate) {
		try {
			if (!tempSortingFolder.exists()) {
				tempSortingFolder.mkdirs();
			}
			boolean sorted = false;
			int[] sortColumns = new int[] { 0, 1 };
			sorted = isSorted(file, sortColumns);
			if (!sorted) {
				File sortedFile = new File(tempSortedFinalFolder, "Sorted" + file.getName());
				FileSorter fsc = new FileSorter(file, sortedFile, tempSortingFolder, sortColumns);
				fsc.execute();
				fsc = null;
				System.gc();

				DeltaGenerator dg = new DeltaGenerator(sortedFile, initDate, endDate, new int[]{0}, 1, deltaFile);
				dg.execute();
				dg=null;
				System.gc();
				if (sortedFile.exists()){
					sortedFile.delete();
				}
			}else{
			
				DeltaGenerator dg = new DeltaGenerator(file, initDate, endDate, new int[]{0}, 1, deltaFile);
				dg.execute();
				dg=null;
				System.gc();
			}
		} catch (Exception e) {
			log.error(e);
			return false;
		} finally {
			FileHelper.emptyFolder(tempSortingFolder);
		}

		return true;
	}

	private static boolean isSorted(File file, int[] sortColumns) {
		SortAnalyzer sa = new SortAnalyzer(file, sortColumns);
		Boolean ret = sa.isSortedFile();
		sa = null;
		System.gc();
		return ret;
	}

}
