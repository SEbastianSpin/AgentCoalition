
public class PackageTask extends Task {
    private Package pkg;

    // Constructor
    public PackageTask(int id, int[][] origin, int[][] destination, Package pkg) {
        super(id, origin, destination);
        this.pkg = pkg;
    }

    public float getNumAgentsRequired() {
        return  pkg.getWeight()/100; ///Should be changed it
    }
}