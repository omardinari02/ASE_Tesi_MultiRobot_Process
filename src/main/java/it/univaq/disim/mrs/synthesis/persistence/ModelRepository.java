package it.univaq.disim.mrs.synthesis.persistence;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import it.univaq.disim.mrs.synthesis.model.MissionContext;
import it.univaq.disim.mrs.synthesis.generator.UnifiedDiagramBuilder;
import java.io.File;

public class ModelRepository {
    private final UnifiedDiagramBuilder unifiedBuilder = new UnifiedDiagramBuilder();
    private static final String OUTPUT_DIR = "src/main/resources/";

    public BpmnModelInstance loadModel(File file) {
        if (!file.exists()) {
            throw new RuntimeException("File non trovato: " + file.getAbsolutePath());
        }
        try {
            return Bpmn.readModelFromFile(file);
        } catch (Exception e) {
            // Stampiamo l'errore reale del parser Camunda per il debug
            System.err.println("[Parser Error] Errore nel file " + file.getName() + ": " + e.getMessage());
            throw new RuntimeException("Errore fatale di parsing in: " + file.getName(), e);
        }
    }

    public void saveModel(BpmnModelInstance model, String fileName) {
        try {
            Bpmn.writeModelToFile(new File(OUTPUT_DIR + fileName), model);
        } catch (Exception e) {
            System.err.println("Errore salvataggio " + fileName + ": " + e.getMessage());
        }
    }

    public void saveUnifiedDiagram(MissionContext context) {
        System.out.println(" Generazione Unified Collaboration Diagram...");
        try {
            BpmnModelInstance unified = unifiedBuilder.build(context);
            Bpmn.writeModelToFile(new File(OUTPUT_DIR + "UnifiedMission.bpmn"), unified);
            System.out.println(" [Output] File salvato: UnifiedMission.bpmn");
        } catch (Exception e) {
            System.err.println(" Errore diagramma unificato: " + e.getMessage());
        }
    }
}