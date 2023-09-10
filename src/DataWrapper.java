import java.util.Queue;

public class DataWrapper {

    private String[][] astarArray;
    private Queue<PackageTask> packageTaskQueue;

    public DataWrapper(String[][] astarArray,
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
