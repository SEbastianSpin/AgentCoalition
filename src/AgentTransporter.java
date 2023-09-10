import com.ai.astar.AStar;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.concurrent.ConcurrentHashMap;

public class AgentTransporter extends TransportAgent {
    int id;
    public AgentTransporter(AStar pf, int startX, int startY) {
        super(pf, startX, startY);
        type = "AgentTransporter";
    }

    private void messageHandler(){
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage rcv = receive();
                if(rcv != null) {
                    switch (rcv.getPerformative()) {
                        case ACLMessage.INFORM -> {
                            //AGENT WILL MOVE TO THE BROKEN AGENT.
                            goalX = Integer.parseInt(rcv.getContent().split(" ")[0]);
                            goalY = Integer.parseInt(rcv.getContent().split(" ")[1]);
                            System.out.println(getAgent()+" I have to save agent at "+goalX+" "+goalY);
                            setStatus(Status.ACTIVE);

                        }
                    }
                    block();
                }
            }
        });
    }

    public void moveToRepair(int locationX, int locationY) {

        System.out.println("Repair-Transporter-" + id + ": Current Pos: (" + curX + "," + curY + ") Goal: (" + locationX + "," + locationY+")");
        int[] cur = pf.move(curX, curY, locationX, locationY, "R");
        curX = cur[1];
        curY = cur[0];
    }
    private void saveAgent(){
        addBehaviour(new TickerBehaviour(this,2000) {
            @Override
            protected void onTick() {
                if(status == Status.ACTIVE) {
                    moveToRepair(goalX, goalY);
                }
            }
        });

    }
    protected void setup() {

        System.out.println("Hello! AGENT-TRANSPORTER "+getAID().getName()+" is ready.");
        registerAgents();
        saveAgent();
        messageHandler();

    }


}

