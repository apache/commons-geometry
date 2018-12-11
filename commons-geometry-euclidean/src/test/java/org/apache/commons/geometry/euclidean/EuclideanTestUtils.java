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
package org.apache.commons.geometry.euclidean;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.partitioning.BSPTree;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.TreeBuilder;
import org.apache.commons.geometry.core.partitioning.TreeDumper;
import org.apache.commons.geometry.core.partitioning.TreePrinter;
import org.apache.commons.geometry.euclidean.oned.IntervalsSet;
import org.apache.commons.geometry.euclidean.oned.OrientedPoint;
import org.apache.commons.geometry.euclidean.oned.SubOrientedPoint;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.threed.Plane;
import org.apache.commons.geometry.euclidean.threed.PolyhedronsSet;
import org.apache.commons.geometry.euclidean.threed.SubPlane;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.twod.Line;
import org.apache.commons.geometry.euclidean.twod.PolygonsSet;
import org.apache.commons.geometry.euclidean.twod.SubLine;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.junit.Assert;

/**
 * Class containing various euclidean-related test utilities.
 */
public class EuclideanTestUtils {

    /** Callback interface for {@link #permute(double, double, double, PermuteCallback2D)}. */
    @FunctionalInterface
    public static interface PermuteCallback2D {
        void accept(double x, double y);
    }

    /** Callback interface for {@link #permute(double, double, double, PermuteCallback3D)} */
    @FunctionalInterface
    public static interface PermuteCallback3D {
        void accept(double x, double y, double z);
    }

    /** Iterate through all {@code (x, y)} permutations for the given range of numbers and
     * call {@code callback} for each.
     *
     * @param min the minimum number in the range
     * @param max the maximum number in the range
     * @param step the step (increment) value for the range
     * @param callback callback to invoke for each permutation.
     */
    public static void permute(double min, double max, double step, PermuteCallback2D callback) {
        permuteInternal(min, max, step, false, callback);
    }

    /** Same as {@link #permute(double, double, double, PermuteCallback2D)} but skips the {@code (0, 0))}
     * permutation.
     *
     * @param min the minimum number in the range
     * @param max the maximum number in the range
     * @param step the step (increment) value for the range
     * @param callback callback to invoke for each permutation.
     */
    public static void permuteSkipZero(double min, double max, double step, PermuteCallback2D callback) {
        permuteInternal(min, max, step, true, callback);
    }

    /** Internal permutation method. Iterates through all {@code (x, y)} permutations for the given range
     * of numbers and calls {@code callback} for each.
     *
     * @param min the minimum number in the range
     * @param max the maximum number in the range
     * @param step the step (increment) value for the range
     * @param skipZero if true, the {@code (0, 0)} permutation will be skipped
     * @param callback callback to invoke for each permutation.
     */
    private static void permuteInternal(double min, double max, double step, boolean skipZero, PermuteCallback2D callback) {
        for (double x = min; x <= max; x += step) {
            for (double y = min; y <= max; y += step) {
                if (!skipZero || (x != 0.0 || y != 0.0)) {
                    callback.accept(x, y);
                }
            }
        }
    }

    /** Iterate through all {@code (x, y, z)} permutations for the given range of numbers and
     * call {@code callback} for each.
     *
     * @param min the minimum number in the range
     * @param max the maximum number in the range
     * @param step the step (increment) value for the range
     * @param callback callback to invoke for each permutation.
     */
    public static void permute(double min, double max, double step, PermuteCallback3D callback) {
        permuteInternal(min, max, step, false, callback);
    }

    /** Same as {@link #permute(double, double, double, PermuteCallback3D)} but skips the {@code (0, 0, 0)}
     * permutation.
     *
     * @param min the minimum number in the range
     * @param max the maximum number in the range
     * @param step the step (increment) value for the range
     * @param callback callback to invoke for each permutation.
     */
    public static void permuteSkipZero(double min, double max, double step, PermuteCallback3D callback) {
        permuteInternal(min, max, step, true, callback);
    }

    /** Internal permutation method. Iterates through all {@code (x, y)} permutations for the given range
     * of numbers and calls {@code callback} for each.
     *
     * @param min the minimum number in the range
     * @param max the maximum number in the range
     * @param step the step (increment) value for the range
     * @param skipZero if true, the {@code (0, 0, 0)} permutation will be skipped
     * @param callback callback to invoke for each permutation.
     */
    private static void permuteInternal(double min, double max, double step, boolean skipZero, PermuteCallback3D callback) {
        for (double x = min; x <= max; x += step) {
            for (double y = min; y <= max; y += step) {
                for (double z = min; z <= max; z += step) {
                    if (!skipZero || (x != 0.0 || y != 0.0 || z != 0.0)) {
                        callback.accept(x, y, z);
                    }
                }
            }
        }
    }

    /**
     * Asserts that corresponding values in the given vectors are equal, using the
     * specified tolerance value.
     *
     * @param expected
     * @param actual
     * @param tolerance
     */
    public static void assertCoordinatesEqual(Vector1D expected, Vector1D actual, double tolerance) {
        String msg = "Expected coordinates to equal " + expected + " but was " + actual + ";";
        Assert.assertEquals(msg, expected.getX(), actual.getX(), tolerance);
    }

    /**
     * Asserts that corresponding values in the given vectors are equal, using the
     * specified tolerance value.
     *
     * @param expected
     * @param actual
     * @param tolerance
     */
    public static void assertCoordinatesEqual(Vector2D expected, Vector2D actual, double tolerance) {
        String msg = "Expected coordinates to equal " + expected + " but was " + actual + ";";
        Assert.assertEquals(msg, expected.getX(), actual.getX(), tolerance);
        Assert.assertEquals(msg, expected.getY(), actual.getY(), tolerance);
    }

    /**
     * Asserts that corresponding values in the given vectors are equal, using the
     * specified tolerance value.
     *
     * @param expected
     * @param actual
     * @param tolerance
     */
    public static void assertCoordinatesEqual(Vector3D expected, Vector3D actual, double tolerance) {
        String msg = "Expected coordinates to equal " + expected + " but was " + actual + ";";
        Assert.assertEquals(msg, expected.getX(), actual.getX(), tolerance);
        Assert.assertEquals(msg, expected.getY(), actual.getY(), tolerance);
        Assert.assertEquals(msg, expected.getZ(), actual.getZ(), tolerance);
    }

    /**
     * Asserts that the given value is positive infinity.
     *
     * @param value
     */
    public static void assertPositiveInfinity(double value) {
        String msg = "Expected value to be positive infinity but was " + value;
        Assert.assertTrue(msg, Double.isInfinite(value));
        Assert.assertTrue(msg, value > 0);
    }

    /**
     * Asserts that the given value is negative infinity..
     *
     * @param value
     */
    public static void assertNegativeInfinity(double value) {
        String msg = "Expected value to be negative infinity but was " + value;
        Assert.assertTrue(msg, Double.isInfinite(value));
        Assert.assertTrue(msg, value < 0);
    }

    /**
     * Get a string representation of an {@link IntervalsSet}.
     *
     * @param intervalsSet region to dump
     * @return string representation of the region
     */
    public static String dump(final IntervalsSet intervalsSet) {
        final TreeDumper<Vector1D> visitor = new TreeDumper<Vector1D>("IntervalsSet", intervalsSet.getTolerance()) {

            /** {@inheritDoc} */
            @Override
            protected void formatHyperplane(final Hyperplane<Vector1D> hyperplane) {
                final OrientedPoint h = (OrientedPoint) hyperplane;
                getFormatter().format("%22.15e %b %22.15e", h.getLocation().getX(), h.isDirect(), h.getTolerance());
            }

        };
        intervalsSet.getTree(false).visit(visitor);
        return visitor.getDump();
    }

    /**
     * Get a string representation of a {@link PolygonsSet}.
     *
     * @param polygonsSet region to dump
     * @return string representation of the region
     */
    public static String dump(final PolygonsSet polygonsSet) {
        final TreeDumper<Vector2D> visitor = new TreeDumper<Vector2D>("PolygonsSet", polygonsSet.getTolerance()) {

            /** {@inheritDoc} */
            @Override
            protected void formatHyperplane(final Hyperplane<Vector2D> hyperplane) {
                final Line h = (Line) hyperplane;
                final Vector2D p = h.toSpace(Vector1D.ZERO);
                getFormatter().format("%22.15e %22.15e %22.15e %22.15e",
                                      p.getX(), p.getY(), h.getAngle(), h.getTolerance());
            }

        };
        polygonsSet.getTree(false).visit(visitor);
        return visitor.getDump();
    }

    /**
     * Get a string representation of a {@link PolyhedronsSet}.
     *
     * @param polyhedronsSet region to dump
     * @return string representation of the region
     */
    public static String dump(final PolyhedronsSet polyhedronsSet) {
        final TreeDumper<Vector3D> visitor = new TreeDumper<Vector3D>("PolyhedronsSet", polyhedronsSet.getTolerance()) {

            /** {@inheritDoc} */
            @Override
            protected void formatHyperplane(final Hyperplane<Vector3D> hyperplane) {
                final Plane h = (Plane) hyperplane;
                final Vector3D p = h.toSpace(Vector2D.ZERO);
                getFormatter().format("%22.15e %22.15e %22.15e %22.15e %22.15e %22.15e %22.15e",
                                      p.getX(), p.getY(), p.getZ(),
                                      h.getNormal().getX(), h.getNormal().getY(), h.getNormal().getZ(),
                                      h.getTolerance());
            }

        };
        polyhedronsSet.getTree(false).visit(visitor);
        return visitor.getDump();
    }

    /**
     * Parse a string representation of an {@link IntervalsSet}.
     *
     * @param s string to parse
     * @return parsed region
     * @exception IOException    if the string cannot be read
     * @exception ParseException if the string cannot be parsed
     */
    public static IntervalsSet parseIntervalsSet(final String s)
        throws IOException, ParseException {
        final TreeBuilder<Vector1D> builder = new TreeBuilder<Vector1D>("IntervalsSet", s) {

            /** {@inheritDoc} */
            @Override
            public OrientedPoint parseHyperplane()
                throws IOException, ParseException {
                return new OrientedPoint(Vector1D.of(getNumber()), getBoolean(), getNumber());
            }

        };
        return new IntervalsSet(builder.getTree(), builder.getTolerance());
    }

    /**
     * Parse a string representation of a {@link PolygonsSet}.
     *
     * @param s string to parse
     * @return parsed region
     * @exception IOException    if the string cannot be read
     * @exception ParseException if the string cannot be parsed
     */
    public static PolygonsSet parsePolygonsSet(final String s)
        throws IOException, ParseException {
        final TreeBuilder<Vector2D> builder = new TreeBuilder<Vector2D>("PolygonsSet", s) {

            /** {@inheritDoc} */
            @Override
            public Line parseHyperplane()
                throws IOException, ParseException {
                return new Line(Vector2D.of(getNumber(), getNumber()), getNumber(), getNumber());
            }

        };
        return new PolygonsSet(builder.getTree(), builder.getTolerance());
    }

    /**
     * Parse a string representation of a {@link PolyhedronsSet}.
     *
     * @param s string to parse
     * @return parsed region
     * @exception IOException    if the string cannot be read
     * @exception ParseException if the string cannot be parsed
     */
    public static PolyhedronsSet parsePolyhedronsSet(final String s)
        throws IOException, ParseException {
        final TreeBuilder<Vector3D> builder = new TreeBuilder<Vector3D>("PolyhedronsSet", s) {

            /** {@inheritDoc} */
            @Override
            public Plane parseHyperplane()
                throws IOException, ParseException {
                return new Plane(Vector3D.of(getNumber(), getNumber(), getNumber()),
                                 Vector3D.of(getNumber(), getNumber(), getNumber()),
                                 getNumber());
            }

        };
        return new PolyhedronsSet(builder.getTree(), builder.getTolerance());
    }

    /**
     * Prints a string representation of the given 1D {@link BSPTree} to the
     * console. This is intended for quick debugging of small trees.
     *
     * @param tree
     */
    public static void printTree1D(BSPTree<Vector1D> tree) {
        TreePrinter1D printer = new TreePrinter1D();
        System.out.println(printer.writeAsString(tree));
    }

    /**
     * Prints a string representation of the given 2D {@link BSPTree} to the
     * console. This is intended for quick debugging of small trees.
     *
     * @param tree
     */
    public static void printTree2D(BSPTree<Vector2D> tree) {
        TreePrinter2D printer = new TreePrinter2D();
        System.out.println(printer.writeAsString(tree));
    }

    /**
     * Prints a string representation of the given 3D {@link BSPTree} to the
     * console. This is intended for quick debugging of small trees.
     *
     * @param tree
     */
    public static void printTree3D(BSPTree<Vector3D> tree) {
        TreePrinter3D printer = new TreePrinter3D();
        System.out.println(printer.writeAsString(tree));
    }

    /**
     * Class for creating string representations of 1D {@link BSPTree}s.
     */
    public static class TreePrinter1D extends TreePrinter<Vector1D> {

        /** {@inheritDoc} */
        @Override
        protected void writeInternalNode(BSPTree<Vector1D> node) {
            SubOrientedPoint cut = (SubOrientedPoint) node.getCut();

            OrientedPoint hyper = (OrientedPoint) cut.getHyperplane();
            write("cut = { hyperplane: ");
            if (hyper.isDirect()) {
                write("[" + hyper.getLocation().getX() + ", inf)");
            } else {
                write("(-inf, " + hyper.getLocation().getX() + "]");
            }

            IntervalsSet remainingRegion = (IntervalsSet) cut.getRemainingRegion();
            if (remainingRegion != null) {
                write(", remainingRegion: [");

                boolean isFirst = true;
                for (double[] interval : remainingRegion) {
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        write(", ");
                    }
                    write(Arrays.toString(interval));
                }

                write("]");
            }

            write("}");
        }
    }

    /**
     * Class for creating string representations of 2D {@link BSPTree}s.
     */
    public static class TreePrinter2D extends TreePrinter<Vector2D> {

        /** {@inheritDoc} */
        @Override
        protected void writeInternalNode(BSPTree<Vector2D> node) {
            SubLine cut = (SubLine) node.getCut();
            Line line = (Line) cut.getHyperplane();
            IntervalsSet remainingRegion = (IntervalsSet) cut.getRemainingRegion();

            write("cut = { angle: " + Math.toDegrees(line.getAngle()) + ", origin: " + line.toSpace(Vector1D.ZERO) + "}");
            write(", remainingRegion: [");

            boolean isFirst = true;
            for (double[] interval : remainingRegion) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    write(", ");
                }
                write(Arrays.toString(interval));
            }

            write("]");
        }
    }

    /**
     * Class for creating string representations of 3D {@link BSPTree}s.
     */
    public static class TreePrinter3D extends TreePrinter<Vector3D> {

        /** {@inheritDoc} */
        @Override
        protected void writeInternalNode(BSPTree<Vector3D> node) {
            SubPlane cut = (SubPlane) node.getCut();
            Plane plane = (Plane) cut.getHyperplane();
            PolygonsSet polygon = (PolygonsSet) cut.getRemainingRegion();

            write("cut = { normal: " + plane.getNormal() + ", origin: " + plane.getOrigin() + "}");
            write(", remainingRegion = [");

            boolean isFirst = true;
            for (Vector2D[] loop : polygon.getVertices()) {
                // convert to 3-space for easier debugging
                List<Vector3D> loop3 = new ArrayList<>();
                for (Vector2D vertex : loop) {
                    if (vertex != null) {
                        loop3.add(plane.toSpace(vertex));
                    } else {
                        loop3.add(null);
                    }
                }

                if (isFirst) {
                    isFirst = false;
                } else {
                    write(", ");
                }

                write(loop3.toString());
            }

            write("]");
        }
    }
}
