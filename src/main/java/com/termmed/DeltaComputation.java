package com.termmed;

import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.otf.owltoolkit.classification.ReasonerTaxonomy;
import org.snomed.otf.owltoolkit.classification.ReasonerTaxonomyWalker;
import org.snomed.otf.owltoolkit.conversion.AxiomRelationshipConversionService;
import org.snomed.otf.owltoolkit.conversion.ConversionException;
import org.snomed.otf.owltoolkit.domain.AxiomRepresentation;
import org.snomed.otf.owltoolkit.normalform.RelationshipChangeCollector;
import org.snomed.otf.owltoolkit.normalform.RelationshipNormalFormGenerator;
import org.snomed.otf.owltoolkit.ontology.PropertyChain;
import org.snomed.otf.owltoolkit.service.ClassificationResultsWriter;
import org.snomed.otf.owltoolkit.service.ReasonerServiceException;
import org.snomed.otf.owltoolkit.taxonomy.SnomedTaxonomy;
import org.snomed.otf.owltoolkit.util.TimerUtil;

import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class DeltaComputation {

    private Logger logger = LoggerFactory.getLogger(getClass());
    public void getInferredDelta(OWLReasoner reasoner, Set<Long> ungroupedRoles, SnomedTaxonomy snomedTaxonomy, Set<PropertyChain> propertyChains, OutputStream resultsRf2DeltaArchive, Date startDate) throws ReasonerServiceException {
        String filePath;

        TimerUtil timer = new TimerUtil("Delta computation");
        timer.checkpoint("Inference computation");

        logger.info("Extract ReasonerTaxonomy");
        ReasonerTaxonomyWalker walker = new ReasonerTaxonomyWalker(reasoner, new ReasonerTaxonomy());
        ReasonerTaxonomy reasonerTaxonomy = walker.walk();
        reasoner.dispose();
        timer.checkpoint("Extract ReasonerTaxonomy");

        logger.info("Generate normal form");
        AxiomRelationshipConversionService axiomRelationshipConversionService = new AxiomRelationshipConversionService(ungroupedRoles);
        Map<Long, Set<AxiomRepresentation>> conceptAxiomStatementMap;
        try {
            conceptAxiomStatementMap = axiomRelationshipConversionService.convertAxiomsToRelationships(snomedTaxonomy.getConceptAxiomMap());
        } catch (ConversionException e) {
            throw new ReasonerServiceException("Failed to convert OWL Axiom Expressions into relationships for normal form generation.", e);
        }
        RelationshipNormalFormGenerator normalFormGenerator = new RelationshipNormalFormGenerator(reasonerTaxonomy, snomedTaxonomy, conceptAxiomStatementMap, propertyChains);

        RelationshipChangeCollector changeCollector = new RelationshipChangeCollector(true);
        normalFormGenerator.collectNormalFormChanges(changeCollector);
        timer.checkpoint("Generate normal form");

//        logger.info("{} relationships added, {} removed", changeCollector.getAddedCount(), changeCollector.getRemovedCount());
//
//        logger.info("Inactivating inferred relationships for new inactive concepts");
//        RelationshipInactivationProcessor processor = new RelationshipInactivationProcessor(snomedTaxonomy);
//        RelationshipChangeCollector inactivationCollector = new RelationshipChangeCollector(false);
//        processor.processInactivationChanges(inactivationCollector);
//        changeCollector.getRemovedStatements().putAll(inactivationCollector.getRemovedStatements());

        logger.info("Writing results archive");
        ClassificationResultsWriter classificationResultsWriter = new ClassificationResultsWriter();
        classificationResultsWriter.writeResultsRf2Archive(changeCollector, reasonerTaxonomy.getEquivalentConceptIds(), resultsRf2DeltaArchive, startDate);
        timer.checkpoint("Write results to disk");
        classificationResultsWriter=null;

    }

}
