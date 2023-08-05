package com.ai.astar;

import com.ai.astar.AStar;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.sleep;

public class TransportAgent extends Agent {

    public AStar pf;
    int id, curX, curY, goalX, goalY, width, height;
    ReentrantLock[][] locks;
    int [][] map;

    public TransportAgent(int id, AStar pf, int startX, int startY, int goalX, int goalY){
        this.pf = pf;
        this.id = id;
        this.curX = startX;
        this.curY = startY;
        this.goalX = goalX;
        this.goalY = goalY;
        printPath();
    }
    private void printPath()
    {
        System.out.println("ID: " + id + "\nCurrent pos: {" + curX + ", " + curY + "}\n" +
                "Goal pos: {" + goalX + ", " + goalY + "}\n" );

    }

    protected void setup() {
        System.out.println("Hello! Transport-Agent "+getAID().getName()+" is ready.");

        addBehaviour(movementBehavior);
    }

    TickerBehaviour movementBehavior = new TickerBehaviour(this, 2000) {
        public void onTick() {
            if (curX == goalX && curY == goalY) {
                stop();
                takeDown();
            }
            else
            {
                int[] cur = pf.move(curX, curY, goalX, goalY, String.valueOf(id));
                curX = cur[0];
                curY = cur[1];
                printPath();
            }
        }

        public void takeDown()
        {
            System.out.println("Agent " + id + ": Destination reached");
        }
    };
}