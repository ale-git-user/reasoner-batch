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
import org.snomed.otf.owltoolkit.ontology.OntologyService;
import org.snomed.otf.owltoolkit.service.ReasonerServiceException;

@Path("reasoner")
public class Reasoner {
	private Gson gson;
	public Reasoner(){
		gson=new Gson();
	}
	@Path("reload")
	@GET
//	@Produces(MediaType.APPLICATION_JSON)
	public Response loadOntology() {
		if (ServerStatus.isIdle()){
			ServerStatus.setStatusMessage(ServerStatus.STATUS.BUSY, "Loading ontology");
		}else{
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(gson.toJson(ServerStatus.getStatusMessage())).build();
		}
		TestMemory.updateMemUsedAtThisMoment("In loadOntology");
		try {
			ReasonerServer.reloadOntology();
			TestMemory.getMemRetainedAtThisMoment("Out ");
			ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
			return Response.status(Response.Status.OK).entity(gson.toJson(new ErrMessage(0, ""))).build();
		} catch (OWLOntologyCreationException | OWLOntologyInputSourceException | ReasonerServiceException | FileNotFoundException e ) {
			e.printStackTrace();
			ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(gson.toJson(new ErrMessage(500, e.getMessage()))).build();
		}
    }
	@Path("preffix")
	@GET
//	@Produces(MediaType.TEXT_PLAIN)
	public Response getIRIPreffix() {

		TestMemory.updateMemUsedAtThisMoment("In getIRIPreffix");
		String ret=ReasonerServer.getPreffix();
		TestMemory.getMemRetainedAtThisMoment("Out ");
		return Response.status(Response.Status.OK).entity(ret).build();
	}
	
	@Path("/concepts/{id}/subclasses")
	@GET
//	@Produces(MediaType.APPLICATION_JSON)
	public Response getSubClasses(@PathParam("id") final String id,
			@QueryParam("direct") final String directChildren,
			@QueryParam("skip") final String skip,
			@QueryParam("limit") final String limit) {

		if (ServerStatus.isIdle()){
			ServerStatus.setStatusMessage(ServerStatus.STATUS.BUSY, "Getting subclasses");
		}else{
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(gson.toJson(ServerStatus.getStatusMessage())).build();
		}
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

		TestMemory.getMemRetainedAtThisMoment("Out ");
		ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
		return Response.status(Response.Status.OK).entity(gson.toJson(list)).build();
	}

	@Path("/concepts/{id}/superclasses")
	@GET
//	@Produces(MediaType.APPLICATION_JSON)
	public Response getSuperClasses(@PathParam("id") final String id,
			@QueryParam("direct") final String directParent,
			@QueryParam("skip") final String skip,
			@QueryParam("limit") final String limit) {

		if (ServerStatus.isIdle()){
			ServerStatus.setStatusMessage(ServerStatus.STATUS.BUSY, "Getting superclasses");
		}else{
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(gson.toJson(ServerStatus.getStatusMessage())).build();
		}
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
		TestMemory.getMemRetainedAtThisMoment("Out ");
		ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
		return Response.status(Response.Status.OK).entity(gson.toJson(list)).build();
	}

	@Path("/concepts/{id}/equivalentclasses")
	@GET
//	@Produces(MediaType.APPLICATION_JSON)
	public Response getEquivalentClasses(@PathParam("id") final String id,
			@QueryParam("skip") final String skip,
			@QueryParam("limit") final String limit) {

		if (ServerStatus.isIdle()){
			ServerStatus.setStatusMessage(ServerStatus.STATUS.BUSY, "Getting equivalent classes");
		}else{
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(gson.toJson(ServerStatus.getStatusMessage())).build();
		}
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
		TestMemory.getMemRetainedAtThisMoment("Out ");
		ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
		return Response.status(Response.Status.OK).entity(gson.toJson(list)).build();
	}

	@Path("/concepts")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
//	@Produces(MediaType.APPLICATION_JSON)
	public Response addAxioms(final String document) {
		if (ServerStatus.isIdle()){
			ServerStatus.setStatusMessage(ServerStatus.STATUS.BUSY, "Adding axioms");
		}else{
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(gson.toJson(ServerStatus.getStatusMessage())).build();
		}
		TestMemory.updateMemUsedAtThisMoment("In addAxioms");
		try {
			JsonElement element = gson.fromJson (document, JsonElement.class);

			JsonObject jo = element.getAsJsonObject();

			QueryResults results = ReasonerServer.addAxioms(jo);

			TestMemory.getMemRetainedAtThisMoment("Out ");
			ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
			return Response.status(Response.Status.OK).entity(gson.toJson(results)).build();
		} 
		catch (JsonSyntaxException | OWLOntologyCreationException | ClassNotFoundException e) {
			e.printStackTrace();
			ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(gson.toJson(new ErrMessage(500, e.getMessage()))).build();
		}
	}
	@Path("/concepts/manchester")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
//	@Produces(MediaType.APPLICATION_JSON)
	public Response addAxiomsInMS(final String document) {
		if (ServerStatus.isIdle()){
			ServerStatus.setStatusMessage(ServerStatus.STATUS.BUSY, "Adding Manchester syntax axioms");
		}else{
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(gson.toJson(ServerStatus.getStatusMessage())).build();
		}
		TestMemory.updateMemUsedAtThisMoment("In addAxioms");
		
		try {
			JsonElement element = gson.fromJson (document, JsonElement.class);
			
			JsonObject jo = element.getAsJsonObject();
			
			ReasonerServer.addAxiomsInMS(jo);
			
			TestMemory.getMemRetainedAtThisMoment("Out ");
			ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
			return Response.status(Response.Status.OK).entity(gson.toJson(new ErrMessage(0, ""))).build();
		} 
		catch (JsonSyntaxException | OWLOntologyCreationException | ClassNotFoundException e) {
			e.printStackTrace();
			ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(gson.toJson(new ErrMessage(500, e.getMessage()))).build();
		}
	}

	@GET
	@Path("/concepts/remove/{id}")
//	@Produces(MediaType.APPLICATION_JSON)
	public Response removeClass(@PathParam("id") final String id,
			@QueryParam("classify") final String classify) {

		if (ServerStatus.isIdle()){
			ServerStatus.setStatusMessage(ServerStatus.STATUS.BUSY, "Removing classes");
		}else{
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(gson.toJson(ServerStatus.getStatusMessage())).build();
		}
		TestMemory.updateMemUsedAtThisMoment("In removeClass");
		try {
			ReasonerServer.removeClass(id,classify);
//			System.out.println("OUT removeClass");
			TestMemory.getMemRetainedAtThisMoment("Out ");
			ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
			return Response.status(Response.Status.OK).entity(gson.toJson(new ErrMessage(0, ""))).build();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(gson.toJson(new ErrMessage(500, e.getMessage()))).build();
		}
	}

	@Path("/dlquery")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
//	@Produces(MediaType.APPLICATION_JSON)
	public Response dlquery(final String document) {

		if (ServerStatus.isIdle()){
			ServerStatus.setStatusMessage(ServerStatus.STATUS.BUSY, "Processing dl query");
		}else{
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(gson.toJson(ServerStatus.getStatusMessage())).build();
		}
		TestMemory.updateMemUsedAtThisMoment("In dlquery");
		try {

			JsonElement element = gson.fromJson (document, JsonElement.class);
			JsonObject jo = element.getAsJsonObject();
			QueryResults results = ReasonerServer.dlquery(jo);
			TestMemory.getMemRetainedAtThisMoment("Out ");
			ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
			return Response.status(Response.Status.OK).entity(gson.toJson(results)).build();
		} catch (JsonSyntaxException | OWLOntologyCreationException | ClassNotFoundException e) {
			e.printStackTrace();
			ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(gson.toJson(new ErrMessage(500, e.getMessage()))).build();
		}
	}

	@Path("/concepts/removeaxioms")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
//	@Produces(MediaType.APPLICATION_JSON)
	public Response removeAxioms(final String document,
			@QueryParam("classify") final String classify) {

		if (ServerStatus.isIdle()){
			ServerStatus.setStatusMessage(ServerStatus.STATUS.BUSY, "Removing axioms");
		}else{
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(gson.toJson(ServerStatus.getStatusMessage())).build();
		}
		TestMemory.updateMemUsedAtThisMoment("In removeAxioms");
		try {
			JsonElement element = gson.fromJson (document, JsonElement.class);
			JsonObject jo = element.getAsJsonObject();
			ReasonerServer.removeAxioms(jo,classify);
			TestMemory.getMemRetainedAtThisMoment("Out ");
			ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
			return Response.status(Response.Status.OK).entity(gson.toJson(new ErrMessage(0, ""))).build();
		} catch (JsonSyntaxException | OWLOntologyCreationException | ClassNotFoundException e) {
			e.printStackTrace();
			ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(gson.toJson(new ErrMessage(500, e.getMessage()))).build();
		}
	}

	@Path("/stats")
	@GET
//	@Produces(MediaType.TEXT_PLAIN)
	public Response getOntologyStats() {
		TestMemory.updateMemUsedAtThisMoment("In getOntologyStats");
		String ret=ReasonerServer.getOntologyStats();
		TestMemory.getMemRetainedAtThisMoment("Out ");
		ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
		return Response.status(Response.Status.OK).entity(ret).build();
	}
	
	@Path("/classify")
	@GET
//	@Produces(MediaType.APPLICATION_JSON)
	public Response classify() {

		if (ServerStatus.isIdle()){
			ServerStatus.setStatusMessage(ServerStatus.STATUS.BUSY, "Classifying");
		}else{
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(gson.toJson(ServerStatus.getStatusMessage())).build();
		}
		TestMemory.updateMemUsedAtThisMoment("In classify");
		ReasonerServer.preComputeInferences();
		TestMemory.getMemRetainedAtThisMoment("Out ");
		ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
		return Response.status(Response.Status.OK).entity(gson.toJson(new ErrMessage(0, ""))).build();
	}

	@Path("/unsatisfiableclasses")
	@GET
//	@Produces(MediaType.APPLICATION_JSON)
	public Response getUnsatisfiableClasses() {

		if (ServerStatus.isIdle()){
			ServerStatus.setStatusMessage(ServerStatus.STATUS.BUSY, "Getting unsatisfiable classes");
		}else{
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(gson.toJson(ServerStatus.getStatusMessage())).build();
		}
		TestMemory.updateMemUsedAtThisMoment("In getUnsatisfiableClasses");
		TestMemory.getMemRetainedAtThisMoment("Out ");
		String ret=gson.toJson(ReasonerServer.getUnsatisfiableClasses());
		ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
		return Response.status(Response.Status.OK).entity(ret).build();
	}

	@Path("/concepts/{idDescendant}/iskindof/{idAncestor}")
	@GET
//	@Produces(MediaType.APPLICATION_JSON)
	public Response isKindOf(@PathParam("idAncestor") final String idAncestor,
			@PathParam("idDescendant") final String idDescendant) {

		if (ServerStatus.isIdle()){
			ServerStatus.setStatusMessage(ServerStatus.STATUS.BUSY, "Calling isKindOf function");
		}else{
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(gson.toJson(ServerStatus.getStatusMessage())).build();
		}
		TestMemory.updateMemUsedAtThisMoment("In isKindOf");
		String ret="{'entailed':" + String.valueOf(ReasonerServer.isKindOf(idAncestor,idDescendant)) + "}";
		TestMemory.getMemRetainedAtThisMoment("Out ");
		ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
		return Response.status(Response.Status.OK).entity(ret).build();
	}

	@Path("/concepts/{id}/superclasseswithisas")
	@GET
//	@Produces(MediaType.APPLICATION_JSON)
	public Response getSuperClassesWithIsAs(@PathParam("id") final String id,
			@QueryParam("skip") final String skip,
			@QueryParam("limit") final String limit) {

		if (ServerStatus.isIdle()){
			ServerStatus.setStatusMessage(ServerStatus.STATUS.BUSY, "Getting superclasses with isas");
		}else{
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(gson.toJson(ServerStatus.getStatusMessage())).build();
		}
		TestMemory.updateMemUsedAtThisMoment("In getSuperClassesWithIsAs");
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
		ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
		return Response.status(Response.Status.OK).entity(ret).build();
	}

	@Path("/consistent")
	@GET
//	@Produces(MediaType.TEXT_PLAIN)
	public Response consistent() {
		if (ServerStatus.isIdle()){
			ServerStatus.setStatusMessage(ServerStatus.STATUS.BUSY, "Checking ontology consistency");
		}else{
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(gson.toJson(ServerStatus.getStatusMessage())).build();
		}
		TestMemory.updateMemUsedAtThisMoment("In consistent");
		String ret=String.valueOf(ReasonerServer.isConsistent());
		TestMemory.getMemRetainedAtThisMoment("Out ");
		ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
		return Response.status(Response.Status.OK).entity(ret).build();
	}

	@Path("/checkontology")
	@GET
//	@Produces(MediaType.TEXT_PLAIN)
	public Response checkOntology() {
		if (ServerStatus.isIdle()){
			ServerStatus.setStatusMessage(ServerStatus.STATUS.BUSY, "Checking ontology");
		}else{
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(gson.toJson(ServerStatus.getStatusMessage())).build();
		}
		TestMemory.updateMemUsedAtThisMoment("In checkOntology");
		String ret= String.valueOf(ReasonerServer.checkOntology());
		TestMemory.getMemRetainedAtThisMoment("Out ");
		ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
		return Response.status(Response.Status.OK).entity(ret).build();
	}
	

	@Path("/concepts/explanation/{idDescendant}/subclassof/{idAncestor}")
	@GET
//	@Produces(MediaType.TEXT_PLAIN)
	public Response getSubClassExplanation(@PathParam("idAncestor") final String idAncestor,
			@PathParam("idDescendant") final String idDescendant)  {
		if (ServerStatus.isIdle()){
			ServerStatus.setStatusMessage(ServerStatus.STATUS.BUSY, "Getting subclasses explanation");
		}else{
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(gson.toJson(ServerStatus.getStatusMessage())).build();
		}
		TestMemory.updateMemUsedAtThisMoment("In getSubClassExplanation");
		
		String expl=ReasonerServer.getSubClassExplanation(idAncestor,idDescendant);
		String ret=StringEscapeUtils.escapeHtml(expl);
//		ret=ret.replaceAll("\r\n", "<br>");
//		ret=ret.replaceAll("\t", "&nbsp;&nbsp;&nbsp");
		TestMemory.getMemRetainedAtThisMoment("Out ");
		ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
		return Response.status(Response.Status.OK).entity(ret).build();
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
//    "<ObjectProperty IRI=\"" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "255234002\"/>" +
//  "</Declaration>" +
//  "<AnnotationAssertion>" +
//    "<AnnotationProperty abbreviatedIRI=\"rdfs:label\"/>" +
//    "<IRI>" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "255234002</IRI>" +
//    "<Literal datatypeIRI=\"rdf:PlainLiteral\">AFTER</Literal>" +
//  "</AnnotationAssertion>" +
//  "<SubObjectPropertyOf>" +
//    "<ObjectProperty IRI=\"" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "255234002\"/>" +
//    "<ObjectProperty IRI=\"" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "47429007\"/>" +
//  "</SubObjectPropertyOf></Ontology>\n");

			conceptDefinitions.setDefinitions("<?xml version=\"1.0\"?>\n" +
							"<!DOCTYPE Ontology [\n" +
							"<!ENTITY xsd \"http://www.w3.org/2001/XMLSchema#\" >\n" +
							"<!ENTITY xml \"http://www.w3.org/XML/1998/namespace\" >\n" +
							"<!ENTITY rdfs \"http://www.w3.org/2000/01/rdf-schema#\" >\n" +
							"<!ENTITY rdf \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" >\n" +
							"]>\n" +
							"<Ontology xmlns=\"http://www.w3.org/2002/07/owl#\"\n" +
							"xml:base=\"http://snomed.info/id/\"\n" +
							"xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
							"xmlns:xml=\"http://www.w3.org/XML/1998/namespace\"\n" +
							"xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n" +
							"xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n" +
							"ontologyIRI=\"" + OntologyService.SNOMED_INTERNATIONAL_EDITION_URI + "\">\n" +
							"    <Prefix name=\"owl\" IRI=\"http://www.w3.org/2002/07/owl#\"/>\n" +
							"    <Prefix name=\"xsd\" IRI=\"http://www.w3.org/2001/XMLSchema#\"/>\n" +
							"    <Prefix name=\"xml\" IRI=\"http://www.w3.org/XML/1998/namespace\"/>\n" +
							"    <Prefix name=\"rdf\" IRI=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"/>\n" +
							"    <Prefix name=\"rdfs\" IRI=\"http://www.w3.org/2000/01/rdf-schema#\"/>\n" +
//					"<?xml version=\"1.0\"?>\n" +
//					"<!DOCTYPE Ontology><Ontology xmlns=\"http://www.w3.org/2002/07/owl#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xml:base=\"http://www.w3.org/2002/07/owl#\">\n" +
//					"    <Prefix name=\"\" IRI=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\"/>\n" +
//					"    <Prefix name=\"owl\" IRI=\"http://www.w3.org/2002/07/owl#\"/>\n" +
//					"    <Prefix name=\"xsd\" IRI=\"http://www.w3.org/2001/XMLSchema#\"/>\n" +
//					"    <Prefix name=\"rdf\" IRI=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"/>\n" +
//					"    <Prefix name=\"rdfs\" IRI=\"http://www.w3.org/2000/01/rdf-schema#\"/>" +
					"  <Declaration>\n" +
					"    <Class IRI=\"" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "38771504530335\"/>\n" +
					"  </Declaration>\n" +
					"  <AnnotationAssertion>\n" +
					"    <AnnotationProperty abbreviatedIRI=\"rdfs:label\"/>\n" +
					"    <IRI>" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "38771504530335</IRI>\n" +
					"    <Literal datatypeIRI=\"&amp;rdf;PlainLiteral\">Abdominal wall procedure (procedure)</Literal>\n" +
					"  </AnnotationAssertion>\n" +
					"  <EquivalentClasses>\n" +
					"    <Class IRI=\"" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "38771504530335\"/>\n" +
					"    <ObjectIntersectionOf>\n" +
					"      <Class IRI=\"" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "118698009\"/>\n" +
					"      <ObjectSomeValuesFrom>\n" +
					"        <ObjectProperty IRI=\"" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "609096000\"/>\n" +
					"        <ObjectSomeValuesFrom>\n" +
					"          <ObjectProperty IRI=\"" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "363704007\"/>\n" +
					"          <Class IRI=\"" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "83908009\"/>\n" +
					"        </ObjectSomeValuesFrom>\n" +
					"      </ObjectSomeValuesFrom>\n" +
					"    </ObjectIntersectionOf>\n" +
					"  </EquivalentClasses></Ontology>\n");


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

	@Path("/testaxiomwithprevious")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String textAxiomWithPrevious() {

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
//    "<ObjectProperty IRI=\"" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "255234002\"/>" +
//  "</Declaration>" +
//  "<AnnotationAssertion>" +
//    "<AnnotationProperty abbreviatedIRI=\"rdfs:label\"/>" +
//    "<IRI>" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "255234002</IRI>" +
//    "<Literal datatypeIRI=\"rdf:PlainLiteral\">AFTER</Literal>" +
//  "</AnnotationAssertion>" +
//  "<SubObjectPropertyOf>" +
//    "<ObjectProperty IRI=\"" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "255234002\"/>" +
//    "<ObjectProperty IRI=\"" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "47429007\"/>" +
//  "</SubObjectPropertyOf></Ontology>\n");

			conceptDefinitions.setDefinitions("<?xml version=\"1.0\"?>\n" +
					"<!DOCTYPE Ontology [\n" +
					"<!ENTITY xsd \"http://www.w3.org/2001/XMLSchema#\" >\n" +
					"<!ENTITY xml \"http://www.w3.org/XML/1998/namespace\" >\n" +
					"<!ENTITY rdfs \"http://www.w3.org/2000/01/rdf-schema#\" >\n" +
					"<!ENTITY rdf \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" >\n" +
					"]>\n" +
					"<Ontology xmlns=\"http://www.w3.org/2002/07/owl#\"\n" +
					"xml:base=\"http://snomed.info/id/\"\n" +
					"xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
					"xmlns:xml=\"http://www.w3.org/XML/1998/namespace\"\n" +
					"xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n" +
					"xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n" +
					"ontologyIRI=\"" + OntologyService.SNOMED_INTERNATIONAL_EDITION_URI + "\">\n" +
					"    <Prefix name=\"owl\" IRI=\"http://www.w3.org/2002/07/owl#\"/>\n" +
					"    <Prefix name=\"xsd\" IRI=\"http://www.w3.org/2001/XMLSchema#\"/>\n" +
					"    <Prefix name=\"xml\" IRI=\"http://www.w3.org/XML/1998/namespace\"/>\n" +
					"    <Prefix name=\"rdf\" IRI=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"/>\n" +
					"    <Prefix name=\"rdfs\" IRI=\"http://www.w3.org/2000/01/rdf-schema#\"/>\n" +
//					"<?xml version=\"1.0\"?>\n" +
//					"<!DOCTYPE Ontology><Ontology xmlns=\"http://www.w3.org/2002/07/owl#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xml:base=\"http://www.w3.org/2002/07/owl#\">\n" +
//					"    <Prefix name=\"\" IRI=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\"/>\n" +
//					"    <Prefix name=\"owl\" IRI=\"http://www.w3.org/2002/07/owl#\"/>\n" +
//					"    <Prefix name=\"xsd\" IRI=\"http://www.w3.org/2001/XMLSchema#\"/>\n" +
//					"    <Prefix name=\"rdf\" IRI=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"/>\n" +
//					"    <Prefix name=\"rdfs\" IRI=\"http://www.w3.org/2000/01/rdf-schema#\"/>" +
					"  <Declaration>\n" +
					"    <Class IRI=\"" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "38771504530335\"/>\n" +
					"  </Declaration>\n" +
					"  <AnnotationAssertion>\n" +
					"    <AnnotationProperty abbreviatedIRI=\"rdfs:label\"/>\n" +
					"    <IRI>" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "38771504530335</IRI>\n" +
					"    <Literal datatypeIRI=\"&amp;rdf;PlainLiteral\">Abdominal wall procedure (procedure)</Literal>\n" +
					"  </AnnotationAssertion>\n" +
					"  <EquivalentClasses>\n" +
					"    <Class IRI=\"" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "38771504530335\"/>\n" +
					"    <ObjectIntersectionOf>\n" +
					"      <Class IRI=\"" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "118698009\"/>\n" +
					"      <ObjectSomeValuesFrom>\n" +
					"        <ObjectProperty IRI=\"" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "609096000\"/>\n" +
					"        <ObjectSomeValuesFrom>\n" +
					"          <ObjectProperty IRI=\"" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "363704007\"/>\n" +
					"          <Class IRI=\"" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "83908009\"/>\n" +
					"        </ObjectSomeValuesFrom>\n" +
					"      </ObjectSomeValuesFrom>\n" +
					"    </ObjectIntersectionOf>\n" +
					"  </EquivalentClasses></Ontology>\n");

			conceptDefinitions.setPreviousResults(true);
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
	@Path("/testaxiomprim")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String textAxiomPrim() {

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
//    "<ObjectProperty IRI=\"" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "255234002\"/>" +
//  "</Declaration>" +
//  "<AnnotationAssertion>" +
//    "<AnnotationProperty abbreviatedIRI=\"rdfs:label\"/>" +
//    "<IRI>" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "255234002</IRI>" +
//    "<Literal datatypeIRI=\"rdf:PlainLiteral\">AFTER</Literal>" +
//  "</AnnotationAssertion>" +
//  "<SubObjectPropertyOf>" +
//    "<ObjectProperty IRI=\"" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "255234002\"/>" +
//    "<ObjectProperty IRI=\"" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "47429007\"/>" +
//  "</SubObjectPropertyOf></Ontology>\n");

			conceptDefinitions.setDefinitions("<?xml version=\"1.0\"?>\n" +
					"<!DOCTYPE Ontology [\n" +
					"<!ENTITY xsd \"http://www.w3.org/2001/XMLSchema#\" >\n" +
					"<!ENTITY xml \"http://www.w3.org/XML/1998/namespace\" >\n" +
					"<!ENTITY rdfs \"http://www.w3.org/2000/01/rdf-schema#\" >\n" +
					"<!ENTITY rdf \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" >\n" +
					"]>\n" +
					"<Ontology xmlns=\"http://www.w3.org/2002/07/owl#\"\n" +
					"xml:base=\"http://snomed.info/id/\"\n" +
					"xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
					"xmlns:xml=\"http://www.w3.org/XML/1998/namespace\"\n" +
					"xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n" +
					"xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n" +
					"ontologyIRI=\"" + OntologyService.SNOMED_INTERNATIONAL_EDITION_URI + "\">\n" +
					"    <Prefix name=\"owl\" IRI=\"http://www.w3.org/2002/07/owl#\"/>\n" +
					"    <Prefix name=\"xsd\" IRI=\"http://www.w3.org/2001/XMLSchema#\"/>\n" +
					"    <Prefix name=\"xml\" IRI=\"http://www.w3.org/XML/1998/namespace\"/>\n" +
					"    <Prefix name=\"rdf\" IRI=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"/>\n" +
					"    <Prefix name=\"rdfs\" IRI=\"http://www.w3.org/2000/01/rdf-schema#\"/>\n" +
//					"<?xml version=\"1.0\"?>\n" +
//					"<!DOCTYPE Ontology><Ontology xmlns=\"http://www.w3.org/2002/07/owl#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xml:base=\"http://www.w3.org/2002/07/owl#\">\n" +
//					"    <Prefix name=\"\" IRI=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\"/>\n" +
//					"    <Prefix name=\"owl\" IRI=\"http://www.w3.org/2002/07/owl#\"/>\n" +
//					"    <Prefix name=\"xsd\" IRI=\"http://www.w3.org/2001/XMLSchema#\"/>\n" +
//					"    <Prefix name=\"rdf\" IRI=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"/>\n" +
//					"    <Prefix name=\"rdfs\" IRI=\"http://www.w3.org/2000/01/rdf-schema#\"/>" +
//					"  <Declaration>\n" +
//					"    <Class IRI=\"" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "38771504530335\"/>\n" +
//					"  </Declaration>\n" +
//					"  <AnnotationAssertion>\n" +
//					"    <AnnotationProperty abbreviatedIRI=\"rdfs:label\"/>\n" +
//					"    <IRI>" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "38771504530335</IRI>\n" +
//					"    <Literal datatypeIRI=\"&amp;rdf;PlainLiteral\">Abdominal wall procedure (procedure)</Literal>\n" +
//					"  </AnnotationAssertion>\n" +
					"  <SubClassOf>\n" +
					"    <Class IRI=\"" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "38771504530335\"/>\n" +
					"      <Class IRI=\"" + OntologyService.SNOMED_CORE_COMPONENTS_URI + "118698009\"/>\n" +
					"  </SubClassOf></Ontology>\n");


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

