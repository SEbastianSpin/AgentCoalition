import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import java.util.Queue;


import static java.lang.Thread.sleep;

public class SchedulerAgent extends Agent {
    private Queue Task;


    public SchedulerAgent(Queue<PackageTask> packageTaskQueue) {
        Task = packageTaskQueue;
    }

    private DFAgentDescription[] searchAgents(String dfSerivce, States state) { //When we need to assing more than 1 robot I will add No of agent parameter

        DFAgentDescription[] result = null;

        try {

            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(dfSerivce);
            sd.addProperties(new Property("Status", state)); // Filtering only IDLE agents
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
                            System.out.println(rcv.getSender() + " has done its assignment");
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

        DFAgentDescription[] idleAgents = searchAgents("PackageTransporter", States.IDLE);

        if (idleAgents.length == 0) {
            System.out.println("There is no available Transport agents for task assignment");

        }
        else {


            for (DFAgentDescription idleAgent : idleAgents) {
                ACLMessage assignment = new ACLMessage(ACLMessage.PROPOSE);
                AID agentAID = idleAgent.getName();
                if (!Task.isEmpty()) {
                    Task task = (Task) Task.poll();
                    System.out.println("Package Origin - "+ task.origin[0][0]+","+task.origin[0][1] + ", Destination - " + task.destination[0][0]+","+task.destination[0][1]);
                    assignment.setContent(task.origin[0][0]+","+task.origin[0][1] +","+ task.destination[0][0]+","+task.destination[0][1]);
                    assignment.addReceiver(agentAID);
                    //THE LOGIC FOR ASSIGNMENT OF PACKAGES CAN BE IMPLEMENTED SOMEWHERE HERE
                    send(assignment);

                } else {
                    System.out.println("No tasks available to assign.");
                    break;

                }
            }
        }
    }


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
