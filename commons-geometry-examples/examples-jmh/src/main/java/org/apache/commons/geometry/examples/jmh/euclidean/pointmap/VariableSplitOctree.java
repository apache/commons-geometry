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
package org.apache.commons.geometry.examples.jmh.euclidean.pointmap;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.core.Precision;

/** Candidate point map that stores entries in a modified octree data structure.
 * The split point for the octree child nodes is variable and is placed at the
 * centroid of the points stored in the node at the time it is split.
* @param <V> map value type
*/
public class VariableSplitOctree<V> extends AbstractMap<Vector3D, V> {

   /** Precision context. */
   private final Precision.DoubleEquivalence precision;

   /** Root of the tree. */
   private VariableSplitOctreeNode<V> root;

   /** Size of the tree. */
   private int entryCount;

   public VariableSplitOctree(final Precision.DoubleEquivalence precision) {
       this.precision = precision;
       this.root = new VariableSplitOctreeNode<>(this);
   }

   /** {@inheritDoc} */
   @Override
   public int size() {
       return entryCount;
   }

   /** {@inheritDoc} */
   @Override
   public V put(final Vector3D key, final V value) {
       Objects.requireNonNull(key);
       if (!key.isFinite()) {
           throw new IllegalArgumentException("Keys must be finite");
       }
       return root.put(key, value);
   }

   /** {@inheritDoc} */
   @Override
   public V get(final Object key) {
       final Vector3DEntry<V> entry = root.getEntry((Vector3D) key);
       return entry != null ?
               entry.getValue() :
               null;
   }

   /** {@inheritDoc} */
   @Override
   public V remove(final Object key) {
       return root.remove((Vector3D) key);
   }

   /** {@inheritDoc} */
   @Override
   public Set<Entry<Vector3D, V>> entrySet() {
       throw new UnsupportedOperationException();
   }

   /** Method called by nodes when a new entry is added to the tree.
    */
   void entryAdded() {
       ++entryCount;
   }

   /** Method called by nodes when an entry is removed from the tree.
    */
   void entryRemoved() {
       --entryCount;
   }

   /** Octree node class.
    */
   private static final class VariableSplitOctreeNode<V> {

       private static final int MAX_ENTRIES = 10;

       private static final int NUM_CHILDREN = 8;

       /** Owning map. */
       private final VariableSplitOctree<V> map;

       /** Child nodes. */
       private List<VariableSplitOctreeNode<V>> children = new ArrayList<>(NUM_CHILDREN);

       /** Points stored in the node; this will only be populated for leaf nodes. */
       private final List<Vector3DEntry<V>> entries = new ArrayList<>(MAX_ENTRIES);

       /** The split point of the node; will be null for leaf nodes. */
       private Vector3D splitPoint;

       VariableSplitOctreeNode(final VariableSplitOctree<V> map) {
           this.map = map;
       }

       /** Return true if the node is a leaf.
        * @return true if the node is a leaf
        */
       public boolean isLeaf() {
           return splitPoint == null;
       }

       /** Get the entry matching the given key or null if not found.
        * @param key key to search for
        * @return the entry matching the given key or null if not found
        */
       public Vector3DEntry<V> getEntry(final Vector3D key) {
           if (isLeaf()) {
               // check the existing entries for a match
               for (Vector3DEntry<V> entry : entries) {
                   if (key.eq(entry.getKey(), map.precision)) {
                       return entry;
                   }
               }
               // not found
               return null;
           }

           // delegate to the child
           return getChildForPoint(key).getEntry(key);
       }

       public V put(final Vector3D key, final V value) {
           if (isLeaf()) {
               // check the existing entries for a match
               for (Vector3DEntry<V> entry : entries) {
                   if (key.eq(entry.getKey(), map.precision)) {
                       // found one; set the value and return the previous value
                       final V prev = entry.getValue();
                       entry.setValue(value);

                       return prev;
                   }
               }
               // no existing entry, we'll need to create a new one
               if (entries.size() < MAX_ENTRIES) {
                   // we have an open spot here so just add the entry
                   entries.add(new Vector3DEntry<>(key, value));

                   map.entryAdded();

                   return null;
               }

               // no available entries; split the node and add to a child
               splitNode();
           }

           // non-leaf node
           return getChildForPoint(key).put(key, value);
       }

       public V remove(final Vector3D key) {
           if (isLeaf()) {
               // check the existing entries for a match
               Vector3DEntry<V> existing = null;
               for (Vector3DEntry<V> entry : entries) {
                   if (key.eq(entry.getKey(), map.precision)) {
                       existing = entry;
                       break;
                   }
               }
               if (existing != null) {
                   entries.remove(existing);
                   return existing.getValue();
               }

               // not found
               return null;
           }

           return getChildForPoint(key).remove(key);
       }

       private void splitNode() {
           splitPoint = computeCentroid();

           for (int i = 0; i < NUM_CHILDREN; ++i) {
               children.add(new VariableSplitOctreeNode<>(map));
           }

           for (Vector3DEntry<V> entry : entries) {
               getChildForPoint(entry.getKey()).entries.add(entry);
           }

           entries.clear();
       }

       private VariableSplitOctreeNode<V> getChildForPoint(final Vector3D pt) {
           final int xbit = map.precision.lte(pt.getX(), splitPoint.getX()) ? 0x1 : 0;
           final int ybit = map.precision.lte(pt.getY(), splitPoint.getY()) ? 0x2 : 0;
           final int zbit = map.precision.lte(pt.getZ(), splitPoint.getZ()) ? 0x4 : 0;

           int idx = xbit | ybit | zbit;

           return children.get(idx);
       }

       /** Compute the centroid of all points currently in the node.
        * @return centroid of the node points
        */
       private Vector3D computeCentroid() {
           Vector3D.Sum sum = Vector3D.Sum.create();
           for (Vector3DEntry<V> entry : entries) {
               sum.add(entry.getKey());
           }

           return sum.get().multiply(1.0 / entries.size());
       }
   }
}
