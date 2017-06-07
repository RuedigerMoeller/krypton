package org.krypton.transaction;

import org.krypton.FloodMessage;
import org.krypton.math.BigInt;

/**
 * Created by ruedi on 07.06.17.
 */
public class TransactionMessage extends FloodMessage {

    String sender;
    String receiver;
    BigInt amount;
    Condition conditions[];

}
