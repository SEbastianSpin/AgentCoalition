package agents;

import com.ai.astar.Node;

import java.util.Queue;

public class DataWrapper {

    private Node[][] astarArray;
    private Queue<PackageTask> packageTaskQueue;

    public DataWrapper(Node[][] astarArray,
                       Queue<PackageTask> packageTaskQueue) {
        this.astarArray = astarArray;
        this.packageTaskQueue = packageTaskQueue;
    }


    public Queue<PackageTask> getPackageTaskQueue() {
        return packageTaskQueue;
    }

    public void setPackageTaskQueue(Queue<PackageTask> packageTaskQueue) {
        this.packageTaskQueue = packageTaskQueue;
    }
}
