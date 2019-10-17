package com.termmed.reasoner.examples;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.profiles.OWL2DLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.profiles.OWLProfileViolation;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;

import com.clarkparsia.owlapi.explanation.DefaultExplanationGenerator;
import com.clarkparsia.owlapi.explanation.util.SilentExplanationProgressMonitor;

//import uk.ac.manchester.cs.bhig.util.Tree;
import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationOrderer;
import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationOrdererImpl;
import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationTree;
//import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;
//import uk.ac.manchester.cs.owl.explanation.ordering.Tree;

/**
 * Created by alo on 3/28/16.
 */
public class Examples1 {

//    private static OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();

    public static void main(String[] args) throws Exception {
//System.out.println( new Date("2016/04/03").getTime());
//      String ks = "/Users/ar/Downloads/SnomedCT_RF2Release_INT_20160131/Snapshot/snomedct_owl.owl";
//      Examples1.shouldLoad2(ks);
//      String pO="/Users/ar/Downloads/owl_20160131.xml";
//      Examples1.shouldLoad2(pO);
    	Examples1.test();
        
    }
    public static void test(){

		Pattern myPattern = Pattern.compile("(\\d{1,})");
		String input=" d 3848910940 f fkskfksfkk (ksf 924942020 p 99234787810)";
		System.out.println("len input=" + input.length());
		Matcher m=myPattern.matcher(input);
		int count=1;
//		MatchResult res = m.toMatchResult();
//		
//		for (int i=0;i<res.groupCount();i++){
//			System.out.println("res i=" + i + ":" + res.group(i));
//		}
//		m.
		
		while (m.find()){
			System.out.println("loop=" + count);
			System.out.println(m.groupCount());
			for (int i=0;i<m.groupCount();i++){
				System.out.println("i=" + i + ":" + m.group(i));
			}
			count++;
		}
    }
    
    public static void shouldLoad() throws Exception {
        long t1 = System.currentTimeMillis();
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = manager.getOWLDataFactory();
        System.out.println("Preparing to load SNOMED CT");
        File testOntology = new File("/Users/alo/Downloads/termspace-owl (3).xml");
        File snomedComplete = new File("/Users/ar/Downloads/conceptsOwlComplete 2.xml");
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(snomedComplete);
        long t2 = System.currentTimeMillis();
        System.out.println("Loaded (" + (t2-t1) + " ms.)");
        System.out.println("Checking with profile");
        OWL2DLProfile profile = new OWL2DLProfile();
        OWLProfileReport report = profile.checkOntology(ontology);
        long t3 = System.currentTimeMillis();
        System.out.println(report.getViolations().size() + " violations (" + (t3-t2) + " ms.)");
        int count = 0;
        for (OWLProfileViolation violation : report.getViolations()) {
            System.out.println("    " + violation.toString());
            count++;
            if (count > 10) break;
        }
        System.out.println("");
        System.out.println("Ontology stats");
        System.out.println("ALL: " + ontology.getAxiomCount());
        System.out.println("DECLARATION: " + ontology.getAxiomCount(AxiomType.DECLARATION));
        System.out.println("SUBCLASS_OF: " + ontology.getAxiomCount(AxiomType.SUBCLASS_OF));
        System.out.println("EQUIVALENT_CLASSES: " + ontology.getAxiomCount(AxiomType.EQUIVALENT_CLASSES));
        System.out.println("ANNOTATION_ASSERTION: " + ontology.getAxiomCount(AxiomType.ANNOTATION_ASSERTION));
        System.out.println("");
        System.out.println("Starting reasoner");
        long t4 = System.currentTimeMillis();
        OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
        reasoner.precomputeInferences((InferenceType.CLASS_HIERARCHY));
        long t4a = System.currentTimeMillis();
        System.out.println("Precomputation end " + " (" + (t4a-t4) + " ms.)");
        boolean consistent = reasoner.isConsistent();
        long t5 = System.currentTimeMillis();
        System.out.println("Consistent: " + consistent + " (" + (t5-t4a) + " ms.)");
        Node<OWLClass> bottomNode = reasoner.getUnsatisfiableClasses();
        Set<OWLClass> unsatisfiable = bottomNode.getEntitiesMinusBottom();
        if (!unsatisfiable.isEmpty()) {
            System.out.println("The following classes are unsatisfiable: ");
            for (OWLClass cls : unsatisfiable) {
                System.out.println(" " + cls);
            }
        } else {
            System.out.println("There are no unsatisfiable classes");
        }
        long t6 = System.currentTimeMillis();
        OWLClass abdominalProcedures = df.getOWLClass(IRI.create("http://www.termspace.com/conceptsOwlComplete#118698009"));
        OWLClass fineNeedlePuncture = df.getOWLClass(IRI.create("http://www.termspace.com/conceptsOwlComplete#441785007"));
        OWLClass clinicalFinding = df.getOWLClass(IRI.create("http://www.termspace.com/conceptsOwlComplete#404684003"));
        OWLObjectProperty findingSite = df.getOWLObjectProperty(IRI.create("http://www.termspace.com/conceptsOwlComplete#363698007"));
        OWLObjectProperty procedureSite = df.getOWLObjectProperty(IRI.create("http://www.termspace.com/conceptsOwlComplete#363704007"));
        OWLClass asthma = df.getOWLClass(IRI.create("http://www.termspace.com/conceptsOwlComplete#195967001"));
        OWLClass abdomen = df.getOWLClass(IRI.create("http://www.termspace.com/conceptsOwlComplete#113345001"));
        OWLClass heartValveRepair = df.getOWLClass(IRI.create("http://www.termspace.com/conceptsOwlComplete#85830006"));
        OWLClass procedureOnTrunk = df.getOWLClass(IRI.create("http://www.termspace.com/conceptsOwlComplete#118694006"));
        OWLClass procedureBySite = df.getOWLClass(IRI.create("http://www.termspace.com/conceptsOwlComplete#362958002"));
        OWLObjectProperty roleGroup = df.getOWLObjectProperty(IRI.create("http://www.termspace.com/conceptsOwlComplete#609096000"));
        OWLClass invasiveHaem  = df.getOWLClass(IRI.create("http://www.termspace.com/conceptsOwlComplete#406583002"));

        NodeSet<OWLClass> subClses = reasoner.getSubClasses(abdominalProcedures, false);
        Set<OWLClass> clses = subClses.getFlattened();
        long t7 = System.currentTimeMillis();
        System.out.println("All subclasses of abdominalProcedures: " + clses.size() + " (" + (t7-t6) + " ms.)");

        //NodeSet<OWLClass> supClses = reasoner.getSuperClasses(fineNeedlePuncture, false);
        //clses = supClses.getFlattened();
        long t8 = System.currentTimeMillis();
        //System.out.println("All superclasses of " + fineNeedlePuncture + ": " + clses.size() + " (" + (t8-t7) + " ms.)");

        //OWLClass newConcept = df.getOWLClass(IRI.create("http://www.termspace.com/conceptsOwlComplete#newConcept"));
        OWLClassExpression hasProcedureSiteSomeAbdomen = df.getOWLObjectSomeValuesFrom(roleGroup,
                df.getOWLObjectSomeValuesFrom(procedureSite, abdomen));
        OWLSubClassOfAxiom ax1 = df.getOWLSubClassOfAxiom(heartValveRepair, hasProcedureSiteSomeAbdomen);

        System.out.println("Adding new axiom");
        manager.applyChange(new AddAxiom(ontology, ax1));
        System.out.println("Axiom added");

        //CGI
        OWLObjectProperty causativeAgent = df.getOWLObjectProperty(IRI.create("http://www.termspace.com/conceptsOwlComplete#246075003"));
        OWLClass sepsis = df.getOWLClass(IRI.create("http://www.termspace.com/conceptsOwlComplete#91302008"));
        OWLClass haemophilus  = df.getOWLClass(IRI.create("http://www.termspace.com/conceptsOwlComplete#44470000"));

        OWLClassExpression left = df.getOWLObjectIntersectionOf(sepsis,
                df.getOWLObjectSomeValuesFrom(roleGroup,
                    df.getOWLObjectSomeValuesFrom(causativeAgent, haemophilus)));
        OWLClassExpression right = invasiveHaem;
        OWLSubClassOfAxiom cgi = df.getOWLSubClassOfAxiom(left, right);

        // Query
        OWLClassExpression query = df.getOWLObjectIntersectionOf(procedureBySite,
                df.getOWLObjectSomeValuesFrom(roleGroup,
                        df.getOWLObjectSomeValuesFrom(procedureSite, abdomen)));
        // Create a fresh name for the query.
        OWLClass newName = df.getOWLClass(IRI.create("temp001"));
        // Make the query equivalent to the fresh class
        OWLAxiom definition = df.getOWLEquivalentClassesAxiom(newName,
                query);
        manager.addAxiom(ontology, definition);

        System.out.println("");
        System.out.println("Ontology stats");
        System.out.println("ALL: " + ontology.getAxiomCount());
        System.out.println("DECLARATION: " + ontology.getAxiomCount(AxiomType.DECLARATION));
        System.out.println("SUBCLASS_OF: " + ontology.getAxiomCount(AxiomType.SUBCLASS_OF));
        System.out.println("EQUIVALENT_CLASSES: " + ontology.getAxiomCount(AxiomType.EQUIVALENT_CLASSES));
        System.out.println("ANNOTATION_ASSERTION: " + ontology.getAxiomCount(AxiomType.ANNOTATION_ASSERTION));
        System.out.println("");

        long t9 = System.currentTimeMillis();
        reasoner.flush();
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        long t10 = System.currentTimeMillis();
        System.out.println("Incremental precomputation end " + " (" + (t10-t9) + " ms.)");

        subClses = reasoner.getSubClasses(newName, false);
        clses = subClses.getFlattened();
        long t11 = System.currentTimeMillis();
        System.out.println("All subclasses of abdominalProcedures : " + clses.size() + " (" + (t11-t10) + " ms.)");

        //ExplanationGeneratorFactory<OWLAxiom> genFac = ExplanationManager.createExplanationGeneratorFactory(rf);

        //System.out.println("isEntailmentCheckingSupported " + reasoner.isEntailmentCheckingSupported(AxiomType.SUBCLASS_OF));
        //OWLAxiom testAxiom = df.getOWLSubClassOfAxiom(fineNeedlePuncture, abdominalProcedures);
        //boolean testresult = reasoner.isEntailed(testAxiom);
        //System.out.println("Entailment test result: " + testresult);
        //for (OWLClass cls : clses) {
        //    System.out.println(" " + cls);
        //}
        // Use an inferred axiom generators
//        List<InferredAxiomGenerator<? extends OWLAxiom>> gens = Collections.singletonList(new InferredSubClassAxiomGenerator());
//        OWLOntology infOnt = manager.createOntology();
//        InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner, gens);
//        iog.fillOntology(df, infOnt);
//        long t8 = System.currentTimeMillis();
//        subClses = reasoner.getSubClasses(abdominalProcedures, true);
//        clses = asSet(subClses.entities());
//        long t9 = System.currentTimeMillis();
//        System.out.println("Direct Inferred subclasses of " + abdominalProcedures + ": " + clses.size() + " (" + (t9-t8) + " ms.)");

        OWLClass coronaryByPass = df.getOWLClass(IRI.create("http://www.termspace.com/conceptsOwlComplete#232721002"));
        OWLAxiom axiomToExplain = df.getOWLSubClassOfAxiom(coronaryByPass, procedureBySite);
        DefaultExplanationGenerator explanationGenerator =
                new DefaultExplanationGenerator(
                        manager, reasonerFactory, ontology, reasoner, new SilentExplanationProgressMonitor());
        Set<OWLAxiom> explanation = explanationGenerator.getExplanation(axiomToExplain);
        ExplanationOrderer deo = new ExplanationOrdererImpl(manager);
        ExplanationTree explanationTree = deo.getOrderedExplanation(axiomToExplain, explanation);
//        printIndented(explanationTree, "");

        System.out.println("Finished all in " + (t11-t1) + " ms.");
        manager.removeAxiom(ontology, definition);
    }

    public static void shouldLoad2(String file) throws Exception {
        long t1 = System.currentTimeMillis();
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = manager.getOWLDataFactory();
        System.out.println("Preparing to load SNOMED CT");
//        File snomedComplete = new File("/Users/ar/Downloads/SnomedCT_RF2Release_INT_20160131/Snapshot/snomedct_owl.owl");
//        File snomedComplete = new File("/Users/ar/Downloads/owl_20160131.xml");
        File snomedComplete = new File(file);
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(snomedComplete);
        long t2 = System.currentTimeMillis();
        System.out.println("Loaded (" + (t2-t1) + " ms.)");
        System.out.println("Checking with profile");
        OWL2DLProfile profile = new OWL2DLProfile();
        OWLProfileReport report = profile.checkOntology(ontology);
        long t3 = System.currentTimeMillis();
        System.out.println(report.getViolations().size() + " violations (" + (t3-t2) + " ms.)");
        int count = 0;
        for (OWLProfileViolation violation : report.getViolations()) {
            System.out.println("    " + violation.toString());
            count++;
            if (count > 10) break;
        }
        
        System.out.println("Starting reasoner");
        long t4 = System.currentTimeMillis();
        OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
        reasoner.precomputeInferences((InferenceType.CLASS_HIERARCHY));
        long t4a = System.currentTimeMillis();
        System.out.println("Precomputation end " + " (" + (t4a-t4) + " ms.)");
        boolean consistent = reasoner.isConsistent();
        long t5 = System.currentTimeMillis();
        System.out.println("Consistent: " + consistent + " (" + (t5-t4a) + " ms.)");
        Node<OWLClass> bottomNode = reasoner.getUnsatisfiableClasses();
        Set<OWLClass> unsatisfiable = bottomNode.getEntitiesMinusBottom();
        if (!unsatisfiable.isEmpty()) {
            System.out.println("The following classes are unsatisfiable: ");
            for (OWLClass cls : unsatisfiable) {
                System.out.println(" " + cls);
            }
        } else {
            System.out.println("There are no unsatisfiable classes");
        }
       
        System.out.println("");
        System.out.println("Ontology stats");
        System.out.println("ALL: " + ontology.getAxiomCount());
        System.out.println("DECLARATION: " + ontology.getAxiomCount(AxiomType.DECLARATION));
        System.out.println("SUBCLASS_OF: " + ontology.getAxiomCount(AxiomType.SUBCLASS_OF));
        System.out.println("EQUIVALENT_CLASSES: " + ontology.getAxiomCount(AxiomType.EQUIVALENT_CLASSES));
        System.out.println("ANNOTATION_ASSERTION: " + ontology.getAxiomCount(AxiomType.ANNOTATION_ASSERTION));
        System.out.println("");
        
        
        IRI f=IRI.create(new File("file.inf"));
        OWLXMLDocumentFormat doc=new OWLXMLDocumentFormat();
       
//       return ontology.getAxioms();
   
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        List<InferredAxiomGenerator<? extends OWLAxiom>> gens = new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
		gens.add(new InferredSubClassAxiomGenerator());
//		
        OWLOntology infOnt = man.createOntology(manager.getOntologyDocumentIRI(ontology));
//
//		// Now get the inferred ontology generator to generate some inferred
//		// axioms for us (into our fresh ontology). We specify the reasoner that
//		// we want to use and the inferred axiom generators that we want to use.
		InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner,
				gens);
		System.out.println("Filling new ontology");
		iog.fillOntology(man.getOWLDataFactory(), infOnt);
		 System.out.println("Inferred Ontology stats");
	        System.out.println("ALL: " + infOnt.getAxiomCount());
	        System.out.println("DECLARATION: " + infOnt.getAxiomCount(AxiomType.DECLARATION));
	        System.out.println("SUBCLASS_OF: " + infOnt.getAxiomCount(AxiomType.SUBCLASS_OF));
	        System.out.println("EQUIVALENT_CLASSES: " + infOnt.getAxiomCount(AxiomType.EQUIVALENT_CLASSES));
	        System.out.println("ANNOTATION_ASSERTION: " + infOnt.getAxiomCount(AxiomType.ANNOTATION_ASSERTION));
	        System.out.println("");
	        System.out.println("Saving new ontology");
		 man.saveOntology(infOnt, doc, f);
//		// Save the inferred ontology. (Replace the URI with one that is
//		// appropriate for your setup)
//
//	     File f2 = new File( "/Users/ar/Downloads/owl_inferred_20160131_parserOWL_OWL.xml");
//	     File f3 = new File( "/Users/ar/Downloads/owl_inferred_20160131_parserOWL_RDF.xml");
//		IRI documentIRI2 = IRI.create(f2);
//		man.saveOntology(infOnt, new OWLXMLOntologyFormat(), documentIRI2);
//		IRI documentIRI3 = IRI.create(f3);
//		man.saveOntology(ontology, new RDFXMLOntologyFormat(), documentIRI3);
    }

//    private static void printIndented(Tree<OWLAxiom> node, String indent) {
//        OWLAxiom axiom = node.getUserObject();
//        System.out.println(indent + renderer.render(axiom));
//        if (!node.isLeaf()) {
//            for (Tree<OWLAxiom> child : node.getChildren()) {
//                printIndented(child, indent + "    ");
//            }
//        }
//    }
}
