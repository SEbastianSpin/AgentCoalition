
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


    public static void createAgents(int Transportagents, Queue packageTaskQueue, ConcurrentHashMap factoryMap){
        String[] guiArgs = {""};

        jade.Boot.main(guiArgs);
        Runtime rt = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.GUI, "true"); // Enable the GUI
        AgentContainer container = rt.createMainContainer(profile);



        SchedulerAgent schedulerAgent = new SchedulerAgent(packageTaskQueue);
        PackageTransporter  carrier= new PackageTransporter(factoryMap);
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


        ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, String>> factoryMap =
                new ConcurrentHashMap<>();


        // Initialize the map
        for (int i = 0; i < 10; i++) {
            factoryMap.put(i, new ConcurrentHashMap<>());
            for (int j = 0; j < 10; j++) {
                factoryMap.get(i).put(j, "Cell " + i + "," + j);
            }
        }

        // Access the map
        factoryMap.get(5).put(7, "new value");

// Shows Map
        //        for (int i = 0; i < map.size(); i++) {  see Map
//            for (int j = 0; j < map.get(i).size(); j++) {
//                System.out.print(map.get(i).get(j) + "\t");
//            }
//            System.out.println();
//        }


        Queue<PackageTask> packageTaskQueue = generatePackageTasks(3);

        System.out.println(packageTaskQueue);
        createAgents(4,packageTaskQueue,factoryMap);

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            Queue<PackageTask> newTasks = generatePackageTasks(1);
            packageTaskQueue.addAll(newTasks);
            //System.out.println("New tasks added: " + newTasks);
        }, 5, 5, TimeUnit.SECONDS);




    }}