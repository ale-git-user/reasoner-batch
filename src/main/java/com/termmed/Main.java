package com.termmed;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.semanticweb.owlapi.io.OWLOntologyInputSourceException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.snomed.otf.owltoolkit.ontology.OntologyService;
import org.snomed.otf.owltoolkit.service.ReasonerServiceException;

/**
 * Main class.
 *
 */
public class Main {
	// Base URI the Grizzly HTTP server will listen on
	public static String BASE_URI = "http://localhost:8080/api/";


	/**
	 * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
	 * @return Grizzly HTTP server.
	 */
	public static HttpServer startServer() {
		try {

			Properties prop=new Properties();
			File propFile=new File("reasoner-server.properties");
			if (propFile.exists()){
				prop.load(new FileReader(propFile));
				getProperties(prop);
			}
			ReasonerServer.init();

			// create a resource config that scans for JAX-RS resources and providers
			// in com.termmed package


			final ResourceConfig rc = new ResourceConfig().packages("com.termmed");

			// create and start a new instance of grizzly http server
			// exposing the Jersey application at BASE_URI
			return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
		} catch (OWLOntologyCreationException | OWLOntologyInputSourceException | ReasonerServiceException | IOException e) {
			e.printStackTrace();
		}
	
		return null;
	}

	/**
	 * Main method.
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		ServerStatus.setStatusMessage(ServerStatus.STATUS.BUSY, "Building myself");
		final HttpServer server = startServer();
		if (server!=null){
			System.out.println(String.format("Jersey app started with WADL available at "
					+ "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
			
//			System.in.read();
//			server.stop();
		}else{
			ServerStatus.setStatusMessage(ServerStatus.STATUS.ERROR, "Building myself");	
			System.out.println("Server cannot load ontology file. Config file: reasoner-server.properties"); 
		}
	}

	private static void getProperties(Properties prop) {

		System.out.println("Getting uri properties");
		for(Object key:prop.keySet()){
			if (key.toString().toLowerCase().startsWith("base_uri")){
				BASE_URI=(String)prop.get(key);
			}
		}
	}
}

