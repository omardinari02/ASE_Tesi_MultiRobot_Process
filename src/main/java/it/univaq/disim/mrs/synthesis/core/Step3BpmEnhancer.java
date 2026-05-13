package it.univaq.disim.mrs.synthesis.core;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnDiagram;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnPlane;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnShape;
import it.univaq.disim.mrs.synthesis.model.*;
import it.univaq.disim.mrs.synthesis.generator.BpmndiGenerator;
import java.util.*;

public class Step3BpmEnhancer {

    public void generateEnhancedModels(MissionContext context) {
        // 1. GESTIONE MITTENTI (Invio e Fork 1->N con Parallel Gateway)
        Map<String, List<InteractionMatch>> taskOutputs = new HashMap<>();
        context.getValidatedMatches().forEach(m -> 
            taskOutputs.computeIfAbsent(m.getSenderTaskId(), k -> new ArrayList<>()).add(m));

        taskOutputs.forEach((taskId, matches) -> {
            BpmnModelInstance model = context.getAllRobotModels().get(matches.get(0).getSenderRobot());
            if (model!= null) {
                BpmnPlane plane = ensureDIInitialized(model);
                if (matches.size() > 1) {
                    injectParallelGateway(model, plane, taskId, matches);
                } else {
                    injectSingleTask(model, plane, taskId, matches.get(0).getMessageName(), true);
                }
            }
        });

        // 2. GESTIONE DESTINATARI (Ricezione chirurgica)
        for (InteractionMatch m : context.getValidatedMatches()) {
            BpmnModelInstance model = context.getAllRobotModels().get(m.getReceiverRobot() + ".bpmn");
            if (model!= null) {
                BpmnPlane plane = ensureDIInitialized(model);
                injectSingleTask(model, plane, m.getReceiverTaskId(), m.getMessageName(), false);
            }
        }
    }

    private void injectParallelGateway(BpmnModelInstance model, BpmnPlane plane, String taskId, List<InteractionMatch> matches) {
        ServiceTask source = model.getModelElementById(taskId);
        
        ParallelGateway fork = model.newInstance(ParallelGateway.class);
        fork.setId("Fork_" + System.currentTimeMillis());
        source.getParentElement().addChildElement(fork);
        
        double sourceX = getX(model, source);
        double sourceY = getY(model, source);
        
        // Disegna il Gateway
        BpmndiGenerator.createShape(model, plane, fork, sourceX + 150, sourceY + 15, 50, 50);

        reconnect(model, plane, source, fork);

        double startY = sourceY - (matches.size() * 50);
        for (int i = 0; i < matches.size(); i++) {
            InteractionMatch m = matches.get(i);
            SendTask send = model.newInstance(SendTask.class);
            send.setId("Send_" + System.currentTimeMillis() + "_" + i);
            send.setName("Invia: " + m.getMessageName());
            fork.getParentElement().addChildElement(send);
            
            // Disegna il SendTask
            BpmndiGenerator.createShape(model, plane, send, sourceX + 300, startY + (i * 100), 100, 80);
            connect(model, plane, fork, send);
        }
        System.out.println(" [1->N] Gateway parallelo e task di invio inseriti graficamente nel robot: " + matches.get(0).getSenderRobot());
    }

    private void injectSingleTask(BpmnModelInstance model, BpmnPlane plane, String taskId, String msg, boolean isSender) {
        ServiceTask task = model.getModelElementById(taskId);
        if (task == null) return;

        Task interaction = isSender? model.newInstance(SendTask.class) : model.newInstance(ReceiveTask.class);
        interaction.setId((isSender? "Send_" : "Receive_") + System.currentTimeMillis() + "_" + Math.random());
        interaction.setName((isSender? "Invia: " : "Ricevi: ") + msg);
        task.getParentElement().addChildElement(interaction);

        double x = getX(model, task);
        double y = getY(model, task);
        
        // Posiziona il nuovo task a destra (+150) o a sinistra (-150)
        BpmndiGenerator.createShape(model, plane, interaction, isSender? x + 150 : x - 150, y, 100, 80);

        if (isSender) reconnect(model, plane, task, interaction); 
        else reconnectReceiver(model, plane, task, interaction);
    }

    private BpmnPlane ensureDIInitialized(BpmnModelInstance model) {
        BpmnPlane plane = model.getModelElementsByType(BpmnPlane.class).stream().findFirst().orElse(null);
        if (plane == null) {
            BpmnDiagram diagram = model.newInstance(BpmnDiagram.class);
            plane = model.newInstance(BpmnPlane.class);
            org.camunda.bpm.model.bpmn.instance.Process process = model.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.Process.class).iterator().next();
            plane.setBpmnElement(process);
            diagram.setBpmnPlane(plane);
            model.getDefinitions().addChildElement(diagram);
        }
        return plane;
    }

    private void reconnect(BpmnModelInstance model, BpmnPlane plane, FlowNode src, FlowNode trg) {
        if (!src.getOutgoing().isEmpty()) {
            SequenceFlow out = src.getOutgoing().iterator().next();
            out.setSource(trg); // Aggancia la freccia uscente originaria al nuovo task 
        }
        connect(model, plane, src, trg);
    }

    private void reconnectReceiver(BpmnModelInstance model, BpmnPlane plane, FlowNode cons, FlowNode rec) {
        if (!cons.getIncoming().isEmpty()) {
            SequenceFlow in = cons.getIncoming().iterator().next();
            in.setTarget(rec);
        }
        connect(model, plane, rec, cons);
    }

    // Qui creiamo sia l'elemento logico (SequenceFlow) che la grafica della freccia (BpmnEdge)
    private void connect(BpmnModelInstance model, BpmnPlane plane, FlowNode from, FlowNode to) {
        SequenceFlow flow = model.newInstance(SequenceFlow.class);
        flow.setId("Flow_Injected_" + System.currentTimeMillis() + "_" + (int)(Math.random()*1000));
        flow.setSource(from);
        flow.setTarget(to);
        from.getParentElement().addChildElement(flow);

        // Calcolo dimensioni in base al tipo di nodo (Gateway = 50x50, Task = 100x80)
        double w1 = from instanceof Gateway? 50 : 100;
        double h1 = from instanceof Gateway? 50 : 80;
        double w2 = to instanceof Gateway? 50 : 100;
        double h2 = to instanceof Gateway? 50 : 80;

        // Calcolo dei punti di ancoraggio per le linee
        double startX = getX(model, from) + w1;      // Bordo destro del mittente
        double startY = getY(model, from) + (h1 / 2); // Centro verticale
        double endX = getX(model, to);               // Bordo sinistro del destinatario
        double endY = getY(model, to) + (h2 / 2);    // Centro verticale

        // Disegna fisicamente la freccia
        BpmndiGenerator.createEdge(model, plane, flow, startX, startY, endX, endY);
    }

    private double getX(BpmnModelInstance m, BaseElement e) {
        return m.getModelElementsByType(BpmnShape.class).stream()
             .filter(s -> e.equals(s.getBpmnElement()))
             .map(s -> s.getBounds().getX()).findFirst().orElse(100.0);
    }

    private double getY(BpmnModelInstance m, BaseElement e) {
        return m.getModelElementsByType(BpmnShape.class).stream()
             .filter(s -> e.equals(s.getBpmnElement()))
             .map(s -> s.getBounds().getY()).findFirst().orElse(100.0);
    }
}