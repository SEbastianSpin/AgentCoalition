
import com.ai.astar.Node;
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



import agents.MapWebSocketHandler;
import  agents.PackageTask;
import  agents.Package;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;


public class Main {
    public static Queue<PackageTask> generatePackageTasks(int numTasks, int rowBound, int colBound) {
        Random random = new Random();
        Queue<PackageTask> packageTaskQueue = new ArrayDeque<>();

        for(int id=0; id<numTasks; id++){
            int[][] origin = {{random.nextInt(rowBound), random.nextInt(colBound)}};
            int[][] destination = {{random.nextInt(rowBound), random.nextInt(colBound)}};
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
    public static String mapToString(AStar pf) {
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
        return "\n\n" + mapStr;
    }




    public static void main(String[] args) throws Exception {
        System.out.println("Hello world!");
        Random random = new Random();
        int rows = 6;
        int cols = 6;
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
            MapWebSocketHandler.broadcastData(mapToString(aStar),packageTaskQueue);
            //System.out.println("New tasks added: " + newTasks);
        }, 5, 5, TimeUnit.SECONDS);

//        executorPrintMap.scheduleAtFixedRate(() -> {
//            System.out.println(mapToString(aStar));
//        }, 2, 2, TimeUnit.SECONDS);



        Server server = new Server(8080);
        server.setHandler(new WebSocketHandler() {
            @Override
            public void configure(WebSocketServletFactory factory) {
                factory.register(MapWebSocketHandler.class);
            }
        });
        server.start();

    }}