import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

enum States{
    ACTIVE,
    IDLE,
    BROKEN
}

public class TransportAgent extends Agent {

    private static int nextId = 0;
    public int id;
    private States state;
    private double probability; // probability of breaking down
    private Random random;

    private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, String>> map;

    public TransportAgent(ConcurrentHashMap factoryMap){
        this.state = States.IDLE; //It must be idle initially
        this.id = nextId++;
        this.probability = 0.5; // probability of breaking down
        this.random = new Random();
        map= factoryMap;
    }

    private void handleProposeMessage(ACLMessage message) {
        if (state == States.IDLE) {
            ACLMessage reply = message.createReply();
            reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            reply.setContent("as");
            send(reply);
            this.state = States.ACTIVE;
            System.out.println(""+this.getName()+" Started working");
            System.out.println("Accepted proposal from " + message.getSender().getName());
        } else {
            System.out.println("Ignoring proposal from " + message.getSender().getName() + " as the agent is not active");
        }
    }

    @Override
    protected void setup() {
        System.out.println("Hello! Transport-Agent "+getAID().getName()+" is ready.");




        DFAgentDescription agentDescription = new DFAgentDescription();
        agentDescription.setName(getAID());

        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("TransportAgent");
        serviceDescription.setName(getLocalName() + "-TransportAgent");

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
                            handleProposeMessage(rcv);
                            break;
                        case ACLMessage.REQUEST:
                            System.out.println(""+rcv.getContent()+"");
                    }
                }
                block();
            }
        });

        addBehaviour(new TickerBehaviour(this, 1000) { // checks 1s
            @Override
            public void onTick() {
                if (state == States.IDLE && random.nextDouble() < probability) { // has to change to States.Active
                    state = States.BROKEN;
                    System.out.println(getName() + " has broken down");
                }
//                else{
//                    System.out.println(getName() + " has not broken down");
//                }
            }
        });
    }
}
