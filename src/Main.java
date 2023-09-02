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
    public static Queue<PackageTask> generatePackageTasks(int numTasks, int rowBound, int colBound) {
        Random random = new Random();
        Queue<PackageTask> packageTaskQueue = new ArrayDeque<>();

        for(int id=0; id<numTasks; id++){
            int[][] origin = {{random.nextInt(rowBound), random.nextInt(colBound)}};
            int[][] destination = {{random.nextInt(rowBound), random.nextInt(colBound)}};
//            float weight = (random.nextBoolean()) ? 200f : 400f;
            float weight = 200f;
            Package pkg = new Package(weight, random.nextInt());
            PackageTask task = new PackageTask(id, origin, destination, pkg);
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

            SchedulerAgent schedulerAgent = new SchedulerAgent(packageTaskQueue);
            try {
                AgentController schedulerController = container.acceptNewAgent("SchedulerAgent", schedulerAgent);
                schedulerController.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }

        };
    }
    public static void printMap(AStar pf)
    {
        Node[][] map = pf.getSearchArea();
        StringBuilder mapStr = new StringBuilder();
        for (Node[] nodes : map) {
            mapStr.append("\n|");
            mapStr.append("-----|".repeat(map[0].length));
            mapStr.append("\n|");
            for (int j = 0; j < map[0].length; j++) {
                mapStr.append("  ");
                if (nodes[j].isBlock())
                    mapStr.append(nodes[j].getValue());
                else
                    mapStr.append(" ");
                mapStr.append("  |");
            }
        }
        mapStr.append("\n|");
        mapStr.append("-----|".repeat(map[0].length));
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