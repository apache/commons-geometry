/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.geometry.examples.jmh.euclidean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import org.apache.commons.geometry.core.collection.PointMap;
import org.apache.commons.geometry.euclidean.EuclideanCollections;
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

/** Benchmarks for the 3D Euclidean
 * {@link org.apache.commons.geometry.core.collection.PointMap PointMap} implementation.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = {"-server", "-Xms512M", "-Xmx512M"})
public class PointMap3DPerformance {

    /** Precision context. */
    private static final Precision.DoubleEquivalence PRECISION =
            Precision.doubleEquivalenceOfEpsilon(1e-6);

    /** Value inserted into maps during runs. */
    private static final Integer VAL = Integer.valueOf(1);

    /** Maximum number of iterable instances used during iterable benchmarks. */
    private static final int MAX_ITERABLES = 100;

    /** Abstract base class for point map benchmark input. */
    @State(Scope.Thread)
    public abstract static class AbstractPointMapInput {

        /** Point list shape. */
        @Param({"block", "line", "sphere"})
        private String shape;

        /** Point distribution. */
        @Param({"none", "random", "ordered"})
        private String dist;

        /** Seed value for randomization. */
        @Param({"1"})
        private int randomSeed;

        /** List of points for the run. */
        private List<Vector3D> points;

        /** Random instance. */
        private Random random;

        /** Set up the instance for the benchmark. */
        @Setup(Level.Iteration)
        public void setup() {
            random = new Random(randomSeed);
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
        public abstract Map<Vector3D, Integer> getMap();

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

        /** Create the list of points for the run.
         * @return list of points
         */
        private List<Vector3D> createPoints() {
            switch (shape.toLowerCase()) {
            case "block":
                return createPointBlock(20, 1);
            case "line":
                return createLine(8_000, 1);
            case "sphere":
                return createPointSphere(5, 5, 10);
            default:
                throw new IllegalArgumentException("Unknown point distribution " + shape);
            }
        }
    }

    /** Input class containing pre-inserted points. */
    @State(Scope.Thread)
    public abstract static class AbstractPreInsertedPointMapInput
        extends AbstractPointMapInput {

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

    /** Input class containing a {@link PointMap} instance.
     */
    @State(Scope.Thread)
    public static class PointMapInput extends AbstractPointMapInput {

        /** {@inheritDoc} */
        @Override
        public PointMap<Vector3D, Integer> getMap() {
            return EuclideanCollections.pointMap3D(PRECISION);
        }
    }

    /** Input class containing a {@link PointMap} instance with pre-inserted points.
     */
    @State(Scope.Thread)
    public static class PreInsertedPointMapInput extends AbstractPreInsertedPointMapInput {

        /** {@inheritDoc} */
        @Override
        public PointMap<Vector3D, Integer> getMap() {
            return EuclideanCollections.pointMap3D(PRECISION);
        }
    }

    /** Input class that uses a {@link TreeMap} to store points.
     */
    @State(Scope.Thread)
    public static class TreeMapInput extends AbstractPointMapInput {

        /** {@inheritDoc} */
        @Override
        public Map<Vector3D, Integer> getMap() {
            return createTreeMap();
        }
    }

    /** Input class that uses a {@link TreeMap} with pre-inserted points.
     */
    @State(Scope.Thread)
    public static class PreInsertedTreeMapInput extends AbstractPreInsertedPointMapInput {

        /** {@inheritDoc} */
        @Override
        public Map<Vector3D, Integer> getMap() {
            return createTreeMap();
        }
    }

    /** Create a {@link TreeMap} configured to compare points using the precision
     * context for the benchmarks. This map is only intended to be used as a baseline
     * for a well-performing tree structure. It does not properly
     * partition the input points.
     * @return a new {@link TreeMap} instance
     */
    private static Map<Vector3D, Integer> createTreeMap() {
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

    /** Create a line of points.
     * @param count number of points
     * @param spacing spacing between each point
     * @return list of points in a lin
     */
    private static List<Vector3D> createLine(final int count, final double spacing) {
        final List<Vector3D> points = new ArrayList<>(count);

        final Vector3D base = Vector3D.of(2.0, 1.0, 0.5);
        for (int i = 0; i < count; ++i) {
            points.add(base.multiply(i));
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

    /** Run a benchmark using the {@link Map#put(Object, Object)} method.
     * @param input input for the run
     * @param bh blackhole instance
     * @return input instance
     */
    private static AbstractPointMapInput doPut(final AbstractPointMapInput input, final Blackhole bh) {
        final Map<Vector3D, Integer> map = input.getMap();

        for (final Vector3D p : input.getPoints()) {
            bh.consume(map.put(p, VAL));
        }

        return input;
    }

    /** Run a benchmark using the {@link Map#get(Object)} method.
     * @param input input for the run
     * @param bh blackhole instance
     * @return input instance
     */
    private static AbstractPointMapInput doGet(final AbstractPointMapInput input, final Blackhole bh) {
        final Map<Vector3D, Integer> map = input.getMap();

        for (final Vector3D p : input.getPoints()) {
            bh.consume(map.get(p));
        }

        return input;
    }

    /** Run a benchmark using the {@link Map#remove(Object)} method.
     * @param input input for the run
     * @param bh blackhole instance
     * @return input instance
     */
    private static AbstractPointMapInput doRemove(final AbstractPointMapInput input, final Blackhole bh) {
        final Map<Vector3D, Integer> map = input.getMap();

        for (final Vector3D p : input.getPoints()) {
            bh.consume(map.remove(p));
        }

        return input;
    }

    /** Run a benchmark for a distance-ordered map iteration.
     * @param input input for the run
     * @param bh blackhole instance
     * @param count maximum number of elements to iterate through for each test point
     * @param iterableFactory function to create the iterable instance for each test point
     * @return input instance
     */
    private static PreInsertedPointMapInput doDistanceIteration(
            final PreInsertedPointMapInput input,
            final Blackhole bh,
            final int count,
            final BiFunction<
                PointMap<Vector3D, Integer>,
                Vector3D,
                Iterable<?>> iterableFactory) {

        final PointMap<Vector3D, Integer> map = input.getMap();

        int i = 0;
        for (final Vector3D pt : input.getTestPoints()) {
            int cnt = 0;
            for (final Object element : iterableFactory.apply(map, pt)) {
                bh.consume(element);

                if (++cnt >= cnt) {
                    break;
                }
            }

            if (++i >= MAX_ITERABLES) {
                break;
            }
        }

        return input;
    }

    /** Run a benchmark for a distance select operation.
     * @param input input for the run
     * @param bh blackhole instance
     * @param selectFn function used to select from the map
     * @return input instance
     */
    private static PreInsertedPointMapInput doDistanceSelect(
            final PreInsertedPointMapInput input,
            final Blackhole bh,
            final BiFunction<
                PointMap<Vector3D, Integer>,
                Vector3D,
                Map.Entry<Vector3D, Integer>> selectFn) {
        final PointMap<Vector3D, Integer> map = input.getMap();

        for (final Vector3D pt : input.getTestPoints()) {
            bh.consume(selectFn.apply(map, pt));
        }

        return input;
    }

    /** Baseline benchmark for {@link Map#put(Object, Object)} using a {@link TreeMap}.
     * @param input input for the run
     * @param bh blackhole instance
     * @return input instance
     */
    @Benchmark
    public Object putTreeMapBaseline(final TreeMapInput input, final Blackhole bh) {
        return doPut(input, bh);
    }

    /** Benchmark that inserts each point in the input into the target map.
     * @param input input for the run
     * @param bh blackhole instance
     * @return input instance
     */
    @Benchmark
    public Object put(final TreeMapInput input, final Blackhole bh) {
        return doPut(input, bh);
    }

    /** Baseline benchmark for {@link Map#get(Object)} using a {@link TreeMap}.
     * @param input input for the run
     * @param bh blackhole instance
     * @return input instance
     */
    @Benchmark
    public Object getTreeMapBaseline(final PreInsertedTreeMapInput input, final Blackhole bh) {
        return doGet(input, bh);
    }

    /** Benchmark that retrieves each point in the input from the target map.
     * @param input input for the run
     * @param bh blackhole instance
     * @return input instance
     */
    @Benchmark
    public Object get(final PreInsertedPointMapInput input, final Blackhole bh) {
        return doGet(input, bh);
    }

    /** Baseline benchmark for {@link Map#remove(Object)} using a {@link TreeMap}.
     * @param input input for the run
     * @param bh blackhole instance
     * @return input instance
     */
    @Benchmark
    public Object removeTreeMapBaseline(final PreInsertedTreeMapInput input, final Blackhole bh) {
        return doGet(input, bh);
    }

    /** Benchmark that removes each point in the input from the target map.
     * @param input input for the run
     * @param bh blackhole instance
     * @return input instance
     */
    @Benchmark
    public Object remove(final PreInsertedPointMapInput input, final Blackhole bh) {
        return doRemove(input, bh);
    }

    /** Benchmark for the {@link PointMap#entriesNearToFar(org.apache.commons.geometry.core.Point)} method.
     * @param input input for the run
     * @param bh blackhole instance
     * @return input instance
     */
    @Benchmark
    public Object entriesNearToFar(final PreInsertedPointMapInput input, final Blackhole bh) {
        return doDistanceIteration(
                input,
                bh,
                input.getPoints().size(),
                PointMap::entriesNearToFar);
    }

    /** Benchmark for iterating through a portion of the elements returned by the
     * {@link PointMap#entriesNearToFar(org.apache.commons.geometry.core.Point)} method.
     * @param input input for the run
     * @param bh blackhole instance
     * @return input instance
     */
    @Benchmark
    public Object entriesNearToFarPartial(final PreInsertedPointMapInput input, final Blackhole bh) {
        final int cnt = input.getPoints().size() / 2;

        return doDistanceIteration(
                input,
                bh,
                cnt,
                PointMap::entriesNearToFar);
    }

    /** Benchmark for the {@link PointMap#nearestEntry(org.apache.commons.geometry.core.Point)} method.
     * @param input input for the run
     * @param bh blackhole instance
     * @return input instance
     */
    @Benchmark
    public Object nearestEntry(final PreInsertedPointMapInput input, final Blackhole bh) {
        return doDistanceSelect(
                input,
                bh,
                PointMap::nearestEntry);
    }

    /** Benchmark for the {@link PointMap#entriesFarToNear(org.apache.commons.geometry.core.Point)} method.
     * @param input input for the run
     * @param bh blackhole instance
     * @return input instance
     */
    @Benchmark
    public Object entriesFarToNear(final PreInsertedPointMapInput input, final Blackhole bh) {
        return doDistanceIteration(
                input,
                bh,
                input.getPoints().size(),
                PointMap::entriesFarToNear);
    }

    /** Benchmark for iterating through a portion of the results of the
     * {@link PointMap#entriesFarToNear(org.apache.commons.geometry.core.Point)} method.
     * @param input input for the run
     * @param bh blackhole instance
     * @return input instance
     */
    @Benchmark
    public Object entriesFarToNearPartial(final PreInsertedPointMapInput input, final Blackhole bh) {
        final int cnt = input.getPoints().size() / 2;

        return doDistanceIteration(
                input,
                bh,
                cnt,
                PointMap::entriesFarToNear);
    }

    /** Benchmark for the {@link PointMap#farthestEntry(org.apache.commons.geometry.core.Point)} method.
     * @param input input for the run
     * @param bh blackhole instance
     * @return input instance
     */
    @Benchmark
    public Object farthestEntry(final PreInsertedPointMapInput input, final Blackhole bh) {
        return doDistanceSelect(
                input,
                bh,
                PointMap::farthestEntry);
    }
}
