import com.ai.astar.AStar;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.concurrent.ConcurrentHashMap;

public class AgentTransporter extends TransportAgent {
    public AgentTransporter(AStar pf, int startX, int startY) {
        super(pf, startX, startY);
        type = "AgentTransporter";
    }

    private void messageHandler(){
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage rcv = receive();
                switch (rcv.getPerformative()){
                    case ACLMessage.REQUEST -> {
                        //AGENT WILL MOVE TO THE BROKEN AGENT.
                    }
                }
                block();
            }
        });
    }
    protected void setup() {

        System.out.println("Hello! AGENT-TRANSPORTER "+getAID().getName()+" is ready.");
        registerAgents();
    }


}

