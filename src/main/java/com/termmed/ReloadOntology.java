package com.termmed;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;
import com.termmed.reasoner.model.CompleteFilesConfig;
import com.termmed.reasoner.model.LanguageComplete;
import org.semanticweb.owlapi.io.OWLOntologyInputSourceException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.termmed.reasoner.model.OntologyFileRequest;
import org.snomed.otf.owltoolkit.service.ReasonerServiceException;

public class ReloadOntology implements I_Task {

	@Override
	public void execute(String json) {
		Gson gson=new Gson();
		try {
			OntologyFileRequest ontoReq = (OntologyFileRequest)gson.fromJson (json,  Class.forName("com.termmed.reasoner.model.OntologyFileRequest"));
			String status=ontoReq.getStatus();
			if (status.equals("2")){
				FileManagerServer.setOntologyBuildTimerOff();
				FileManagerServer.downloadFile(ontoReq);
				List<String> files=new ArrayList<String>();
				files.add(ontoReq.getFilename());
				List<CompleteFilesConfig> completeFilesConfig=ontoReq.getCompleteFilesConfig();
				if (completeFilesConfig.size()>0){
					List<LanguageComplete> lang_completes = completeFilesConfig.get(0).getLanguage_complete();
					if(lang_completes!=null) {

						for (LanguageComplete lang_complete : lang_completes) {
							if (!Strings.isNullOrEmpty( lang_complete.getSource_folder())) {
								files.add(lang_complete.getSource_folder());
							}
						}
					}
				}
				ReasonerServer.setPathToBaselineZipFolder((String[]) files.toArray());
				try {
					ReasonerServer.reloadOntology();
					ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
				} catch (OWLOntologyInputSourceException e) {
					e.printStackTrace();
				} catch (OWLOntologyCreationException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (ReasonerServiceException e) {
                    e.printStackTrace();
                }
                FileManagerServer.removeTimer();
			}else if (status.equals("3")){
				FileManagerServer.setOntologyBuildTimerOff();
				FileManagerServer.removeTimer();
			}
		} catch (JsonSyntaxException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
