package org.krypton;

import org.nustaq.kontraktor.Actor;
import org.nustaq.kontraktor.Callback;
import org.nustaq.kontraktor.IPromise;
import org.nustaq.kontraktor.remoting.base.ConnectableActor;
import org.nustaq.kontraktor.util.Log;

/**
 * Created by ruedi on 05.06.17.
 */
public class PeerEntry {

    String id;
    ConnectableActor connectable;
    KNode node; // if connected
    long latency;
    int latCount;

    public PeerEntry(String id, ConnectableActor connectable) {
        this.id = id;
        this.connectable = connectable;
    }

    public PeerEntry(long latency, RemotePeer rp) {
        this.id = rp.getId();
        this.connectable = rp.getConnectable();
        measure(latency);
    }

    public void measure(long latency) {
        this.latency += latency;
        latCount++;
    }

    public boolean isConnected() {
        return node != null && !node.isStopped();
    }

    public IPromise<KNode> connect(Callback<Actor> disconCB) {
        return connectable.connect(
            (acc,err) -> {
                node = null;
                Log.Info(this,"acc disconnected "+acc);
            },
            actor -> disconCB.complete(actor,null)
        );
    }

    public void setLatency(int latency) {
        this.latency = latency;
    }

    public String getId() {
        return id;
    }

    public ConnectableActor getConnectable() {
        return connectable;
    }

    public KNode getNode() {
        return node;
    }

    public long getLatency() {
        return latency/latCount;
    }
}
