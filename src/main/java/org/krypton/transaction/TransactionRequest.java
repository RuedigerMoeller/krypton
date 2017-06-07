package org.krypton.transaction;

import org.krypton.FloodMessage;
import org.krypton.math.BigInt;

/**
 * Created by ruedi on 07.06.17.
 */
public class TransactionRequest extends FloodMessage {

    protected String sender;      // sending account
    protected String receiver;    // receiving account
    protected BigInt amount;      // amount transfered
    protected Condition conditions[]; // preconditions in order to process

}
