package org.krypton;

import org.nustaq.kontraktor.Actor;
import org.nustaq.kontraktor.Callback;
import org.nustaq.kontraktor.IPromise;
import org.nustaq.kontraktor.Promise;
import org.nustaq.kontraktor.remoting.base.ConnectableActor;
import org.nustaq.kontraktor.remoting.websockets.WebSocketConnectable;
import org.nustaq.kontraktor.util.Log;

/**
 * Created by ruedi on 05.06.17.
 *
 * This represents an actual peer connected locally
 */
public class PeerEntry extends RemotePeer {

    KryptonNode node; // if connected
    long latency;
    int latCount;

    public PeerEntry(ConnectableActor connectable) {
        super(connectable);
    }

    public void measure(long latency) {
        this.latency += latency;
        latCount++;
        if ( latCount > 5 ) {
            this.latency = getAvgLatency();
            latCount = 1;
        }
    }

    public boolean isConnected() {
        return node != null && !node.isStopped();
    }

    public IPromise<KryptonNode> connect(Callback<Actor> disconCB) {
        Promise p = new Promise();
        connectable.connect(
            (acc,err) -> {
                node = null;
                Log.Info(this,"acc disconnected "+acc);
            },
            actor -> disconCB.complete(actor,null)
        ).then( (nd,errnd) -> {
            node = (KryptonNode) nd;
            if ( node != null ) {
                Log.Info(this,"node connected "+connectable);
            }
            p.complete(nd,errnd);
        });
        return p;
    }

    public void setLatency(int latency) {
        this.latency = latency;
    }

    public String getId() {
        //fixme: need id method on connectable
        if ( false == connectable instanceof WebSocketConnectable)
            throw new RuntimeException("unexpected type");
        WebSocketConnectable ws = (WebSocketConnectable) connectable;
        return ws.getUrl();
    }

    public ConnectableActor getConnectable() {
        return connectable;
    }

    public KryptonNode getNode() {
        return node;
    }

    public long getAvgLatency() {
        if ( latCount == 0 ) {
            return 1000;
        }
        return latency/latCount;
    }
}
