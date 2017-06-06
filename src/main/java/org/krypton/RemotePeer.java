package org.krypton;

import org.nustaq.kontraktor.remoting.base.ConnectableActor;
import org.nustaq.kontraktor.remoting.websockets.WebSocketConnectable;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by ruedi on 05.06.17.
 */
public class RemotePeer implements Serializable {

    protected ConnectableActor connectable;
    protected Set<RemotePeer> peers;

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

    public RemotePeer peers(final Set<RemotePeer> peers) {
        this.peers = peers;
        return this;
    }

    public Set<RemotePeer> getPeers() {
        return peers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RemotePeer)) return false;

        RemotePeer that = (RemotePeer) o;
        return getConnectableId().equals(that.getConnectableId());
    }

    // workaround as connectableactor does not implement identity/equality TODO @ kontraktor
    public String getConnectableId() {
        return ((WebSocketConnectable)connectable).getUrl();
    }

    @Override
    public int hashCode() {
        return getConnectableId().hashCode();
    }

    @Override
    public String toString() {
        return "RemotePeer{" +
            "connectable=" + connectable +
            ", peers=" + peers +
            ", id=" + getId() +
            '}';
    }
}
