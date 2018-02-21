package test;

import impl.BitVecNSet;

public class BVNSTest extends NSetTest {

    protected void reset() {
        testSet = new BitVecNSet(data.length);
    }

}
