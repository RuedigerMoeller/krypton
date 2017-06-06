package org.krypton;

/**
 * Created by ruedi on 06.06.17.
 */
public class BenchFloodMessage extends FloodMessage {
    int count;

    public BenchFloodMessage(int count) {
        super();
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}
