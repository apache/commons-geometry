package org.apache.commons.geometry.core.partition.test;

import java.util.Objects;

import org.apache.commons.geometry.core.partition.BSPTree;
import org.apache.commons.geometry.core.partition.BSPTreeVisitor;
import org.apache.commons.geometry.core.partitioning.BSPTree_Old;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;

public class PartitionTestUtils {

    public static final double EPS = 1e-6;

    public static final DoublePrecisionContext PRECISION =
            new EpsilonDoublePrecisionContext(EPS);

    public static <T> void printTree(StubBSPTree<T> tree) {
        StubTreePrinter<T> printer = new StubTreePrinter<T>();
        String str = printer.writeAsString(tree);

        System.out.println(str);
    }

    public static class StubTreePrinter<T> implements BSPTreeVisitor<StubPoint, T> {

        /** Indent per tree level */
        protected static final String INDENT = "    ";

        /** Current depth in the tree */
        protected int depth;

        /** Contains the string output */
        protected StringBuilder output = new StringBuilder();

        /** Returns a string representation of the given {@link BSPTree_Old}.
         * @param tree
         * @return
         */
        public String writeAsString(StubBSPTree<T> tree) {
            output.delete(0, output.length());

            tree.getRoot().visit(this);

            return output.toString();
        }

        @Override
        public void visit(BSPTree.Node<StubPoint, T> node) {
            writeLinePrefix(node);

            if (node.isLeaf()) {
                visitLeafNode(node);
            }
            else {
                visitInternalNode(node);
            }
        }

        public void visitInternalNode(BSPTree.Node<StubPoint, T> node) {
            writeInternalNode(node);

            write("\n");

            ++depth;
        }

        public void visitLeafNode(BSPTree.Node<StubPoint, T> node) {
            writeLeafNode(node);

            write("\n");

            BSPTree.Node<StubPoint, T> cur = node;
            while (cur.isPlus()) {
                --depth;
                cur = cur.getParent();
            }
        }

        /** Writes the prefix for the current line in the output. This includes
         * the line indent, the plus/minus node indicator, and a string identifier
         * for the node itself.
         * @param node
         */
        protected void writeLinePrefix(BSPTree.Node<StubPoint, T>node) {
            for (int i=0; i<depth; ++i) {
                write(INDENT);
            }

            if (node.getParent() != null) {
                if (node.isMinus()) {
                    write("[-] ");
                }
                else {
                    write("[+] ");
                }
            }

            write(nodeIdString(node) + " | ");
        }

        /** Returns a short string identifier for the given node.
         * @param node
         * @return
         */
        protected String nodeIdString(BSPTree.Node<StubPoint, T> node) {
            return node.getClass().getSimpleName() + "@"  + Objects.hashCode(node);
        }

        /** Adds the given string to the output.
         * @param str
         */
        protected void write(String str) {
            output.append(str);
        }

        /** Method for subclasses to provide their own string representation
         * of the given internal node.
         */
        protected void writeInternalNode(BSPTree.Node<StubPoint, T> node) {
            write(node.toString());
        }

        /** Writes a leaf node. The default implementation here simply writes
         * the node attribute as a string.
         * @param node
         */
        protected void writeLeafNode(BSPTree.Node<StubPoint, T> node) {
            write(node.toString());
        }
    }
}
