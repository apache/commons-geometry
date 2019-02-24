package org.apache.commons.geometry.core.partition;

import org.apache.commons.geometry.core.Point;

@FunctionalInterface
public interface BSPTreeVisitor<P extends Point<P>, T> {

    void visit(BSPTree.Node<P, T> node);
}
