package com.termmed;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;
import org.ihtsdo.otf.snomedboot.ReleaseImportException;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.io.OWLOntologyInputSourceException;
import org.semanticweb.owlapi.io.OWLParser;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxOntologyParserFactory;
import org.semanticweb.owlapi.model.*;
//import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.profiles.OWL2DLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.profiles.OWLProfileViolation;
import org.semanticweb.owlapi.reasoner.*;

import com.clarkparsia.owlapi.explanation.DefaultExplanationGenerator;
import com.clarkparsia.owlapi.explanation.util.SilentExplanationProgressMonitor;
import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.termmed.reasoner.model.Concept;
import com.termmed.reasoner.model.ConceptDefinitions;
import com.termmed.reasoner.model.ConceptList;
import com.termmed.reasoner.model.ErrMessage;
import com.termmed.reasoner.model.QueryResults;

import org.snomed.otf.owltoolkit.constants.Concepts;
import org.snomed.otf.owltoolkit.ontology.OntologyService;
import org.snomed.otf.owltoolkit.ontology.PropertyChain;
import org.snomed.otf.owltoolkit.ontology.render.SnomedFunctionalSyntaxDocumentFormat;
import org.snomed.otf.owltoolkit.ontology.render.SnomedPrefixManager;
import org.snomed.otf.owltoolkit.service.ReasonerServiceException;
import org.snomed.otf.owltoolkit.taxonomy.SnomedTaxonomy;
import org.snomed.otf.owltoolkit.taxonomy.SnomedTaxonomyBuilder;
import org.snomed.otf.owltoolkit.util.InputStreamSet;
import org.snomed.otf.owltoolkit.util.TimerUtil;
import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationOrdererImpl;
import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationTree;
import uk.ac.manchester.cs.owl.explanation.ordering.Tree;

import static java.lang.Long.parseLong;
import static org.snomed.otf.owltoolkit.ontology.OntologyService.ONTOLOGY_URI_VERSION_POSTFIX;
import static org.snomed.otf.owltoolkit.ontology.OntologyService.SNOMED_CORE_COMPONENTS_URI;
import static org.snomed.otf.owltoolkit.ontology.OntologyService.SNOMED_INTERNATIONAL_EDITION_URI;

public class ReasonerServer {

//	private static final String IRI_PREFFIX_DEFAULT = "http://o.wl#";
	private static final String BASELINE_ZIP_FOLDER_DEFAULT = "releaseBaseline.zip";

	static OWLOntologyManager manager;
	static OWLDataFactory df ;
	static String[] pathToBaselineZipFolder;
	static OWLOntology ontology;
	static OWLReasoner reasoner;

	static OWLObjectRenderer renderer ;

	static String version;
	static String versionDate;
	static Gson gson;
	private static String IRIpreffix;
	private static IRI IRI;
	private static Pattern myPattern;
	private static String rendererPreffix;
	private static DefaultExplanationGenerator explanationGenerator;
	private static ExplanationOrdererImpl deo;
	private static OWL2DLProfile profile;
	private static boolean baselineLoaded;
	private static boolean deltaDownloaded;
	private static boolean deltaDownloading;
	private static String ELK_REASONER_FACTORY ="org.semanticweb.elk.owlapi.ElkReasonerFactory";
	private static Set<Long> ungroupedRoles;
	private static OntologyService ontologyService;
//	private static OWLDataFactory factory;

	public static void init() throws OWLOntologyCreationException, OWLOntologyInputSourceException, FileNotFoundException, ReasonerServiceException {
		TestMemory.test();
		TestMemory.updateMemUsedAtThisMoment("Init process");
		baselineLoaded=false;
		deltaDownloaded=false;
		deltaDownloading=false;
		Properties prop=new Properties();
		try {
			ReasonerServer.IRIpreffix=OntologyService.SNOMED_CORE_COMPONENTS_URI;
			File propFile=new File("reasoner-server.properties");
			if (propFile.exists()){
				prop.load(new FileReader(propFile));
				getProperties(prop);
			}else{
				ReasonerServer.pathToBaselineZipFolder = new String[]{BASELINE_ZIP_FOLDER_DEFAULT};
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		renderer = new DLSyntaxObjectRenderer();
		manager = OWLManager.createOWLOntologyManager();
		df = manager.getOWLDataFactory();
//		factory = new OWLDataFactoryImpl();
//		rendererPreffix=ReasonerServer.IRIpreffix.substring(ReasonerServer.IRIpreffix.lastIndexOf("/") + 1, ReasonerServer.IRIpreffix.lastIndexOf("#") + 1);

		gson=new Gson();
		myPattern = Pattern.compile("(\\d{1,})");

		setVersion();
		File base=new File(ReasonerServer.pathToBaselineZipFolder[0]);
		if (base.exists()){
			ErrMessage err = FileManagerServer.loadLastDelta();
			if (err==null || err.getNumber()==0){
				loadOntology();
				if (err==null ){
					ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
				}
			}else{
				FileManagerServer.setOntologyBuildTimerOff();
				FileManagerServer.removeTimer();
				ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
			}
		}else{

			ServerStatus.setStatusMessage(ServerStatus.STATUS.IDLE, "");
		}
		TestMemory.getMemRetainedAtThisMoment("Out init process");

		TestMemory.test();

	}

	private static void getProperties(Properties prop) {

		System.out.println("Getting properties");
		int cont=0;
		List<String>filePaths=new ArrayList<String>();
		for(Object key:prop.keySet()){
			if (key.toString().toLowerCase().startsWith("releasefile")){
				cont++;
				filePaths.add((String)prop.get(key));
			}else if (key.toString().toLowerCase().startsWith("versiondate")){
				versionDate=(String)prop.get(key);
			}
		}
		ReasonerServer.pathToBaselineZipFolder=new String[cont];
		filePaths.toArray(ReasonerServer.pathToBaselineZipFolder);
	}

	public static void loadOntology() throws OWLOntologyCreationException, OWLOntologyInputSourceException, FileNotFoundException, ReasonerServiceException {
		long t1 = System.currentTimeMillis();
		System.out.println("Preparing to load SNOMED CT");
//		File snomedComplete =new File(pathToBaselineZipFolder);

//		ontology = manager.loadOntologyFromOntologyDocument(snomedComplete);
		Date startDate = new Date();
		TimerUtil timer = new TimerUtil("Classification");
		System.out.println("Checking requested reasoner is available");
//		OWLReasonerFactory reasonerFactory = getOWLReasonerFactory(ELK_REASONER_FACTORY);
		timer.checkpoint("Create reasoner factory");

		System.out.println("Building snomedTaxonomy");
		SnomedTaxonomyBuilder snomedTaxonomyBuilder = new SnomedTaxonomyBuilder();
		SnomedTaxonomy snomedTaxonomy;

		Set<File> snapshotFiles = new HashSet<File>();
		for(String filePath:pathToBaselineZipFolder) {
			snapshotFiles.add(new File(filePath));
		}

		try {
			InputStreamSet previousReleaseRf2SnapshotArchives = new InputStreamSet(snapshotFiles);
			snomedTaxonomy = snomedTaxonomyBuilder.build(previousReleaseRf2SnapshotArchives, null, true);
		} catch (ReleaseImportException e) {
			throw new ReasonerServiceException("Failed to build existing taxonomy.", e);
		}
		timer.checkpoint("Build existing taxonomy");

		System.out.println("Creating OwlOntology");
		ungroupedRoles = snomedTaxonomy.getUngroupedRolesForContentTypeOrDefault(parseLong(Concepts.ALL_PRECOORDINATED_CONTENT));
		ontologyService = new OntologyService(ungroupedRoles);
		try {
			ontology=createOntology(snomedTaxonomy,manager,null,versionDate);
		} catch (OWLOntologyCreationException e) {
			throw new ReasonerServiceException("Failed to build OWL Ontology.", e);
		}
		timer.checkpoint("Create OWL Ontology");

		Set<PropertyChain> propertyChains = ontologyService.getPropertyChains(ontology);

//		IRI=manager.getOntologyDocumentIRI(ontology);
//		System.out.println("Ontology IRI:" + manager.getOntologyDocumentIRI(ontology));
		System.out.println("Checking requested reasoner is available");
		OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();

		final OWLReasonerConfiguration configuration = new SimpleConfiguration(new ConsoleProgressMonitor());
		reasoner = reasonerFactory.createReasoner(ontology, configuration);
//		reasoner = reasonerFactory.createReasoner(ontology);

		timer.checkpoint("Create reasoner factory");
		explanationGenerator =
				new DefaultExplanationGenerator(
						manager, reasonerFactory, ontology, reasoner, new SilentExplanationProgressMonitor());
		deo = new ExplanationOrdererImpl(manager);

		profile = new OWL2DLProfile();
//		reasoner.flush();
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		long t2 = System.currentTimeMillis();
		System.out.println("Baseline loaded (" + (t2-t1) + " ms.)");
		baselineLoaded=true;

	}
	public static OWLOntology createOntology(SnomedTaxonomy snomedTaxonomy, OWLOntologyManager manager,String ontologyUri, String versionDate) throws OWLOntologyCreationException {

		Map<Long, Set<OWLAxiom>> axiomsFromStatedRelationships = ontologyService.createAxiomsFromStatedRelationships(snomedTaxonomy);

		Set<OWLAxiom> axioms = new HashSet<>();
		for (Long conceptId : snomedTaxonomy.getAllConceptIds()) {

			// Add raw axioms from the axiom reference set file
			axioms.addAll(snomedTaxonomy.getConceptAxiomMap().getOrDefault(conceptId, Collections.emptySet()));

			// Add axioms generated from stated relationships
			axioms.addAll(axiomsFromStatedRelationships.getOrDefault(conceptId, Collections.emptySet()));

			// Add FSN annotation
			addFSNAnnotation(conceptId, snomedTaxonomy, axioms);
		}

		OWLOntology onto;
		if (Strings.isNullOrEmpty(ontologyUri)) {
			ontologyUri = SNOMED_INTERNATIONAL_EDITION_URI;
		}
		if (Strings.isNullOrEmpty(versionDate)) {
			onto = manager.createOntology(IRI.create(ontologyUri));
		} else {
			onto = manager.createOntology(new OWLOntologyID(
					com.google.common.base.Optional.of(IRI.create(ontologyUri)),
					com.google.common.base.Optional.of(IRI.create(ontologyUri + ONTOLOGY_URI_VERSION_POSTFIX + versionDate))));
		}

		manager.addAxioms(onto, axioms);
//		manager.setOntologyFormat(ontology, getFunctionalSyntaxDocumentFormat());
		return onto;
	}

	private static void addFSNAnnotation(Long conceptId, SnomedTaxonomy snomedTaxonomy, Set<OWLAxiom> axioms) {
		String conceptFsnTerm = snomedTaxonomy.getConceptFsnTerm(conceptId);
		if (conceptFsnTerm != null) {
			axioms.add(df.getOWLAnnotationAssertionAxiom(df.getRDFSLabel(), IRI.create(SNOMED_CORE_COMPONENTS_URI + conceptId), df.getOWLLiteral(conceptFsnTerm)));
		}
	}

	public static FunctionalSyntaxDocumentFormat getFunctionalSyntaxDocumentFormat() {
		FunctionalSyntaxDocumentFormat owlDocumentFormat = new SnomedFunctionalSyntaxDocumentFormat();
		SnomedPrefixManager prefixManager = getSnomedPrefixManager();
		owlDocumentFormat.setPrefixManager(prefixManager);
		owlDocumentFormat.setDefaultPrefix(SNOMED_CORE_COMPONENTS_URI);
		return owlDocumentFormat;
	}

	public static SnomedPrefixManager getSnomedPrefixManager() {
		SnomedPrefixManager prefixManager = new SnomedPrefixManager();
		prefixManager.setDefaultPrefix(SNOMED_CORE_COMPONENTS_URI);
		return prefixManager;
	}
	private static OWLReasonerFactory getOWLReasonerFactory(String reasonerFactoryClassName) throws ReasonerServiceException {
		Class<?> reasonerFactoryClass = null;
		try {
			reasonerFactoryClass = Class.forName(reasonerFactoryClassName);
			return (OWLReasonerFactory) reasonerFactoryClass.newInstance();
		} catch (ClassNotFoundException e) {
			throw new ReasonerServiceException(String.format("Requested reasoner class '%s' not found.", reasonerFactoryClassName), e);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ReasonerServiceException(String.format("An instance of requested reasoner '%s' could not be created.", reasonerFactoryClass), e);
		}
	}


	public static void reloadOntology() throws OWLOntologyCreationException, OWLOntologyInputSourceException, FileNotFoundException, ReasonerServiceException {
		if (reasoner!=null){
			reasoner.dispose();
			reasoner=null;
		}
		if (manager!=null && ontology!=null){
			manager.removeOntology(ontology);
		}
		ontology=null;
		manager=null;

		manager = OWLManager.createOWLOntologyManager();
		df = manager.getOWLDataFactory();

		loadOntology();
	}

	public static String checkOntology(){
		System.out.println("Checking with profile");
		OWLProfileReport report = profile.checkOntology(ontology);
		int count = 0;
		StringBuffer ret=new StringBuffer("");
		for (OWLProfileViolation violation : report.getViolations()) {
			System.out.println("    " + violation.toString());
			ret.append("    ");
			ret.append( violation.toString());
			ret.append ("\r\n");
			count++;
			if (count > 10) break;
		}
		report=null;
		return ret.toString();
	}

	public static String getOntologyStats(){

		StringBuffer ret=new StringBuffer("");
		ret.append("Ontology stats");
		ret.append ("\r\n");
		ret.append("ALL: " + ontology.getAxiomCount());
		ret.append ("\r\n");
		ret.append("DECLARATION: " + ontology.getAxiomCount(AxiomType.DECLARATION));
		ret.append ("\r\n");
		ret.append("SUBCLASS_OF: " + ontology.getAxiomCount(AxiomType.SUBCLASS_OF));
		ret.append ("\r\n");
		ret.append("EQUIVALENT_CLASSES: " + ontology.getAxiomCount(AxiomType.EQUIVALENT_CLASSES));
		ret.append ("\r\n");
		ret.append("ANNOTATION_ASSERTION: " + ontology.getAxiomCount(AxiomType.ANNOTATION_ASSERTION));
		ret.append ("\r\n");
		return ret.toString();
	}

	public static void preComputeInferences(){
		reasoner.flush();
		reasoner.precomputeInferences((InferenceType.CLASS_HIERARCHY));
	}

	public static boolean isConsistent(){
		boolean consistent = reasoner.isConsistent();
		return consistent;
	}

	public static ConceptList getUnsatisfiableClasses(){
		Node<OWLClass> bottomNode = reasoner.getUnsatisfiableClasses();
		Set<OWLClass> unsatisfiable = bottomNode.getEntitiesMinusBottom();

		return getConceptList(0,100,unsatisfiable);
	}

	public static OWLClass getOWLClass (String iri){
		OWLClass owlClass=getClass(iri);

		return owlClass;
	}

	public static OWLObjectProperty getOWLObjectProperty (String iri){
		OWLObjectProperty owlObjectProperty=df.getOWLObjectProperty(getIRI(iri));
		return owlObjectProperty;
	}

	public static Set<OWLClass> getSubClasses(OWLClassExpression id, boolean directChildren){

		reasoner.flush();
		NodeSet<OWLClass> subClses = reasoner.getSubClasses(id, directChildren);
		Set<OWLClass> clses = subClses.getFlattened();

		return clses;
	}

	public static ConceptList getSuperClasses(String id, boolean directChildren, Integer skip, Integer limit) {

		Set<OWLClass> classes=getSuperClasses(getClass(id),directChildren);
		return getConceptList(skip, limit, classes);
	}

	public static Set<OWLClass> getSuperClasses(OWLClassExpression id, boolean directParent){

//		reasoner.flush();
		NodeSet<OWLClass> superClses = reasoner.getSuperClasses(id, directParent);
		Set<OWLClass> clses = superClses.getFlattened();

		return clses;
	}

	public static ConceptList getSubClasses(String id, boolean directChildren, Integer skip, Integer limit) {

		Set<OWLClass> classes=getSubClasses(getClass(id),directChildren);
		return getConceptList(skip, limit, classes);
	}

	private static ConceptList getConceptList(Integer skip, Integer limit, Set<OWLClass> classes) {
		ConceptList list=new ConceptList();
		list.setConcepts(new ArrayList<Concept>());
		int sk=0;
		if (skip!=null){
			sk=skip;
		}
		int lim=100;
		if (limit!=null && limit>0){
			lim=limit;
		}
		int count=0;
		int added=0;
		int ign=0;
		list.setLimit(lim);
		list.setSkip(sk);
		if (classes!=null){
			for (OWLClass cls : classes) {
				if (!cls.getIRI().toString().toLowerCase().contains("owl#thing")
						&& !cls.getIRI().toString().toLowerCase().contains("owl#nothing")){
					count++;
					if (sk<count && lim>added){
						addClassToConceptList(list, cls);
						added++;
					}
				}else{
					ign++;
				}
			}
			list.setTotal(classes.size() - ign);
		}else{
			list.setTotal(0);
		}

		return list;
	}

	private static void addClassToConceptList(ConceptList list, OWLClass cls) {
		String term=getClassAnnotationAssertion(cls,"rdfs:label");
		Concept concept=new Concept(cls.getIRI().toString().replace(IRIpreffix, ""),term);
		list.getConcepts().add(concept);
	}

	public static String getOWLObjectSomeValuesFrom(String propertyIdentifier, String classIdentifier) throws IOException{

		OWLClassExpression owlClassExpression = df.getOWLObjectSomeValuesFrom(df.getOWLObjectProperty(getIRI(propertyIdentifier)),
				getClass(classIdentifier));
		System.out.println(renderer.render(owlClassExpression));
		System.out.println("eeede");

		return owlClassExpression.toString();
	}

	public static OWLClassExpression getOWLObjectSomeValuesFrom(OWLObjectProperty owlProperty, OWLClassExpression owlClass){

		OWLClassExpression owlClassExpression = df.getOWLObjectSomeValuesFrom(owlProperty,
				owlClass);

		return owlClassExpression;
	}


	public static String getOWLSubClassOfAxiom(String subClassExpressionIdentifier,String superClassExpressionIdentifier ){
		OWLSubClassOfAxiom ax1 = df.getOWLSubClassOfAxiom(getClass(subClassExpressionIdentifier), getClass(superClassExpressionIdentifier));

		return ax1.toString();
	}

	public static OWLSubClassOfAxiom getOWLSubClassOfAxiom(OWLClassExpression subClass,OWLClassExpression superClass ){
		OWLSubClassOfAxiom ax1 = df.getOWLSubClassOfAxiom(subClass, superClass);

		return ax1;
	}

	public static void addAxiom(OWLAxiom owlAxion){
		manager.applyChange(new AddAxiom(ontology, owlAxion));
	}


	public static OWLClassExpression getOWLObjectIntersectionOf(OWLClassExpression... operands){

		OWLClassExpression owlClassExpression = df.getOWLObjectIntersectionOf(operands);

		return owlClassExpression;
	}

	public static OWLAxiom getOWLEquivalentClassesAxiom(OWLClassExpression owlClass1,OWLClassExpression owlClass2){

		OWLAxiom owlAxiom = df.getOWLEquivalentClassesAxiom(owlClass1,owlClass2);

		return owlAxiom;
	}
	public static String getOrderedExplanation(OWLAxiom axiomToExplain){

		StringBuffer ret=new StringBuffer("");	
		Set<OWLAxiom> explanation = explanationGenerator.getExplanation(axiomToExplain);
		deo = new ExplanationOrdererImpl(manager);
		ExplanationTree explanationTree = deo.getOrderedExplanation(axiomToExplain, explanation);
		printIndented(ret, explanationTree, "");
		return ret.toString();
	}

	private static void printIndented(StringBuffer sb, Tree<OWLAxiom> node, String indent) {

		OWLAxiom axiom = node.getUserObject();
		String axiomRendered = renderer.render(axiom);
		axiomRendered=replaceIds(axiomRendered);
		sb.append(indent + axiomRendered);
		sb.append ("\r\n");
		if (!node.isLeaf()) {
			for (Tree<OWLAxiom> child : node.getChildren()) {
				printIndented(sb, child, indent + "\t");
			}
		}
	}

	private static String replaceIds(String axiomRendered) {
		Matcher m = myPattern.matcher(axiomRendered);
		String replacedString=axiomRendered;
		while(m.find()) {
			String id=m.group(0);
			String term=getClassAnnotationAssertion(id,"rdfs:label");
//			replacedString= replacedString.replaceAll(rendererPreffix + id, id + "|" + term + "|");
			replacedString= replacedString.replaceAll( IRIpreffix + id, id + "|" + term + "|");
		}
		return replacedString;
	}

	public static ConceptList getEquivalentClasses(String id, Integer skip, Integer limit) {
		OWLClass classExp=getClass(id);
		Set<OWLClass>classes=getEquivalentClasses(classExp);
		return getConceptList(skip, limit, classes);

	}

	private static OWLClass getClass(String id) {
		return df.getOWLClass(getIRI(id));
	}

	private static org.semanticweb.owlapi.model.IRI getIRI(String suffix) {
		return IRI.create(IRIpreffix + suffix);
	}
	public static Set<OWLClass> getEquivalentClasses(OWLClass clas) {
//		reasoner.flush();
		Node<OWLClass> classesNode = reasoner.getEquivalentClasses(clas);
		return classesNode.getEntities();

	}

	public static void addAxiomsInMS(JsonObject document) throws OWLOntologyCreationException, JsonSyntaxException, ClassNotFoundException{

		ConceptDefinitions conceptDefinitions = (ConceptDefinitions) gson.fromJson(document, Class.forName("com.termmed.reasoner.model.ConceptDefinitions"));
		String owlString = conceptDefinitions.getDefinitions();
		System.out.println("content in MS:" + owlString);
		OWLParser parserm = new ManchesterOWLSyntaxOntologyParserFactory().createParser(  );
		try {
			parserm.parse(new StringDocumentSource(owlString), ontology, new OWLOntologyLoaderConfiguration());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static QueryResults addAxioms(JsonObject document) throws OWLOntologyCreationException, JsonSyntaxException, ClassNotFoundException{
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();

		ConceptDefinitions conceptDefinitions = (ConceptDefinitions) gson.fromJson(document, Class.forName("com.termmed.reasoner.model.ConceptDefinitions"));
		String owlString = conceptDefinitions.getDefinitions();
		Integer limit=conceptDefinitions.getLimit();
		InputStream is = new ByteArrayInputStream(owlString.getBytes());

		OWLOntology ont = man.loadOntologyFromOntologyDocument(is);
		Set<OWLClass> allClasses=new HashSet<OWLClass>();
		Set<OWLAxiom> removedAxioms =new HashSet<OWLAxiom>();
		Set<OWLAxiom> axioms = ont.getAxioms();
		for (OWLAxiom axiom: axioms){
			
			if (axiom.getAxiomType().equals(AxiomType.DECLARATION)){
//			if (axiom.getAxiomType().equals(AxiomType.SUBCLASS_OF)
//			||axiom.getAxiomType().equals(AxiomType.EQUIVALENT_CLASSES)){
				OWLDeclarationAxiom daxiom=(OWLDeclarationAxiom)axiom;
				Set<OWLObjectProperty> ops = daxiom.getObjectPropertiesInSignature();
				for(OWLObjectProperty op:ops){
					removeObjectProperty(op);

				}
				
				Set<OWLClass> clas=daxiom.getClassesInSignature();
				allClasses.addAll(clas);
//				for (OWLClass cl:clas){
//					removedAxioms.addAll(removeAllAxioms(cl));
//				}
			}
		}

		QueryResults prevQueryResults=null;
		if (conceptDefinitions.getPreviousResults()!=null && conceptDefinitions.getPreviousResults()) {
			if (allClasses.size() > 0) {
				reasoner.flush();
				reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
				prevQueryResults = getResults(allClasses, 0, limit,null);
			}
		}
		for(OWLClass clas:allClasses){
			removedAxioms.addAll(removeAllAxioms(clas));
		}
		if (axioms.size()>0){
			manager.addAxioms(ontology, axioms);
			try {
				reasoner.flush();
				reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		if (man!=null){
			man.removeOntology(ont);
		}
		man=null;
		ont=null;
		return getResults(allClasses,0,limit, prevQueryResults);


	}
	private static void removeObjectProperty(OWLObjectProperty op) {

		Set<OWLSubObjectPropertyOfAxiom> axiomSub = ontology.getObjectSubPropertyAxiomsForSubProperty(op);
		if (axiomSub!=null){
			if (axiomSub.size()>0){
				manager.removeAxioms(ontology, axiomSub);
			}
		}
		Set<OWLEntity> signatures = op.getSignature();
		if (signatures!=null){

			for(OWLEntity entity:signatures){
				Set<OWLDeclarationAxiom> decAxiom = ontology.getDeclarationAxioms(entity);
				if (decAxiom!=null){
					if (decAxiom.size()>0){
						manager.removeAxioms(ontology, decAxiom);
					}
				}
			}
		}

		Set<OWLAnnotationAssertionAxiom> annoAxiom = ontology.getAnnotationAssertionAxioms( op.getIRI());
		if (annoAxiom!=null){
			if (annoAxiom.size()>0){
				manager.removeAxioms(ontology, annoAxiom);
			}
		}

	}

	private static QueryResults getResults(Set<OWLClass> classes, Integer i, Integer limit, QueryResults prevQueryResults) {
		QueryResults queryResults=new QueryResults();
		queryResults.setConcepts(new ArrayList<Concept>());
		for (OWLClass clas:classes){

			String term=getClassAnnotationAssertion(clas,"rdfs:label");
			Concept concept=new Concept(clas.getIRI().toString(),term);

			Set<OWLClass> retClasses = getSubClasses(clas, false);
			concept.setSubClasses(getConceptList(i, limit, retClasses));

			retClasses = getSubClasses(clas, true);
			concept.setDirectSubClasses(getConceptList(i, limit, retClasses));

			retClasses = getSuperClasses(clas, false);
			concept.setSuperClasses(getConceptList(i, limit, retClasses));

			retClasses = getSuperClasses(clas, true);
			concept.setDirectSuperClasses(getConceptList(i, limit, retClasses));

			retClasses = getEquivalentClasses(clas);
			concept.setEquivalentClasses(getConceptList(i, limit, retClasses));

			if (prevQueryResults!=null){
				int pos=prevQueryResults.getConcepts().indexOf(concept);
				concept.setPreviousVersion(prevQueryResults.getConcepts().get(pos));
			}
			queryResults.getConcepts().add(concept);
		}
		return queryResults;
	}

	private static Set<OWLAxiom> removeAllAxioms(OWLClass cl) {

		//	*** references mustn't be deleted (just in case of deleted concept) ***
//		Set<OWLAxiom> axioms=ontology.getReferencingAxioms(cl);
//		if (axioms!=null){
//			if (axioms.size()>0){
//				manager.removeAxioms(ontology, axioms);
//			}
//		}
		Set<OWLAxiom> removedAxioms=new HashSet<OWLAxiom>();
		Set<OWLClassAxiom> clAxioms = ontology.getAxioms(cl);
		if (clAxioms!=null){
			if (clAxioms.size()>0){
				removedAxioms.addAll(clAxioms);
				manager.removeAxioms(ontology, clAxioms);
			}
		}
//	*** there are no declarations on new model ***
//		Set<OWLEntity> signatures = cl.getSignature();
//		if (signatures!=null){
//
//			for(OWLEntity entity:signatures){
//				Set<OWLDeclarationAxiom> decAxiom = ontology.getDeclarationAxioms(entity);
//				if (decAxiom!=null){
//					if (decAxiom.size()>0){
//						manager.removeAxioms(ontology, decAxiom);
//					}
//				}
//			}
//		}

		Set<OWLAnnotationAssertionAxiom> annoAxiom = ontology.getAnnotationAssertionAxioms( cl.getIRI());
		if (annoAxiom!=null){
			if (annoAxiom.size()>0){
				removedAxioms.addAll(annoAxiom);
				manager.removeAxioms(ontology, annoAxiom);
			}
		}
		return removedAxioms;
	}

	public static boolean isKindOf(String idAncestor, String idDescendant){
		OWLClass ancestor = getClass(idAncestor);
		OWLClass descendant = getClass(idDescendant);
		Set<OWLClass> superClasses = getSuperClasses(descendant, false);

		return superClasses.contains(ancestor);

	}

	public static String getClassAnnotationAssertion(String id, String propertyName){
		OWLClass cls=getClass(id);
		return getClassAnnotationAssertion(cls,"rdfs:label");
	}

	public static String getClassAnnotationAssertion(OWLClass owlclass, String propertyName){
		Set<OWLAnnotationAssertionAxiom> annoAxiom = ontology.getAnnotationAssertionAxioms( owlclass.getIRI());
		if (annoAxiom!=null){
			for (OWLAnnotationAssertionAxiom annotation : annoAxiom){
				if (annotation.getProperty().toString().equals(propertyName)){
					OWLAnnotationValue annovalue = annotation.getValue();
					Optional<OWLLiteral> literal = annovalue.asLiteral();
					//					java.util.Optional<OWLLiteral> literal = annovalue.asLiteral();
					return literal.get().getLiteral();
				}
			}
		}
		return "";
	}

	public static void removeAxioms(JsonObject document, String classify) throws OWLOntologyCreationException, JsonSyntaxException, ClassNotFoundException {
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		ConceptDefinitions conceptDef = (ConceptDefinitions) gson.fromJson(document, Class.forName("com.termmed.reasoner.model.ConceptDefinitions"));

		String owlString = conceptDef.getDefinitions();
		InputStream is = new ByteArrayInputStream(owlString.getBytes());
		OWLOntology ont = man.loadOntologyFromOntologyDocument(is);

		Set<OWLAxiom> axioms = ont.getAxioms();
		Set<OWLAxiom> axiomsToRemove=new HashSet<OWLAxiom>(); 
		for (OWLAxiom axiom: axioms){
			if (ontology.containsAxiomIgnoreAnnotations(axiom)){
				axiomsToRemove.add(axiom);
			}
		}
		if (axioms.size()>0){
			manager.removeAxioms(ontology, axiomsToRemove);		
		}else{
			System.out.println("there are no axioms to remove");
		}
		if (classify!=null && classify.toLowerCase().equals("true")){
			reasoner.flush();
			reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		}
		axioms=null;
		axiomsToRemove=null;

		if (man!=null){
			man.removeOntology(ont);
		}
		man=null;
		ont=null;
	}

	public static void addClassDeclaration(String id){

		OWLClass conceptClass=getClass(id);

		OWLDeclarationAxiom declarationAxiom = df
				.getOWLDeclarationAxiom(conceptClass);
		manager.addAxiom(ontology, declarationAxiom);
	}

	public static void removeClass(String id, String classify) {
		OWLClass clas=getClass(id);
		removeAllAxioms(clas);
		if (classify!=null && classify.toLowerCase().equals("true")){
			reasoner.flush();
			reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		}
	}

	public static QueryResults dlquery(JsonObject document) throws OWLOntologyCreationException, JsonSyntaxException, ClassNotFoundException {
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		ConceptDefinitions conceptDefinitions = (ConceptDefinitions) gson.fromJson(document, Class.forName("com.termmed.reasoner.model.ConceptDefinitions"));
		String owlString = conceptDefinitions.getDefinitions();
		Integer limit=conceptDefinitions.getLimit();
		InputStream is = new ByteArrayInputStream(owlString.getBytes());
		OWLOntology ont = man.loadOntologyFromOntologyDocument(is);
		Set<OWLClass> allClasses=new HashSet<OWLClass>();
		Set<OWLAxiom> axioms = ont.getAxioms();
		Set<OWLAxiom> removedAxioms =new HashSet<OWLAxiom>();
		for (OWLAxiom axiom: axioms){
			if (axiom.getAxiomType().equals(AxiomType.DECLARATION)){
				OWLDeclarationAxiom daxiom=(OWLDeclarationAxiom)axiom;
				Set<OWLClass> clas=daxiom.getClassesInSignature();
				allClasses.addAll(clas);
//				for (OWLClass cl:clas){
//					removedAxioms.addAll(removeAllAxioms(cl));
//				}
			}
		}

		QueryResults prevQueryResults=null;
		if (conceptDefinitions.getPreviousResults()!=null && conceptDefinitions.getPreviousResults()) {
			if (allClasses.size() > 0) {
				reasoner.flush();
				reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
				prevQueryResults = getResults(allClasses, 0, limit,null);
			}
		}
		for(OWLClass clas:allClasses){
			removedAxioms.addAll(removeAllAxioms(clas));
		}
		if (axioms.size()>0){
			manager.addAxioms(ontology, axioms);
			reasoner.flush();
			reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		}
		QueryResults queryResults=getResults(allClasses,0,limit,prevQueryResults);
		if (axioms.size()>0){

			manager.removeAxioms(ontology, axioms);
			manager.addAxioms(ontology,removedAxioms);
		}

		if (man!=null){
			man.removeOntology(ont);
		}
		man=null;
		ont=null;
		return queryResults;
	}

	public static String getPreffix() {
		return IRIpreffix;
	}

	public static String testExplain() {
		OWLClass ancestor = getClass("362958002");
		OWLClass descendant = getClass("232721002");
		OWLSubClassOfAxiom axiom = getOWLSubClassOfAxiom(descendant, ancestor);
		return getOrderedExplanation(axiom);
	}

	public static String getSubClassExplanation(String idAncestor, String idDescendant){
		OWLClass ancestor = getClass(idAncestor);
		OWLClass descendant = getClass(idDescendant);
		OWLSubClassOfAxiom axiom = getOWLSubClassOfAxiom(descendant, ancestor);
		return getOrderedExplanation(axiom);
	}

	public static ConceptList getSuperClassesWithIsAs(String id, Integer iskip, Integer ilimit) {
		ConceptList parentList = getSuperClasses(id, false, iskip, ilimit);
		OWLClass mainCls=getClass(id);
		addClassToConceptList(parentList, mainCls);
		parentList.setTotal(parentList.getTotal()+1);
		for (Concept conceptParent:parentList.getConcepts()){
			OWLClass cls=getClass(conceptParent.getConceptId());
			Set<OWLClass> parentDirectSuperClasses = getSuperClasses(cls, true);
			ConceptList parentDirectParentList = getConceptList(0, 100, parentDirectSuperClasses);
			conceptParent.setDirectSuperClasses(parentDirectParentList);
		}
		return parentList;
	}

	public static String[] getPathToBaselineZipFolder() {
		return pathToBaselineZipFolder;
	}

	public static void setPathToBaselineZipFolder(String[] pathToBaselineZipFolder) {
		ReasonerServer.pathToBaselineZipFolder = pathToBaselineZipFolder;
	}

	
	public static void loadDeltaOntology(String deltaFilename) throws OWLOntologyCreationException, ReasonerServiceException {
		SnomedTaxonomyBuilder snomedTaxonomyBuilder = new SnomedTaxonomyBuilder();
		SnomedTaxonomy snomedTaxonomy;
		Set<File> snapshotFiles = new HashSet<File>();
		snapshotFiles.add(new File(deltaFilename));
		TimerUtil timer = new TimerUtil("Load delta and classify");

		try {
			InputStreamSet previousReleaseRf2SnapshotArchives = new InputStreamSet(snapshotFiles);
			snomedTaxonomy = snomedTaxonomyBuilder.build(previousReleaseRf2SnapshotArchives, null, true);
		} catch (ReleaseImportException | FileNotFoundException e) {
			throw new ReasonerServiceException("Failed to build existing taxonomy.", e);
		}
		timer.checkpoint("Build existing taxonomy");

		System.out.println("Creating OwlOntology");
//		ungroupedRoles = snomedTaxonomy.getUngroupedRolesForContentTypeOrDefault(parseLong(Concepts.ALL_PRECOORDINATED_CONTENT));
//		OntologyService ontologyService = new OntologyService(ungroupedRoles);
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology ont;
		try {
			ont = createOntology(snomedTaxonomy,man,null,versionDate);
		} catch (OWLOntologyCreationException e) {
			throw new ReasonerServiceException("Failed to build OWL Ontology.", e);
		}
		timer.checkpoint("Create OWL Ontology");



//		OWLOntology ont = man.loadOntologyFromOntologyDocument(new File(filename));
		
//		System.out.println("Delta ontology IRI:" + man.getOntologyDocumentIRI(ont));
		Set<OWLClass> allClasses=new HashSet<OWLClass>();
		Set<OWLAxiom> axioms = ont.getAxioms();
		for (OWLAxiom axiom: axioms){
			Set<OWLClass> clas=null;
			if (axiom.getAxiomType().equals(AxiomType.DECLARATION)) {

				OWLDeclarationAxiom daxiom=(OWLDeclarationAxiom)axiom;
				clas=daxiom.getClassesInSignature();
			}else if(axiom.getAxiomType().equals(AxiomType.EQUIVALENT_CLASSES)){
				OWLEquivalentClassesAxiom daxiom=(OWLEquivalentClassesAxiom)axiom;
				clas=daxiom.getNamedClasses();
//				clas=daxiom.getClassesInSignature();

			}else if (axiom.getAxiomType().equals(AxiomType.SUBCLASS_OF)) {
				OWLSubClassOfAxiom daxiom = (OWLSubClassOfAxiom) axiom;
				OWLClassExpression subclas = daxiom.getSubClass();
				clas=subclas.getClassesInSignature();
			}else if (!axiom.getAxiomType().equals(AxiomType.ANNOTATION_ASSERTION)){
				clas=axiom.getClassesInSignature();
			}
			if (clas!=null) {
				for (OWLClass cl : clas) {
					removeAllAxioms(cl);
				}
			}
		}
		System.out.println("axioms before to add delta:" + ontology.getAxiomCount());
		if (axioms.size()>0){
			
			manager.addAxioms(ontology, axioms);
				
			System.out.println("axioms after to add delta:" + ontology.getAxiomCount());
			reasoner.flush();
			reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
			timer.checkpoint("Finished delta classification");
		}
		if (man!=null){
			man.removeOntology(ont);
		}
		man=null;
		ont=null;
	}

	private static void setVersion(){
		InputStream projProp = ReasonerServer.class.getResourceAsStream("/com/termmed/project.properties");

		Properties properties=new Properties();
		try {
			properties.load(projProp);
			version=properties.getProperty("version");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		properties=null;
		try {
			projProp.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		projProp=null;
	}
	public static String getVersion(){
		if (version==null){
			setVersion();
		}
		return version;
	}

	public static boolean isBaselineLoaded() {
		return baselineLoaded;
	}

	public static void setBaselineLoaded(boolean baselineLoaded) {
		ReasonerServer.baselineLoaded = baselineLoaded;
	}

	public static boolean isDeltaDownloaded() {
		return deltaDownloaded;
	}

	public static void setDeltaDownloaded(boolean deltaDownloaded) {
		ReasonerServer.deltaDownloaded = deltaDownloaded;
	}

	public static boolean isDeltaDownloading() {
		return deltaDownloading;
	}

	public static void setDeltaDownloading(boolean deltaDownloading) {
		ReasonerServer.deltaDownloading = deltaDownloading;
	}
}