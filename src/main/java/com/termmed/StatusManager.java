package com.termmed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.semanticweb.owlapi.io.OWLOntologyInputSourceException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.termmed.reasoner.model.ConceptDefinitions;
import com.termmed.reasoner.model.ErrMessage;
import com.termmed.reasoner.model.StatusMessage;

@Path("servermanager")
public class StatusManager {
	Gson gson;
	public StatusManager(){
		gson=new Gson();
	}
	@Path("status")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getStatus() {
		TestMemory.updateMemUsedAtThisMoment("In getStatus");
		try {
			StatusMessage status=ServerStatus.getStatusMessage();
			TestMemory.getMemRetainedAtThisMoment("Out ");
			return gson.toJson(status);
		} catch (OWLOntologyInputSourceException  e) {
			e.printStackTrace();
			return gson.toJson(new ErrMessage(500, e.getMessage()));
		}
	}
	@Path("statusreset")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getStatusReset() {

		TestMemory.updateMemUsedAtThisMoment("In getStatusReset");
		StatusMessage status=ServerStatus.getStatusReset();
		TestMemory.getMemRetainedAtThisMoment("Out ");
		return gson.toJson(status);
	}


	@Path("/changepath")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String setChange()  {
		//		TestMemory.updateMemUsedAtThisMoment("In getSubClassExplanation");
		try {
			TestMemory.updateMemUsedAtThisMoment("In getSubClassExplanation");
			long t1 = System.currentTimeMillis();
			ReasonerPoolServer.changeToPath("1","451841000124107");
			long t2 = System.currentTimeMillis();
			System.out.println("iter 1 - Path 1 Loaded (" + (t2-t1) + " ms.)");

			TestMemory.getMemRetainedAtThisMoment("Out ");

			t1 = System.currentTimeMillis();
			ReasonerPoolServer.changeToPath("2","451781000124106");
			t2 = System.currentTimeMillis();
			System.out.println("iter 2 - Path 2 Loaded (" + (t2-t1) + " ms.)");

			TestMemory.getMemRetainedAtThisMoment("Out ");

			t1 = System.currentTimeMillis();
			ReasonerPoolServer.changeToPath("1","451841000124107");
			t2 = System.currentTimeMillis();
			System.out.println("iter 3 - Path 1 Loaded (" + (t2-t1) + " ms.)");

			TestMemory.getMemRetainedAtThisMoment("Out ");
			t1 = System.currentTimeMillis();
			ReasonerPoolServer.changeToPath("2","451781000124106");
			t2 = System.currentTimeMillis();
			System.out.println("iter 4 - Path 2 Loaded (" + (t2-t1) + " ms.)");

			TestMemory.getMemRetainedAtThisMoment("Out ");

			t1 = System.currentTimeMillis();
			ReasonerPoolServer.changeToPath("1","451841000124107");
			t2 = System.currentTimeMillis();
			System.out.println("iter 5 - Path 1 Loaded (" + (t2-t1) + " ms.)");

			TestMemory.getMemRetainedAtThisMoment("Out ");
			t1 = System.currentTimeMillis();
			ReasonerPoolServer.changeToPath("2","451781000124106");
			t2 = System.currentTimeMillis();
			System.out.println("iter 6 - Path 2 Loaded (" + (t2-t1) + " ms.)");

			TestMemory.getMemRetainedAtThisMoment("Out ");

			t1 = System.currentTimeMillis();
			ReasonerPoolServer.changeToPath("1","451841000124107");
			t2 = System.currentTimeMillis();
			System.out.println("iter 7 - Path 1 Loaded (" + (t2-t1) + " ms.)");
			TestMemory.getMemRetainedAtThisMoment("Out ");

			t1 = System.currentTimeMillis();
			ReasonerPoolServer.changeToPath("2","451781000124106");
			t2 = System.currentTimeMillis();
			System.out.println("iter 8 - Path 2 Loaded (" + (t2-t1) + " ms.)");

			TestMemory.getMemRetainedAtThisMoment("Out ");

			t1 = System.currentTimeMillis();
			ReasonerPoolServer.changeToPath("1","451841000124107");
			t2 = System.currentTimeMillis();
			System.out.println("iter 9 - Path 1 Loaded (" + (t2-t1) + " ms.)");

			TestMemory.getMemRetainedAtThisMoment("Out ");

			t1 = System.currentTimeMillis();

			ReasonerPoolServer.changeToPath("2","451781000124106");

			t2 = System.currentTimeMillis();
			System.out.println("iter 10 - Path 2 Loaded (" + (t2-t1) + " ms.)");

			TestMemory.getMemRetainedAtThisMoment("Out ");
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "end test";
	}
	@Path("/changepathandclassify")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String setChangeAndClassify()  {
		//		TestMemory.updateMemUsedAtThisMoment("In getSubClassExplanation");
		try {
			TestMemory.updateMemUsedAtThisMoment("In getSubClassExplanation");
			long t1 = System.currentTimeMillis();
			ReasonerPoolServer.changeToPathAndClassify("1","451841000124107","451781000124106");
			long t2 = System.currentTimeMillis();
			System.out.println("iter 1 - Path 1 Loaded (" + (t2-t1) + " ms.)");

			TestMemory.getMemRetainedAtThisMoment("Out ");

			t1 = System.currentTimeMillis();
			ReasonerPoolServer.changeToPathAndClassify("2","451781000124106","451841000124107");
			t2 = System.currentTimeMillis();
			System.out.println("iter 2 - Path 2 Loaded (" + (t2-t1) + " ms.)");

			TestMemory.getMemRetainedAtThisMoment("Out ");

			t1 = System.currentTimeMillis();
			ReasonerPoolServer.changeToPathAndClassify("1","451841000124107","451781000124106");
			t2 = System.currentTimeMillis();
			System.out.println("iter 3 - Path 1 Loaded (" + (t2-t1) + " ms.)");

			TestMemory.getMemRetainedAtThisMoment("Out ");
			t1 = System.currentTimeMillis();
			ReasonerPoolServer.changeToPathAndClassify("2","451781000124106","451841000124107");
			t2 = System.currentTimeMillis();
			System.out.println("iter 4 - Path 2 Loaded (" + (t2-t1) + " ms.)");

			TestMemory.getMemRetainedAtThisMoment("Out ");

			t1 = System.currentTimeMillis();
			ReasonerPoolServer.changeToPathAndClassify("1","451841000124107","451781000124106");
			t2 = System.currentTimeMillis();
			System.out.println("iter 5 - Path 1 Loaded (" + (t2-t1) + " ms.)");

			TestMemory.getMemRetainedAtThisMoment("Out ");
			t1 = System.currentTimeMillis();
			ReasonerPoolServer.changeToPathAndClassify("2","451781000124106","451841000124107");
			t2 = System.currentTimeMillis();
			System.out.println("iter 6 - Path 2 Loaded (" + (t2-t1) + " ms.)");

			TestMemory.getMemRetainedAtThisMoment("Out ");

			t1 = System.currentTimeMillis();
			ReasonerPoolServer.changeToPathAndClassify("1","451841000124107","451781000124106");
			t2 = System.currentTimeMillis();
			System.out.println("iter 7 - Path 1 Loaded (" + (t2-t1) + " ms.)");
			TestMemory.getMemRetainedAtThisMoment("Out ");

			t1 = System.currentTimeMillis();
			ReasonerPoolServer.changeToPathAndClassify("2","451781000124106","451841000124107");
			t2 = System.currentTimeMillis();
			System.out.println("iter 8 - Path 2 Loaded (" + (t2-t1) + " ms.)");

			TestMemory.getMemRetainedAtThisMoment("Out ");

			t1 = System.currentTimeMillis();
			ReasonerPoolServer.changeToPathAndClassify("1","451841000124107","451781000124106");
			t2 = System.currentTimeMillis();
			System.out.println("iter 9 - Path 1 Loaded (" + (t2-t1) + " ms.)");

			TestMemory.getMemRetainedAtThisMoment("Out ");

			t1 = System.currentTimeMillis();

			ReasonerPoolServer.changeToPathAndClassify("2","451781000124106","451841000124107");

			t2 = System.currentTimeMillis();
			System.out.println("iter 10 - Path 2 Loaded (" + (t2-t1) + " ms.)");

			TestMemory.getMemRetainedAtThisMoment("Out ");
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "end test";
	}
	@Path("/testaxiom")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String textAxiom() {

		try {
			ConceptDefinitions conceptDefinitions=new ConceptDefinitions();
			conceptDefinitions.setDefinitions("<?xml version=\"1.0\"?>\n" +
					"<!DOCTYPE Ontology><Ontology xmlns=\"http://www.w3.org/2002/07/owl#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xml:base=\"http://www.w3.org/2002/07/owl#\">\n" +
					"    <Prefix name=\"\" IRI=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\"/>\n" +
					"    <Prefix name=\"owl\" IRI=\"http://www.w3.org/2002/07/owl#\"/>\n" +
					"    <Prefix name=\"xsd\" IRI=\"http://www.w3.org/2001/XMLSchema#\"/>\n" +
					"    <Prefix name=\"rdf\" IRI=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"/>\n" +
					"    <Prefix name=\"rdfs\" IRI=\"http://www.w3.org/2000/01/rdf-schema#\"/>" +
					"  <Declaration>" +
					"<ObjectProperty IRI=\"http://www.termspace.com/conceptsOwlComplete#255234002\"/>" +
					"</Declaration>" +
					"<AnnotationAssertion>" +
					"<AnnotationProperty abbreviatedIRI=\"rdfs:label\"/>" +
					"<IRI>http://www.termspace.com/conceptsOwlComplete#255234002</IRI>" +
					"<Literal datatypeIRI=\"rdf:PlainLiteral\">AFTER</Literal>" +
					"</AnnotationAssertion>" +
					"<SubObjectPropertyOf>" +
					"<ObjectProperty IRI=\"http://www.termspace.com/conceptsOwlComplete#255234002\"/>" +
					"<ObjectProperty IRI=\"http://www.termspace.com/conceptsOwlComplete#47429007\"/>" +
					"</SubObjectPropertyOf></Ontology>\n");
			//			conceptDefinitions.setDefinitions("<?xml version=\"1.0\"?>\n" +
			//					"<!DOCTYPE Ontology><Ontology xmlns=\"http://www.w3.org/2002/07/owl#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xml:base=\"http://www.w3.org/2002/07/owl#\">\n" +
			//					"    <Prefix name=\"\" IRI=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\"/>\n" +
			//					"    <Prefix name=\"owl\" IRI=\"http://www.w3.org/2002/07/owl#\"/>\n" +
			//					"    <Prefix name=\"xsd\" IRI=\"http://www.w3.org/2001/XMLSchema#\"/>\n" +
			//					"    <Prefix name=\"rdf\" IRI=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"/>\n" +
			//					"    <Prefix name=\"rdfs\" IRI=\"http://www.w3.org/2000/01/rdf-schema#\"/>" +
			//					"  <Declaration>\n" +
			//					"    <Class IRI=\"http://www.termspace.com/conceptsOwlComplete#38771504530335\"/>\n" +
			//					"  </Declaration>\n" +
			//					"  <AnnotationAssertion>\n" +
			//					"    <AnnotationProperty abbreviatedIRI=\"rdfs:label\"/>\n" +
			//					"    <IRI>http://www.termspace.com/conceptsOwlComplete#38771504530335</IRI>\n" +
			//					"    <Literal datatypeIRI=\"&amp;rdf;PlainLiteral\">Abdominal wall procedure (procedure)</Literal>\n" +
			//					"  </AnnotationAssertion>\n" +
			//					"  <EquivalentClasses>\n" +
			//					"    <Class IRI=\"http://www.termspace.com/conceptsOwlComplete#38771504530335\"/>\n" +
			//					"    <ObjectIntersectionOf>\n" +
			//					"      <Class IRI=\"http://www.termspace.com/conceptsOwlComplete#118698009\"/>\n" +
			//					"      <ObjectSomeValuesFrom>\n" +
			//					"        <ObjectProperty IRI=\"http://www.termspace.com/conceptsOwlComplete#609096000\"/>\n" +
			//					"        <ObjectSomeValuesFrom>\n" +
			//					"          <ObjectProperty IRI=\"http://www.termspace.com/conceptsOwlComplete#363704007\"/>\n" +
			//					"          <Class IRI=\"http://www.termspace.com/conceptsOwlComplete#83908009\"/>\n" +
			//					"        </ObjectSomeValuesFrom>\n" +
			//					"      </ObjectSomeValuesFrom>\n" +
			//					"    </ObjectIntersectionOf>\n" +
			//					"  </EquivalentClasses></Ontology>\n");

			Gson gson=new Gson();

			String strjo=gson.toJson(conceptDefinitions);
			JsonElement element = gson.fromJson (strjo, JsonElement.class);
			JsonObject jo = element.getAsJsonObject();
			try {
				ReasonerServer.addAxioms(jo);

			} catch (JsonSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "Got it!";
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Error:"  + e.getMessage();
		}
	}
}

