public class Task {
    protected int id;
    protected int[][] origin;
    protected int[][] destination;

    // Constructor
    public Task(int id, int[][] origin, int[][] destination) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
    }
}