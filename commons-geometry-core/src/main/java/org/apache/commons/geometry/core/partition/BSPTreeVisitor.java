package org.apache.commons.geometry.core.partition;

import org.apache.commons.geometry.core.Point;

@FunctionalInterface
public interface BSPTreeVisitor<P extends Point<P>, T> {

    void visit(BSPTreeNode<P, T> node);
}
