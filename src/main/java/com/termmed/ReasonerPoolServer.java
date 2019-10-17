package com.termmed;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyInputSourceException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public class ReasonerPoolServer {

	private static final String IRI_PREFFIX_DEFAULT = "http://www.termspace.com/conceptsOwlComplete#";
	private static final String ONTOLOGY_FILE_DEFAULT = "conceptsOwlComplete.xml";
	
	static String pathToOntologyXmlFile;
	
	private static String IRIpreffix;
	static OWLOntologyManager manager;
	static OWLOntology ontology;
	private static IRI IRI;
	static OWLDataFactory df ;
	private static HashMap<String, Set<OWLAxiom>> hashAxioms;
	private static String baseFileName;
	static OWLReasonerFactory reasonerFactory;
	static OWLReasoner reasoner ;
	
	public static void init() throws OWLOntologyCreationException, OWLOntologyInputSourceException, FileNotFoundException{
//		TestMemory.test();
//		TestMemory.updateMemUsedAtThisMoment("Init process");
		baseFileName="/Users/ar/Downloads/owlReasoner/conceptsOwlDelta";
//		baseFileName="conceptsOwlDelta";
		hashAxioms=new HashMap<String,Set<OWLAxiom>>();
		Properties prop=new Properties();
		try {
			File propFile=new File("reasoner-server.properties");
			if (propFile.exists()){
				prop.load(new FileReader(propFile));
				getProperties(prop);
			}else{
				ReasonerPoolServer.pathToOntologyXmlFile=ONTOLOGY_FILE_DEFAULT;
				ReasonerPoolServer.IRIpreffix=IRI_PREFFIX_DEFAULT;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		manager = OWLManager.createOWLOntologyManager();
		df = manager.getOWLDataFactory();
		reasonerFactory = new ElkReasonerFactory();
		loadOntology();
//		TestMemory.getMemRetainedAtThisMoment("Out init process");
//
//		TestMemory.test();

	}

	private static void getProperties(Properties prop) {
		ReasonerPoolServer.pathToOntologyXmlFile=prop.getProperty("ontologyFile", ONTOLOGY_FILE_DEFAULT);		
		ReasonerPoolServer.IRIpreffix=prop.getProperty("IRIPreffix", IRI_PREFFIX_DEFAULT);		
	}
	public static void loadOntology() throws OWLOntologyCreationException,OWLOntologyInputSourceException,FileNotFoundException{
		long t1 = System.currentTimeMillis();
		System.out.println("Preparing to load SNOMED CT");
		File snomedComplete =new File( pathToOntologyXmlFile);
		System.out.println("************  USING CONFIGURATION **************");
		System.out.println("Ontology File: " + ReasonerPoolServer.pathToOntologyXmlFile);
		System.out.println("IRI Preffix: " + ReasonerPoolServer.IRIpreffix);
		System.out.println("************************************************");
		ontology = manager.loadOntologyFromOntologyDocument(snomedComplete);
		IRI=manager.getOntologyDocumentIRI(ontology);
//		System.out.println("Ontology IRI:" + manager.getOntologyDocumentIRI(ontology));
//		OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
//		reasoner = reasonerFactory.createReasoner(ontology);
//		explanationGenerator =
//				new DefaultExplanationGenerator(
//						manager, reasonerFactory, ontology, reasoner, new SilentExplanationProgressMonitor());
//		deo = new ExplanationOrdererImpl(manager);
//
//		profile = new OWL2DLProfile();
		long t2 = System.currentTimeMillis();
		System.out.println("Loaded (" + (t2-t1) + " ms.)");

	}
	
	public static OWLOntology changeToPathAndClassify(String pathId, String id, String id2) throws OWLOntologyCreationException{
		
		reasoner=null;
		OWLOntology ont;
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		System.out.println("step 1");
		ont=man.copyOntology(ontology, OntologyCopy.SHALLOW);
		System.out.println("Axioms in baseline:" + ont.getAxiomCount());
		System.out.println("step 2");
		Set<OWLAxiom>  axioms=getAxiomsLog( pathId);
		System.out.println("step 3");
		if (axioms!=null && axioms.size()>0){
			System.out.println("step 4");
			man.addAxioms(ont, axioms);
			System.out.println("step 5");
		}
		System.out.println("for PathId:" + pathId + " Axioms in ontology:" + ont.getAxiomCount());
		Set<OWLClass> subClasses = getSubClasses(ont,id,true,null,null);
		System.out.println("step 6");
		System.out.println("#subclasses of:" + subClasses.size());
		long t1 = System.currentTimeMillis();
		subClasses = getSubClasses(ont,id2,true,null,null);
		long t2 = System.currentTimeMillis();
		System.out.println("second subclass made in (" + (t2-t1) + " ms.)");
		System.out.println("step 7");
		System.out.println("#subclasses of:" + subClasses.size());
		return ont;
	}
public static OWLOntology changeToPath(String pathId, String id) throws OWLOntologyCreationException{
		
		OWLOntology ont;
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		System.out.println("step 1");
		ont=man.copyOntology(ontology, OntologyCopy.SHALLOW);
		System.out.println("Axioms in baseline:" + ont.getAxiomCount());
		System.out.println("step 2");
		Set<OWLAxiom>  axioms=getAxiomsLog( pathId);
		System.out.println("step 3");
		if (axioms!=null && axioms.size()>0){
			System.out.println("step 4");
			man.addAxioms(ont, axioms);
			System.out.println("step 5");
		}
		System.out.println("for PathId:" + pathId + " Axioms in ontology:" + ont.getAxiomCount());
//		Set<OWLClass> subClasses = getSubClasses(ont,id,true,null,null);
		System.out.println("step 6");
//		System.out.println("#subclasses:" + subClasses.size());
		return ont;
	}

	private static Set<OWLAxiom> getAxiomsLog(String pathId) throws OWLOntologyCreationException {

		Set<OWLAxiom> axioms=hashAxioms.get(pathId);
		if (axioms==null){

			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			OWLOntology ont = man.loadOntologyFromOntologyDocument(new File(baseFileName + pathId + ".xml"));
			
			axioms = ont.getAxioms();

			if (man!=null){
				man.removeOntology(ont);
			}
			man=null;
			ont=null;
			System.out.println("from path:" + pathId + " getting " + axioms.size() + " axioms from file");
			hashAxioms.put(pathId, axioms);
		}else{
			System.out.println("from path:" + pathId + " getting " + axioms.size() + " axioms from memory");
		}
		return axioms;
	}

	private static OWLClass getClass(String id) {
		return df.getOWLClass(getIRI(id));
	}

	private static org.semanticweb.owlapi.model.IRI getIRI(String suffix) {
		return IRI.create(IRIpreffix + suffix);
	}
	public static Set<OWLClass> getSubClasses(OWLOntology ont, String id, boolean directChildren, Integer skip, Integer limit) {

		Set<OWLClass> classes=getSubClasses(ont, getClass(id),directChildren);
		return classes;
	}
	public static Set<OWLClass> getSubClasses(OWLOntology ont, OWLClassExpression id, boolean directChildren){
		
		if (reasoner==null){
			System.out.println("null reasoner, creating new");
			reasoner = reasonerFactory.createReasoner(ont);
		}else{
			System.out.println("reusing reasoner");
		}
		//reasoner.flush();
		NodeSet<OWLClass> subClses = reasoner.getSubClasses(id, directChildren);
		Set<OWLClass> clses = subClses.getFlattened();
//		reasoner=null;
		return clses;
	}
}
