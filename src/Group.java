import jade.core.AID;

import java.util.ArrayList;
import java.util.List;

/*
 * @brief Used for creating group/coalition of agents.
 */
public class Group {
    private final int numAgents;
    private final int id;
    private List<PackageTransporter> agents;
    private PackageTransporter leader;
    private char value;

    public Group(int numAgents, int id) {
        this.numAgents = numAgents;
        this.id = id;
        agents = new ArrayList<>();
        value = (char) (id % 26 + 'A');
    }

    List<PackageTransporter> getMembers() {
        return agents;
    }

    public void addMember(PackageTransporter agent, boolean isLeader) {
        agents.add(agent);
        if (isLeader)
            this.leader = agent;
    }

    /*
     * @brief Removes member from the group. Used for removing a broken agent from the group.
     */
    public void removeMember(PackageTransporter agentID) {
        agents.remove(agentID);
    }

    /*
     * @brief Removes all agents except the leader from the map to set the group to move
     * according to the leader's movement.
     */
    public void prepareAgentsToMove()
    {
        for(PackageTransporter agent : agents) {
            if(agent != leader)
                agent.removeFromMap();
        }
        leader.updateNodeValue(value);
    }
}
