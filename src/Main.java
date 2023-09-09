import com.ai.astar.*;
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

public class Main {
    public static int taskID = 0;
    public static Queue<PackageTask> generatePackageTasks(int numTasks, int rowBound, int colBound) {
        Random random = new Random();
        Queue<PackageTask> packageTaskQueue = new ArrayDeque<>();

        for(int i = 0; i <numTasks; i++){
            int[][] origin = {{random.nextInt(rowBound - 1), random.nextInt(colBound - 1)}};
            int[][] destination = {{random.nextInt(rowBound - 1), random.nextInt(colBound - 1)}};
//            float weight = (random.nextBoolean()) ? 200f : 400f;
            float weight = 200f;
            Package pkg = new Package(weight, random.nextInt());
            PackageTask task = new PackageTask(taskID++, origin, destination, pkg);
            packageTaskQueue.add(task);
        }

        return packageTaskQueue;
    }


    public static void createAgents(int transportAgents, Queue packageTaskQueue, AStar pf){
        String[] guiArgs = {""};

        jade.Boot.main(guiArgs);
        Runtime rt = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.GUI, "true"); // Enable the GUI
        AgentContainer container = rt.createMainContainer(profile);

        for (int i = 0; i < transportAgents; i++) {
            int startX =  0;
            int startY = (i + 1) % pf.getSearchArea()[0].length;

            try {
                PackageTransporter agent = new PackageTransporter(pf, startX, startY);
                AgentController TransportController = container.acceptNewAgent("Transport" + i, agent);
                TransportController.start();
            }
            catch (StaleProxyException e) {
                e.printStackTrace();
            }


        };

        SchedulerAgent schedulerAgent = new SchedulerAgent(packageTaskQueue);
        try {
            AgentController schedulerController = container.acceptNewAgent("SchedulerAgent", schedulerAgent);
            schedulerController.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
        AgentTransporter agentTransporter = new AgentTransporter(pf, 0, 0);
        try{
            AgentController agentController = container.acceptNewAgent("AgentTransporter1", agentTransporter);
            agentController.start();
        }
        catch (StaleProxyException e){
            e.printStackTrace();
        }
    }
    public static void printMap(AStar pf)
    {
        String[][] stringMap = pf.getStringArrayMap();
        StringBuilder mapStr = new StringBuilder();
        for (String[] nodes : stringMap) {
            mapStr.append("\n|");
            mapStr.append("-----|".repeat(stringMap[0].length));
            mapStr.append("\n|");
            for (int j = 0; j < stringMap[0].length; j++) {
                mapStr.append("  ");
                mapStr.append(nodes[j]);
                mapStr.append("  |");
            }
        }
        mapStr.append("\n|");
        mapStr.append("-----|".repeat(stringMap[0].length));
        System.out.println("\n\n" + mapStr);
    }

    public static void main(String[] args) {
        System.out.println("Hello world!");
        Random random = new Random();
        int rows = 10;
        int cols = 10;
        AStar aStar = new AStar(rows, cols);
        Queue<PackageTask> packageTaskQueue = generatePackageTasks(2, rows, cols);

        System.out.println(packageTaskQueue);
        createAgents(2, packageTaskQueue,aStar);
        ScheduledExecutorService executorTasks = Executors.newScheduledThreadPool(1); //Periodically adding new tasks
        ScheduledExecutorService executorPrintMap = Executors.newScheduledThreadPool(1);

        executorTasks.scheduleAtFixedRate(() -> {
            Queue<PackageTask> newTasks = generatePackageTasks(1, rows, cols);
            packageTaskQueue.addAll(newTasks);
            //   System.out.println("New tasks added: " + newTasks);
        }, 5, 5, TimeUnit.SECONDS);

        executorPrintMap.scheduleAtFixedRate(() -> {
            printMap(aStar);
        }, 2, 2, TimeUnit.SECONDS);
    }
}