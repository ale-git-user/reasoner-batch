package com.termmed;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Timer;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.termmed.reasoner.model.ErrMessage;
import com.termmed.reasoner.model.OntologyFileRequest;


public class FileManagerServer {

	private static final String API_ONTOLOGY_BUILD_PATH = "/api/server/classifier/ontologybuild";
	private static final String ONTO_DELTA_FILE_PATH = "releaseDelta.zip";
	private static final String ONTO_FILE_PATH = "releaseBaseline.zip";
	private static final String COMPLETE_ONTOLOGY = "complete";
	private static final String DELTA_ONTOLOGY = "delta";
	private static final String LAST_DELTA_BUILDCONFIG_FILE = "deltaBuildConfig.json";
	static Gson gson=new Gson();
	private static BuildControlTask buildControlTask;
	static Timer timer;
	private static Timer timer2;
	private static boolean baselineCreated;
	private static Timer baselineTimer;

	public static ErrMessage requestNewOntologyFileBuild(JsonObject document) throws JsonSyntaxException, ClassNotFoundException {
		if (timer==null){
			WebTarget target;
			OntologyFileRequest buildConfig = (OntologyFileRequest) gson.fromJson(document, Class.forName("com.termmed.reasoner.model.OntologyFileRequest"));
			Client c = ClientBuilder.newClient();
			String filename=buildConfig.getFilename();
			if (filename==null || filename.trim().equals("")){
				buildConfig.setFilename(ONTO_FILE_PATH);
			}
			System.out.println("connecting to " + buildConfig.getReleaseServer());
			String fileKey="snomedCT/dev/" + buildConfig.getDbTarget() + "/" + buildConfig.getCollectionTarget() + "/" + buildConfig.getPath().getPathId() + "/ontology/" + buildConfig.getFilename();
			buildConfig.setFileKey(fileKey);
			String releaseServer="http://" + buildConfig.getReleaseServer();
			target = c.target(releaseServer );
			Builder req = target.path(API_ONTOLOGY_BUILD_PATH).request(MediaType.APPLICATION_JSON_TYPE);
			Response response=req.post(Entity.entity(gson.toJson( buildConfig).toString(),MediaType.APPLICATION_JSON_TYPE));
			String taskId=response.readEntity(String.class);
			taskId=taskId.replaceAll("\"", "");
			System.out.println("task id in release server:" + taskId);

			launchControlTask(releaseServer, taskId,COMPLETE_ONTOLOGY);
			return new ErrMessage(0, "");
		}else{
			return new ErrMessage(500, "The server is busy. Previous request Id in process:" + buildControlTask.getBuildId());
		}
	}

	private static void createBaselineAndLoadit(JsonObject document) throws JsonSyntaxException, ClassNotFoundException{
		try{
			if (baselineTimer==null){
				ReasonerServer.setBaselineLoaded(false);
				OntologyFileRequest buildConfig = (OntologyFileRequest) gson.fromJson(document, Class.forName("com.termmed.reasoner.model.OntologyFileRequest"));
//				TODO if it's necesary get String[] with paths to files and set buildConfig
//				buildConfig.setFilename(ReasonerServer.getPathToBaselineZipFolder());
				buildConfig.setFileKey(ReasonerServer.getPreffix());
				BuildBaselineTask buildBaselineTask=new BuildBaselineTask();
				buildBaselineTask.setBuildConfig(buildConfig);

				baselineTimer=new Timer();
				baselineTimer.schedule(buildBaselineTask, 100l, 100l);

			}
		}catch(Exception e){
			ServerStatus.setStatusMessage(ServerStatus.STATUS.ERROR, "Error: cannot create baseline from files:" + e.getMessage());
		}
	}

	private static void launchControlTask(String releaseServer, String taskId, String taskType) {
		if (timer==null){
			buildControlTask=new BuildControlTask();
			buildControlTask.setBuildId(taskId);
			buildControlTask.setReleaseServer(releaseServer);
			if (taskType.equals(COMPLETE_ONTOLOGY)){
				buildControlTask.setTask(new ReloadOntology());
			}else if(taskType.equals(DELTA_ONTOLOGY)){
				buildControlTask.setTask(new LoadDeltaOntology());
			}
			timer=new Timer();
			timer.schedule(buildControlTask, 10000l, 10000l);

		}
	}

	public static void setOntologyBuildTimerOff() {
		timer.cancel();
	}

	public static void downloadFile(OntologyFileRequest ontoReq) {
		BucketGetFile bucket=new BucketGetFile();
		bucket.setBucketName(ontoReq.getBucketName());
		bucket.setAccessKey(ontoReq.getAccessKeyId());
		bucket.setSecretAccessKey(ontoReq.getSecretAccessKey());
		bucket.setTargetDirectory(new File("."));
		bucket.setFileKey(ontoReq.getFileKey());
		String ontoZipFile= ontoReq.getFilename();
		if (!ontoReq.getFilename().endsWith(".zip")) {
			ontoZipFile += ".zip";
		}
		bucket.setFile(ontoZipFile);
		try {
			bucket.execute();
//			File ontoFile=new File(ontoZipFile);
//			Utils.unzip(ontoFile, new File("."));
//			ontoFile.delete();
			ReasonerServer.setDeltaDownloaded(true);
			ReasonerServer.setDeltaDownloading(false);
		} catch (Exception e) {
			e.printStackTrace();
			ReasonerServer.setDeltaDownloaded(false);
			ReasonerServer.setDeltaDownloading(false);
			FileManagerServer.deleteBuildConfig();
			ServerStatus.setStatusMessage(ServerStatus.STATUS.ERROR, "Error(Exception): cannot download file from aws:" + e.getMessage());
		}
		bucket=null;

	}

	private static void deleteBuildConfig() {

		File buildConfig=new File(LAST_DELTA_BUILDCONFIG_FILE);
		if (buildConfig.exists()){
			buildConfig.delete();
		}
	}

	public static void removeTimer() {
		timer=null;
	}

	public static ErrMessage requestLoadOntologyDeltaFileBuild(JsonObject document)throws JsonSyntaxException, ClassNotFoundException  {
		if (timer==null){
			ReasonerServer.setDeltaDownloaded(false);
			WebTarget target;
			OntologyFileRequest buildConfig = (OntologyFileRequest) gson.fromJson(document, Class.forName("com.termmed.reasoner.model.OntologyFileRequest"));
			Client c = ClientBuilder.newClient();
			String filename=buildConfig.getFilename();
			if (filename==null || filename.trim().equals("")){
				buildConfig.setFilename(ONTO_DELTA_FILE_PATH);
			}
			System.out.println("connecting to " + buildConfig.getReleaseServer());
			String fileKey="snomedCT/dev/" + buildConfig.getDbTarget() + "/" + buildConfig.getCollectionTarget() + "/" + buildConfig.getPath().getPathId() + "/ontology/" + buildConfig.getFilename();
			buildConfig.setFileKey(fileKey);
			String releaseServer="http://" + buildConfig.getReleaseServer();
			target = c.target(releaseServer );
			Builder req = target.path(API_ONTOLOGY_BUILD_PATH).request(MediaType.APPLICATION_JSON_TYPE);
			Response response=req.post(Entity.entity(gson.toJson( buildConfig).toString(),MediaType.APPLICATION_JSON_TYPE));
			String taskId=response.readEntity(String.class);
			taskId=taskId.replaceAll("\"", "");
			System.out.println("task id in release server:" + taskId);

			saveBuildConfig(buildConfig);
			launchControlTask(releaseServer, taskId,DELTA_ONTOLOGY);
			return new ErrMessage(0, "");
		}else{
			return new ErrMessage(500, "The server is busy. Previous request Id in process:" + buildControlTask.getBuildId());
		}
	}

	private static void saveBuildConfig(OntologyFileRequest buildConfig) {

		Gson gsonBuilder=new GsonBuilder().setPrettyPrinting().create();

		FileOutputStream tfos;
		try {
			tfos = new FileOutputStream( LAST_DELTA_BUILDCONFIG_FILE);
			OutputStreamWriter tfosw = new OutputStreamWriter(tfos,"UTF-8");
			BufferedWriter bw=new BufferedWriter(tfosw);

			bw.append(gsonBuilder.toJson(buildConfig).toString());

			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ErrMessage buildOntologyFromRelease(JsonObject jo) throws JsonSyntaxException, ClassNotFoundException {
		if (timer==null){
			createBaselineAndLoadit(jo);

			return new ErrMessage(0, "");
		}else{
			return new ErrMessage(500, "The server is busy. Previous request Id in process:" + buildControlTask.getBuildId());
		}

	}

	public static boolean isBaselineCreated() {
		return baselineCreated;
	}

	public static void setBaselineCreated(boolean baselineCreated) {
		FileManagerServer.baselineCreated = baselineCreated;
	}

	public static void setOntologyBuildBaselineTimerOff() {
		baselineTimer.cancel();

	}

	public static void removeBaselineTimer() {
		baselineTimer=null;		
	}

	public static String getLastDeltaBuildconfigFile() {
		return LAST_DELTA_BUILDCONFIG_FILE;
	}

	public static ErrMessage loadLastDelta() {
		ErrMessage err=null;
		File buildConfig=new File(FileManagerServer.getLastDeltaBuildconfigFile());
		if (buildConfig.exists()){

			FileInputStream rfis;
			try {
				rfis = new FileInputStream(buildConfig);
				InputStreamReader risr = new InputStreamReader(rfis,"UTF-8");
				BufferedReader rbr = new BufferedReader(risr);
				StringBuffer sb=new StringBuffer();
				String line;
				while((line=rbr.readLine())!=null){
					sb.append(line);
				}
				rbr.close();

				JsonElement element = gson.fromJson (sb.toString(), JsonElement.class);

				JsonObject jo = element.getAsJsonObject();

				err = FileManagerServer.requestLoadOntologyDeltaFileBuild(jo);
				return err;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				err=new ErrMessage(1001, e.getMessage());
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				err=new ErrMessage(1002, e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
				err=new ErrMessage(1003, e.getMessage());
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
				err=new ErrMessage(1004, e.getMessage());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				err=new ErrMessage(1005, e.getMessage());
			}
		}		
		return err;
	}
}
