package it.univaq.disim.mrs.synthesis.model;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import java.util.*;

public class MissionContext {
    private BpmnModelInstance globalChoreography;
    private Map<String, BpmnModelInstance> robotModels = new HashMap<>();
    private List<RobotTaskMetadata> extractedMetadata = new ArrayList<>();
    private List<InteractionMatch> validatedMatches = new ArrayList<>();

    public void setGlobalChoreography(BpmnModelInstance model) { this.globalChoreography = model; }
    public BpmnModelInstance getGlobalChoreography() { return globalChoreography; }
    
    // Aggiungi questo metodo
    public BpmnModelInstance getRobotModel(String id) { return robotModels.get(id); }
    
    public void addRobotModel(String id, BpmnModelInstance model) { robotModels.put(id, model); }
    public Map<String, BpmnModelInstance> getAllRobotModels() { return robotModels; }
    public List<RobotTaskMetadata> getExtractedMetadata() { return extractedMetadata; }
    public List<InteractionMatch> getValidatedMatches() { return validatedMatches; }
}