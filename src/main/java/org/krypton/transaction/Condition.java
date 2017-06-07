package org.krypton.transaction;

import org.krypton.math.BigInt;
import org.krypton.storage.IStorage;

import java.io.Serializable;

/**
 * Created by ruedi on 07.06.17.
 */
public class Condition implements Serializable {

    final static int EQUAL = 1;
    final static int GREAT = 2;
    final static int LESS = 3;
    final static int GREAT_EQUAL = 4;
    final static int LESS_EQUAL = 5;
    int operation;
    String account;
    BigInt compareValue;


    public Condition(int operation, String account, BigInt compareValue) {
        this.operation = operation;
        this.account = account;
        this.compareValue = compareValue;
    }

    public BigInt getCompareValue() {
        return compareValue;
    }

    public int getOperation() {
        return operation;
    }

    public String getAccount() {
        return account;
    }

    public boolean isValid( IStorage storage ) {
        BigInt accountValue = storage.getValue(account);
        if ( accountValue == null ) {
            return compareValue == null;
        }
        switch (operation) {
            case EQUAL:
                return compareValue.equals(accountValue);
            default:
                return false;
        }
    }
}
