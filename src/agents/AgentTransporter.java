//import jade.core.Agent;
//import jade.core.behaviours.CyclicBehaviour;
//import jade.domain.DFService;
//import jade.domain.FIPAAgentManagement.DFAgentDescription;
//import jade.domain.FIPAAgentManagement.Property;
//import jade.domain.FIPAAgentManagement.ServiceDescription;
//import jade.domain.FIPAException;
//import jade.lang.acl.ACLMessage;
//
//import java.util.concurrent.ConcurrentHashMap;
//
//public class AgentTransporter extends TransportAgent {
//    public AgentTransporter(ConcurrentHashMap factoryMap) {
//        super(factoryMap);
//    }
//
//    protected void setup() {
//
//        System.out.println("Hello! AGENT-TRANSPORTER "+getAID().getName()+" is ready.");
//
//        DFAgentDescription agentDescription = new DFAgentDescription();
//        agentDescription.setName(getAID());
//
//        ServiceDescription serviceDescription = new ServiceDescription();
//        serviceDescription.setType("AgentTransporter");
//        serviceDescription.setName(getLocalName() + "-TransportAgent");
//        serviceDescription.addProperties(new Property("Status",this.state));
//        agentDescription.addServices(serviceDescription);
//
//        try {
//            DFService.register(this, agentDescription);
//        } catch (FIPAException e) {
//            e.printStackTrace();
//        }
//
//        addBehaviour(new CyclicBehaviour(this) {
//            @Override
//            public void action() {
//                ACLMessage rcv = receive();
//                if (rcv != null) {
//                    switch (rcv.getPerformative()) {
//                        case ACLMessage.PROPOSE:
//                       //     handleProposeMessage(rcv);
//                            break;
//                        case ACLMessage.REQUEST:
//                            System.out.println(""+rcv.getContent()+"");
//                    }
//                }
//                block();
//            }
//        });
//    }
//
//
//}
//
