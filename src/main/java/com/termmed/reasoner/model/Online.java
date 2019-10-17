package com.termmed.reasoner.model;

import com.termmed.ReasonerServer;

public class Online {

	Boolean online=true;
	String version="";
	public Online() {
		
		version=ReasonerServer.getVersion();
	}	
	
	
}
