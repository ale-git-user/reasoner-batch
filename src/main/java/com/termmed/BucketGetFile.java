package com.termmed;

import java.io.File;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.util.StringInputStream;

/**
 * @author Alejandro Rodriguez
 * 
 */
public class BucketGetFile {

	/**
	 * Location of the build directory.
	 * 
	 */
	private File targetDirectory;

	/**
	 * Location of the destination file.
	 * 
	 */
	private String file;

	/**
	 * Name of the bucket.
	 * 
	 */
	private String bucketName;

	/**
	 * Key of file in the bucket.
	 * 
	 */
	private String fileKey;

	/**
	 * file to save metadata
	 * 
	 */
	private String accessKey;

	/**
	 *  secret access key
	 * 
	 */
	private String secretAccessKey;

	private int failcount;
	
	public static void main(String[] args) throws Exception {
		BucketGetFile gobj=new BucketGetFile();
		//		gobj.setBucketName("termspace-releases");
		//		gobj.setFile("/Users/ar/Downloads/es_release/es_snapshot_20160430.zip");
		//		gobj.setFileKey("TSRP_ES_RELEASE_SNAPSHOT_20160430");
		////		gobj.setFile("/Users/ar/Downloads/uruguay/uy_delta_20160215.zip");
		////		gobj.setFileKey("DEV_UY_RELEASE_DELTA_20160215");
		////		gobj.setFileKey("WBRP_INT_EXTRACTION_DELTA_20150207220403");
		////		gobj.setMetadataKey("org.ihtsdo.terminologyReleaseCatalog");
		////		gobj.setMetadataToFile("/Users/ar/Downloads/delta_meta_last7.json");
		//		//		gobj.setFile("/Users/ar/Downloads/full_20150130.zip");
		//		//		gobj.setFileKey("WBRP_INT_DAILY_FULL_LAST");
		//		
		//		gobj.setTargetDirectory(new File("."));
		//		gobj.execute();
		gobj=new BucketGetFile();
		gobj.setBucketName("termspace-releases");
		gobj.setFile("/Users/ar/Downloads/es_release/es_full_20160430.zip");
		//		gobj.setFileKey("DEV_UY_RELEASE_FULL_20160215");
		gobj.setFileKey("TSRP_ES_RELEASE_FULL_20160430");
		//		gobj.setMetadataKey("org.ihtsdo.terminologyReleaseCatalog");
		//		gobj.setMetadataToFile("/Users/ar/Downloads/delta_meta_last7.json");
		//		gobj.setFile("/Users/ar/Downloads/full_20150130.zip");
		//		gobj.setFileKey("WBRP_INT_DAILY_FULL_LAST");
		//		
		gobj.setTargetDirectory(new File("."));
		gobj.execute();
		gobj=new BucketGetFile();
		gobj.setBucketName("termspace-releases");
		gobj.setFile("/Users/ar/Downloads/es_release/es_delta_20160430.zip");
		//		gobj.setFile("/Users/ar/Downloads/uruguay/uy_snapshot_20160215.zip");
		//		gobj.setFileKey("DEV_UY_RELEASE_SNAPSHOT_20160215");
		gobj.setFileKey("TSRP_ES_RELEASE_DELTA_20160430");
		//		gobj.setMetadataKey("org.ihtsdo.terminologyReleaseCatalog");
		//		gobj.setMetadataToFile("/Users/ar/Downloads/delta_meta_last7.json");
		//		gobj.setFile("/Users/ar/Downloads/full_20150130.zip");
		//		gobj.setFileKey("WBRP_INT_DAILY_FULL_LAST");

		gobj.setTargetDirectory(new File("."));
		gobj.execute();
	}
	public void execute() throws Exception {

		PropertiesCredentials pc;
		try {
			//	            credentials = new ProfileCredentialsProvider().getCredentials();

			if (secretAccessKey!=null && !secretAccessKey.trim().equals("")
					&& accessKey!=null && !accessKey.trim().equals("")){
				pc = new PropertiesCredentials(new StringInputStream("secretKey=" + secretAccessKey + "\naccessKey=" + accessKey));
			}else{
				File s3credsFile = new File("AwsCredentials.properties");
				pc = new PropertiesCredentials(s3credsFile);
			}
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. " +
							"Please make sure that your credentials file is at the correct " +
							"location (~/.aws/credentials), and is in valid format.",
							e);
		}
		ObjectMetadata object=null ;
		AmazonS3 s3;
		try {
			s3 = new AmazonS3Client(pc);
			object = s3.getObject(new GetObjectRequest(bucketName,fileKey),new File(file) );

		} catch (Exception e) {
			e.printStackTrace();
			if (failcount==3){
				throw new Exception("There was a problem trying to get file: key=" + fileKey + " - file: " + file );
			}else{
				failcount++;
				System.out.println("********************************************");
				System.out.println("There was a problem trying to get file: key=" + fileKey + " - file: " + file + ". #Fail: " + failcount + "\nTrying again...");
				execute();
			}
		}

		if (object==null){
			throw new Exception("File not found on S3: key= " + fileKey );
		}
		
		s3=null;
	}


	public File getTargetDirectory() {
		return targetDirectory;
	}
	public void setTargetDirectory(File targetDirectory) {
		this.targetDirectory = targetDirectory;
	}
	public String getBucketName() {
		return bucketName;
	}
	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}
	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
	}
	public String getFileKey() {
		return fileKey;
	}
	public void setFileKey(String fileKey) {
		this.fileKey = fileKey;
	}
	public String getAccessKey() {
		return accessKey;
	}
	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}
	public String getSecretAccessKey() {
		return secretAccessKey;
	}
	public void setSecretAccessKey(String secretAccessKey) {
		this.secretAccessKey = secretAccessKey;
	}

}
