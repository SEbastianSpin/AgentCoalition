import com.ai.astar.AStar;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
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
    int curX, curY, width, height;
    protected Status status;
    private double probability; // probability of breaking down
    private Random random;

    public TransportAgent(AStar pf, int X, int Y){
        this.status = Status.IDLE; //It must be idle initially
        this.id = nextId++;
        this.probability = 0.01; // probability of breaking down
        this.random = new Random();
        this.pf = pf;
        this.curX = X;
        this.curY = Y;
    }

    public void setStatus(Status newState) { //Will be used to change the state of agent
        this.status = newState;

        // Update the property value whenever the state changes
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        // Create a new service description with the updated state property
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("TransportAgent");
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

        addBehaviour(new TickerBehaviour(this, 1000) { // checks 1s , PROBABILITY EXPONENTIAL DISTRIBUTION GOOD IDEA
            @Override
            public void onTick() {
                if (status == Status.IDLE && random.nextDouble() < probability) { // has to change to States.Active
                    status = Status.BROKEN;
                    System.out.println(getName() + " has broken down");
                }
            }
        });

    }
}