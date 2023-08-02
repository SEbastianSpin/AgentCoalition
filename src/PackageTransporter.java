import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.concurrent.ConcurrentHashMap;
import static java.lang.Thread.sleep;

public class PackageTransporter extends TransportAgent {
    public PackageTransporter(ConcurrentHashMap factoryMap) {
        super(factoryMap);
    }

    private void onWork() {

        try {
            sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("JOB HAS DONE");
        setState(States.IDLE);
    }

    private void handleProposeMessage(ACLMessage message) throws InterruptedException {
        if (state == States.IDLE) {
            ACLMessage reply = message.createReply();
            reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            send(reply);
            setState(States.ACTIVE);
            System.out.println(""+this.getName()+" Started working");
            System.out.println("Accepted proposal from " + message.getSender().getName());
            onWork();

        } else {
            System.out.println("Ignoring proposal from " + message.getSender().getName() + " as the agent is not active");
        }
    }

    @Override
    protected void setup() {

        System.out.println("Hello! PACKAGE-TRANSPORTER "+getAID().getName()+" is ready.");

        DFAgentDescription agentDescription = new DFAgentDescription();
        agentDescription.setName(getAID());

        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("PackageTransporter");
        serviceDescription.setName(getLocalName() + "-TransportAgent");
        serviceDescription.addProperties(new Property("Status",this.state));
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
                            try {
                                handleProposeMessage(rcv);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
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
