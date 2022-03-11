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

import org.apache.commons.geometry.euclidean.threed.Bounds3D;
import org.apache.commons.geometry.euclidean.threed.SphericalCoordinates;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.angle.Angle;
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
import org.openjdk.jmh.infra.Blackhole;

/** Benchmarks for the testing candidate implementations of point map
 * data structures.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = {"-server", "-Xms512M", "-Xmx512M"})
public class PointMapDataStructurePerformance {

    /** Precision context. */
    private static final Precision.DoubleEquivalence PRECISION =
            Precision.doubleEquivalenceOfEpsilon(1e-6);

    /** Value inserted into maps during runs. */
    private static final Integer VAL = Integer.valueOf(1);

    /** Base input class for point map benchmarks. */
    @State(Scope.Thread)
    public static class PointMapInput {

        /** Data structure implementation. */
        @Param({"treemap", "varoctree", "kdtree", "rebuilding-kdtree", "bucket-kdtree"})
        private String impl;

        /** Point list shape. */
        @Param({"block", "sphere"})
        private String shape;

        /** Point distribution. */
        @Param({"none", "random", "ordered"})
        private String dist;

        /** Seed value for randomization. */
        @Param({"1"})
        private int randomSeed;

        /** Map instance for the run. */
        private Map<Vector3D, Integer> map;

        /** List of points for the run. */
        private List<Vector3D> points;

        /** Random instance. */
        private Random random;

        /** Set up the instance for the benchmark. */
        @Setup(Level.Iteration)
        public void setup() {
            random = new Random(randomSeed);

            map = createMap();
            points = createPoints();

            switch (dist) {
            case "none":
                break;
            case "random":
                Collections.shuffle(points, random);
                break;
            case "ordered":
                Collections.sort(points, Vector3D.COORDINATE_ASCENDING_ORDER);
                break;
            default:
                throw new IllegalArgumentException("Unknown distribution: " + dist);
            }
        }

        /** Get the map instance under test.
         * @return map instance
         */
        public Map<Vector3D, Integer> getMap() {
            return map;
        }

        /** Get the points for the run.
         * @return list of points
         */
        public List<Vector3D> getPoints() {
            return points;
        }

        /** Get the random number generator for the instance.
         * @return random number generate
         */
        public Random getRandom() {
            return random;
        }

        /** Create the map implementation for the run.
         * @return map instance
         */
        private Map<Vector3D, Integer> createMap() {
            switch (impl.toLowerCase()) {
            case "treemap":
                return new TreeMap<>((a, b) -> {
                    int cmp = PRECISION.compare(a.getX(), b.getX());
                    if (cmp == 0) {
                        cmp = PRECISION.compare(a.getY(), b.getY());
                        if (cmp == 0) {
                            cmp = PRECISION.compare(a.getZ(), b.getZ());
                        }
                    }
                    return cmp;
                });
            case "varoctree":
                return new VariableSplitOctree<>(PRECISION);
            case "kdtree":
                return new KDTree<>(PRECISION);
            case "rebuilding-kdtree":
                return new RebuildingKDTree<>(PRECISION);
            case "bucket-kdtree":
                return new BucketKDTree<>(PRECISION);
            default:
                throw new IllegalArgumentException("Unknown map implementation: " + impl);
            }
        }

        /** Create the list of points for the run.
         * @return list of points
         */
        private List<Vector3D> createPoints() {
            switch (shape.toLowerCase()) {
            case "block":
                return createPointBlock(20, 1);
            case "sphere":
                return createPointSphere(5, 5, 10);
            default:
                throw new IllegalArgumentException("Unknown point distribution " + impl);
            }
        }
    }

    /** Input class containing pre-inserted points. */
    @State(Scope.Thread)
    public static class PreInsertedPointMapInput extends PointMapInput {

        /** List of test points. */
        private List<Vector3D> testPoints;

        /** {@inheritDoc} */
        @Override
        @Setup(Level.Iteration)
        public void setup() {
            super.setup();

            final List<Vector3D> pts = getPoints();

            // add the points to the map
            final Map<Vector3D, Integer> map = getMap();
            for (final Vector3D pt : pts) {
                map.put(pt, VAL);
            }

            // compute test points
            testPoints = new ArrayList<>(pts.size() * 2);
            testPoints.addAll(pts);

            final Random rnd = getRandom();
            final Bounds3D bounds = Bounds3D.from(pts);
            final Vector3D diag = bounds.getDiagonal();
            for (int i = 0; i < pts.size(); ++i) {
                testPoints.add(Vector3D.of(
                        bounds.getMin().getX() + (rnd.nextDouble() * diag.getX()),
                        bounds.getMin().getY() + (rnd.nextDouble() * diag.getY()),
                        bounds.getMin().getZ() + (rnd.nextDouble() * diag.getZ())));
            }

            Collections.shuffle(testPoints, rnd);
        }

        /** Get a list of test points to look for in the map. The
         * returned list contains 2x the number of points in the map,
         * with half equal to map entries and half random.
         * @return list of test points
         */
        public List<Vector3D> getTestPoints() {
            return testPoints;
        }
    }

    /** Create a solid block of points.
     * @param pointsPerSide number of points along each side
     * @param spacing spacing between each point
     * @return list of points in a block
     */
    private static List<Vector3D> createPointBlock(final int pointsPerSide, final double spacing) {
        final List<Vector3D> points = new ArrayList<>(pointsPerSide * pointsPerSide * pointsPerSide);

        for (int x = 0; x < pointsPerSide; ++x) {
            for (int y = 0; y < pointsPerSide; ++y) {
                for (int z = 0; z < pointsPerSide; ++z) {
                    points.add(Vector3D.of(x, y, z).multiply(spacing));
                }
            }
        }

        return points;
    }

    /** Create a hollow sphere of points.
     * @param slices number of sections in the x-y plane, not counting the poles
     * @param segments number of section perpendicular to the x-y plane for each slice
     * @param radius sphere radius
     * @return list of points in a hollow sphere
     */
    private static List<Vector3D> createPointSphere(final int slices, final int segments, final double radius) {
        final List<Vector3D> points = new ArrayList<>();

        final double polarDelta = Math.PI / (slices + 1);
        final double azDelta = Angle.TWO_PI / segments;

        // add the top pole
        points.add(Vector3D.of(0, 0, radius));

        // add the lines of latitude
        for (int i = 1; i <= slices; ++i) {
            for (int j = 0; j < segments; ++j) {
                final SphericalCoordinates coords = SphericalCoordinates.of(
                        radius,
                        j * azDelta,
                        i * polarDelta);

                points.add(coords.toVector());
            }
        }

        // add the bottom pole
        points.add(Vector3D.of(0, 0, -radius));

        return points;
    }

    /** Benchmark that inserts each point in the input into the target map.
     * @param input input for the run
     * @param bh blackhole instance
     * @return input instance
     */
    @Benchmark
    public PointMapInput put(final PointMapInput input, final Blackhole bh) {
        final Map<Vector3D, Integer> map = input.getMap();

        for (final Vector3D p : input.getPoints()) {
            bh.consume(map.put(p, VAL));
        }

        return input;
    }

    /** Benchmark that retrieves each point in the input from the target map.
     * @param input input for the run
     * @param bh blackhole instance
     * @return input instance
     */
    @Benchmark
    public PointMapInput get(final PreInsertedPointMapInput input, final Blackhole bh) {
        final Map<Vector3D, Integer> map = input.getMap();

        for (final Vector3D p : input.getTestPoints()) {
            bh.consume(map.get(p));
        }

        return input;
    }

    /** Benchmark that remove each point in the input from the target map.
     * @param input input for the run
     * @param bh blackhole instance
     * @return input instance
     */
    @Benchmark
    public PointMapInput remove(final PreInsertedPointMapInput input, final Blackhole bh) {
        final Map<Vector3D, Integer> map = input.getMap();

        for (final Vector3D p : input.getPoints()) {
            bh.consume(map.remove(p));
        }

        return input;
    }
}
