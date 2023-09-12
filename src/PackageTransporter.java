import com.ai.astar.AStar;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
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
import java.util.Objects;

import static java.lang.Thread.sleep;

public class PackageTransporter extends TransportAgent {
    List<AID> group;
    private int state = 1;
    private int startX, startY, goalX, goalY, taskId;


    public PackageTransporter(AStar pf, int startX, int startY) {
        super(pf, startX, startY);
        type = "PackageTransporter";
    }

    private void onWork() {
        addBehaviour(PackageTransporterBehavior);
    }

    /*
     * @brief Package transporter behaviour.
     * State 1 is for processing incoming messages from scheduler.
     * State 2 is to move to the location of the task.
     * State 3 is for awaiting incoming message from the leader of the group on completion of the task.
     * State 4 is for the movement as a group to the destination of the task.
     * State 5 is on completion of a task and the disbanding of the group.
     */
        Behaviour PackageTransporterBehavior = new TickerBehaviour(this,2000) {
        public void onTick() {

            switch (state) {
                case (1)-> {
                    System.out.println("Debug-Transporter-" + id + ": In State 1."+status);
                    ACLMessage rcv = receive();
                    if (rcv != null) {
                        processMessageFromScheduler(rcv);
                    } else {
                        //blocking doesn't seem to be working.
                        System.out.println("Debug-Transporter-" + id + ": No message received. Blocking.");
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
                        state = 5;
                        System.out.println("Debug-Transporter-" + id + ": Reached the destination.");
                        informGroupMembers();

                    }
                }
                case (5) -> {
                    setStatus(Status.IDLE);
                    System.out.println("Debug-Transporter-" + id + ": Status set to idle.");
                    state = 1;
                }
            }
        }

    };

    /*
     * @brief Used by the leader of the group to inform its group members of completion of a task.
     */
    private void informGroupMembers() {
        for(AID agent : group)
        {
            if(!Objects.equals(agent.getName(), getAID().getName()))
            {
                ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                message.setContent("Destination reached");
                message.addReceiver(agent);
                send(message);
            }
        }
    }

    /*
     * @brief Used by agents in a group to await completion status from its leader.
     */
    private void waitForCompletionFromLeader(ACLMessage rcv) {
        curX = goalX;
        curY = goalY;
        state = 5;
        System.out.println("Debug-Transporter-" + id + ": Reached the destination. Message received from leader");
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
            case ACLMessage.REQUEST -> System.out.println("Debug-Transporter-" + id + " " + message.getContent() + "");
            case ACLMessage.INFORM -> {
                if(message.hasByteSequenceContent()) {
                    state = 4;
                    value = "A"; // Currently sets group to A by default (Placeholder).
                    pf.updateNode(curX, curY, "A");
                    try {
                        group = (List<AID>) message.getContentObject();
                        pf.clearNode(curX, curY);
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
        if(this.status == Status.ACTIVE) {
            System.out.println("Debug-Transporter-" + id + ": Current Pos: (" + curX + "," + curY + ") Goal: (" + locationX + "," + locationY + ") State: " + state);
            int[] cur = pf.move(curX, curY, locationX, locationY, value);
            curX = cur[1];
            curY = cur[0];
        }
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

        System.out.println("Debug-Transporter-" + id + ": Agent" + getAID().getName() + " is ready.");
        registerAgents();
        startAging();
        onWork();
    }
}