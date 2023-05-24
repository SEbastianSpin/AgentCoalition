package agents.Transport;
import DStarLiteJava.DStarLite;
import DStarLiteJava.State;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.sleep;

public class TransportAgent extends Agent {

    public DStarLite pf;
    int id, curX, curY, goalX, goalY, width, height;
    ReentrantLock[][] locks;
    int [][] map;
    private void printPath()
    {
        List<State> path = pf.getPath();
        System.out.println("Agent: " + id);
        for (State i : path)
        {
            System.out.print("(" + i.x + "," + i.y + ") ");
        }
        System.out.println();
    }

    private void setBoundary()
    {
        for(int i = 0; i < width; i++)
        {
            pf.updateCell(i, -1, -1);
            pf.updateCell(i, height, -1);
        }
        for(int j = 0; j < height; j++)
        {
            pf.updateCell(-1, j, -1);
            pf.updateCell(width, j, -1);
        }
        pf.updateCell(-1, -1, -1);
        pf.updateCell(width + 1, -1, -1);
        pf.updateCell(width + 1, height + 1, -1);
        pf.updateCell(-1, height + 1, -1);

    }

    private void updateMap()
    {
        pf.init(curX, curY, goalX, goalY);
        setBoundary();
        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                if(map[i][j] != 0)
                    pf.updateCell(i, j, -1);
            }
        }
        pf.replan();
    }
    protected void setup() {
        Object[] attr = getArguments();

        curX =  (int)attr[0];
        curY =  (int)attr[1];
        goalX = (int)attr[2];
        goalY = (int)attr[3];
        id = (int)attr[4];
        locks = (ReentrantLock[][])attr[5];
        map = (int[][])attr[6];
        pf = new DStarLite();
        width = locks[0].length;
        height = locks.length;
        updateMap();
        assert(locks[curX][curY].tryLock());
        printPath();
        try {
            sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        addBehaviour(movementBehavior);
    }

    CyclicBehaviour movementBehavior = new CyclicBehaviour(this) {
        public void action() {
            if (curX == goalX && curY == goalY) {
                takeDown();
            } else {
                 List<State> state = pf.getPath();
                 State next;
                 if(state.size() >= 2) {
                     next = state.get(1);
                     if (locks[next.x][next.y].tryLock()) {
                         locks[next.x][next.y].lock();
                         locks[curX][curY].unlock();
                         map[curX][curY] = 0;
                         map[next.x][next.y] = id;
                         curX = next.x;
                         curY = next.y;
                         pf.init(curX, curY, goalX, goalY);
                     } else {
                         pf.updateCell(next.x, next.y, -1);
                     }
                     updateMap();
                     printPath();
                 }
                 else{
                     System.out.println("No path\n");
                 }
            }
        }

        public void takeDown()
        {
            System.out.println("Agent " + id + ": Destination reached");
        }
    };


}
