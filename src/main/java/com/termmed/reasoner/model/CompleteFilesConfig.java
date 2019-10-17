package com.termmed.reasoner.model;

import java.util.List;

public class CompleteFilesConfig {
	String initialDataTxTime;
	String moduleId;
	String releaseDate;
	List<LanguageComplete> language_complete;
	String previousSnapshotReleaseAWSKey;
	public String getInitialDataTxTime() {
		return initialDataTxTime;
	}

	public void setInitialDataTxTime(String initialDataTxTime) {
		this.initialDataTxTime = initialDataTxTime;
	}


	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}
	public List<LanguageComplete> getLanguage_complete() {
		return language_complete;
	}

	public void setLanguage_complete(List<LanguageComplete> language_complete) {
		this.language_complete = language_complete;
	}

	public String getPreviousSnapshotReleaseAWSKey() {
		return previousSnapshotReleaseAWSKey;
	}

	public void setPreviousSnapshotReleaseAWSKey(String previousSnapshotReleaseAWSKey) {
		this.previousSnapshotReleaseAWSKey = previousSnapshotReleaseAWSKey;
	} 
}
