package org.krypton.transaction;

import org.krypton.math.BigInt;

/**
 * Created by ruedi on 08.06.17.
 */
public class ValidatedTransaction extends TransactionRequest {

    BigInt sequence;

    String validatorAddress;
    BigInt validationCode;
    boolean success;
    String errorMessage;

}
