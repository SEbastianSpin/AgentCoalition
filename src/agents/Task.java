package agents;

public class Task {
    public int id;
    public int[][] origin;
    public int[][] destination;


    public Task(int id, int[][] origin, int[][] destination) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
    }

    int  getId(){return id;}

    int[][] getOrigin() {return  origin;}



}