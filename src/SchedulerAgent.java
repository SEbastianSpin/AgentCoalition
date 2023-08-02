import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.Queue;


import static java.lang.Thread.sleep;

public class SchedulerAgent extends Agent {
    private Queue Task;

    public SchedulerAgent(Queue<PackageTask> packageTaskQueue) {
        Task=packageTaskQueue;
    }

    private DFAgentDescription[] searchAgents(String dfSerivce,States state) { //When we need to assing more than 1 robot I will add No of agent parameter

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

    private void assignTask(){

        DFAgentDescription[] idleAgents= searchAgents("PackageTransporter", States.IDLE);

        if(idleAgents.length == 0){
            System.out.println("There is no available Transport agents for task assignment");
        }

        else {
            ACLMessage assignment = new ACLMessage(ACLMessage.PROPOSE);
            for (DFAgentDescription idleAgent : idleAgents) {

                AID agentAID = idleAgent.getName();
                assignment.addReceiver(agentAID);
                assignment.setContent("take the task " + idleAgent + "");
                send(assignment);
            }
            addBehaviour(new CyclicBehaviour(this) {
                @Override
                public void action() {

                    ACLMessage rcv = receive();
                    if (rcv != null && rcv.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                        System.out.println("" + rcv.getContent() + "");
                    }
                    block();
                }
            });
        }
    }



    @Override
    protected void setup() {

        try {
            sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Hello! Scheduler-agent " + getAID().getName() + " is ready.");

        assignTask(); //We need to implement it in a message sending-receiving loop otherwise it will be working once

    }

}