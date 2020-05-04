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
package org.apache.commons.geometry.examples.jmh.euclidean;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.twod.LineConvexSubset;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.geometry.euclidean.twod.shapes.Circle;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/** Benchmarks for the {@link RegionBSPTree2D} class.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = {"-server", "-Xms512M", "-Xmx512M"})
public class RegionBSPTree2DPerformance {

    /** Base class for inputs that use circle approximation boundaries.
     */
    @State(Scope.Thread)
    public static class CircularBoundaryInputBase {

        /** The input to use for the segments parameter when generating the circle boundaries. */
        @Param({"10", "20", "50"})
        private int segments;

        /** Compute the boundaries for this instance.
         * @return the boundaries for this instance
         */
        protected List<LineConvexSubset> computeBoundaries() {
            final Circle circle = Circle.from(Vector2D.ZERO, 1, new EpsilonDoublePrecisionContext(1e-10));
            return circle.toTree(segments).getBoundaries();
        }
    }

    /** Class providing a list of boundaries for a circle approximation.
     */
    @State(Scope.Thread)
    public static class CircularBoundaryInput extends CircularBoundaryInputBase {

        /** List containing the convex boundaries of the circle approximation. */
        private List<LineConvexSubset> boundaries;

        /** Set up the instance for the benchmark. */
        @Setup(Level.Iteration)
        public void setup() {
            boundaries = computeBoundaries();
        }

        /** Get the computed circle boundaries.
         * @return the computed circle boundaries
         */
        public List<LineConvexSubset> getBoundaries() {
            return boundaries;
        }
    }

    /** Class providing a region approximating a circular boundary. The region is in a worst-case
     * tree structure, meaning that the tree is completely unbalanced.
     */
    @State(Scope.Thread)
    public static class WorstCaseCircularRegionInput extends CircularBoundaryInputBase {

        /** Tree containing the circle approximation. */
        private RegionBSPTree2D tree;

        /** Set up the instance for the benchmark. */
        @Setup(Level.Iteration)
        public void setup() {
            tree = RegionBSPTree2D.empty();
            tree.insert(computeBoundaries());
        }

        /** Get the computed circle approximation tree.
         * @return the computed circle approximation tree.
         */
        public RegionBSPTree2D getTree() {
            return tree;
        }
    }

    /** Benchmark testing the performance of tree creation for a convex region. The insertion
     * behavior is worst-case, meaning that the tree is unbalanced and degenerates into a simple
     * list of nodes.
     * @param input benchmark boundary input
     * @return created BSP tree
     */
    @Benchmark
    public RegionBSPTree2D insertConvexWorstCase(final CircularBoundaryInput input) {
        final RegionBSPTree2D tree = RegionBSPTree2D.empty();

        for (LineConvexSubset boundary : input.getBoundaries()) {
            tree.insert(boundary);
        }

        return tree;
    }

    /** Benchmark testing the performance of boundary determination using a tree with a worst-case,
     * unbalanced structure.
     * @param input input tree
     * @return list of tree boundaries
     */
    @Benchmark
    public List<LineConvexSubset> boundaryConvexWorstCase(final WorstCaseCircularRegionInput input) {
        return input.getTree().getBoundaries();
    }
}
