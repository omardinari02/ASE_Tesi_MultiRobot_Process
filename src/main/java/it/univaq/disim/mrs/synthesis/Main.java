package it.univaq.disim.mrs.synthesis;

import it.univaq.disim.mrs.synthesis.core.MissionSynthesisEngine;
import java.io.File;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        MissionSynthesisEngine engine = new MissionSynthesisEngine();
        
        File drone = new File("src/main/resources/Drone.bpmn");
        File cutter = new File("src/main/resources/Tagliatore.bpmn");
        File digger = new File("src/main/resources/Scavatore.bpmn");
        File sower = new File("src/main/resources/Seminatore.bpmn");
        
        File global = new File("src/main/resources/GlobalMission.bpmn");
        File ontology = new File("src/main/resources/MissionOntology.owl");

        // Avvio pipeline con flotta completa
        engine.runSynthesisPipeline(Arrays.asList(drone, cutter, digger, sower), global, ontology);
    }
}