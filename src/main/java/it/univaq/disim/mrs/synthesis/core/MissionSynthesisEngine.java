package it.univaq.disim.mrs.synthesis.core;

import it.univaq.disim.mrs.synthesis.model.*;
import it.univaq.disim.mrs.synthesis.persistence.ModelRepository;
import it.univaq.disim.mrs.synthesis.semantic.OntologyManager;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Orchestratore principale della pipeline di sintesi automatica.
 * Implementa i tre step computazionali descritti nel Metodo Automatico (Capitolo 5).
 */
public class MissionSynthesisEngine {

    private final ModelRepository repository;
    private final OntologyManager ontologyManager;
    private final Step1IOAnalyzer ioAnalyzer;
    private final Step2SemanticMatcher semanticMatcher;
    private final Step3BpmEnhancer bpmEnhancer;

    public MissionSynthesisEngine() {
        this.repository = new ModelRepository();
        this.ontologyManager = new OntologyManager();
        this.ioAnalyzer = new Step1IOAnalyzer();
        this.semanticMatcher = new Step2SemanticMatcher();
        this.bpmEnhancer = new Step3BpmEnhancer();
    }

    /**
     * Esegue il processo completo di sintesi: Analisi I/O -> Matchmaking Semantico -> Iniezione Task.
     * 
     * @param localProcesses Lista di file.bpmn dei processi locali originali dei robot.
     * @param globalChoreo File.bpmn contenente il diagramma di coreografia globale (Specifica Missione).
     * @param ontologyFile File.owl contenente l'ontologia globale per il matchmaking semantico.
     */
    public void runSynthesisPipeline(List<File> localProcesses, File globalChoreo, File ontologyFile) {
        System.out.println("=== [Engine] Inizio Pipeline di Sintesi Automatica ===");

        // 0. Inizializzazione del Contesto della Missione
        MissionContext context = new MissionContext();
        context.setGlobalChoreography(repository.loadModel(globalChoreo));
        ontologyManager.loadOntology(ontologyFile);

        // STEP 1: Analisi Input/Output locale
        // Scansiona i robot per estrarre cosa producono (Output) e cosa consumano (Input) [1, 5.2]
        for (File robotFile : localProcesses) {
            String fileName = robotFile.getName();
            BpmnModelInstance model = repository.loadModel(robotFile);
            context.addRobotModel(fileName, model);
            
            System.out.println(" Analisi task operativi per: " + fileName);
            List<RobotTaskMetadata> metadata = ioAnalyzer.analyze(fileName, model);
            context.getExtractedMetadata().addAll(metadata);
        }

        // STEP 2: Matchmaking Semantico e Disambiguazione
        // Incrocia i dati estratti con i messaggi della coreografia tramite il Reasoner OWL [1, 5.4, 5.7]
        System.out.println(" Esecuzione Matchmaking Semantico (Subsumption)...");
        semanticMatcher.findMatches(context, ontologyManager);

        // STEP 3: Iniezione e Generazione Modelli Enhanced
        // Trasforma chirurgicamente i modelli locali inserendo Send/Receive e Gateway Paralleli [1, 5.12]
        System.out.println(" Trasformazione in Diagrammi Enhanced e gestione Gateway...");
        bpmEnhancer.generateEnhancedModels(context);

        // 4. Fase di Export (Salvataggio Risultati)
        System.out.println(" [Export] Salvataggio degli artefatti sintetizzati...");
        
        // Salvataggio dei singoli file ENHANCED (Drone_enhanced.bpmn, etc.)
        context.getAllRobotModels().forEach((fileName, model) -> {
            String enhancedName = fileName.replace(".bpmn", "_enhanced.bpmn");
            repository.saveModel(model, enhancedName);
            System.out.println("  -> File Enhanced generato: " + enhancedName);
        });

        // Salvataggio del diagramma UNIFIED (Vista olistica della missione MRS) [1, 5.13]
        repository.saveUnifiedDiagram(context);

        System.out.println("=== [Engine] Pipeline terminata con successo. Controlla la cartella output. ===");
    }
}