package com.termmed;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.termmed.reasoner.model.OntologyFileRequest;
import org.snomed.otf.owltoolkit.service.ReasonerServiceException;

public class LoadDeltaOntology implements I_Task {

	@Override
	public void execute(String json) {
		Gson gson=new Gson();
		try {
			OntologyFileRequest ontoReq = (OntologyFileRequest)gson.fromJson (json,  Class.forName("com.termmed.reasoner.model.OntologyFileRequest"));
			String status=ontoReq.getStatus();
			if (status.equals("2") 
					&& !ReasonerServer.isDeltaDownloaded()
					&& !ReasonerServer.isDeltaDownloading()){
				ReasonerServer.setDeltaDownloading(true);
				FileManagerServer.downloadFile(ontoReq);
			}
			if (status.equals("2") 
					&& ReasonerServer.isDeltaDownloaded()
					&& ReasonerServer.isBaselineLoaded()){
				FileManagerServer.setOntologyBuildTimerOff();
//				FileManagerServer.downloadFile(ontoReq);

				ReasonerServer.loadDeltaOntology(ontoReq.getFilename());
				FileManagerServer.removeTimer();
				ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
			}else if (status.equals("3")){
				FileManagerServer.setOntologyBuildTimerOff();
				FileManagerServer.removeTimer();
				ServerStatus.setStatusMessage(ServerStatus.STATUS.ERROR, "Error: cannot get owl delta from db");
			}
		} catch (JsonSyntaxException | ClassNotFoundException | OWLOntologyCreationException | ReasonerServiceException e) {
			e.printStackTrace();
			ServerStatus.setStatusMessage(ServerStatus.STATUS.ERROR, "Error: cannot get owl delta from db");
		}
	}
}
