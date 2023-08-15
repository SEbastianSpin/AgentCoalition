import com.ai.astar.AStar;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;


import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

enum States{
    ACTIVE,
    IDLE,
    BROKEN
}

public class TransportAgent extends Agent {

    private static int nextId = 0;
    public AStar pf;
    public int id;

    int curX, curY, goalX, goalY, width, height;
    ReentrantLock[][] locks;


    protected States state;
    private double probability; // probability of breaking down
    private Random random;
    private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, String>> map;

    public TransportAgent(AStar pf, int startX, int startY, int goalX, int goalY){
        this.state = States.IDLE; //It must be idle initially
        this.id = nextId++;
        this.probability = 0.01; // probability of breaking down
        this.random = new Random();

        this.pf = pf;
        this.curX = startX;
        this.curY = startY;
        this.goalX = goalX;
        this.goalY = goalY;
    }

    public void setState(States newState) { //Will be used to change the state of agent
        this.state = newState;

        // Update the property value whenever the state changes
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        // Create a new service description with the updated state property
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("TransportAgent");
        serviceDescription.setName(getLocalName() + "-TransportAgent");
        serviceDescription.addProperties(new Property("Status", this.state));
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
                if (state == States.IDLE && random.nextDouble() < probability) { // has to change to States.Active
                    state = States.BROKEN;
                    System.out.println(getName() + " has broken down");
                }
//                else{
//                    System.out.println(getName() + " has not broken down");
//                }
            }
        });


        addBehaviour(movementBehavior);
    }


    private void printPath()
    {
        System.out.println("ID: " + id + "\nCurrent pos: (" + curX + ", " + curY + ")\n" +
                "Goal pos: (" + goalX + ", " + goalY + ")\n" );

    }

    TickerBehaviour movementBehavior = new TickerBehaviour(this, 2000) {
        public void onTick() {
            if (curX == goalX && curY == goalY) {
                stop();
                takeDown();
            }
            else
            {
                int[] cur = pf.move(curX, curY, goalX, goalY, String.valueOf(id));
                curX = cur[1];
                curY = cur[0];
                printPath();
            }
        }

        public void takeDown()
        {
            System.out.println("Agent " + id + ": Destination reached");
        }
    };

}