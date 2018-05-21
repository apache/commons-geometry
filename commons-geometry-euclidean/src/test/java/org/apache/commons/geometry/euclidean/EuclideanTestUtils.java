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
import org.apache.commons.geometry.euclidean.oned.Cartesian1D;
import org.apache.commons.geometry.euclidean.oned.IntervalsSet;
import org.apache.commons.geometry.euclidean.oned.OrientedPoint;
import org.apache.commons.geometry.euclidean.oned.Point1D;
import org.apache.commons.geometry.euclidean.oned.SubOrientedPoint;
import org.apache.commons.geometry.euclidean.threed.Cartesian3D;
import org.apache.commons.geometry.euclidean.threed.Plane;
import org.apache.commons.geometry.euclidean.threed.Point3D;
import org.apache.commons.geometry.euclidean.threed.PolyhedronsSet;
import org.apache.commons.geometry.euclidean.threed.SubPlane;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.twod.Cartesian2D;
import org.apache.commons.geometry.euclidean.twod.Line;
import org.apache.commons.geometry.euclidean.twod.Point2D;
import org.apache.commons.geometry.euclidean.twod.PolygonsSet;
import org.apache.commons.geometry.euclidean.twod.SubLine;
import org.junit.Assert;

/** Class containing various euclidean-related test utilities.
 */
public class EuclideanTestUtils {

    /** Asserts that corresponding values in the given vectors are equal, using the specified
     * tolerance value.
     * @param expected
     * @param actual
     * @param tolerance
     */
    public static void assertCoordinatesEqual(Cartesian1D expected, Cartesian1D actual, double tolerance) {
        String msg = "Expected coordinates to equal " + expected + " but was " + actual + ";";
        Assert.assertEquals(msg, expected.getX(), actual.getX(), tolerance);
    }

    /** Asserts that corresponding values in the given vectors are equal, using the specified
     * tolerance value.
     * @param expected
     * @param actual
     * @param tolerance
     */
    public static void assertCoordinatesEqual(Cartesian2D expected, Cartesian2D actual, double tolerance) {
        String msg = "Expected coordinates to equal " + expected + " but was " + actual + ";";
        Assert.assertEquals(msg, expected.getX(), actual.getX(), tolerance);
        Assert.assertEquals(msg, expected.getY(), actual.getY(), tolerance);
    }

    /** Asserts that corresponding values in the given vectors are equal, using the specified
     * tolerance value.
     * @param expected
     * @param actual
     * @param tolerance
     */
    public static void assertCoordinatesEqual(Cartesian3D expected, Cartesian3D actual, double tolerance) {
        String msg = "Expected coordinates to equal " + expected + " but was " + actual + ";";
        Assert.assertEquals(msg, expected.getX(), actual.getX(), tolerance);
        Assert.assertEquals(msg, expected.getY(), actual.getY(), tolerance);
        Assert.assertEquals(msg, expected.getZ(), actual.getZ(), tolerance);
    }

    /** Asserts that the given value is positive infinity.
     * @param value
     */
    public static void assertPositiveInfinity(double value) {
        String msg = "Expected value to be positive infinity but was " + value;
        Assert.assertTrue(msg, Double.isInfinite(value));
        Assert.assertTrue(msg, value > 0);
    }

    /** Asserts that the given value is negative infinity..
     * @param value
     */
    public static void assertNegativeInfinity(double value) {
        String msg = "Expected value to be negative infinity but was " + value;
        Assert.assertTrue(msg, Double.isInfinite(value));
        Assert.assertTrue(msg, value < 0);
    }

    /** Get a string representation of an {@link IntervalsSet}.
     * @param intervalsSet region to dump
     * @return string representation of the region
     */
    public static String dump(final IntervalsSet intervalsSet) {
        final TreeDumper<Point1D> visitor = new TreeDumper<Point1D>("IntervalsSet", intervalsSet.getTolerance()) {

            /** {@inheritDoc} */
            @Override
            protected void formatHyperplane(final Hyperplane<Point1D> hyperplane) {
                final OrientedPoint h = (OrientedPoint) hyperplane;
                getFormatter().format("%22.15e %b %22.15e",
                                      h.getLocation().getX(), h.isDirect(), h.getTolerance());
            }

        };
        intervalsSet.getTree(false).visit(visitor);
        return visitor.getDump();
    }

    /** Get a string representation of a {@link PolygonsSet}.
     * @param polygonsSet region to dump
     * @return string representation of the region
     */
    public static String dump(final PolygonsSet polygonsSet) {
        final TreeDumper<Point2D> visitor = new TreeDumper<Point2D>("PolygonsSet", polygonsSet.getTolerance()) {

            /** {@inheritDoc} */
            @Override
            protected void formatHyperplane(final Hyperplane<Point2D> hyperplane) {
                final Line h = (Line) hyperplane;
                final Point2D p = h.toSpace(Point1D.ZERO);
                getFormatter().format("%22.15e %22.15e %22.15e %22.15e",
                                      p.getX(), p.getY(), h.getAngle(), h.getTolerance());
            }

        };
        polygonsSet.getTree(false).visit(visitor);
        return visitor.getDump();
    }

    /** Get a string representation of a {@link PolyhedronsSet}.
     * @param polyhedronsSet region to dump
     * @return string representation of the region
     */
    public static String dump(final PolyhedronsSet polyhedronsSet) {
        final TreeDumper<Point3D> visitor = new TreeDumper<Point3D>("PolyhedronsSet", polyhedronsSet.getTolerance()) {

            /** {@inheritDoc} */
            @Override
            protected void formatHyperplane(final Hyperplane<Point3D> hyperplane) {
                final Plane h = (Plane) hyperplane;
                final Point3D p = h.toSpace(Point2D.ZERO);
                getFormatter().format("%22.15e %22.15e %22.15e %22.15e %22.15e %22.15e %22.15e",
                                      p.getX(), p.getY(), p.getZ(),
                                      h.getNormal().getX(), h.getNormal().getY(), h.getNormal().getZ(),
                                      h.getTolerance());
            }

        };
        polyhedronsSet.getTree(false).visit(visitor);
        return visitor.getDump();
    }

    /** Parse a string representation of an {@link IntervalsSet}.
     * @param s string to parse
     * @return parsed region
     * @exception IOException if the string cannot be read
     * @exception ParseException if the string cannot be parsed
     */
    public static IntervalsSet parseIntervalsSet(final String s)
        throws IOException, ParseException {
        final TreeBuilder<Point1D> builder = new TreeBuilder<Point1D>("IntervalsSet", s) {

            /** {@inheritDoc} */
            @Override
            public OrientedPoint parseHyperplane()
                throws IOException, ParseException {
                return new OrientedPoint(new Point1D(getNumber()), getBoolean(), getNumber());
            }

        };
        return new IntervalsSet(builder.getTree(), builder.getTolerance());
    }

    /** Parse a string representation of a {@link PolygonsSet}.
     * @param s string to parse
     * @return parsed region
     * @exception IOException if the string cannot be read
     * @exception ParseException if the string cannot be parsed
     */
    public static PolygonsSet parsePolygonsSet(final String s)
        throws IOException, ParseException {
        final TreeBuilder<Point2D> builder = new TreeBuilder<Point2D>("PolygonsSet", s) {

            /** {@inheritDoc} */
            @Override
            public Line parseHyperplane()
                throws IOException, ParseException {
                return new Line(new Point2D(getNumber(), getNumber()), getNumber(), getNumber());
            }

        };
        return new PolygonsSet(builder.getTree(), builder.getTolerance());
    }

    /** Parse a string representation of a {@link PolyhedronsSet}.
     * @param s string to parse
     * @return parsed region
     * @exception IOException if the string cannot be read
     * @exception ParseException if the string cannot be parsed
     */
    public static PolyhedronsSet parsePolyhedronsSet(final String s)
        throws IOException, ParseException {
        final TreeBuilder<Point3D> builder = new TreeBuilder<Point3D>("PolyhedronsSet", s) {

            /** {@inheritDoc} */
            @Override
            public Plane parseHyperplane()
                throws IOException, ParseException {
                return new Plane(new Point3D(getNumber(), getNumber(), getNumber()),
                                 new Vector3D(getNumber(), getNumber(), getNumber()),
                                 getNumber());
            }

        };
        return new PolyhedronsSet(builder.getTree(), builder.getTolerance());
    }



    /** Prints a string representation of the given 1D {@link BSPTree} to
     * the console. This is intended for quick debugging of small trees.
     * @param tree
     */
    public static void printTree1D(BSPTree<Point1D> tree) {
        TreePrinter1D printer = new TreePrinter1D();
        System.out.println(printer.writeAsString(tree));
    }

    /** Prints a string representation of the given 2D {@link BSPTree} to
     * the console. This is intended for quick debugging of small trees.
     * @param tree
     */
    public static void printTree2D(BSPTree<Point2D> tree) {
        TreePrinter2D printer = new TreePrinter2D();
        System.out.println(printer.writeAsString(tree));
    }

    /** Prints a string representation of the given 3D {@link BSPTree} to
     * the console. This is intended for quick debugging of small trees.
     * @param tree
     */
    public static void printTree3D(BSPTree<Point3D> tree) {
        TreePrinter3D printer = new TreePrinter3D();
        System.out.println(printer.writeAsString(tree));
    }


    /** Class for creating string representations of 1D {@link BSPTree}s.
     */
    public static class TreePrinter1D extends TreePrinter<Point1D> {

        /** {@inheritDoc} */
        @Override
        protected void writeInternalNode(BSPTree<Point1D> node) {
            SubOrientedPoint cut = (SubOrientedPoint) node.getCut();

            OrientedPoint hyper = (OrientedPoint) cut.getHyperplane();
            write("cut = { hyperplane: ");
            if (hyper.isDirect()) {
                write("[" + hyper.getLocation().getX() + ", inf)");
            }
            else {
                write("(-inf, " + hyper.getLocation().getX() + "]");
            }

            IntervalsSet remainingRegion = (IntervalsSet) cut.getRemainingRegion();
            if (remainingRegion != null) {
                write(", remainingRegion: [");

                boolean isFirst = true;
                for (double[] interval : remainingRegion) {
                    if (isFirst) {
                        isFirst = false;
                    }
                    else {
                        write(", ");
                    }
                    write(Arrays.toString(interval));
                }

                write("]");
            }

            write("}");
        }
    }

    /** Class for creating string representations of 2D {@link BSPTree}s.
     */
    public static class TreePrinter2D extends TreePrinter<Point2D> {

        /** {@inheritDoc} */
        @Override
        protected void writeInternalNode(BSPTree<Point2D> node) {
            SubLine cut = (SubLine) node.getCut();
            Line line = (Line) cut.getHyperplane();
            IntervalsSet remainingRegion = (IntervalsSet) cut.getRemainingRegion();

            write("cut = { angle: " + Math.toDegrees(line.getAngle()) + ", origin: " + line.toSpace(Point1D.ZERO) + "}");
            write(", remainingRegion: [");

            boolean isFirst = true;
            for (double[] interval : remainingRegion) {
                if (isFirst) {
                    isFirst = false;
                }
                else {
                    write(", ");
                }
                write(Arrays.toString(interval));
            }

            write("]");
        }
    }

    /** Class for creating string representations of 3D {@link BSPTree}s.
     */
    public static class TreePrinter3D extends TreePrinter<Point3D> {

        /** {@inheritDoc} */
        @Override
        protected void writeInternalNode(BSPTree<Point3D> node) {
            SubPlane cut = (SubPlane) node.getCut();
            Plane plane = (Plane) cut.getHyperplane();
            PolygonsSet polygon = (PolygonsSet) cut.getRemainingRegion();

            write("cut = { normal: " + plane.getNormal() + ", origin: " + plane.getOrigin() + "}");
            write(", remainingRegion = [");

            boolean isFirst = true;
            for (Point2D[] loop : polygon.getVertices()) {
                // convert to 3-space for easier debugging
                List<Point3D> loop3 = new ArrayList<>();
                for (Point2D vertex : loop) {
                    if (vertex != null) {
                        loop3.add(plane.toSpace(vertex));
                    }
                    else {
                        loop3.add(null);
                    }
                }

                if (isFirst) {
                    isFirst = false;
                }
                else {
                    write(", ");
                }

                write(loop3.toString());
            }

            write("]");
        }
    }
}
