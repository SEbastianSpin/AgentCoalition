import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import org.omg.CORBA.SystemException;


import java.io.Console;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class SchedulerAgent extends Agent {
    private List<AID> idleAgents;
    public static List<AID> activeAgents;



    private void assignTask(){

        if(idleAgents.isEmpty()){
            System.out.println("There is no available Transport agents for task assignment");

        }
        for(AID idleAgent : idleAgents){
            ACLMessage assignment = new ACLMessage(ACLMessage.PROPOSE);
            assignment.addReceiver(idleAgent);
            assignment.setContent("take the task "+idleAgent+"");
            send(assignment);

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

        idleAgents = new ArrayList<>();

        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("TransportAgent");
        template.addServices(sd);

        try {
            // Use the DF service to search for agents
            DFAgentDescription[] result = DFService.search(this, template);
            for (DFAgentDescription dfad : result) {
                idleAgents.add(dfad.getName());
            }
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        assignTask();


        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage rcv = receive();
                if (rcv != null && rcv.getPerformative()== ACLMessage.ACCEPT_PROPOSAL) {
                    System.out.println(""+rcv.getContent()+"");
                    activeAgents = idleAgents;
                    idleAgents.clear();

                }
                block();
            }
        });


    }



}
