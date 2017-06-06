package org.krypton;

import org.nustaq.kontraktor.remoting.base.ConnectableActor;
import org.nustaq.kontraktor.remoting.websockets.WebSocketConnectable;

import java.io.Serializable;

/**
 * Created by ruedi on 05.06.17.
 */
public class RemotePeer implements Serializable {

    ConnectableActor connectable;

    public RemotePeer(ConnectableActor connectable) {
        this.connectable = connectable;
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
}
