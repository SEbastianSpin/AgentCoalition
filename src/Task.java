public class Task {
    protected int id;
    protected int[][] origin;
    protected int[][] destination;

    public Task(int id, int[][] origin, int[][] destination) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
    }

    int getId() {
        return id;
    }

    int[][] getOrigin() {
        return origin;
    }

}