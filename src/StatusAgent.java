//import jade.core.AID;
//import jade.core.Agent;
//import jade.core.behaviours.TickerBehaviour;
//import jade.lang.acl.ACLMessage;
//
//
//public class StatusAgent extends Agent {
//
//
//
//    private void pingAgent(){
//
//       ACLMessage ping = new ACLMessage(ACLMessage.REQUEST);
//
//        for(AID activeAgent : SchedulerAgent.activeAgents){
//
//           ping.addReceiver(activeAgent);
//           System.out.println(""+activeAgent+"");
//
//       }
//        ping.setContent("Are you alive ?");
//       send(ping);
//    }
//
//
//    protected void setup(){
//
//        System.out.println("Hello! Status-Agent "+getAID().getName()+" is ready.");
//        addBehaviour(new TickerBehaviour(this,1000) {
//         @Override
//          protected void onTick() {
//
//             pingAgent();
//
//         }
//
//       });
//
//    }
//
//
//}
