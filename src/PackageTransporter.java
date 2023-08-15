import com.ai.astar.AStar;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;


import static java.lang.Thread.sleep;

public class PackageTransporter extends TransportAgent {
    private int state = 1;
    private int startX, startY, goalX, goalY;
    public PackageTransporter(AStar pf, int startX, int startY) {
        super(pf, startX, startY);
    }
    private void onWork() {
        addBehaviour(new TickerBehaviour(this, 2000){
            public void onTick() {
                switch (state) {
                    case (1) -> {
                        ACLMessage rcv = receive();
                        if (rcv != null) {
                            processMessageFromScheduler(rcv);
                        } else {
                            block();
                        }
                    }
                    case (2) -> {
                        moveToLocation(startX, startY);
                    }
                    case (3) -> {
                        moveToLocation(goalX, goalY);
                    }
                    case (4) -> {
                        setStatus(Status.IDLE);
                        state = 1;
                    }
                }
            }

            public void takeDown(){
                setStatus(Status.IDLE);
                stop();
            }
        });
    }

    private void processMessageFromScheduler(ACLMessage message) {
        switch (message.getPerformative()) {
            case ACLMessage.PROPOSE -> {
                try {
                    handleProposeMessage(message);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            case ACLMessage.REQUEST -> System.out.println("" + message.getContent() + "");
        }
    }
    private void moveToLocation(int locationX, int locationY)
    {
        if (curX == locationX && curY == locationY) {
            state++;
        } else {
            int[] cur = pf.move(curX, curY, locationX, locationY, String.valueOf(id));
            curX = cur[1];
            curY = cur[0];
        }
    }
    private void handleProposeMessage(ACLMessage message) throws InterruptedException {
        if (status == Status.IDLE) {
            String[] parts = message.getContent().split(",");
            startX = Integer.parseInt(parts[0].trim());//ox and oy can be used if the robot isnt near the box and wants go to it
            startY = Integer.parseInt(parts[1].trim());

            goalX = Integer.parseInt(parts[2].trim());
            goalY = Integer.parseInt(parts[3].trim());

            ACLMessage reply = message.createReply();
            reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            send(reply);
            setStatus(Status.ACTIVE);
            state++;
        } else {
            System.out.println("Ignoring proposal from " + message.getSender().getName() + " as the agent is not active");
        }
    }

    @Override
    protected void setup() {

        System.out.println("Hello! PACKAGE-TRANSPORTER "+getAID().getName()+" is ready.");

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
