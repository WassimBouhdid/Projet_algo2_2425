package algorithm;

import data.Stop;

public class Node implements Comparable<Node> {
    private final Stop stop;
    private final int dist;
    public Node(Stop stop, int dist) {
        this.stop = stop;
        this.dist = dist;
    }
    @Override
    public int compareTo(Node o) {
        return Integer.compare(this.dist, o.dist);
    }

    public Stop getStop() {
        return stop;
    }

    public int getDist() {
        return dist;
    }
}