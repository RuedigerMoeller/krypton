package org.krypton.storage;

import org.krypton.math.BigInt;

/**
 * Created by ruedi on 07.06.17.
 */
public interface IStorage {

    BigInt getValue(String address);

}
