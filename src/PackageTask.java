public class PackageTask extends Task {
    private final Package pkg;

    // Constructor
    public PackageTask(int id, int[][] origin, int[][] destination, Package pkg) {
        super(id, origin, destination);
        this.pkg = pkg;
    }

    int getNumAgentsRequired() {
        return (int) (pkg.getWeight() / 100);
    }
}