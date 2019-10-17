package com.termmed;

import java.io.FileNotFoundException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringEscapeUtils;
import org.semanticweb.owlapi.io.OWLOntologyInputSourceException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.termmed.reasoner.model.ConceptDefinitions;
import com.termmed.reasoner.model.ConceptList;
import com.termmed.reasoner.model.ErrMessage;
import com.termmed.reasoner.model.QueryResults;
import org.snomed.otf.owltoolkit.service.ReasonerServiceException;

@Path("testreasoner")
public class TestReasoner {

	@Path("reload")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String loadOntology() {
		TestMemory.updateMemUsedAtThisMoment("In loadOntology");
		Gson gson=new Gson();
		try {
			ReasonerServer.reloadOntology();
			TestMemory.getMemRetainedAtThisMoment("Out ");
			return gson.toJson(new ErrMessage(0, ""));
		} catch (OWLOntologyCreationException | OWLOntologyInputSourceException | FileNotFoundException | ReasonerServiceException e) {
			e.printStackTrace();
			return gson.toJson(new ErrMessage(500, e.getMessage()));
		}
	}
	@Path("preffix")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getIRIPreffix() {

		TestMemory.updateMemUsedAtThisMoment("In getIRIPreffix");
		String ret=ReasonerServer.getPreffix();
		TestMemory.getMemRetainedAtThisMoment("Out ");
		return ret;
	}

	@Path("/concepts/{id}/subclasses")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getSubClasses(@PathParam("id") final String id,
			@QueryParam("direct") final String directChildren,
			@QueryParam("skip") final String skip,
			@QueryParam("limit") final String limit) {
		TestMemory.updateMemUsedAtThisMoment("In getSubClasses");
		boolean direct=false;
		if (directChildren!=null && directChildren.toLowerCase().trim().equals("true")){
			direct=true;
		}
		Integer iskip=0;
		if (skip!=null){
			iskip=Integer.parseInt(skip);
		}
		Integer ilimit=0;
		if (limit!=null){
			ilimit=Integer.parseInt(limit);
		}
		ConceptList list= ReasonerServer.getSubClasses(id,direct,iskip,ilimit);
		Gson gson=new Gson();

		TestMemory.getMemRetainedAtThisMoment("Out ");
		return gson.toJson(list);
	}

	@Path("/concepts/{id}/superclasses")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getSuperClasses(@PathParam("id") final String id,
			@QueryParam("direct") final String directParent,
			@QueryParam("skip") final String skip,
			@QueryParam("limit") final String limit) {
		TestMemory.updateMemUsedAtThisMoment("In getSuperClasses");
		boolean direct=false;
		if (directParent!=null && directParent.toLowerCase().equals("true")){
			direct=true;
		}
		Integer iskip=0;
		if (skip!=null){
			iskip=Integer.parseInt(skip);
		}
		Integer ilimit=0;
		if (limit!=null){
			ilimit=Integer.parseInt(limit);
		}
		ConceptList list= ReasonerServer.getSuperClasses(id,direct,iskip,ilimit);
		Gson gson=new Gson();
		TestMemory.getMemRetainedAtThisMoment("Out ");
		return gson.toJson(list);
	}

	@Path("/concepts/{id}/equivalentclasses")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getEquivalentClasses(@PathParam("id") final String id,
			@QueryParam("skip") final String skip,
			@QueryParam("limit") final String limit) {

		TestMemory.updateMemUsedAtThisMoment("In getEquivalentClasses");
		Integer iskip=0;
		if (skip!=null){
			iskip=Integer.parseInt(skip);
		}
		Integer ilimit=0;
		if (limit!=null){
			ilimit=Integer.parseInt(limit);
		}
		ConceptList list= ReasonerServer.getEquivalentClasses(id,iskip,ilimit);
		Gson gson=new Gson();
		TestMemory.getMemRetainedAtThisMoment("Out ");
		return gson.toJson(list);
	}

	@Path("/loaddelta")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response requestLoadOntologyDeltaFileBuild() {
		String document= "/Users/ar/dev/git/reasoner-server-live-owl-refsets/reasoner-batch/delta/WBRP_US1000124_RELEASE_DELTA_20180901.zip";
		Gson gson=new Gson();
		if (ServerStatus.isIdle()){
			ServerStatus.setStatusMessage(ServerStatus.STATUS.BUSY, "Building delta ontology file");
		}else{
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(gson.toJson(ServerStatus.getStatusMessage())).build();
		}
		TestMemory.updateMemUsedAtThisMoment("In requestNewOntologyFileBuild");
		try {
			ReasonerServer.loadDeltaOntology(document);

			TestMemory.getMemRetainedAtThisMoment("Out ");
//			ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
			return Response.status(Response.Status.OK).entity(gson.toJson(new ErrMessage(0, ""))).build();
		} catch (JsonSyntaxException | OWLOntologyCreationException | ReasonerServiceException e) {
			e.printStackTrace();
			ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(gson.toJson(new ErrMessage(500, e.getMessage()))).build();
		}
	}
	@Path("/concepts")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String addAxioms(final String document) {
		TestMemory.updateMemUsedAtThisMoment("In addAxioms");
		Gson gson=new Gson();
		try {
			JsonElement element = gson.fromJson (document, JsonElement.class);

			JsonObject jo = element.getAsJsonObject();

			QueryResults results = ReasonerServer.addAxioms(jo);

			TestMemory.getMemRetainedAtThisMoment("Out ");
			return gson.toJson(results);
		} 
		catch (JsonSyntaxException | OWLOntologyCreationException | ClassNotFoundException e) {
			e.printStackTrace();
			return gson.toJson(new ErrMessage(500, e.getMessage()));
		}
	}
	@Path("/concepts/manchester")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String addAxiomsInMS(final String document) {
		TestMemory.updateMemUsedAtThisMoment("In addAxioms");
		Gson gson=new Gson();
		try {
			JsonElement element = gson.fromJson (document, JsonElement.class);

			JsonObject jo = element.getAsJsonObject();

			ReasonerServer.addAxiomsInMS(jo);

			TestMemory.getMemRetainedAtThisMoment("Out ");
			return gson.toJson(new ErrMessage(0, ""));
		} 
		catch (JsonSyntaxException | OWLOntologyCreationException | ClassNotFoundException e) {
			e.printStackTrace();
			return gson.toJson(new ErrMessage(500, e.getMessage()));
		}
	}

	@GET
	@Path("/concepts/remove/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public String removeClass(@PathParam("id") final String id,
			@QueryParam("classify") final String classify) {
		TestMemory.updateMemUsedAtThisMoment("In removeClass");
		//		System.out.println("IN removeClass");
		Gson gson=new Gson();
		try {
			ReasonerServer.removeClass(id,classify);
			//			System.out.println("OUT removeClass");
			TestMemory.getMemRetainedAtThisMoment("Out ");
			return gson.toJson(new ErrMessage(0, ""));
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			return gson.toJson(new ErrMessage(500, e.getMessage()));
		}
	}

	@Path("/dlquery")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String dlquery(final String document) {
		TestMemory.updateMemUsedAtThisMoment("In dlquery");
		Gson gson=new Gson();
		try {

			JsonElement element = gson.fromJson (document, JsonElement.class);
			JsonObject jo = element.getAsJsonObject();
			QueryResults results = ReasonerServer.dlquery(jo);
			TestMemory.getMemRetainedAtThisMoment("Out ");
			return gson.toJson(results);
		} catch (JsonSyntaxException | OWLOntologyCreationException | ClassNotFoundException e) {
			e.printStackTrace();
			return gson.toJson(new ErrMessage(500, e.getMessage()));
		}
	}

	@Path("/concepts/removeaxioms")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String removeAxioms(final String document,
			@QueryParam("classify") final String classify) {
		TestMemory.updateMemUsedAtThisMoment("In removeAxioms");
		Gson gson=new Gson();
		try {
			JsonElement element = gson.fromJson (document, JsonElement.class);
			JsonObject jo = element.getAsJsonObject();
			ReasonerServer.removeAxioms(jo,classify);
			TestMemory.getMemRetainedAtThisMoment("Out ");
			return gson.toJson(new ErrMessage(0, ""));
		} catch (JsonSyntaxException | OWLOntologyCreationException | ClassNotFoundException e) {
			e.printStackTrace();
			return gson.toJson(new ErrMessage(500, e.getMessage()));
		}
	}

	@Path("/stats")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getOntologyStats() {
		TestMemory.updateMemUsedAtThisMoment("In getOntologyStats");
		String ret=ReasonerServer.getOntologyStats();
		TestMemory.getMemRetainedAtThisMoment("Out ");
		return ret;
	}

	@Path("/classify")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String classify() {
		TestMemory.updateMemUsedAtThisMoment("In classify");
		Gson gson=new Gson();
		ReasonerServer.preComputeInferences();
		TestMemory.getMemRetainedAtThisMoment("Out ");
		return gson.toJson(new ErrMessage(0, ""));
	}

	@Path("/unsatisfiableclasses")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getUnsatisfiableClasses() {
		TestMemory.updateMemUsedAtThisMoment("In getUnsatisfiableClasses");
		Gson gson=new Gson();
		TestMemory.getMemRetainedAtThisMoment("Out ");
		String ret=gson.toJson(ReasonerServer.getUnsatisfiableClasses());
		return ret;
	}

	@Path("/concepts/{idDescendant}/iskindof/{idAncestor}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String isKindOf(@PathParam("idAncestor") final String idAncestor,
			@PathParam("idDescendant") final String idDescendant) {
		TestMemory.updateMemUsedAtThisMoment("In isKindOf");
		String ret="{'entailed':" + String.valueOf(ReasonerServer.isKindOf(idAncestor,idDescendant)) + "}";
		TestMemory.getMemRetainedAtThisMoment("Out ");
		return ret;
	}

	@Path("/concepts/{id}/superclasseswithisas")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getSuperClassesWithIsAs(@PathParam("id") final String id,
			@QueryParam("skip") final String skip,
			@QueryParam("limit") final String limit) {
		TestMemory.updateMemUsedAtThisMoment("In getSuperClassesWithIsAs");
		Gson gson=new Gson();
		Integer iskip=0;
		if (skip!=null){
			iskip=Integer.parseInt(skip);
		}
		Integer ilimit=0;
		if (limit!=null){
			ilimit=Integer.parseInt(limit);
		}
		String ret= gson.toJson(ReasonerServer.getSuperClassesWithIsAs(id,iskip,ilimit));
		TestMemory.getMemRetainedAtThisMoment("Out ");
		return ret;
	}

	@Path("/consistent")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String consistent() {
		TestMemory.updateMemUsedAtThisMoment("In consistent");
		String ret=String.valueOf(ReasonerServer.isConsistent());
		TestMemory.getMemRetainedAtThisMoment("Out ");
		return ret;
	}

	@Path("/checkontology")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String checkOntology() {
		TestMemory.updateMemUsedAtThisMoment("In checkOntology");
		String ret= String.valueOf(ReasonerServer.checkOntology());
		TestMemory.getMemRetainedAtThisMoment("Out ");
		return ret;
	}


	@Path("/concepts/explanation/{idDescendant}/subclassof/{idAncestor}")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getSubClassExplanation(@PathParam("idAncestor") final String idAncestor,
			@PathParam("idDescendant") final String idDescendant)  {
		TestMemory.updateMemUsedAtThisMoment("In getSubClassExplanation");

		String expl=ReasonerServer.getSubClassExplanation(idAncestor,idDescendant);
		String ret=StringEscapeUtils.escapeHtml(expl);
		//		ret=ret.replaceAll("\r\n", "<br>");
		//		ret=ret.replaceAll("\t", "&nbsp;&nbsp;&nbsp");
		TestMemory.getMemRetainedAtThisMoment("Out ");
		return  ret;
	}

	@Path("/testexplain")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String textExplain() {

		String expl=ReasonerServer.testExplain();
		String ret=StringEscapeUtils.escapeHtml(expl);
		ret=ret.replaceAll("\r\n", "<br>");
		ret=ret.replaceAll("\t", "&nbsp;&nbsp;&nbsp");
		return  "<html><body>" + ret + "</body></html>";
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
//			conceptDefinitions.setDefinitions("<?xml version=\"1.0\"?>\n" +
//					"<!DOCTYPE Ontology><Ontology xmlns=\"http://www.w3.org/2002/07/owl#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xml:base=\"http://www.w3.org/2002/07/owl#\">\n" +
//					"    <Prefix name=\"\" IRI=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\"/>\n" +
//					"    <Prefix name=\"owl\" IRI=\"http://www.w3.org/2002/07/owl#\"/>\n" +
//					"    <Prefix name=\"xsd\" IRI=\"http://www.w3.org/2001/XMLSchema#\"/>\n" +
//					"    <Prefix name=\"rdf\" IRI=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"/>\n" +
//					"    <Prefix name=\"rdfs\" IRI=\"http://www.w3.org/2000/01/rdf-schema#\"/>" +
//					"  <Declaration>" +
//					"<ObjectProperty IRI=\"http://www.termspace.com/conceptsOwlComplete#255234002\"/>" +
//					"</Declaration>" +
//					"<AnnotationAssertion>" +
//					"<AnnotationProperty abbreviatedIRI=\"rdfs:label\"/>" +
//					"<IRI>http://www.termspace.com/conceptsOwlComplete#255234002</IRI>" +
//					"<Literal datatypeIRI=\"rdf:PlainLiteral\">AFTER</Literal>" +
//					"</AnnotationAssertion>" +
//					"<SubObjectPropertyOf>" +
//					"<ObjectProperty IRI=\"http://www.termspace.com/conceptsOwlComplete#255234002\"/>" +
//					"<ObjectProperty IRI=\"http://www.termspace.com/conceptsOwlComplete#47429007\"/>" +
//					"</SubObjectPropertyOf></Ontology>\n");
						conceptDefinitions.setDefinitions("<?xml version=\"1.0\"?>\n" +
								"<!DOCTYPE Ontology><Ontology xmlns=\"http://www.w3.org/2002/07/owl#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xml:base=\"http://www.w3.org/2002/07/owl#\">\n" +
								"    <Prefix name=\"\" IRI=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\"/>\n" +
								"    <Prefix name=\"owl\" IRI=\"http://www.w3.org/2002/07/owl#\"/>\n" +
								"    <Prefix name=\"xsd\" IRI=\"http://www.w3.org/2001/XMLSchema#\"/>\n" +
								"    <Prefix name=\"rdf\" IRI=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"/>\n" +
								"    <Prefix name=\"rdfs\" IRI=\"http://www.w3.org/2000/01/rdf-schema#\"/>" +
								"  <Declaration>\n" +
								"    <Class IRI=\"http://www.termspace.com/conceptsOwlComplete#38771504530335\"/>\n" +
								"  </Declaration>\n" +
								"  <AnnotationAssertion>\n" +
								"    <AnnotationProperty abbreviatedIRI=\"rdfs:label\"/>\n" +
								"    <IRI>http://www.termspace.com/conceptsOwlComplete#38771504530335</IRI>\n" +
								"    <Literal datatypeIRI=\"&amp;rdf;PlainLiteral\">Abdominal wall procedure (procedure)</Literal>\n" +
								"  </AnnotationAssertion>\n" +
								"  <EquivalentClasses>\n" +
								"    <Class IRI=\"http://www.termspace.com/conceptsOwlComplete#38771504530335\"/>\n" +
								"    <ObjectIntersectionOf>\n" +
								"      <Class IRI=\"http://www.termspace.com/conceptsOwlComplete#118698009\"/>\n" +
								"      <ObjectSomeValuesFrom>\n" +
								"        <ObjectProperty IRI=\"http://www.termspace.com/conceptsOwlComplete#609096000\"/>\n" +
								"        <ObjectSomeValuesFrom>\n" +
								"          <ObjectProperty IRI=\"http://www.termspace.com/conceptsOwlComplete#363704007\"/>\n" +
								"          <Class IRI=\"http://www.termspace.com/conceptsOwlComplete#83908009\"/>\n" +
								"        </ObjectSomeValuesFrom>\n" +
								"      </ObjectSomeValuesFrom>\n" +
								"    </ObjectIntersectionOf>\n" +
								"  </EquivalentClasses></Ontology>\n");

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

