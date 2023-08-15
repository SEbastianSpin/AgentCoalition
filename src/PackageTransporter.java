import com.ai.astar.AStar;
import com.ai.astar.Node;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import com.ai.astar.AStar;


import static java.lang.Thread.sleep;

public class PackageTransporter extends TransportAgent {
    public PackageTransporter(AStar pf, int startX, int startY) {
        super(pf, startX, startY);
    }
    private void onWork(int goalX,int goalY) {

        addBehaviour(new TickerBehaviour(this, 2000){
            public void onTick() {
                if (curX == goalX && curY == goalY) {
                    stop();
                    takeDown();
                }
                else
                {
                    int[] cur = pf.move(curX, curY,goalX,goalY,String.valueOf(id));
                    printMap();
                    curX = cur[1];
                    curY = cur[0];
                }
            }

            public void takeDown(){
                setState(States.IDLE);
                System.out.println("Agent " + id + ": Destination reached");
            }
        });



    }

    private void handleProposeMessage(ACLMessage message) throws InterruptedException {


        if (state == States.IDLE) {
            String[] parts = message.getContent().split(",");

            int ox = Integer.parseInt(parts[0].trim());//ox and oy can be used if the robot isnt near the box and wants go to it
            int oy = Integer.parseInt(parts[1].trim());

            int dx = Integer.parseInt(parts[2].trim());
            int dy = Integer.parseInt(parts[3].trim());

            ACLMessage reply = message.createReply();
            reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            send(reply);

            onWork(dx,dy); //It will send the information about where to leave the packages
            setState(States.ACTIVE);
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
        serviceDescription.addProperties(new Property("Status",this.state));
        agentDescription.addServices(serviceDescription);

        try {
            DFService.register(this, agentDescription);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage rcv = receive();
                if (rcv != null) {
                    switch (rcv.getPerformative()) {
                        case ACLMessage.PROPOSE:
                            try {
                                handleProposeMessage(rcv);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        case ACLMessage.REQUEST:
                            System.out.println(""+rcv.getContent()+"");
                            break;
                    }
                }
                block();
            }
        });

    }

    private void printMap()
    {
        Node[][] map = pf.getSearchArea();
        StringBuilder mapStr = new StringBuilder();
        for(int i = 0; i < map.length; i++)
        {
            mapStr.append("\n|");
            for(int k = 0; k < map[0].length; k++)
            {
                mapStr.append("-----|");
            }
            mapStr.append("\n|");
            for(int j = 0; j < map[0].length; j++)
            {
                mapStr.append("  ");
                if(map[i][j].isBlock())
                    mapStr.append(map[i][j].getValue());
                else
                    mapStr.append(" ");
                mapStr.append("  |");
            }
        }
        mapStr.append("\n|");
        for(int k = 0; k < map[0].length; k++) {
            mapStr.append("-----|");
        }
        System.out.println("\n\n" + mapStr);
    }

}
