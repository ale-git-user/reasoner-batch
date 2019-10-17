package com.termmed.reasoner.model;

import java.util.List;

public class OntologyFileRequest {
	Path path;
	String type;
	String authoringServer;
	String secondaryServer;
	String dbTarget;
	String collectionTarget;
    String mainDbPort;
	String projectId;
	String releaseServer;
	String user;
	String pass;
	String bucketName;
	String accessKeyId;
	String secretAccessKey;
	String region;
	String fileKey;
	String filename;
	String status;
	List<CompleteFilesConfig>completeFilesConfig;
	public Path getPath() {
		return path;
	}
	public void setPath(Path path) {
		this.path = path;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getAuthoringServer() {
		return authoringServer;
	}
	public void setAuthoringServer(String authoringServer) {
		this.authoringServer = authoringServer;
	}
	public String getSecondaryServer() {
		return secondaryServer;
	}
	public void setSecondaryServer(String secondaryServer) {
		this.secondaryServer = secondaryServer;
	}
	public String getDbTarget() {
		return dbTarget;
	}
	public void setDbTarget(String dbTarget) {
		this.dbTarget = dbTarget;
	}
	public String getCollectionTarget() {
		return collectionTarget;
	}
	public void setCollectionTarget(String collectionTarget) {
		this.collectionTarget = collectionTarget;
	}


    public String getMainDbPort() {
        return mainDbPort;
    }

    public void setMainDbPort(String mainDbPort) {
        this.mainDbPort = mainDbPort;
    }
	public String getProjectId() {
		return projectId;
	}
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	public String getReleaseServer() {
		return releaseServer;
	}
	public void setReleaseServer(String releaseServer) {
		this.releaseServer = releaseServer;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPass() {
		return pass;
	}
	public void setPass(String pass) {
		this.pass = pass;
	}
	public String getBucketName() {
		return bucketName;
	}
	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}
	public String getAccessKeyId() {
		return accessKeyId;
	}
	public void setAccessKeyId(String accessKeyId) {
		this.accessKeyId = accessKeyId;
	}
	public String getSecretAccessKey() {
		return secretAccessKey;
	}
	public void setSecretAccessKey(String secretAccessKey) {
		this.secretAccessKey = secretAccessKey;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getFileKey() {
		return fileKey;
	}
	public void setFileKey(String fileKey) {
		this.fileKey = fileKey;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public List<CompleteFilesConfig> getCompleteFilesConfig() {
		return completeFilesConfig;
	}
	public void setCompleteFilesConfig(List<CompleteFilesConfig> completeFilesConfig) {
		this.completeFilesConfig = completeFilesConfig;
	}
	
}
