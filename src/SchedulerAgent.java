import agents.PackageTask;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.util.*;


import static java.lang.Thread.sleep;



enum Statuss {
    ACTIVE,
    IDLE,
    BROKEN
}

public class SchedulerAgent extends Agent {
    private Queue<PackageTask> Task;

    private Map<Integer, List<AID>> packageGroupMap = new HashMap<>(); // to track agents per package
    private Map<Integer, Integer> agentsAtOriginCount = new HashMap<>();// // package id vs count of agents at origin


    public SchedulerAgent(Queue<PackageTask> packageTaskQueue) {
        Task = packageTaskQueue;
    }

   // private DFAgentDescription[] searchAgents(String dfSerivce, States state) { //When we need to assing more than 1 robot I will add No of agent parameter
   private DFAgentDescription[] searchAgents(String dfSerivce, Statuss state) {
        DFAgentDescription[] result = null;

        try {

            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(dfSerivce);
           // sd.addProperties(new Property("Status", state)); // Filtering only IDLE agents
            //When the program stars Scheduler agent search for all agents and save them in idleAgents list to be used.
            template.addServices(sd);
            result = DFService.search(this, template);
//          for (DFAgentDescription dfad : result) { /it will be used in future
//
//          }

        } catch (FIPAException e) {
            e.printStackTrace();
        }

        return result;
    }

    private void listenAgents() {
        addBehaviour(new CyclicBehaviour(this) {
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
                            if(agentsAtOriginCount.get(agentTaskId) == packageGroupMap.get(agentTaskId).size()){

//                                for(AID agent : packageGroupMap.get(agentTaskId)) {
//                                    ACLMessage proceedMsg = new ACLMessage(ACLMessage.INSTRUCT);
//                                    proceedMsg.addReceiver(agent);
//                                    proceedMsg.setContent("Proceed to destination");
//                                    send(proceedMsg);
//                                }
// ALSO that   packageGroupMap should be removed or something needs to be done
                                System.out.println("For "+agentTaskId+" All agent reached Origin");

                            }

                            // Needs to be updates
                            ///System.out.println(rcv.getSender() + " has done its assignment");
                            break;
                        case ACLMessage.ACCEPT_PROPOSAL:
                            System.out.println("The task has been accepted by" + rcv.getSender());
                            break;
                    }
                }
                block();
            }
        });

    }

    private void assignTask(){
        addBehaviour(new TickerBehaviour(this, 2000) {  //2 seconds
            @Override
            protected void onTick() {

                List<AID> groupAgents = new ArrayList<>();
                if (!Task.isEmpty()) {
                    // DFAgentDescription[] idleAgents = searchAgents("PackageTransporter", States.IDLE);
                    DFAgentDescription[] idleAgents = searchAgents("PackageTransporter", Statuss.IDLE);
                    Queue<DFAgentDescription> idleAgentsQueue = new LinkedList<>(Arrays.asList(idleAgents));


                    if (idleAgentsQueue.isEmpty()) {
                        //System.out.println("There is no available Transport agents for task assignment");
                    }
                    else {
                        PackageTask task =Task.poll(); // is removed
                        if (idleAgentsQueue.size() < task.getNumAgentsRequired()) {
                            System.out.println("No agents available to assign.");
                            Task.add(task); // It is not done so need to be placed back
                        } else {
                            while (groupAgents.size() < task.getNumAgentsRequired() && !idleAgentsQueue.isEmpty()) {
                                groupAgents.add(idleAgentsQueue.poll().getName());
                            }

                            System.out.println("Task needs: "+ task.getNumAgentsRequired());
                            System.out.println("Package Origin - "+ task.origin[0][0]+","+task.origin[0][1] + ", Destination - " + (task.destination[0][0])+","+task.destination[0][1]
                                            + "Task ID - " + task.id
                                    );
                            int adjust = 0;
                            for (AID groupAgent : groupAgents) {

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
                                assignment.setContent(
                                        (task.origin[0][0] + xAdjust) + "," + (task.origin[0][1] + yAdjust) + "," +
                                                task.destination[0][0] + "," + task.destination[0][1] + "," +
                                                 task.id
                                );
                                adjust++;
                                //THE LOGIC FOR ASSIGNMENT OF PACKAGES CAN BE IMPLEMENTED SOMEWHERE HERE
                                assignment.addReceiver(groupAgent);
                                send(assignment);
                                adjust++;
                            }
                            packageGroupMap.put(task.id, groupAgents);
                            System.out.println(packageGroupMap);



            }}}}
            });

            }

//                 else {
//                    System.out.println("No tasks available to assign.");
//                    break;
//
//                }





    @Override
    protected void setup () {

        try {
            sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Hello! Scheduler-agent " + getAID().getName() + " is ready.");

        listenAgents();
        assignTask(); // MUST BE WORKING IN LOOP.


    }

}