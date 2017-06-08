package org.krypton.storage;

import org.krypton.math.BigInt;
import org.krypton.transaction.ValidatedTransaction;

import java.util.Map;

/**
 * Created by ruedi on 08.06.17.
 *
 * A tree keeping all variations of possible transaction sequences.
 *
 */
public class ChainTree {

    TreeNode root;

    public ChainTree() {
    }

    public void add( ValidatedTransaction ta ) {
        if ( root == null ) {
            root = new TreeNode(ta);
            return;
        }
    }

    static class TreeNode {
        ValidatedTransaction req;
        Map<String,TreeNode> nextVariants;

        public TreeNode(ValidatedTransaction req) {
            this.req = req;
        }

        public BigInt getSequence() {
            return req.getSequence();
        }
    }

}
