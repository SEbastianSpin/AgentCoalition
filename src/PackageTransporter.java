import com.ai.astar.AStar;
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.List;

public class PackageTransporter extends TransportAgent {
    List<AID> group;
    private int state = 1;
    private AID schedulerAID;
    private int startX, startY, goalX, goalY, taskId;

    public PackageTransporter(AStar pf, int startX, int startY) {
        super(pf, startX, startY);
    }

    private void onWork() {
        addBehaviour(new TickerBehaviour(this, 2000) {
            public void onTick() {
                switch (state) {
                    case (1)-> {
                        ACLMessage rcv = receive();
                        if (rcv != null) {
                            processMessageFromScheduler(rcv);
                        } else {
                            block();
                        }
                    }
                    case (2) -> {
                        moveToLocation(startX, startY);
                        if (curX == startX && curY == startY) {
                            state = 1;
                            ACLMessage informMsg = new ACLMessage(ACLMessage.INFORM);
                            informMsg.addReceiver(schedulerAID);
                            informMsg.setContent(taskId + " ,");
                            send(informMsg);
                        }
                    }
                    case(3) -> {
                        ACLMessage rcv = receive();
                        if (rcv != null) {
                            waitForCompletionFromLeader(rcv);
                        } else {
                            block();
                        }
                    }
                    case(4) -> {
                        moveToLocation(goalX, goalY);
                        if(curX == goalX && curY == goalY)
                        {
                            ACLMessage informMsg = new ACLMessage(ACLMessage.INFORM);
                            informMsg.addReceiver(schedulerAID);
                            informMsg.setContent("Completed Task:" + taskId);
                            send(informMsg);
                            state++;
                            System.out.println("Debug-Transporter-" + id + ": Reached the destination.");
                            informGroupMembers();
                        }
                    }
                    case (5) -> {
                        setStatus(Status.IDLE);
                        state = 1;
                    }
                }
            }

            public void takeDown() {
                setStatus(Status.IDLE);
                stop();
            }
        });
    }

    private void informGroupMembers() {
        for(AID agent : group)
        {
            if(agent != getAID())
            {
                ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                message.setContent("Destination reached");
                message.addReceiver(agent);
                send(message);
            }
        }
    }

    private void waitForCompletionFromLeader(ACLMessage rcv) {
        curX = goalX;
        curY = goalY;
        state = 5;
        System.out.println("Debug-Transporter-" + id + ": Reached the destination. Message received from leader");
    }

    /*
     * @brief Used to remove the individual agents from the map when the agents move as a group.
     */
    public void removeFromMap() {
        pf.clearNode(curX, curY);
    }

    private void processMessageFromScheduler(ACLMessage message) {

        if (schedulerAID == null) {
            schedulerAID = message.getSender();
        }

        switch (message.getPerformative()) {
            case ACLMessage.PROPOSE -> {
                try {
                    handleProposeMessage(message);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            case ACLMessage.REQUEST -> System.out.println("" + message.getContent() + "");
            case ACLMessage.INFORM -> {
                if(message.hasByteSequenceContent()) {
                    state = 4;
                    value = "A";
                    pf.updateNode(curX, curY, "A");
                    try {
                        group = (List<AID>) message.getContentObject();
                    } catch (UnreadableException e) {
                        throw new RuntimeException(e);
                    }
                }
                else
                {
                    state = 3;
                    pf.clearNode(curX, curY);
                }
            }
        }
    }

    public void moveToLocation(int locationX, int locationY) {
        int[] cur = pf.move(curX, curY, locationX, locationY, value);
        curX = cur[1];
        curY = cur[0];
    }

    private void handleProposeMessage(ACLMessage message) throws InterruptedException {
        if (status == Status.IDLE) {
            setStatus(Status.ACTIVE);
            String[] parts = message.getContent().split(",");
            startX = Integer.parseInt(parts[0].trim());//ox and oy can be used if the robot isnt near the box and wants go to it
            startY = Integer.parseInt(parts[1].trim());

            goalX = Integer.parseInt(parts[2].trim());
            goalY = Integer.parseInt(parts[3].trim());
            taskId = Integer.parseInt(parts[4].trim());

            ACLMessage reply = message.createReply();
            reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            send(reply);
            state++;
        } else {
            System.out.println("Ignoring proposal from " + message.getSender().getName() + " as the agent is not active");
        }
    }

    @Override
    protected void setup() {

        System.out.println("Hello! PACKAGE-TRANSPORTER " + getAID().getName() + " is ready.");

        DFAgentDescription agentDescription = new DFAgentDescription();
        agentDescription.setName(getAID());

        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("PackageTransporter");
        serviceDescription.setName(getLocalName() + "-TransportAgent");
        serviceDescription.addProperties(new Property("Status", this.status));
        agentDescription.addServices(serviceDescription);

        try {
            DFService.register(this, agentDescription);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        onWork();
    }
}
