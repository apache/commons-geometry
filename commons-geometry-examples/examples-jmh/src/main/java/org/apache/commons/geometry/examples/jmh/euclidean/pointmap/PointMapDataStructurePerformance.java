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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.core.Precision;
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

/** Benchmarks for the testing implementations of point map
 * data structures.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = {"-server", "-Xms512M", "-Xmx512M"})
public class PointMapDataStructurePerformance {

    private static final Precision.DoubleEquivalence PRECISION =
            Precision.doubleEquivalenceOfEpsilon(1e-6);

    /** Input class containing a list of equally-spaced points.
     */
    @State(Scope.Thread)
    public static class EquallySpacedPointInput {
        /** Number of points in the input. */
        @Param({"125", "1000", "1000000"})
        private int points;

        /** Max coordinate value. */
        @Param({"100"})
        private double max;

        /** Min coordinate value. */
        @Param({"-100"})
        private double min;

        /** List of points. */
        private List<Vector3D> pointList;

        /** Set up the instance for the benchmark. */
        @Setup(Level.Iteration)
        public void setup() {
            pointList = new ArrayList<>(points);

            final double step = Math.abs(max - min) / Math.floor(Math.cbrt(points));
            for (double x = min; x <= max; x += step) {
                for (double y = min; y <= max; y += step) {
                    for (double z = min; z <= max; z += step) {
                        pointList.add(Vector3D.of(x, y, z));
                    }
                }
            }
        }

        /** Get the points for the input.
         * @return points for the input
         */
        public List<Vector3D> getPoints() {
            return pointList;
        }
    }

    /** Input class containing a list of randomly shuffled equally-spaced points.
     */
    @State(Scope.Thread)
    public static class ShuffledEquallySpacedPointInput extends EquallySpacedPointInput {

        /** {@inheritDoc} */
        @Setup(Level.Iteration)
        @Override
        public void setup() {
            super.setup();
            Collections.shuffle(getPoints(), new Random(1L));
        }
    }

    /** Insert each point into the {@code map}. The same value is inserted for each point.
     * @param map to insert into
     * @param points points to insert
     * @return the input map
     */
    private static Map<Vector3D, Integer> insert(final Map<Vector3D, Integer> map, final List<Vector3D> points) {
        final Integer val = Integer.valueOf(1);
        for (Vector3D p : points) {
            map.put(p, val);
        }
        return map;
    }

    /** Construct a map to use as a baseline for comparisons. The returned tree map does not
     * meet the requirements of a point map but serves as a good performance baseline for tree
     * data structures.
     * @return a new baseline map instance
     */
    private static Map<Vector3D, Integer> baselineMap() {
        return new TreeMap<>(Vector3D.COORDINATE_ASCENDING_ORDER);
    }

    /** Baseline benchmark for inserting equally spaced points.
     * @param input input points
     * @return map under test
     */
    @Benchmark
    public Map<Vector3D, Integer> baselineEquallySpacedInsert(final EquallySpacedPointInput input) {
        return insert(baselineMap(), input.getPoints());
    }

    /** Baseline benchmark for inserting randomly shuffled, equally spaced points.
     * @param input input points
     * @return map under test
     */
    @Benchmark
    public Map<Vector3D, Integer> baselineShuffledEquallySpacedInsert(final ShuffledEquallySpacedPointInput input) {
        return insert(baselineMap(), input.getPoints());
    }

    /** Variable split octree benchmark for inserting equally spaced points.
     * @param input input points
     * @return map under test
     */
    @Benchmark
    public Map<Vector3D, Integer> variableSplitOctreeEquallySpacedInsert(final EquallySpacedPointInput input) {
        return insert(new VariableSplitOctree<>(PRECISION), input.getPoints());
    }

    /** Variable split octree benchmark for inserting randomly shuffled, equally spaced points.
     * @param input input points
     * @return map under test
     */
    @Benchmark
    public Map<Vector3D, Integer> variableSplitOctreeShuffledEquallySpacedInsert(
            final ShuffledEquallySpacedPointInput input) {
        return insert(new VariableSplitOctree<>(PRECISION), input.getPoints());
    }
}
