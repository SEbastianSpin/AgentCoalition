package Main;
import DStarLiteJava.State;
import DStarLiteJava.DStarLite;
import jade.core.Agent;
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import agents.Transport.TransportAgent;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    public static ReentrantLock[][] locks;
    public static int height = 5;
    public static int width = 5 ;
    static DStarLite pf;
    public static int[][] map;

    public static void createAgents(int agents){
        String[] guiArgs = {"-gui"};
        jade.Boot.main(guiArgs);
        Runtime runtime = Runtime.instance();
        Profile profile = new ProfileImpl();
        AgentContainer container = runtime.createMainContainer(profile);
        try {
            // Create agents
            for (int i = 0; i < agents; i++) {
                int startX = 0;
                int startY = i;
                int goalX = agents - i - 1;
                int goalY = 0;
                Object[] args = new Object[]{startX, startY, goalX, goalY, i+1, locks, map };
                AgentController agentController = container.createNewAgent("Agent" + (i + 1), "agents.Transport.TransportAgent", args);
                agentController.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws InterruptedException {

        locks = new ReentrantLock[width][height];
        map = new int[width][height];
        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                locks[i][j] = new ReentrantLock();
            }
        }
        pf = new DStarLite();
        pf.init(0,0,width - 1,height - 1);

        //impassable objects
        locks[1][1].lock();
        locks[2][1].lock();
        locks[3][1].lock();
        locks[1][3].lock();
        locks[2][3].lock();
        locks[3][3].lock();

        map[1][1] = -1;
        map[1][2] = -1;
        map[1][3] = -1;
        map[3][1] = -1;
        map[3][2] = -1;
        map[3][3] = -1;

        createAgents(4);
    }
}
