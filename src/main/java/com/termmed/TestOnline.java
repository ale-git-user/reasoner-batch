package com.termmed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import com.termmed.reasoner.model.Online;

@Path("server")
public class TestOnline {
	Online ol=new Online();
	Gson gson=new Gson();
	
	@Path("online")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String testOnline(final String document) {
			return gson.toJson(ol);
	}
	
}
