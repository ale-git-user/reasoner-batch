package com.termmed;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.termmed.reasoner.model.ErrMessage;

@Path("filemanager")
public class FileManager {
	Gson gson;
	public FileManager(){
		gson=new Gson();
	}
	@Path("reload")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
//	@Produces(MediaType.APPLICATION_JSON)
	public Response requestNewOntologyFileBuild(final String document) {

		if (ServerStatus.isIdle()){
			ServerStatus.setStatusMessage(ServerStatus.STATUS.BUSY, "Rebuilding full ontology file from db");
		}else{
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(gson.toJson(ServerStatus.getStatusMessage())).build();
		}
		TestMemory.updateMemUsedAtThisMoment("In requestNewOntologyFileBuild");
		
		try {
			JsonElement element = gson.fromJson (document, JsonElement.class);

			JsonObject jo = element.getAsJsonObject();
			
			ErrMessage err=FileManagerServer.requestNewOntologyFileBuild(jo);

			TestMemory.getMemRetainedAtThisMoment("Out ");
//			ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
			return Response.status(Response.Status.OK).entity(gson.toJson(err)).build();
		} catch (  JsonSyntaxException | ClassNotFoundException e) {
			e.printStackTrace();
			ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(gson.toJson(new ErrMessage(500, e.getMessage()))).build();
		}
	}
	@Path("buildontology")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
//	@Produces(MediaType.APPLICATION_JSON)
	public Response buildOntology(final String document) {

		if (ServerStatus.isIdle()){
			ServerStatus.setStatusMessage(ServerStatus.STATUS.BUSY, "Building full ontology from release");
		}else{
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(gson.toJson(ServerStatus.getStatusMessage())).build();
		}
		TestMemory.updateMemUsedAtThisMoment("In requestNewOntologyFileBuild");
		
		try {
			JsonElement element = gson.fromJson (document, JsonElement.class);

			JsonObject jo = element.getAsJsonObject();
			
			ErrMessage err=FileManagerServer.buildOntologyFromRelease(jo);
			if (err.getNumber()==0){
				err=FileManagerServer.requestLoadOntologyDeltaFileBuild(jo);
			}
			
			TestMemory.getMemRetainedAtThisMoment("Out ");
//			ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
			return Response.status(Response.Status.OK).entity(gson.toJson(err)).build();
		} catch (  JsonSyntaxException | ClassNotFoundException e) {
			e.printStackTrace();
			ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(gson.toJson(new ErrMessage(500, e.getMessage()))).build();
		}
	}
	@Path("deltaload")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
//	@Produces(MediaType.APPLICATION_JSON)
	public Response requestLoadOntologyDeltaFileBuild(final String document) {
		if (ServerStatus.isIdle()){
			ServerStatus.setStatusMessage(ServerStatus.STATUS.BUSY, "Building delta ontology file");
		}else{
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(gson.toJson(ServerStatus.getStatusMessage())).build();
		}
		TestMemory.updateMemUsedAtThisMoment("In requestNewOntologyFileBuild");
		try {
			JsonElement element = gson.fromJson (document, JsonElement.class);

			JsonObject jo = element.getAsJsonObject();
			
			ErrMessage err=FileManagerServer.requestLoadOntologyDeltaFileBuild(jo);

			TestMemory.getMemRetainedAtThisMoment("Out ");
//			ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
			return Response.status(Response.Status.OK).entity(gson.toJson(err)).build();
		} catch (  JsonSyntaxException | ClassNotFoundException e) {
			e.printStackTrace();
			ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(gson.toJson(new ErrMessage(500, e.getMessage()))).build();
		}
	}
}
