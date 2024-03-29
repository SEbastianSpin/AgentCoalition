import com.ai.astar.AStar;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.util.Random;

enum Status {
    ACTIVE,
    IDLE,
    BROKEN
}

public class TransportAgent extends Agent {
    private static int nextId = 0;
    public AStar pf;
    public int id;

    protected AID schedulerAID;
    protected int curX, curY,goalX,goalY,startX,startY, taskId;
    protected Status status;

    protected String type;
    private Random random;
    protected double reliabiltiy; // probability of staying active
    private double age; //time it spends till breakdown


    //private double lambda = 0.9;
   private double lambda = 0.00333333333;


    public String value;

    public TransportAgent(AStar pf, int X, int Y) {
        this.status = Status.IDLE; //It must be idle initially
        this.id = nextId++;
        this.reliabiltiy = 1; //initially each robot has the value 1


        this.random = new Random();
        this.pf = pf;
        this.curX = X;
        this.curY = Y;
        value = String.valueOf(id);
    }

    protected void startAging() {
        addBehaviour(new TickerBehaviour(this, 2000) {
            @Override
            protected void onTick() {
                if (status == Status.ACTIVE) {
                    age++;
                    reliabiltiy = Math.exp(-lambda * age);
                    System.out.println("Debug-Transport "+ id +" Reliability = "+ reliabiltiy);
                    if ( random.nextDouble(1)> reliabiltiy) { // has to change to States.Active
                        setStatus(Status.BROKEN);
                        age = 0;
                        System.out.println(getName() + " has broken down");
                        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                        message.setContent("I am broken "+curX+" "+curY);
                        message.addReceiver(schedulerAID);
                        send(message);

                    }
                }
            }
        });

    }
    protected void registerAgents(){
        DFAgentDescription agentDescription = new DFAgentDescription();

        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType(type);
        serviceDescription.setName(getLocalName() + "-TransportAgent");
        serviceDescription.addProperties(new Property("Status", this.status));
        agentDescription.addServices(serviceDescription);

        try {
            DFService.register(this, agentDescription);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
    public void setStatus(Status newState) { //Will be used to change the state of agent
        this.status = newState;

        // Update the property value whenever the state changes
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        // Create a new service description with the updated state property
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType(this.type);
        serviceDescription.setName(getLocalName() + "-TransportAgent");
        serviceDescription.addProperties(new Property("Status", this.status));
        dfd.addServices(serviceDescription);

        try {
            DFService.modify(this, dfd);

        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    @Override
    protected void setup() {

    }

}