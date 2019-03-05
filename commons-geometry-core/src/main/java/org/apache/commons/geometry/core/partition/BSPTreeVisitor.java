package org.apache.commons.geometry.core.partition;

import org.apache.commons.geometry.core.Point;

@FunctionalInterface
public interface BSPTreeVisitor<P extends Point<P>, N extends BSPTree.Node<P, N>> {

    void visit(N node);
}
