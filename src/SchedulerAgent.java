import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import static java.lang.Thread.sleep;

/*
 * @brief Scheduler agent class which is responsible for converting tasks to packages, for creating groups
 * of transport agents, for sending task locations and handling messages received from transport agents.
 */
public class SchedulerAgent extends Agent {
    /*
     * @brief Queue of pending tasks.
     */
    private final Queue<PackageTask> Task;

    /*
     * @brief Dictionary with key as a task ID and value as group of agents assigned to the task.
     * Used to keep track of the group of agents associated with a task.
     */
    private final Map<Integer, List<AID>> taskGroupMap = new HashMap<>();

    /*
     * @brief Dictionary with key as a task ID and value as the count of agents that reached
     * the location of the task. Used for knowing how many agents reached the location of the task.
     */
    private final Map<Integer, Integer> agentsAtOriginCount = new HashMap<>();

    /*
     * @brief Constructor to initialize task queue.
     */
    public SchedulerAgent(Queue<PackageTask> packageTaskQueue) {
        Task = packageTaskQueue;
    }

    /*
     * @brief Inform agents to proceed to destination as a group. Used once all the agents reach the
     * location of the package.
     */
    private void informAgentsToProceedToDestination(int agentTaskId) {
        List<AID> agents = taskGroupMap.get(agentTaskId);

        /*
         * Sends message to the group leader with a queue of the group members. The group leader
         * moves on behalf of the group and informs the rest of the group members on the completion
         * of the task.
         */
        try {
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.setContentObject((Serializable) agents);
            message.addReceiver(agents.get(0));
            send(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        /*
         * Normal message to other members in the group. Causes them to "zone out" and wait for message
         * from the group leader
         */
        for(int i = 1; i < agents.size(); i++) {
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.setContent("Proceed to destination");
            message.addReceiver(agents.get(i));
            send(message);
        }
    }

    /*
     * @brief searches for agents based on the specified state.
     * @params The service of the agent type requested and the status,
     * typically uses package dfService as PackageTransporter with state as idle.
     */
    private DFAgentDescription[] searchAgents(String dfSerivce, Status state) {
        DFAgentDescription[] result = null;

        try {
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

    /*
     * @brief listen to incoming messages from transport agents.
     */
    private void listenAgents() {
        ACLMessage rcv = receive();

        if(rcv == null) {
            System.out.println("Debug-Scheduler: No message received");
            return;
        }

        switch (rcv.getPerformative()) {
            case ACLMessage.INFORM -> {
                if (rcv.getContent().contains("Completed")) {
                    System.out.println("Debug-Scheduler: Task " + rcv.getContent().split(":")[1] + " Completed");
                } else {
                    String[] parts = rcv.getContent().split(",");
                    String content = rcv.getContent();
                    int agentTaskId = Integer.parseInt(parts[0].trim()); // ox and oy can be used if the robot isnt near the box and wants go to it
                    agentsAtOriginCount.put(agentTaskId, agentsAtOriginCount.getOrDefault(agentTaskId, 0) + 1);
                    if (agentsAtOriginCount.get(agentTaskId) == taskGroupMap.get(agentTaskId).size()) {
                        System.out.println("Debug-Scheduler: For " + agentTaskId + " All agents reached origin");
                        informAgentsToProceedToDestination(agentTaskId);
                    }
                }
            }
            case ACLMessage.ACCEPT_PROPOSAL -> {
                System.out.println("Debug-Scheduler: The task has been accepted by" + rcv.getSender());
            }
        }
    }

    private void assignTask() {
        List<AID> group = new ArrayList<>();
        if (!Task.isEmpty()) {
            DFAgentDescription[] idleAgents = searchAgents("PackageTransporter", Status.IDLE);
            Queue<DFAgentDescription> idleAgentsQueue = new LinkedList<>(Arrays.asList(idleAgents));
            if (idleAgentsQueue.isEmpty()) {
                System.out.println("Debug-Scheduler: There is no available Transport agents for task assignment");
            } else {
                PackageTask task = Task.poll(); // is removed
                if (idleAgentsQueue.size() < task.getNumAgentsRequired()) {
                    System.out.println("Debug-Scheduler: Insufficient agents available.");
                    Task.add(task); // It is not done so need to be placed back
                } else {
                    while (group.size() < task.getNumAgentsRequired() && !idleAgentsQueue.isEmpty()) {
                        group.add(idleAgentsQueue.poll().getName());
                    }
                    System.out.println("Debug-Scheduler: Task needs: " + task.getNumAgentsRequired());
                    System.out.println("Debug-Scheduler: Package Origin - " + task.origin[0][0] + "," + task.origin[0][1] + ", Destination - " + (task.destination[0][0]) + "," + task.destination[0][1] + " Task ID - " + task.id);
                    sendOriginAndDestinationToGroup(group, task);
                    taskGroupMap.put(task.id, group);
                    System.out.println(taskGroupMap);
                }
            }
        }
    }

    private void sendOriginAndDestinationToGroup(List<AID> group, Task task) {
        int adjust = 0;
        for (AID agent : group) {

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
            //THE LOGIC FOR ASSIGNMENT OF PACKAGES CAN BE IMPLEMENTED SOMEWHERE HERE
            assignment.addReceiver(agent);
            send(assignment);
            adjust++;
        }

    }

    @Override
    protected void setup() {

        try {
            sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Debug-Scheduler: Hello! Scheduler-agent " + getAID().getName() + " is ready.");
        addBehaviour(new TickerBehaviour(this, 2000) {
            public void onTick() {
                listenAgents();
                assignTask();
            }
        });
    }

}