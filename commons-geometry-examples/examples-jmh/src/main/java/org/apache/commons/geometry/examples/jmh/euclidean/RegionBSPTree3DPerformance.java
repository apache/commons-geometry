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
import org.apache.commons.geometry.euclidean.threed.ConvexSubPlane;
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.shapes.Sphere;
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

/** Benchmarks for the {@link RegionBSPTree3D} class.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = {"-server", "-Xms512M", "-Xmx512M"})
public class RegionBSPTree3DPerformance {

    /** Base class for inputs that use sphere approximation boundaries.
     */
    @State(Scope.Thread)
    public static class SphericalBoundaryInputBase {

        /** The input to use for the stacks and slices parameters when generating the
         * sphere boundaries.
         */
        @Param({"5", "10", "15"})
        private int stacksSlices;

        /** Compute the boundaries for the instance.
         * @return the boundaries for the instance.
         */
        protected List<ConvexSubPlane> computeBoundaries() {
            final Sphere sphere = Sphere.from(Vector3D.ZERO, 1, new EpsilonDoublePrecisionContext(1e-10));
            return sphere.toTree(stacksSlices, stacksSlices).getBoundaries();
        }
    }

    /** Class providing a list of boundaries for a sphere approximation.
     */
    @State(Scope.Thread)
    public static class SphericalBoundaryInput extends SphericalBoundaryInputBase {

        /** List containing the convex boundaries of the sphere approximation. */
        private List<ConvexSubPlane> boundaries;

        /** Set up the instance for the benchmark. */
        @Setup(Level.Iteration)
        public void setup() {
            boundaries = computeBoundaries();
        }

        /** Get the computed sphere boundaries.
         * @return the computed sphere boundaries
         */
        public List<ConvexSubPlane> getBoundaries() {
            return boundaries;
        }
    }

    /** Class providing a region approximating a spherical boundary. The region is in a worst-case
     * tree structure, meaning that the tree is completely unbalanced.
     */
    @State(Scope.Thread)
    public static class WorstCaseSphericalRegionInput extends SphericalBoundaryInputBase {

        /** The sphere approximation region. */
        private RegionBSPTree3D tree;

        /** Set up the instance for the benchmark. */
        @Setup(Level.Iteration)
        public void setup() {
            tree = RegionBSPTree3D.empty();
            tree.insert(computeBoundaries());
        }

        /** Get the tree for the instance.
         * @return the tree for the instance
         */
        public RegionBSPTree3D getTree() {
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
    public RegionBSPTree3D insertConvexWorstCase(final SphericalBoundaryInput input) {
        final RegionBSPTree3D tree = RegionBSPTree3D.empty();

        for (ConvexSubPlane boundary : input.getBoundaries()) {
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
    public List<ConvexSubPlane> boundaryConvexWorstCase(final WorstCaseSphericalRegionInput input) {
        return input.getTree().getBoundaries();
    }
}
