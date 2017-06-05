package org.krypton;

import org.nustaq.kontraktor.Actor;
import org.nustaq.kontraktor.IPromise;
import org.nustaq.kontraktor.annotations.Local;
import org.nustaq.kontraktor.remoting.base.ConnectableActor;
import org.nustaq.kontraktor.util.Log;
import org.nustaq.kontraktor.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by ruedi on 05.06.17.
 */
public class KNode extends Actor<KNode> {
    private static final long TICK_MILLIS = 1000;
    private Map<String,PeerEntry> peers;
    private String id;
    private RemotePeer remoteSelf;

    @Local
    public IPromise init(List<ConnectableActor> bootStrap) {
        this.id = UUID.randomUUID().toString();
        return resolve();
    }

    public IPromise<Pair<Long,RemotePeer>> ping(long time, RemotePeer sender) {
        return resolve(new Pair(time,remoteSelf));
    }

    public IPromise<Long> pingFast(long time, String senderId) {
        return resolve(time);
    }

    public IPromise<RemotePeer> getRemoteSelf() {
        return resolve(remoteSelf);
    }

    int counter = 0;
    public void cycle() {
        if ( ! isStopped() ) {
            try {
                counter++;
                for ( PeerEntry peerEntry : peers.values() ) {
                    if ( ! peerEntry.isConnected() ) {
                        // connect and firstping
                        peerEntry.connect( (self,err) -> {
                            peers.remove(peerEntry.getId());
                        }).then( (remoteActor,error) -> {
                            long now = System.currentTimeMillis();
                            if ( remoteActor != null ) {
                                remoteActor.ping(now,remoteSelf).then((pair,err) -> {
                                    if ( pair != null ) {
                                        PeerEntry pe = new PeerEntry(now - System.currentTimeMillis(), pair.cdr());
                                        peers.put(pe.getId(),pe);
                                    } else {
                                        Log.Info(this,"ping failed "+peerEntry.getConnectable());
                                    }
                                });
                            } else {
                                Log.Info(this,"connect failed "+peerEntry.getConnectable());
                            }
                        });
                    } else {
                        long now = System.currentTimeMillis();
                        peerEntry.getNode().pingFast(now,id).then((pair,err) -> {
                            if ( pair != null ) {
                                peerEntry.measure(now-System.currentTimeMillis());
                            } else {
                                Log.Info(this,"ping failed "+peerEntry.getConnectable());
                                peers.remove(peerEntry.getId());
                            }
                        });
                    }
                }
                if ( counter%2 == 1 ) {
//                    siblings = nextSiblings;
//                    if (primaryDesc!=null)
//                        siblings.put(primaryDesc.getId(),primaryDesc);
////                    System.out.println("switching: "+nextSiblings.size());
//                    nextSiblings = new HashMap<>();
                }
                if (counter > 100_000) {
                    counter = 0;
                }
            } catch (Throwable th) {
                Log.Info(this,th);
            }
            delayed(TICK_MILLIS, () -> cycle());
        }
    }

}
