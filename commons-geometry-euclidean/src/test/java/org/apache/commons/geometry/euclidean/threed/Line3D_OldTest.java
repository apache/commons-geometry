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
package org.apache.commons.geometry.euclidean.threed;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.junit.Assert;
import org.junit.Test;

public class Line3D_OldTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testContains() {
        Vector3D p1 = Vector3D.of(0, 0, 1);
        Line3D_Old l = new Line3D_Old(p1, Vector3D.of(0, 0, 2), TEST_PRECISION);
        Assert.assertTrue(l.contains(p1));
        Assert.assertTrue(l.contains(Vector3D.linearCombination(1.0, p1, 0.3, l.getDirection())));
        Vector3D u = l.getDirection().orthogonal();
        Vector3D v = l.getDirection().cross(u);
        for (double alpha = 0; alpha < 2 * Math.PI; alpha += 0.3) {
            Assert.assertTrue(! l.contains(p1.add(Vector3D.linearCombination(Math.cos(alpha), u,
                                                               Math.sin(alpha), v))));
        }
    }

    @Test
    public void testSimilar() {
        Vector3D p1  = Vector3D.of(1.2, 3.4, -5.8);
        Vector3D p2  = Vector3D.of(3.4, -5.8, 1.2);
        Line3D_Old     lA  = new Line3D_Old(p1, p2, TEST_PRECISION);
        Line3D_Old     lB  = new Line3D_Old(p2, p1, TEST_PRECISION);
        Assert.assertTrue(lA.isSimilarTo(lB));
        Assert.assertTrue(!lA.isSimilarTo(new Line3D_Old(p1, p1.add(lA.getDirection().orthogonal()), TEST_PRECISION)));
    }

    @Test
    public void testPointDistance() {
        Line3D_Old l = new Line3D_Old(Vector3D.of(0, 1, 1), Vector3D.of(0, 2, 2), TEST_PRECISION);
        Assert.assertEquals(Math.sqrt(3.0 / 2.0), l.distance(Vector3D.of(1, 0, 1)), TEST_EPS);
        Assert.assertEquals(0, l.distance(Vector3D.of(0, -4, -4)), TEST_EPS);
    }

    @Test
    public void testLineDistance() {
        Line3D_Old l = new Line3D_Old(Vector3D.of(0, 1, 1), Vector3D.of(0, 2, 2), TEST_PRECISION);
        Assert.assertEquals(1.0,
                            l.distance(new Line3D_Old(Vector3D.of(1, 0, 1), Vector3D.of(1, 0, 2), TEST_PRECISION)),
                            1.0e-10);
        Assert.assertEquals(0.5,
                            l.distance(new Line3D_Old(Vector3D.of(-0.5, 0, 0), Vector3D.of(-0.5, -1, -1), TEST_PRECISION)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.distance(l),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.distance(new Line3D_Old(Vector3D.of(0, -4, -4), Vector3D.of(0, -5, -5), TEST_PRECISION)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.distance(new Line3D_Old(Vector3D.of(0, -4, -4), Vector3D.of(0, -3, -4), TEST_PRECISION)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.distance(new Line3D_Old(Vector3D.of(0, -4, -4), Vector3D.of(1, -4, -4), TEST_PRECISION)),
                            1.0e-10);
        Assert.assertEquals(Math.sqrt(8),
                            l.distance(new Line3D_Old(Vector3D.of(0, -4, 0), Vector3D.of(1, -4, 0), TEST_PRECISION)),
                            1.0e-10);
    }

    @Test
    public void testClosest() {
        Line3D_Old l = new Line3D_Old(Vector3D.of(0, 1, 1), Vector3D.of(0, 2, 2), TEST_PRECISION);
        Assert.assertEquals(0.0,
                            l.closestPoint(new Line3D_Old(Vector3D.of(1, 0, 1), Vector3D.of(1, 0, 2), TEST_PRECISION)).distance(Vector3D.of(0, 0, 0)),
                            1.0e-10);
        Assert.assertEquals(0.5,
                            l.closestPoint(new Line3D_Old(Vector3D.of(-0.5, 0, 0), Vector3D.of(-0.5, -1, -1), TEST_PRECISION)).distance(Vector3D.of(-0.5, 0, 0)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.closestPoint(l).distance(Vector3D.of(0, 0, 0)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.closestPoint(new Line3D_Old(Vector3D.of(0, -4, -4), Vector3D.of(0, -5, -5), TEST_PRECISION)).distance(Vector3D.of(0, 0, 0)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.closestPoint(new Line3D_Old(Vector3D.of(0, -4, -4), Vector3D.of(0, -3, -4), TEST_PRECISION)).distance(Vector3D.of(0, -4, -4)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.closestPoint(new Line3D_Old(Vector3D.of(0, -4, -4), Vector3D.of(1, -4, -4), TEST_PRECISION)).distance(Vector3D.of(0, -4, -4)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.closestPoint(new Line3D_Old(Vector3D.of(0, -4, 0), Vector3D.of(1, -4, 0), TEST_PRECISION)).distance(Vector3D.of(0, -2, -2)),
                            1.0e-10);
    }

    @Test
    public void testIntersection() {
        Line3D_Old l = new Line3D_Old(Vector3D.of(0, 1, 1), Vector3D.of(0, 2, 2), TEST_PRECISION);
        Assert.assertNull(l.intersection(new Line3D_Old(Vector3D.of(1, 0, 1), Vector3D.of(1, 0, 2), TEST_PRECISION)));
        Assert.assertNull(l.intersection(new Line3D_Old(Vector3D.of(-0.5, 0, 0), Vector3D.of(-0.5, -1, -1), TEST_PRECISION)));
        Assert.assertEquals(0.0,
                            l.intersection(l).distance(Vector3D.of(0, 0, 0)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.intersection(new Line3D_Old(Vector3D.of(0, -4, -4), Vector3D.of(0, -5, -5), TEST_PRECISION)).distance(Vector3D.of(0, 0, 0)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.intersection(new Line3D_Old(Vector3D.of(0, -4, -4), Vector3D.of(0, -3, -4), TEST_PRECISION)).distance(Vector3D.of(0, -4, -4)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.intersection(new Line3D_Old(Vector3D.of(0, -4, -4), Vector3D.of(1, -4, -4), TEST_PRECISION)).distance(Vector3D.of(0, -4, -4)),
                            1.0e-10);
        Assert.assertNull(l.intersection(new Line3D_Old(Vector3D.of(0, -4, 0), Vector3D.of(1, -4, 0), TEST_PRECISION)));
    }

    @Test
    public void testRevert() {

        // setup
        Line3D_Old line = new Line3D_Old(Vector3D.of(1653345.6696423641, 6170370.041579291, 90000),
                             Vector3D.of(1650757.5050732433, 6160710.879908984, 0.9),
                             TEST_PRECISION);
        Vector3D expected = line.getDirection().negate();

        // action
        Line3D_Old reverted = line.revert();

        // verify
        Assert.assertArrayEquals(expected.toArray(), reverted.getDirection().toArray(), 0);

    }

}
