package com.termmed;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.TimerTask;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.termmed.owl.RF2Parser;
import com.termmed.reasoner.model.LanguageComplete;
import com.termmed.reasoner.model.OntologyFileRequest;
import com.termmed.util.CommonUtils;
import com.termmed.util.ConvertionSnapshotDelta;
import com.termmed.util.FileHelper;
import org.snomed.otf.owltoolkit.service.ReasonerServiceException;

public class BuildBaselineTask extends TimerTask {

	private static final String RELEASES_DOWNLOAD_FOLDER = "/root/releasesFiles/";
	private static final String FALLBACK_COMPUTED = "WithPrecomputedDefaults";
	private OntologyFileRequest buildConfig;
	private BucketGetFile bucket;
	private HashSet<String> concepts;
	private HashSet<String> descriptions;
	private HashSet<String> languages;
	private HashSet<String> statedRels;
	private File sortFolderTmp;
	private File sortedFolderTmp;
	private File outputDir;
	@Override
	public void run() {
		FileManagerServer.setOntologyBuildBaselineTimerOff();
		FileManagerServer.removeBaselineTimer();

		createFolders();
		
		concepts=new HashSet<String>();
		descriptions=new HashSet<String>();
		languages=new HashSet<String>();
		statedRels=new HashSet<String>();

		bucket=new BucketGetFile();

		bucket.setBucketName(buildConfig.getBucketName());
		bucket.setAccessKey(buildConfig.getAccessKeyId());
		bucket.setSecretAccessKey(buildConfig.getSecretAccessKey());

		getAwsFiles(buildConfig.getCompleteFilesConfig().get(0).getPreviousSnapshotReleaseAWSKey());
		if (buildConfig.getCompleteFilesConfig().get(0).getLanguage_complete()!=null){
			for (LanguageComplete language_complete:buildConfig.getCompleteFilesConfig().get(0).getLanguage_complete()){
				getAwsFiles(language_complete.getSource_AWSKey());

			}
		}


		bucket=null;

		try {
			createOwl();
			ReasonerServer.reloadOntology();
		} catch (IOException  e) {
			e.printStackTrace();
			ServerStatus.setStatusMessage(ServerStatus.STATUS.ERROR, "Error(IOException): cannot get owl from rf2 release files" + e.getMessage());
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			ServerStatus.setStatusMessage(ServerStatus.STATUS.ERROR, "Error(OWLOntologyCreationException): cannot get owl from rf2 release files" + e.getMessage());
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
			ServerStatus.setStatusMessage(ServerStatus.STATUS.ERROR, "Error(OWLOntologyStorageException): cannot get owl from rf2 release files" + e.getMessage());
		} catch (ReasonerServiceException e) {
			e.printStackTrace();
			ServerStatus.setStatusMessage(ServerStatus.STATUS.ERROR, "Error(OWLOntologyStorageException): cannot get owl from rf2 release files" + e.getMessage());
		}

	}
	private void createOwl() throws IOException, OWLOntologyCreationException, OWLOntologyStorageException {
		File finalConceptFile=joinAndSnapshotFiles(concepts,"con_");
		File finalDescriptionFile=joinAndSnapshotFiles(descriptions,"des_");
		File finalLanguageFile=joinAndSnapshotFiles(languages,"lan_");
		File finalStatedFile=joinAndSnapshotFiles(statedRels,"sta_");
		
		String concept=null;
		String description=null;
		String language=null;
		String statedRel=null;
		if (finalConceptFile!=null){
			concept=finalConceptFile.getAbsolutePath();
		}
		if (finalDescriptionFile!=null){
			description=finalDescriptionFile.getAbsolutePath();
		}
		if (finalLanguageFile!=null){
			language=finalLanguageFile.getAbsolutePath();
		}
		if (finalStatedFile!=null){
			statedRel=finalStatedFile.getAbsolutePath();
		}
		RF2Parser parser=new RF2Parser(concept,
				statedRel,
				description, 
				null, 
				language, 
				buildConfig.getFilename(), 
				buildConfig.getFileKey());
		
		parser.parse();
	}
	private void createFolders() {

		outputDir=new File("joinedFiles");
		if (!outputDir.exists()){
			outputDir.mkdirs();
		}else{
			FileHelper.emptyFolder(outputDir);
		}
		sortFolderTmp=new File( "sortFolderTmp");
		if (!sortFolderTmp.exists()){
			sortFolderTmp.mkdirs();
		}else{
			FileHelper.emptyFolder(sortFolderTmp);
		}
		sortedFolderTmp=new File( "sortedFolderTmp");
		if (!sortedFolderTmp.exists()){
			sortedFolderTmp.mkdirs();
		}else{
			FileHelper.emptyFolder(sortedFolderTmp);
		}
		
	}


	private File joinAndSnapshotFiles(HashSet<String> files,String prefix) throws IOException{
		
		File outputFile=null;
		if (files.size()==1){
			return new File(files.iterator().next());
		}else if (files.size()>1){
			File tmpFile=File.createTempFile(prefix, "txt", sortedFolderTmp);
			concatFile(files, tmpFile);
			outputFile=File.createTempFile(prefix, "txt", outputDir);
			ConvertionSnapshotDelta.snapshotFile(tmpFile, sortFolderTmp, sortedFolderTmp, outputFile, "99999999");
		}
		return outputFile;
	}
	private void getAwsFiles(String AWSKey) {
		bucket.setFileKey(AWSKey);
		int pos =AWSKey.lastIndexOf("/");
		String filename;
		if (pos>-1){
			filename=AWSKey.substring(pos +1);
		}else{
			filename=AWSKey;
		}
		File folderTgt=new File(RELEASES_DOWNLOAD_FOLDER + filename );
		folderTgt.mkdirs();
		String ontoZipFile=folderTgt.getAbsolutePath() +"/" + filename + ".zip";
		bucket.setFile(ontoZipFile);
		try {
			bucket.execute();
			File ontoFile=new File(ontoZipFile);
			Utils.unzip(ontoFile, folderTgt);
			ontoFile.delete();

			getFiles(folderTgt);

		} catch (Exception e) {
			e.printStackTrace();
			ServerStatus.setStatusMessage(ServerStatus.STATUS.ERROR, "Error: cannot get rf2 release files from aws");
		}		
	}
	private void getFiles(File folderTgt) throws IOException, Exception {
		String conceptFile = FileHelper.getFile(folderTgt, "rf2-concepts",null,null,null);
		String descriptionFile = FileHelper.getFile( folderTgt, "rf2-descriptions",null,null,null);
		String languageFile = FileHelper.getFile( folderTgt, "rf2-language",null,null,FALLBACK_COMPUTED);
		String statRelsFile=FileHelper.getFile( folderTgt, "rf2-relationships",null,"stated",null);

		if (conceptFile!=null){
			concepts.add(conceptFile);
		}
		if (descriptionFile!=null){
			descriptions.add(descriptionFile);
		}
		if (languageFile!=null){
			languages.add(languageFile);
		}
		if (statRelsFile!=null){
			statedRels.add(statRelsFile);
		}
	}
	public OntologyFileRequest getBuildConfig() {
		return buildConfig;
	}
	public void setBuildConfig(OntologyFileRequest buildConfig) {
		this.buildConfig = buildConfig;
	}

	public static void concatFile(HashSet<String> files, File outputFile) {
		HashSet<File>ffiles=new HashSet<File>();
		for (String file:files){
			ffiles.add(new File(file));
		}
		CommonUtils.concatFile(ffiles, outputFile);
	}
}
