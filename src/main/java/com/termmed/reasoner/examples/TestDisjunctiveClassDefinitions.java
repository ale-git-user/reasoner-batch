package com.termmed.reasoner.examples;

import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.profiles.OWL2DLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.profiles.OWLProfileViolation;
import org.semanticweb.owlapi.reasoner.*;

import uk.ac.manchester.cs.owl.explanation.ordering.Tree;

import java.io.File;
import java.util.Set;

/**
 * Created by alo on 3/28/16.
 */
public class TestDisjunctiveClassDefinitions {

    private static OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();

    public static void main(String[] args) throws Exception {
        TestDisjunctiveClassDefinitions.shouldLoad();
    }

    public static void shouldLoad() throws Exception {
        long t1 = System.currentTimeMillis();
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = manager.getOWLDataFactory();
        System.out.println("Preparing to load SNOMED CT");
        File testOntology = new File("/Users/alo/Downloads/termspace-owl (3).xml");
        File snomedComplete = new File("/Users/alo/Downloads/conceptsOwlComplete 2.xml");
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

        NodeSet<OWLClass> subClses = reasoner.getSubClasses(invasiveHaem, false);
        Set<OWLClass> clses = subClses.getFlattened();
        long t7 = System.currentTimeMillis();
        System.out.println("All subclasses of invasiveHaem: " + clses.size() + " (" + (t7-t6) + " ms.)");
        for (OWLClass loopClass : clses) System.out.println("       - " + loopClass);
        long t8 = System.currentTimeMillis();
        //Disjunctive
        //Burn 125666000, Head 69536005, Neck 45048000
        OWLClass burn  = df.getOWLClass(IRI.create("http://www.termspace.com/conceptsOwlComplete#125666000"));
        OWLClass head  = df.getOWLClass(IRI.create("http://www.termspace.com/conceptsOwlComplete#69536005"));
        OWLClass neck  = df.getOWLClass(IRI.create("http://www.termspace.com/conceptsOwlComplete#45048000"));

        OWLClassExpression headNeckSites = df.getOWLObjectUnionOf(
                df.getOWLObjectSomeValuesFrom(roleGroup,
                        df.getOWLObjectSomeValuesFrom(findingSite, head)),
                df.getOWLObjectSomeValuesFrom(roleGroup,
                        df.getOWLObjectSomeValuesFrom(findingSite, neck)));
        OWLClassExpression query = df.getOWLObjectIntersectionOf(burn, headNeckSites);
        // Create a fresh name for the query.
        OWLClass newName = df.getOWLClass(IRI.create("temp001"));
        // Make the query equivalent to the fresh class
        OWLAxiom definition = df.getOWLEquivalentClassesAxiom(newName,
                query);
        manager.applyChange(new AddAxiom(ontology, definition));

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
        System.out.println("All subclasses of newName : " + clses.size() + " (" + (t11-t10) + " ms.)");
        for (OWLClass loopClass : clses) System.out.println("       - " + loopClass);

        System.out.println("Finished all in " + (t11-t1) + " ms.");
    }

    private static void printIndented(Tree<OWLAxiom> node, String indent) {
        OWLAxiom axiom = node.getUserObject();
        System.out.println(indent + renderer.render(axiom));
        if (!node.isLeaf()) {
            for (Tree<OWLAxiom> child : node.getChildren()) {
                printIndented(child, indent + "    ");
            }
        }
    }
}
