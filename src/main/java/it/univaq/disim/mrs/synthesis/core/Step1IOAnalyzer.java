package it.univaq.disim.mrs.synthesis.core;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputParameter;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaOutputParameter;
import it.univaq.disim.mrs.synthesis.model.RobotTaskMetadata;
import java.util.*;

public class Step1IOAnalyzer {
    public List<RobotTaskMetadata> analyze(String robotId, BpmnModelInstance model) {
        List<RobotTaskMetadata> list = new ArrayList<>();
        Collection<ServiceTask> tasks = model.getModelElementsByType(ServiceTask.class);

        for (ServiceTask task : tasks) {
            RobotTaskMetadata meta = new RobotTaskMetadata();
            meta.setRobotId(robotId);
            meta.setTaskId(task.getId());
            meta.setTaskName(task.getName());

            if (task.getExtensionElements()!= null) {
                // Legge Output (Dati prodotti)
                task.getExtensionElements().getElementsQuery()
                  .filterByType(CamundaOutputParameter.class).list()
                  .forEach(out -> {
                        if ("producedData".equals(out.getCamundaName())) 
                            meta.setProducedDataType(out.getTextContent());
                    });

                // Legge Input (Dati richiesti)
                task.getExtensionElements().getElementsQuery()
                  .filterByType(CamundaInputParameter.class).list()
                  .forEach(in -> {
                        if ("requiredData".equals(in.getCamundaName())) 
                            meta.getRequiredDataTypes().add(in.getTextContent());
                    });
            }
            list.add(meta);
        }
        return list;
    }
}