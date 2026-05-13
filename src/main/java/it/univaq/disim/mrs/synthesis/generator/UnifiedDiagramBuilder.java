package it.univaq.disim.mrs.synthesis.generator;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnDiagram;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnPlane;
import it.univaq.disim.mrs.synthesis.model.*;
import java.util.HashMap;
import java.util.Map;

public class UnifiedDiagramBuilder {

    public BpmnModelInstance build(MissionContext context) {
        BpmnModelInstance modelInstance = Bpmn.createEmptyModel();
        Definitions definitions = modelInstance.newInstance(Definitions.class);
        definitions.setTargetNamespace("http://it.univaq.disim/mrs/unified");
        modelInstance.setDefinitions(definitions);

        Collaboration collaboration = modelInstance.newInstance(Collaboration.class);
        collaboration.setId("Collaboration_Global_MRS");
        definitions.addChildElement(collaboration);

        BpmnDiagram diagram = modelInstance.newInstance(BpmnDiagram.class);
        BpmnPlane plane = modelInstance.newInstance(BpmnPlane.class);
        plane.setBpmnElement(collaboration);
        diagram.setBpmnPlane(plane);
        definitions.addChildElement(diagram);

        Map<String, Participant> pools = new HashMap<>();
        double currentY = 100;

        for (Participant p : context.getGlobalChoreography().getModelElementsByType(Participant.class)) {
            Participant pool = modelInstance.newInstance(Participant.class);
            pool.setName(p.getName());
            pool.setId("Pool_" + p.getName());
            collaboration.addChildElement(pool);
            pools.put(p.getName(), pool);

            BpmndiGenerator.createShape(modelInstance, plane, pool, 100, currentY, 600, 150);
            currentY += 200;
        }

        for (InteractionMatch match : context.getValidatedMatches()) {
            String senderKey = match.getSenderRobot().replace(".bpmn", "");
            Participant source = pools.get(senderKey);
            Participant target = pools.get(match.getReceiverRobot());

            if (source!= null && target!= null) {
                MessageFlow msgFlow = modelInstance.newInstance(MessageFlow.class);
                msgFlow.setId("Flow_" + System.currentTimeMillis() + "_" + Math.random());
                msgFlow.setSource(source);
                msgFlow.setTarget(target);
                collaboration.addChildElement(msgFlow);
            }
        }
        return modelInstance;
    }
}