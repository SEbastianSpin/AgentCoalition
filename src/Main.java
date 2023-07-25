
import jade.core.Agent;
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;


public class Main {


    public static Queue<PackageTask> generatePackageTasks(int numTasks) {
        Random random = new Random();
        Queue<PackageTask> packageTaskQueue = new ArrayDeque<>();

        for(int id=0; id<numTasks; id++){
            int[][] origin = {{random.nextInt(10), random.nextInt(10)}};
            int[][] destination = {{random.nextInt(10), random.nextInt(10)}};
            float weight = (random.nextBoolean()) ? 200f : 400f;
            Package pkg = new Package(weight, random.nextInt());
            PackageTask task = new PackageTask(id, origin, destination, pkg);
            packageTaskQueue.add(task);
        }

        return packageTaskQueue;
    }


    public static void createAgents(int Transportagents, Queue packageTaskQueue){
        String[] guiArgs = {"-gui"};
        jade.Boot.main(guiArgs);
        Runtime runtime = Runtime.instance();
        Profile profile = new ProfileImpl();
        AgentContainer container = runtime.createMainContainer(profile);
       // AgentController agentController =

        SchedulerAgent schedulerAgent = new SchedulerAgent(packageTaskQueue);
        TransportAgent  carrier= new TransportAgent();
        try {
            AgentController schedulerController = container.acceptNewAgent("SchedulerAgent", schedulerAgent);
            AgentController TransportController = container.acceptNewAgent("DHL1", carrier);
            schedulerController.start();
            TransportController.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        System.out.println("Hello world!");
        Random random = new Random();


        Queue<PackageTask> packageTaskQueue = generatePackageTasks(2);

        System.out.println(packageTaskQueue);

        System.out.println(packageTaskQueue);

        createAgents(4,packageTaskQueue);




}}