package org.krypton;

import org.nustaq.kontraktor.Actor;
import org.nustaq.kontraktor.IPromise;
import org.nustaq.kontraktor.annotations.Local;
import org.nustaq.kontraktor.remoting.base.ConnectableActor;
import org.nustaq.kontraktor.remoting.websockets.WebSocketConnectable;
import org.nustaq.kontraktor.remoting.websockets.WebSocketPublisher;
import org.nustaq.kontraktor.util.Log;
import org.nustaq.kontraktor.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ruedi on 05.06.17.
 */
public class KryptonNode extends Actor<KryptonNode> {
    private static final long TICK_MILLIS = 5000;
    private Map<String,PeerEntry> peers;
    private RemotePeer remoteSelf;

    @Local
    public IPromise init(List<String> bootStrap, ConnectableActor self) {
        peers = new HashMap<>();
        remoteSelf = new RemotePeer(self);
        bootStrap.stream()
            .forEach( str -> {
                WebSocketConnectable wsc = new WebSocketConnectable(KryptonNode.class, str);
                PeerEntry remotePeer = new PeerEntry(wsc);
                peers.put(remotePeer.getId(),remotePeer);
            });

        self().cycle();
        return resolve();
    }

    public IPromise<Pair<Long,RemotePeer>> pingInit(long time, RemotePeer sender) {
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
                // ping or try connect each peer
                for ( PeerEntry peerEntry : peers.values() ) {
                    if ( ! peerEntry.isConnected() ) {
                        // connect and firstping
                        peerEntry.connect( (self,err) -> {
                            peers.remove(peerEntry.getId());
                        }).then( (remoteActor,error) -> {
                            long now = System.currentTimeMillis();
                            if ( remoteActor != null ) {
                                remoteActor.pingInit(now,remoteSelf).then((pair, err) -> {
                                    if ( pair != null ) {
                                        //
                                    } else {
                                        Log.Info(this,"ping failed "+peerEntry.getConnectable()+":"+err);
                                    }
                                });
                            } else {
                                Log.Info(this,"connect failed "+peerEntry.getConnectable());
                            }
                        });
                    } else {
                        long now = System.currentTimeMillis();
                        peerEntry.getNode().pingFast(now,remoteSelf.getId()).then((pair,err) -> {
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

    public static void main(String[] args) {
        KryptonNodeArgs kna = KryptonNodeArgs.parseCommandLine(args, new KryptonNodeArgs());

        KryptonNode kn = AsActor(KryptonNode.class);
        kn.init(
            kna.getBootstrap(),
            new WebSocketConnectable(KryptonNode.class,"ws://"+kna.getHost()+":"+kna.getPort())
        );

        WebSocketPublisher pub = new WebSocketPublisher(kn,kna.getHost(),"/",kna.getPort());
        pub.publish( act -> {
           Log.Info(KryptonNode.class,"server disconnected");
        });
    }

}
