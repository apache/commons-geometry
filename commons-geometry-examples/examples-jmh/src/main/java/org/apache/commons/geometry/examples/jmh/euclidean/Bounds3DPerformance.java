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
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.geometry.euclidean.threed.Bounds3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.line.Line3D;
import org.apache.commons.geometry.euclidean.threed.line.Lines3D;
import org.apache.commons.geometry.euclidean.threed.line.Segment3D;
import org.apache.commons.numbers.core.Precision;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.shape.UnitBallSampler;
import org.apache.commons.rng.simple.RandomSource;
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

/** Benchmarks for the {@link Bounds3D} class.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = {"-server", "-Xms512M", "-Xmx512M"})
public class Bounds3DPerformance {

    /** Precision context. */
    private static final Precision.DoubleEquivalence PRECISION =
            Precision.doubleEquivalenceOfEpsilon(1e-6);

    /** Benchmark input class providing random line segments. */
    @State(Scope.Thread)
    public static class SegmentInput {

        /** Minimum value used to construct random point coordinates. */
        private static final double COORDINATE_MIN = -10;

        /** Maximum value used to construct random point coordinates. */
        private static final double COORDINATE_MAX = +10;

        /** Number of line segments to generate. */
        @Param({"1000"})
        private int count;

        /** Seed value for randomization. */
        @Param({"1"})
        private int randomSeed;

        /** List of segments for the run. */
        private List<Segment3D> segments;

        /** Random instance. */
        private UniformRandomProvider random;

        /** Get the segments for the run.
         * @return segments for the run
         */
        public List<Segment3D> getSegments() {
            return segments;
        }

        /** Set up the instance for the benchmark. */
        @Setup(Level.Iteration)
        public void setup() {
            random = RandomSource.XO_SHI_RO_256_PP.create(randomSeed);

            final UnitBallSampler ballSampler = UnitBallSampler.of(random, 3);

            segments = new ArrayList<>(count);
            for (int i = 0; i < count; ++i) {
                final Vector3D pt = randomPoint();
                final Vector3D dir = Vector3D.of(ballSampler.sample());

                final Line3D line = Lines3D.fromPointAndDirection(pt, dir, PRECISION);

                segments.add(line.segment(randomCoordinate(), randomCoordinate()));
            }
        }

        /** Return a random point with coordinates within the configured min and max range.
         * @return a random point with coordinates within the configured min and max range
         */
        private Vector3D randomPoint() {
            return Vector3D.of(
                    randomCoordinate(),
                    randomCoordinate(),
                    randomCoordinate());
        }

        /** Return a random double coordinate value within the configured min and max range.
         * @return a random double coordinate value within the configured min and max range
         */
        private double randomCoordinate() {
            return ((COORDINATE_MAX - COORDINATE_MIN) * random.nextDouble()) + COORDINATE_MIN;

        }
    }

    /** Construct a default {@link Bounds3D} instance for performance testing.
     * @return a default {@link Bounds3D} instance
     */
    private static Bounds3D createDefaultTestBounds() {
        return Bounds3D.from(Vector3D.of(-1, -1, -1), Vector3D.of(1, 1, 1));
    }

    /** Baseline benchmark that construct a bounds instance and iterates through all
     * input values.
     * @param input input for the run
     * @param bh blackhole instance
     * @return bounds instance
     */
    @Benchmark
    public Bounds3D segmentBaseline(final SegmentInput input, final Blackhole bh) {
        final Bounds3D bounds = createDefaultTestBounds();
        for (final Segment3D segment : input.getSegments()) {
            bh.consume(segment);
        }
        return bounds;
    }

    /** Benchmark that tests the performance of the
     * {@link Bounds3D#intersects(org.apache.commons.geometry.euclidean.threed.line.LineConvexSubset3D)} method.
     * @param input input for the run
     * @param bh blackhole instance
     * @return bounds instance
     */
    @Benchmark
    public Bounds3D segmentIntersects(final SegmentInput input, final Blackhole bh) {
        final Bounds3D bounds = createDefaultTestBounds();
        for (final Segment3D segment : input.getSegments()) {
            bh.consume(bounds.intersects(segment));
        }
        return bounds;
    }

    /** Benchmark that tests the performance of the
     * {@link Bounds3D#intersection(org.apache.commons.geometry.euclidean.threed.line.LineConvexSubset3D)} method.
     * @param input input for the run
     * @param bh blackhole instance
     * @return bounds instance
     */
    @Benchmark
    public Bounds3D segmentIntersection(final SegmentInput input, final Blackhole bh) {
        final Bounds3D bounds = createDefaultTestBounds();
        for (final Segment3D segment : input.getSegments()) {
            bh.consume(bounds.intersection(segment));
        }
        return bounds;
    }

    /** Benchmark that tests the performance of the
     * {@link Bounds3D#linecast(org.apache.commons.geometry.euclidean.threed.line.LineConvexSubset3D)} method.
     * @param input input for the run
     * @param bh blackhole instance
     * @return bounds instance
     */
    @Benchmark
    public Bounds3D segmentLinecast(final SegmentInput input, final Blackhole bh) {
        final Bounds3D bounds = createDefaultTestBounds();
        for (final Segment3D segment : input.getSegments()) {
            bh.consume(bounds.linecast(segment));
        }
        return bounds;
    }

    /** Benchmark that tests the performance of the
     * {@link Bounds3D#linecastFirst(org.apache.commons.geometry.euclidean.threed.line.LineConvexSubset3D)} method.
     * @param input input for the run
     * @param bh blackhole instance
     * @return bounds instance
     */
    @Benchmark
    public Bounds3D segmentLinecastFirst(final SegmentInput input, final Blackhole bh) {
        final Bounds3D bounds = createDefaultTestBounds();
        for (final Segment3D segment : input.getSegments()) {
            bh.consume(bounds.linecastFirst(segment));
        }
        return bounds;
    }
}
