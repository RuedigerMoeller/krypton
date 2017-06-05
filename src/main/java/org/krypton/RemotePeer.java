package org.krypton;

import org.nustaq.kontraktor.remoting.base.ConnectableActor;

import java.io.Serializable;

/**
 * Created by ruedi on 05.06.17.
 */
public class RemotePeer implements Serializable {

    String id;
    ConnectableActor connectable;

    public RemotePeer(String id, ConnectableActor connectable) {
        this.id = id;
        this.connectable = connectable;
    }

    public String getId() {
        return id;
    }

    public ConnectableActor getConnectable() {
        return connectable;
    }
}
