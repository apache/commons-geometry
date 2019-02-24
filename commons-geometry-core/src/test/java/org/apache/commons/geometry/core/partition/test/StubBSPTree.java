package org.apache.commons.geometry.core.partition.test;

import org.apache.commons.geometry.core.partition.AbstractBSPTree;

public class StubBSPTree<T> extends AbstractBSPTree<StubPoint, T> {

    public StubBSPTree() {
        super(SimpleNode::new);
    }
}
