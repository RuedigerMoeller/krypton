package org.krypton;

import org.nustaq.kontraktor.Actor;
import org.nustaq.kontraktor.IPromise;
import org.nustaq.kontraktor.annotations.Local;
import org.nustaq.kontraktor.impl.DispatcherThread;
import org.nustaq.kontraktor.remoting.base.ConnectableActor;
import org.nustaq.kontraktor.remoting.websockets.WebSocketConnectable;
import org.nustaq.kontraktor.remoting.websockets.WebSocketPublisher;
import org.nustaq.kontraktor.util.Log;
import org.nustaq.kontraktor.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by ruedi on 05.06.17.
 */
public class KryptonNode extends Actor<KryptonNode> {
    private static final long TICK_MILLIS = 3000;
    public static final int PEER_SYNC_FREQ = 3;
    public static final int MAX_FLOOD_PEERS = 100;
    public static final int MSG_FLIP_FREQ = 5;

    private Map<String,PeerEntry> peers;
    private List<PeerEntry> peersLatencySorted;
    private RemotePeer remoteSelf;

    @Local
    public IPromise init(List<String> bootStrap, ConnectableActor self) {
        peers = new HashMap<>();
        remoteSelf = new RemotePeer(self);
        bootStrap.stream()
            .forEach( str -> {
                WebSocketConnectable wsc = new WebSocketConnectable(KryptonNode.class, str);
                PeerEntry remotePeer = new PeerEntry(wsc);
                if ( ! remoteSelf.getId().equals(remotePeer.getId()) )
                    peers.put(remotePeer.getId(),remotePeer);
            });

        self().cycle();
        return resolve();
    }

    public IPromise<Pair<Long,RemotePeer>> pingInit(long time, RemotePeer sender) {
        HashSet<RemotePeer> peerCopy = new HashSet<>();
        peers.values().forEach( pe -> {
            peerCopy.add(new RemotePeer(pe.getConnectable()));
        });
        return resolve(new Pair(time,new RemotePeer(remoteSelf.getConnectable()).peers(peerCopy)));
    }

    public IPromise<Long> pingFast(long time, String senderId) {
        if ( ! peers.containsKey(senderId) && ! remoteSelf.getId().equals(senderId) ) {
            WebSocketConnectable wsc = new WebSocketConnectable(KryptonNode.class, senderId);
            PeerEntry remotePeer = new PeerEntry(wsc);
            peers.put(remotePeer.getId(),remotePeer);
        }
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

                if ( counter% PEER_SYNC_FREQ == 1 )
                    Log.Info(this,"running peer sync");

                if ( counter % MSG_FLIP_FREQ == 1 ) {
                    // flip msgid cache
                    Log.Info(this,"PREFLIP cur:"+currentMsgCache);
                    currentMsgCache = 1-currentMsgCache;
                    Log.Info(this,"FLIP PRECLEAR siz0:"+msgCache[0].size()+" siz1:"+msgCache[1].size());
                    msgCache[currentMsgCache].clear();
                    Log.Info(this,"FLIP siz0:"+msgCache[0].size()+" siz1:"+msgCache[1].size());
                    Log.Info(this,"PASTFLIP cur:"+currentMsgCache);
                }

                // ping or try connect each peer
                for ( PeerEntry peerEntry : peers.values() ) {
                    if ( ! peerEntry.isConnected() ) {
                        // connect and firstping
                        peerEntry.connect( (self,err) -> {
                            peers.remove(peerEntry.getId());
                        }).then( (remoteActor,error) -> {
                            long now = System.currentTimeMillis();
                            if ( remoteActor != null ) {
                                remoteActor.pingInit(now,remoteSelf)
                                    .then((pair, err) -> processPingInit(peerEntry, pair, err));
                            } else {
                                Log.Info(this,"connect failed "+peerEntry.getConnectable());
                            }
                        });
                    } else {
                        long now = System.currentTimeMillis();
                        // each X interval do pingInit
                        if ( counter% PEER_SYNC_FREQ == 1 ) {
                            peerEntry.getNode().pingInit(now,remoteSelf)
                                .then((pair, err) -> processPingInit(peerEntry, pair, err));
                        } else {
                            peerEntry.getNode().pingFast(now,remoteSelf.getId()).then((pair,err) -> {
                                if ( pair != null ) {
                                    peerEntry.measure(System.currentTimeMillis() - now);
                                } else {
                                    Log.Info(this,"ping failed "+peerEntry.getConnectable());
                                    peers.remove(peerEntry.getId());
                                }
                            });
                        }
                    }
                }
                if ( counter%(2*PEER_SYNC_FREQ) == 1 ) {
                    Log.Info(this,"resetting latsorted peers");
                    peersLatencySorted = null;
                }
                if ( counter%2 == 1 ) {
                    dumpState();
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

    private void dumpState() {
        Log.Info(this,"self:"+remoteSelf);
        peers.forEach((k,v) -> Log.Info(this,"    "+k+" "+v.isConnected()+" lat:"+v.getAvgLatency()));
    }

    private void processPingInit(PeerEntry parentPeer /*the node reporting peers*/, Pair<Long, RemotePeer> pair, Object err) {
        if ( pair != null ) {
            pair.cdr().getPeers().forEach( rp -> {
                PeerEntry pe = new PeerEntry(rp.getConnectable());
                String peId = pe.getId();
                if ( !peers.containsKey(peId) && ! peId.equals(remoteSelf.getId())) {
                    Log.Info(this,"adding peer of remote node "+peId+" num peers "+(peers.size()+1));
                    peers.put(peId,pe);
                }
            });
        } else {
            Log.Info(this,"ping failed "+parentPeer.getConnectable()+":"+err);
        }
    }

    // contains ids of messages received. Flipping used for time-based fade out
    HashSet<String> msgCache[] = new HashSet[] { new HashSet(), new HashSet() };
    int currentMsgCache = 0;
    public void flood(FloodMessage msg, int depth) {
        if ( msgCache[0].contains(msg.getMsgId()) || msgCache[1].contains(msg.getMsgId()) ) {
            return;
        }
        msgCache[currentMsgCache].add(msg.getMsgId());
        if ( depth >= 0 )
            processMessage(msg);
        //FIXME: sort by lat here
        List<PeerEntry> pLat = getPeersLatencySorted();
        for (int i = 0; i < pLat.size(); i++) {
            PeerEntry peerEntry = pLat.get(i);
            if ( peerEntry.isConnected() )
                peerEntry.getNode().flood(msg,depth+1);
        }
    }

    int benchCount = 0;
    private void processMessage(FloodMessage msg) {
        if ( msg instanceof BenchFloodMessage ) {
            if ( benchCount++ % 10_000 == 0 ) {
                System.out.println("receive message "+((BenchFloodMessage) msg).getCount()+" rec:"+benchCount+" "+msgCache[0].size()+" "+msgCache[1].size() );
            }
        }
    }

    private List<PeerEntry> getPeersLatencySorted() {
        if ( peersLatencySorted == null ) {
            peersLatencySorted = peers.values().stream()
                .sorted((a, b) -> Long.compare(b.getAvgLatency(), a.getAvgLatency()))
                .limit(MAX_FLOOD_PEERS)
                .collect(Collectors.toList());
        }
        return peersLatencySorted;
    }

    public static void main(String[] args) throws InterruptedException {
        DispatcherThread.DUMP_CATCHED = true;
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

        //if ( kna.getPort() == 8005 )
        {
            Thread.sleep(10_000 );
            int cnt = 0;
            while( true ) {
                long tim = System.currentTimeMillis();
                for ( int i = 0; i < 2000; i++ ) {
                    FloodMessage fl = new BenchFloodMessage(cnt++);
                    kn.flood( fl, -1 );
                }
//                System.out.println("time: "+(System.currentTimeMillis()-tim));
                Thread.sleep(1000 );
            }
        }
    }

}
