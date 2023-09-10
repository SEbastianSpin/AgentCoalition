import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import com.ai.astar.AStar;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;


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


        for (int i = 0; i < 3; i++) {
            int startX =  9;
            int startY = (i + 1) % pf.getSearchArea()[0].length;

            try {
                AgentTransporter agent = new AgentTransporter(pf, startX, startY);
                AgentController TransportController = container.acceptNewAgent("Repair" + i, agent);
                TransportController.start();
            }
            catch (StaleProxyException e) {
                e.printStackTrace();
            }

        };
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




    public static void main(String[] args) throws Exception {
        System.out.println("Hello world!");
        Random random = new Random();
        int rows = 10;
        int cols = 10;
        AStar aStar = new AStar(rows, cols);
        Queue<PackageTask> packageTaskQueue = generatePackageTasks(2, rows, cols);

        System.out.println(packageTaskQueue);
        createAgents(8, packageTaskQueue,aStar);
        ScheduledExecutorService executorTasks = Executors.newScheduledThreadPool(1); //Periodically adding new tasks
        ScheduledExecutorService executorPrintMap = Executors.newScheduledThreadPool(1);

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
        executor.scheduleAtFixedRate(() -> {
            Queue<PackageTask> newTasks = generatePackageTasks(1, rows, cols);
            packageTaskQueue.addAll(newTasks);

            //   System.out.println("New tasks added: " + newTasks);

        }, 5, 5, TimeUnit.SECONDS);





        Server server = new Server(4000);
        server.setHandler(new WebSocketHandler() {
            @Override
            public void configure(WebSocketServletFactory factory) {
                factory.register(MapWebSocketHandler.class);
            }
        });
        server.start();



        executorPrintMap.scheduleAtFixedRate(() -> {
//            System.out.println(mapToString(aStar));
            printMap(aStar);
            MapWebSocketHandler.broadcastData(aStar.getStringArrayMap(),packageTaskQueue);
        }, 1, 1, TimeUnit.SECONDS);

    }}