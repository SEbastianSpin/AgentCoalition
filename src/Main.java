import jade.core.Agent;
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import com.ai.astar.AStar;


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


    public static void createAgents(int Transportagents, Queue packageTaskQueue, AStar pf){
        String[] guiArgs = {""};

        jade.Boot.main(guiArgs);
        Runtime rt = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.GUI, "true"); // Enable the GUI
        AgentContainer container = rt.createMainContainer(profile);

        for (int i = 0; i < Transportagents; i++) {
            int startX =  0;
            int startY = i+1;

            try {

                PackageTransporter agent = new PackageTransporter(pf, startX, startY);
                AgentController TransportController = container.acceptNewAgent("Transport" + i, agent);
                TransportController.start();
            }
            catch (StaleProxyException e) {
                e.printStackTrace();
            }

            SchedulerAgent schedulerAgent = new SchedulerAgent(packageTaskQueue);
            try {
                AgentController schedulerController = container.acceptNewAgent("SchedulerAgent", schedulerAgent);
                schedulerController.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }

        };
    }

    public static void main(String[] args) {
        System.out.println("Hello world!");
        Random random = new Random();
        int rows = 10;
        int cols = 10;
        AStar aStar = new AStar(rows, cols);

        Queue<PackageTask> packageTaskQueue = generatePackageTasks(3);

        System.out.println(packageTaskQueue);
        createAgents(5, packageTaskQueue,aStar);

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1); //Periodically adding new tasks
        executor.scheduleAtFixedRate(() -> {
            Queue<PackageTask> newTasks = generatePackageTasks(1);
            packageTaskQueue.addAll(newTasks);
         //   System.out.println("New tasks added: " + newTasks);
        }, 5, 5, TimeUnit.SECONDS);




    }}