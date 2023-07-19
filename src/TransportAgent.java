import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

enum States{
    ACTIVE,
    IDLE,
    BROKEN
}
public class TransportAgent extends Agent {
    public States state;
    private boolean handleProposeMessage(ACLMessage message) { //This handler check if transport agent is free for task assignment
        if (state == States.IDLE) {
            ACLMessage reply = message.createReply();
            reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            reply.setContent("as");
            send(reply);
            this.state = States.ACTIVE;
            System.out.println(""+this.getName()+"Started working");
            System.out.println("Accepted proposal from " + message.getSender().getName());
            return true;
        } else {
            System.out.println("Ignoring proposal from " + message.getSender().getName() + " as the agent is not active");
            return false;
        }
    }

    @Override
    protected void setup() {

        this.state = States.IDLE; //It must be idle initially

        // REGISTERING THE TRANSPORT AGENTS TO DF

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

        //RECEIVING MESSAGES

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

    }

}
