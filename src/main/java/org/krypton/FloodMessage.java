package org.krypton;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by ruedi on 06.06.17.
 */
public class FloodMessage implements Serializable {
    String msgId;

    public FloodMessage() {
        this.msgId = UUID.randomUUID().toString();
    }

    public String getMsgId() {
        return msgId;
    }
}
