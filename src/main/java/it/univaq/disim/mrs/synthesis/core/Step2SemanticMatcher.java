package it.univaq.disim.mrs.synthesis.core;

import org.camunda.bpm.model.bpmn.instance.*;
import it.univaq.disim.mrs.synthesis.model.*;
import it.univaq.disim.mrs.synthesis.semantic.OntologyManager;

public class Step2SemanticMatcher {
    public void findMatches(MissionContext context, OntologyManager ontology) {
        for (RobotTaskMetadata pMeta : context.getExtractedMetadata()) {
            // Salta i task che non producono dati
            if (pMeta.getProducedDataType() == null) continue;

            for (MessageFlow mf : context.getGlobalChoreography().getModelElementsByType(MessageFlow.class)) {
                String reqMsgName = mf.getMessage().getName();
                String targetRobot = ((Participant) mf.getTarget()).getName();

                if (ontology.isCompatible(pMeta.getProducedDataType(), reqMsgName)) {
                    // Cerca il task nel destinatario che richiede questo input (Analisi Consumi)
                    for (RobotTaskMetadata cMeta : context.getExtractedMetadata()) {
                        if (cMeta.getRobotId().contains(targetRobot) && cMeta.getRequiredDataTypes().contains(reqMsgName)) {
                            context.getValidatedMatches().add(new InteractionMatch(
                                pMeta.getRobotId(), pMeta.getTaskId(), targetRobot, cMeta.getTaskId(), reqMsgName
                            ));
                            System.out.println(" [Match Found] " + pMeta.getRobotId() + " -> " + targetRobot);
                        }
                    }
                }
            }
        }
    }
}