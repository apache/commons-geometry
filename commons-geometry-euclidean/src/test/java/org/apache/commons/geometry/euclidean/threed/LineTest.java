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

import org.junit.Assert;
import org.junit.Test;

public class LineTest {

    @Test
    public void testContains() {
        Point3D p1 = Point3D.of(0, 0, 1);
        Line l = new Line(p1, Point3D.of(0, 0, 2), 1.0e-10);
        Assert.assertTrue(l.contains(p1));
        Assert.assertTrue(l.contains(Point3D.vectorCombination(1.0, p1, 0.3, l.getDirection())));
        Vector3D u = l.getDirection().orthogonal();
        Vector3D v = l.getDirection().crossProduct(u);
        for (double alpha = 0; alpha < 2 * Math.PI; alpha += 0.3) {
            Assert.assertTrue(! l.contains(p1.add(Vector3D.linearCombination(Math.cos(alpha), u,
                                                               Math.sin(alpha), v))));
        }
    }

    @Test
    public void testSimilar() {
        Point3D p1  = Point3D.of(1.2, 3.4, -5.8);
        Point3D p2  = Point3D.of(3.4, -5.8, 1.2);
        Line     lA  = new Line(p1, p2, 1.0e-10);
        Line     lB  = new Line(p2, p1, 1.0e-10);
        Assert.assertTrue(lA.isSimilarTo(lB));
        Assert.assertTrue(! lA.isSimilarTo(new Line(p1, p1.add(lA.getDirection().orthogonal()), 1.0e-10)));
    }

    @Test
    public void testPointDistance() {
        Line l = new Line(Point3D.of(0, 1, 1), Point3D.of(0, 2, 2), 1.0e-10);
        Assert.assertEquals(Math.sqrt(3.0 / 2.0), l.distance(Point3D.of(1, 0, 1)), 1.0e-10);
        Assert.assertEquals(0, l.distance(Point3D.of(0, -4, -4)), 1.0e-10);
    }

    @Test
    public void testLineDistance() {
        Line l = new Line(Point3D.of(0, 1, 1), Point3D.of(0, 2, 2), 1.0e-10);
        Assert.assertEquals(1.0,
                            l.distance(new Line(Point3D.of(1, 0, 1), Point3D.of(1, 0, 2), 1.0e-10)),
                            1.0e-10);
        Assert.assertEquals(0.5,
                            l.distance(new Line(Point3D.of(-0.5, 0, 0), Point3D.of(-0.5, -1, -1), 1.0e-10)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.distance(l),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.distance(new Line(Point3D.of(0, -4, -4), Point3D.of(0, -5, -5), 1.0e-10)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.distance(new Line(Point3D.of(0, -4, -4), Point3D.of(0, -3, -4), 1.0e-10)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.distance(new Line(Point3D.of(0, -4, -4), Point3D.of(1, -4, -4), 1.0e-10)),
                            1.0e-10);
        Assert.assertEquals(Math.sqrt(8),
                            l.distance(new Line(Point3D.of(0, -4, 0), Point3D.of(1, -4, 0), 1.0e-10)),
                            1.0e-10);
    }

    @Test
    public void testClosest() {
        Line l = new Line(Point3D.of(0, 1, 1), Point3D.of(0, 2, 2), 1.0e-10);
        Assert.assertEquals(0.0,
                            l.closestPoint(new Line(Point3D.of(1, 0, 1), Point3D.of(1, 0, 2), 1.0e-10)).distance(Point3D.of(0, 0, 0)),
                            1.0e-10);
        Assert.assertEquals(0.5,
                            l.closestPoint(new Line(Point3D.of(-0.5, 0, 0), Point3D.of(-0.5, -1, -1), 1.0e-10)).distance(Point3D.of(-0.5, 0, 0)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.closestPoint(l).distance(Point3D.of(0, 0, 0)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.closestPoint(new Line(Point3D.of(0, -4, -4), Point3D.of(0, -5, -5), 1.0e-10)).distance(Point3D.of(0, 0, 0)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.closestPoint(new Line(Point3D.of(0, -4, -4), Point3D.of(0, -3, -4), 1.0e-10)).distance(Point3D.of(0, -4, -4)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.closestPoint(new Line(Point3D.of(0, -4, -4), Point3D.of(1, -4, -4), 1.0e-10)).distance(Point3D.of(0, -4, -4)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.closestPoint(new Line(Point3D.of(0, -4, 0), Point3D.of(1, -4, 0), 1.0e-10)).distance(Point3D.of(0, -2, -2)),
                            1.0e-10);
    }

    @Test
    public void testIntersection() {
        Line l = new Line(Point3D.of(0, 1, 1), Point3D.of(0, 2, 2), 1.0e-10);
        Assert.assertNull(l.intersection(new Line(Point3D.of(1, 0, 1), Point3D.of(1, 0, 2), 1.0e-10)));
        Assert.assertNull(l.intersection(new Line(Point3D.of(-0.5, 0, 0), Point3D.of(-0.5, -1, -1), 1.0e-10)));
        Assert.assertEquals(0.0,
                            l.intersection(l).distance(Point3D.of(0, 0, 0)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.intersection(new Line(Point3D.of(0, -4, -4), Point3D.of(0, -5, -5), 1.0e-10)).distance(Point3D.of(0, 0, 0)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.intersection(new Line(Point3D.of(0, -4, -4), Point3D.of(0, -3, -4), 1.0e-10)).distance(Point3D.of(0, -4, -4)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.intersection(new Line(Point3D.of(0, -4, -4), Point3D.of(1, -4, -4), 1.0e-10)).distance(Point3D.of(0, -4, -4)),
                            1.0e-10);
        Assert.assertNull(l.intersection(new Line(Point3D.of(0, -4, 0), Point3D.of(1, -4, 0), 1.0e-10)));
    }

    @Test
    public void testRevert() {

        // setup
        Line line = new Line(Point3D.of(1653345.6696423641, 6170370.041579291, 90000),
                             Point3D.of(1650757.5050732433, 6160710.879908984, 0.9),
                             1.0e-10);
        Vector3D expected = line.getDirection().negate();

        // action
        Line reverted = line.revert();

        // verify
        Assert.assertArrayEquals(expected.toArray(), reverted.getDirection().toArray(), 0);

    }

}
