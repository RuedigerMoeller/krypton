package org.krypton.transaction;

import org.krypton.math.BigInt;

/**
 * Created by ruedi on 08.06.17.
 */
public class ValidatedTransaction extends TransactionRequest {

    BigInt sequence;
    String previousTAId;

    String validatorAddress;
    BigInt validationCode;
    boolean success;
    String errorMessage;

    public String getPreviousTAId() {
        return previousTAId;
    }

    public BigInt getSequence() {
        return sequence;
    }

    public String getValidatorAddress() {
        return validatorAddress;
    }

    public BigInt getValidationCode() {
        return validationCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
