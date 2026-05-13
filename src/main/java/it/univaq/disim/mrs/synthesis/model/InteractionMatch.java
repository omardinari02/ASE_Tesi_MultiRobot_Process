package it.univaq.disim.mrs.synthesis.model;

public class InteractionMatch {
    private String senderRobot;
    private String senderTaskId;
    private String receiverRobot;
    private String receiverTaskId; // Aggiunto per l'iniezione chirurgica
    private String messageName;

    public InteractionMatch(String sender, String senderTask, String receiver, String receiverTask, String msg) {
        this.senderRobot = sender;
        this.senderTaskId = senderTask;
        this.receiverRobot = receiver;
        this.receiverTaskId = receiverTask;
        this.messageName = msg;
    }

    public String getSenderRobot() { return senderRobot; }
    public String getSenderTaskId() { return senderTaskId; }
    public String getReceiverRobot() { return receiverRobot; }
    public String getReceiverTaskId() { return receiverTaskId; }
    public String getMessageName() { return messageName; }
}