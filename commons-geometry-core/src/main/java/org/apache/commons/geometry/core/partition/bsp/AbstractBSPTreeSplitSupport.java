/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.geometry.core.partition.bsp;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.partition.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partition.Split;
import org.apache.commons.geometry.core.partition.SplitLocation;
import org.apache.commons.geometry.core.partition.bsp.AbstractBSPTree.AbstractNode;

public class AbstractBSPTreeSplitSupport<P extends Point<P>, N extends AbstractNode<P, N>>
    extends AbstractBSPTreeOutputSupport<P, N> {

    /** Split the subtree rooted at the given node by a partitioning convex subhyperplane
    * defined on the same region as the node.
    * @param node the root node of the subtree to split; the node may be a leaf node
    * @param partitioner partitioning convex subhyperplane
    * @return node containing the split subtree
    */
   protected N splitSubtree(final N node, final ConvexSubHyperplane<P> partitioner) {
       if (node.isLeaf()) {
           return splitLeafNode(node, partitioner);
       }
       return splitInternalNode(node, partitioner);
   }

   /** Split the given leaf node by a partitioning convex subhyperplane defined on the
    * same region.
    * @param node the leaf node to split
    * @param partitioner partitioning convex subhyperplane
    * @return node containing the split subtree
    */
   private N splitLeafNode(final N node, final ConvexSubHyperplane<P> partitioner) {
       // in this case, we just create a new parent node with the partitioner as its
       // cut and two copies of the original node as children
      final N parent = outputNode();
      parent.setSubtree(partitioner, outputNode(node), outputNode(node));

      return parent;
   }

   /** Split the given internal node by a partitioning convex subhyperplane defined on the same region
    * as the node.
    * @param node the internal node to split
    * @param partitioner partitioning convex subhyperplane
    * @return node containing the split subtree
    */
   private N splitInternalNode(final N node, final ConvexSubHyperplane<P> partitioner) {
       // split the partitioner and node cut with each other's hyperplanes to determine their relative positions
       final Split<? extends ConvexSubHyperplane<P>> partitionerSplit = partitioner.split(node.getCutHyperplane());
       final Split<? extends ConvexSubHyperplane<P>> nodeCutSplit = node.getCut().split(partitioner.getHyperplane());

       final SplitLocation partitionerSplitSide = partitionerSplit.getLocation();
       final SplitLocation nodeCutSplitSide = nodeCutSplit.getLocation();

       final N result = outputNode();

       N resultMinus;
       N resultPlus;

       if (partitionerSplitSide == SplitLocation.PLUS) {
           if (nodeCutSplitSide == SplitLocation.PLUS) {
               // partitioner is on node cut plus side, node cut is on partitioner plus side
               final N nodePlusSplit = splitSubtree(node.getPlus(), partitioner);

               resultMinus = nodePlusSplit.getMinus();

               resultPlus = outputNode(node);
               resultPlus.setSubtree(node.getCut(), outputSubtree(node.getMinus()), nodePlusSplit.getPlus());
           }
           else {
               // partitioner is on node cut plus side, node cut is on partitioner minus side
               final N nodePlusSplit = splitSubtree(node.getPlus(), partitioner);

               resultMinus = outputNode(node);
               resultMinus.setSubtree(node.getCut(), outputSubtree(node.getMinus()), nodePlusSplit.getMinus());

               resultPlus = nodePlusSplit.getPlus();
           }
       }
       else if (partitionerSplitSide == SplitLocation.MINUS) {
           if (nodeCutSplitSide == SplitLocation.MINUS) {
               // partitioner is on node cut minus side, node cut is on partitioner minus side
               final N nodeMinusSplit = splitSubtree(node.getMinus(), partitioner);

               resultMinus = outputNode(node);
               resultMinus.setSubtree(node.getCut(), nodeMinusSplit.getMinus(), outputSubtree(node.getPlus()));

               resultPlus = nodeMinusSplit.getPlus();
           }
           else {
               // partitioner is on node cut minus side, node cut is on partitioner plus side
               final N nodeMinusSplit = splitSubtree(node.getMinus(), partitioner);

               resultMinus = nodeMinusSplit.getMinus();

               resultPlus = outputNode(node);
               resultPlus.setSubtree(node.getCut(), nodeMinusSplit.getPlus(), outputSubtree(node.getPlus()));
           }
       }
       else if (partitionerSplitSide == SplitLocation.BOTH) {
           // partitioner and node cut split each other
           final N nodeMinusSplit = splitSubtree(node.getMinus(), partitionerSplit.getMinus());
           final N nodePlusSplit = splitSubtree(node.getPlus(), partitionerSplit.getPlus());

           resultMinus = outputNode(node);
           resultMinus.setSubtree(nodeCutSplit.getMinus(), nodeMinusSplit.getMinus(), nodePlusSplit.getMinus());

           resultPlus = outputNode(node);
           resultPlus.setSubtree(nodeCutSplit.getPlus(), nodeMinusSplit.getPlus(), nodePlusSplit.getPlus());
       }
       else {
           // partitioner and node cut are parallel or anti-parallel
           final boolean sameOrientation = partitioner.getHyperplane().similarOrientation(node.getCutHyperplane());

           resultMinus = outputSubtree(sameOrientation ? node.getMinus() : node.getPlus());
           resultPlus = outputSubtree(sameOrientation ? node.getPlus() : node.getMinus());
       }

       result.setSubtree(partitioner, resultMinus, resultPlus);

       return result;
   }
}
