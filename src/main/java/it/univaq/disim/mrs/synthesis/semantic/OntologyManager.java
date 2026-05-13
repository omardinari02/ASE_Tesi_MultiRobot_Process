package it.univaq.disim.mrs.synthesis.semantic;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import openllet.owlapi.OpenlletReasonerFactory;
import java.io.File;

public class OntologyManager {
    private OWLReasoner reasoner;
    private OWLDataFactory dataFactory;
    private final String BASE_IRI = "http://it.univaq.disim/mrs/ontology";

    public void loadOntology(File file) {
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
            this.reasoner = new OpenlletReasonerFactory().createReasoner(ontology);
            this.dataFactory = manager.getOWLDataFactory();
        } catch (Exception e) { System.err.println("Errore: " + e.getMessage()); }
    }

    public boolean isCompatible(String produced, String required) {
        if (produced == null || required == null) return false;
        if (produced.equals(required)) return true;
        
        try {
            OWLClass clsA = dataFactory.getOWLClass(IRI.create(BASE_IRI + "#" + produced));
            OWLClass clsB = dataFactory.getOWLClass(IRI.create(BASE_IRI + "#" + required));
            return reasoner.getSuperClasses(clsA, false).getFlattened().contains(clsB);
        } catch (Exception e) { return false; }
    }
}