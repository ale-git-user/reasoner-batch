package com.termmed;

import java.util.TimerTask;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.termmed.reasoner.model.OntologyFileRequest;

public class BuildControlTask extends TimerTask {

	private static final String SERVER_BUILDS_PATH = "/api/server/classifier/ontologybuilds/";
	private String buildId;
	private String releaseServer;
	private I_Task task;
	
	@Override
	public void run() {
		WebTarget target;
		Client c = ClientBuilder.newClient();
		System.out.println("connecting to " + releaseServer + SERVER_BUILDS_PATH + buildId);
		target = c.target(releaseServer  );
		Builder req = target.path(SERVER_BUILDS_PATH + buildId).request(MediaType.APPLICATION_JSON_TYPE);
		Response response=req.get();
		String jsonTask=response.readEntity(String.class);
		System.out.println("Response from server" + jsonTask);
		
		task.execute(jsonTask);

	}


	public String getBuildId() {
		return buildId;
	}


	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}


	public String getReleaseServer() {
		return releaseServer;
	}


	public void setReleaseServer(String releaseServer) {
		this.releaseServer = releaseServer;
	}


	public I_Task getTask() {
		return task;
	}


	public void setTask(I_Task task) {
		this.task = task;
	}

}
