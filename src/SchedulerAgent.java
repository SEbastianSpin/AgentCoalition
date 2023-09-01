import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.*;
import java.util.function.IntToLongFunction;

import static java.lang.Thread.sleep;

public class SchedulerAgent extends Agent {
    private final Queue<PackageTask> Task;
    private final Map<Integer, List<AID>> packageGroupMap = new HashMap<>(); // to track agents per package
    private final Map<Integer, Integer> agentsAtOriginCount = new HashMap<>(); // package id vs count of agents at origin

    public SchedulerAgent(Queue<PackageTask> packageTaskQueue) {
        Task = packageTaskQueue;
    }
    private void listenAgents() {
        addBehaviour(checkForMessagesBehaviour);
    }

    private void assignTask() {
        addBehaviour(taskAssignmentBehaviour);
    }
    protected DFAgentDescription[] searchAgents(String dfSerivce, Status state,int agentNo) {
        DFAgentDescription[] result = null;

        try {
            SearchConstraints c = new SearchConstraints();
            long l = agentNo;
            c.setMaxResults(l);
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(dfSerivce);
            template.addServices(sd);
            result = DFService.search(this, template);

        } catch (FIPAException e) {
            e.printStackTrace();
        }

        return result;
    }
    Behaviour checkForMessagesBehaviour = new CyclicBehaviour(this) {
        @Override
        public void action() {
            ACLMessage rcv = receive();
            if (rcv != null) {
                switch (rcv.getPerformative()) {
                    case ACLMessage.INFORM:
                        String[] parts = rcv.getContent().split(",");
                        String content = rcv.getContent();
                        int agentTaskId = Integer.parseInt(parts[0].trim());//ox and oy can be used if the robot isnt near the box and wants go to it

                        agentsAtOriginCount.put(agentTaskId, agentsAtOriginCount.getOrDefault(agentTaskId, 0) + 1);
                        if (agentsAtOriginCount.get(agentTaskId) == packageGroupMap.get(agentTaskId).size()) {
                            System.out.println("For " + agentTaskId + " All agent reached Origin");
                        }
                        break;
                    case ACLMessage.ACCEPT_PROPOSAL:
                        System.out.println("The task has been accepted by" + rcv.getSender());
                        break;
                }
            }
            block();
        }
    };
    Behaviour taskAssignmentBehaviour = new TickerBehaviour(this, 2000) {
        @Override
        protected void onTick() {


            List<AID> groupAgents = new ArrayList<>();
            if (!Task.isEmpty()) {
                PackageTask task = Task.poll(); // is removed
                int requiredAgent = task.getNumAgentsRequired();
                DFAgentDescription[] idleAgents = searchAgents("PackageTransporter", Status.IDLE,requiredAgent);
                if (idleAgents.length < requiredAgent) {
                    System.out.println("No agents available to assign.");
                    Task.add(task); // It is not done so need to be placed back
                }
                else {

                    System.out.println("Task needs: " + requiredAgent);
                    System.out.println("Package Origin - " + task.origin[0][0] + "," + task.origin[0][1] + ", Destination - " + (task.destination[0][0]) + "," + task.destination[0][1] + " Task ID - " + task.id);
                    int adjust = 0;
                    for (DFAgentDescription groupAgent : idleAgents) {

                        int xAdjust = 0, yAdjust = 0;

                        switch (adjust) {
                            case 0:
                                // No adjustment for the first agent
                                xAdjust = 0;
                                yAdjust = 0;
                                break;
                            case 1:
                                xAdjust = 1;
                                yAdjust = 0;
                                break;
                            case 2:
                                xAdjust = 0;
                                yAdjust = 1;
                                break;
                            case 3:
                                xAdjust = 1;
                                yAdjust = 1;
                                break;
                        }

                        ACLMessage assignment = new ACLMessage(ACLMessage.PROPOSE);
                        assignment.setContent((task.origin[0][0] + xAdjust) + "," + (task.origin[0][1] + yAdjust) + "," + task.destination[0][0] + "," + task.destination[0][1] + "," + task.id);
                        adjust++;
                        //THE LOGIC FOR ASSIGNMENT OF PACKAGES CAN BE IMPLEMENTED SOMEWHERE HERE
                        assignment.addReceiver(groupAgent.getName());
                        send(assignment);
                        adjust++;
                    }
                    for (int i = idleAgents.length - 1; i >= 0; i--) {
                        groupAgents.add(idleAgents[i].getName());
                    }
                    Arrays.fill(idleAgents,null);
                    packageGroupMap.put(task.id, groupAgents);
                    System.out.println(packageGroupMap);
                }
            }
            detectBreakout();

        }
    };

    private void detectBreakout() //THIS FUNCTION WILL DETECT BREAKOUT AND SEND MESSAGE (CONTAINS COORDINATES OF BROKEN AGENT TO THE AGENTRANSPOTER .
    {
        ArrayList<Integer> Coordinates = new ArrayList<>();
        DFAgentDescription[] result = searchAgents("PackageTransporter", Status.BROKEN,1); // DETECTS BROKEN PackageTransporters.

    }

    @Override
    protected void setup() {

        try {
            sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Hello! Scheduler-agent " + getAID().getName() + " is ready.");
        addBehaviour(new TickerBehaviour(this, 2000) {
            public void onTick() {
                listenAgents();
                assignTask();
            }
        });
    }

}