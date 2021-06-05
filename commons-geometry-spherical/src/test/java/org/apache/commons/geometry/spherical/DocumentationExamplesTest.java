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
package org.apache.commons.geometry.spherical;

import java.util.List;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.oned.AngularInterval;
import org.apache.commons.geometry.spherical.oned.Point1S;
import org.apache.commons.geometry.spherical.oned.RegionBSPTree1S;
import org.apache.commons.geometry.spherical.twod.GreatArcPath;
import org.apache.commons.geometry.spherical.twod.GreatCircle;
import org.apache.commons.geometry.spherical.twod.GreatCircles;
import org.apache.commons.geometry.spherical.twod.Point2S;
import org.apache.commons.geometry.spherical.twod.RegionBSPTree2S;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** This class contains code listed as examples in the user guide and other documentation.
 * If any portion of this code changes, the corresponding examples in the documentation <em>must</em> be updated.
 */
class DocumentationExamplesTest {

    private static final double TEST_EPS = 1e-12;

    @Test
    void testAngularIntervalExample() {
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-6);

        // create angular intervals of different sizes, one of size pi/2 and one of size 3pi/2
        final AngularInterval a = AngularInterval.of(0, Angle.PI_OVER_TWO, precision);
        final AngularInterval b = AngularInterval.of(Point1S.PI, Point1S.of(Angle.PI_OVER_TWO), precision);

        // test some points
        a.contains(Point1S.of(0.25 * Math.PI)); // true
        b.contains(Point1S.of(0.25 * Math.PI)); // true

        final RegionLocation aLocZero = a.classify(Point1S.ZERO); // RegionLocation.BOUNDARY
        final RegionLocation bLocZero = b.classify(Point1S.ZERO); // RegionLocation.INSIDE

        // -------------------
        Assertions.assertTrue(a.contains(Point1S.of(0.25 * Math.PI)));
        Assertions.assertTrue(b.contains(Point1S.of(0.25 * Math.PI)));

        Assertions.assertEquals(RegionLocation.BOUNDARY, aLocZero);
        Assertions.assertEquals(RegionLocation.INSIDE, bLocZero);
    }

    @Test
    void testRegionBSPTree1SExample() {
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-6);

        // create a region from the union of multiple angular intervals
        final RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(0, 0.25 * Math.PI, precision));
        tree.add(AngularInterval.of(0.5 * Math.PI, Math.PI, precision));
        tree.add(AngularInterval.of(0.75 * Math.PI, 1.5 * Math.PI, precision));

        // compute the region size in radians
        final double size = tree.getSize(); // 1.25pi

        // convert back to intervals
        final List<AngularInterval> intervals = tree.toIntervals(); //size = 2

        // ---------------
        Assertions.assertEquals(1.25 * Math.PI, size, TEST_EPS);
        Assertions.assertEquals(2, intervals.size());
    }

    @Test
    void testGreatCircleIntersectionExample() {
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-6);

        // create two great circles
        final GreatCircle a = GreatCircles.fromPoints(Point2S.PLUS_I, Point2S.PLUS_K, precision);
        final GreatCircle b = GreatCircles.fromPole(Vector3D.Unit.PLUS_Z, precision);

        // find the two intersection points of the great circles
        final Point2S ptA = a.intersection(b); //(pi, pi/2)
        final Point2S ptB = ptA.antipodal(); // (0, pi/2)

        // ----------------------
        SphericalTestUtils.assertPointsEq(Point2S.MINUS_I, ptA, TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.PLUS_I, ptB, TEST_EPS);
    }

    @Test
    void testRegionBSPTree2SExample() {
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-6);

        // create a path outlining a quadrant triangle
        final GreatArcPath path = GreatArcPath.builder(precision)
                .append(Point2S.PLUS_I)
                .append(Point2S.PLUS_J)
                .append(Point2S.PLUS_K)
                .build(true); // close the path with the starting path

        // convert to a region
        final RegionBSPTree2S tree = path.toTree();

        // split in two through the centroid
        final GreatCircle splitter = GreatCircles.fromPoints(tree.getCentroid(), Point2S.PLUS_K, precision);
        final Split<RegionBSPTree2S> split = tree.split(splitter);

        // compute some properties for the minus side
        final RegionBSPTree2S minus = split.getMinus();

        final double minusSize = minus.getSize(); // pi/4
        final List<GreatArcPath> minusPaths = minus.getBoundaryPaths(); // size = 1

        // ---------------------
        Assertions.assertEquals(Math.PI / 4, minusSize, TEST_EPS);
        Assertions.assertEquals(1, minusPaths.size());
    }
}
