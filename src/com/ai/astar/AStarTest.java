package com.ai.astar;
import jade.core.Agent;
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;

import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;


import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class AStarTest {

    public static void createAgents(int agents, AStar pf){
        String[] guiArgs = {""};

        jade.Boot.main(guiArgs);
        Runtime rt = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.GUI, "true"); // Enable the GUI
        AgentContainer container = rt.createMainContainer(profile);
        for (int i = 0; i < agents; i++) {
            int startX =  i;
            int startY = 0;
            int goalX = 0;
            int goalY = i;
            try {

                TransportAgent agent = new TransportAgent(i, pf, startX, startY, goalX, goalY);
                AgentController agentController = container.acceptNewAgent("Transport" + i, agent);
                agentController.start();
            }
            catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args){
        int rows = 3;
        int cols = 3;
        AStar aStar = new AStar(rows, cols);
        createAgents(3, aStar);

    }
}
