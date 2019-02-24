package org.apache.commons.geometry.core.partition;

import org.apache.commons.geometry.core.Point;

public interface BSPTreeEntryPoint<P extends Point<P>, T> {

    void visit(BSPTreeVisitor<P, T> visitor);

    BSPTreeNode<P, T> findNode(P pt);
}
