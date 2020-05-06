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
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.oned.AngularInterval;
import org.apache.commons.geometry.spherical.oned.Point1S;
import org.apache.commons.geometry.spherical.oned.RegionBSPTree1S;
import org.apache.commons.geometry.spherical.twod.GreatArcPath;
import org.apache.commons.geometry.spherical.twod.GreatCircle;
import org.apache.commons.geometry.spherical.twod.GreatCircles;
import org.apache.commons.geometry.spherical.twod.Point2S;
import org.apache.commons.geometry.spherical.twod.RegionBSPTree2S;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

/** This class contains code listed as examples in the user guide and other documentation.
 * If any portion of this code changes, the corresponding examples in the documentation <em>must</em> be updated.
 */
public class DocumentationExamplesTest {

    private static final double TEST_EPS = 1e-12;

    @Test
    public void testAngularIntervalExample() {
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-6);

        // create angular intervals of different sizes, one of size pi/2 and one of size 3pi/2
        AngularInterval a = AngularInterval.of(0, PlaneAngleRadians.PI_OVER_TWO, precision);
        AngularInterval b = AngularInterval.of(Point1S.PI, Point1S.of(PlaneAngleRadians.PI_OVER_TWO), precision);

        // test some points
        a.contains(Point1S.of(0.25 * Math.PI)); // true
        b.contains(Point1S.of(0.25 * Math.PI)); // true

        RegionLocation aLocZero = a.classify(Point1S.ZERO); // RegionLocation.BOUNDARY
        RegionLocation bLocZero = b.classify(Point1S.ZERO); // RegionLocation.INSIDE

        // -------------------
        Assert.assertTrue(a.contains(Point1S.of(0.25 * Math.PI)));
        Assert.assertTrue(b.contains(Point1S.of(0.25 * Math.PI)));

        Assert.assertEquals(RegionLocation.BOUNDARY, aLocZero);
        Assert.assertEquals(RegionLocation.INSIDE, bLocZero);
    }

    @Test
    public void testRegionBSPTree1SExample() {
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-6);

        // create a region from the union of multiple angular intervals
        RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(0, 0.25 * Math.PI, precision));
        tree.add(AngularInterval.of(0.5 * Math.PI, Math.PI, precision));
        tree.add(AngularInterval.of(0.75 * Math.PI, 1.5 * Math.PI, precision));

        // compute the region size in radians
        double size = tree.getSize(); // 1.25pi

        // convert back to intervals
        List<AngularInterval> intervals = tree.toIntervals(); //size = 2

        // ---------------
        Assert.assertEquals(size, 1.25 * Math.PI, TEST_EPS);
        Assert.assertEquals(2, intervals.size());
    }

    @Test
    public void testGreatCircleIntersectionExample() {
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-6);

        // create two great circles
        GreatCircle a = GreatCircles.fromPoints(Point2S.PLUS_I, Point2S.PLUS_K, precision);
        GreatCircle b = GreatCircles.fromPole(Vector3D.Unit.PLUS_Z, precision);

        // find the two intersection points of the great circles
        Point2S ptA = a.intersection(b); //(pi, pi/2)
        Point2S ptB = ptA.antipodal(); // (0, pi/2)

        // ----------------------
        SphericalTestUtils.assertPointsEq(Point2S.MINUS_I, ptA, TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.PLUS_I, ptB, TEST_EPS);
    }

    @Test
    public void testRegionBSPTree2SExample() {
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-6);

        // create a path outlining a quadrant triangle
        GreatArcPath path = GreatArcPath.builder(precision)
                .append(Point2S.PLUS_I)
                .append(Point2S.PLUS_J)
                .append(Point2S.PLUS_K)
                .build(true); // close the path with the starting path

        // convert to a region
        RegionBSPTree2S tree = path.toTree();

        // split in two through the barycenter
        GreatCircle splitter = GreatCircles.fromPoints(tree.getBarycenter(), Point2S.PLUS_K, precision);
        Split<RegionBSPTree2S> split = tree.split(splitter);

        // compute some properties for the minus side
        RegionBSPTree2S minus = split.getMinus();

        double minusSize = minus.getSize(); // pi/4
        List<GreatArcPath> minusPaths = minus.getBoundaryPaths(); // size = 1

        // ---------------------
        Assert.assertEquals(Math.PI / 4, minusSize, TEST_EPS);
        Assert.assertEquals(1, minusPaths.size());
    }
}
