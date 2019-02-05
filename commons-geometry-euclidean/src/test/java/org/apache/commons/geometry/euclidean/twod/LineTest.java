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
package org.apache.commons.geometry.euclidean.twod;

import org.apache.commons.geometry.core.partitioning.Transform;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.junit.Assert;
import org.junit.Test;

public class LineTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testContains() {
        Line l = new Line(Vector2D.of(0, 1), Vector2D.of(1, 2), TEST_PRECISION);
        Assert.assertTrue(l.contains(Vector2D.of(0, 1)));
        Assert.assertTrue(l.contains(Vector2D.of(1, 2)));
        Assert.assertTrue(l.contains(Vector2D.of(7, 8)));
        Assert.assertTrue(! l.contains(Vector2D.of(8, 7)));
    }

    @Test
    public void testAbscissa() {
        Line l = new Line(Vector2D.of(2, 1), Vector2D.of(-2, -2), TEST_PRECISION);
        Assert.assertEquals(0.0,
                            (l.toSubSpace(Vector2D.of(-3,  4))).getX(),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            (l.toSubSpace(Vector2D.of( 3, -4))).getX(),
                            1.0e-10);
        Assert.assertEquals(-5.0,
                            (l.toSubSpace(Vector2D.of( 7, -1))).getX(),
                            1.0e-10);
        Assert.assertEquals(5.0,
                             (l.toSubSpace(Vector2D.of(-1, -7))).getX(),
                             1.0e-10);
    }

    @Test
    public void testOffset() {
        Line l = new Line(Vector2D.of(2, 1), Vector2D.of(-2, -2), TEST_PRECISION);
        Assert.assertEquals(-5.0, l.getOffset(Vector2D.of(5, -3)), 1.0e-10);
        Assert.assertEquals(+5.0, l.getOffset(Vector2D.of(-5, 2)), 1.0e-10);
    }

    @Test
    public void testDistance() {
        Line l = new Line(Vector2D.of(2, 1), Vector2D.of(-2, -2), TEST_PRECISION);
        Assert.assertEquals(+5.0, l.distance(Vector2D.of(5, -3)), 1.0e-10);
        Assert.assertEquals(+5.0, l.distance(Vector2D.of(-5, 2)), 1.0e-10);
    }

    @Test
    public void testPointAt() {
        Line l = new Line(Vector2D.of(2, 1), Vector2D.of(-2, -2), TEST_PRECISION);
        for (double a = -2.0; a < 2.0; a += 0.2) {
            Vector1D pA = Vector1D.of(a);
            Vector2D point = l.toSpace(pA);
            Assert.assertEquals(a, (l.toSubSpace(point)).getX(), 1.0e-10);
            Assert.assertEquals(0.0, l.getOffset(point),   1.0e-10);
            for (double o = -2.0; o < 2.0; o += 0.2) {
                point = l.getPointAt(pA, o);
                Assert.assertEquals(a, (l.toSubSpace(point)).getX(), 1.0e-10);
                Assert.assertEquals(o, l.getOffset(point),   1.0e-10);
            }
        }
    }

    @Test
    public void testOriginOffset() {
        Line l1 = new Line(Vector2D.of(0, 1), Vector2D.of(1, 2), TEST_PRECISION);
        Assert.assertEquals(Math.sqrt(0.5), l1.getOriginOffset(), 1.0e-10);
        Line l2 = new Line(Vector2D.of(1, 2), Vector2D.of(0, 1), TEST_PRECISION);
        Assert.assertEquals(-Math.sqrt(0.5), l2.getOriginOffset(), 1.0e-10);
    }

    @Test
    public void testParallel() {
        Line l1 = new Line(Vector2D.of(0, 1), Vector2D.of(1, 2), TEST_PRECISION);
        Line l2 = new Line(Vector2D.of(2, 2), Vector2D.of(3, 3), TEST_PRECISION);
        Assert.assertTrue(l1.isParallelTo(l2));
        Line l3 = new Line(Vector2D.of(1, 0), Vector2D.of(0.5, -0.5), TEST_PRECISION);
        Assert.assertTrue(l1.isParallelTo(l3));
        Line l4 = new Line(Vector2D.of(1, 0), Vector2D.of(0.5, -0.51), TEST_PRECISION);
        Assert.assertTrue(! l1.isParallelTo(l4));
    }

    @Test
    public void testTransform() {

        Line l1 = new Line(Vector2D.of(1.0 ,1.0), Vector2D.of(4.0 ,1.0), TEST_PRECISION);
        Transform<Vector2D, Vector1D> t1 =
            Line.getTransform(0.0, 0.5, -1.0, 0.0, 1.0, 1.5);
        Assert.assertEquals(0.5 * Math.PI,
                            ((Line) t1.apply(l1)).getAngle(),
                            1.0e-10);

        Line l2 = new Line(Vector2D.of(0.0, 0.0), Vector2D.of(1.0, 1.0), TEST_PRECISION);
        Transform<Vector2D, Vector1D> t2 =
            Line.getTransform(0.0, 0.5, -1.0, 0.0, 1.0, 1.5);
        Assert.assertEquals(Math.atan2(1.0, -2.0),
                            ((Line) t2.apply(l2)).getAngle(),
                            1.0e-10);

    }

    @Test
    public void testIntersection() {
        Line    l1 = new Line(Vector2D.of( 0, 1), Vector2D.of(1, 2), TEST_PRECISION);
        Line    l2 = new Line(Vector2D.of(-1, 2), Vector2D.of(2, 1), TEST_PRECISION);
        Vector2D p  = l1.intersection(l2);
        Assert.assertEquals(0.5, p.getX(), 1.0e-10);
        Assert.assertEquals(1.5, p.getY(), 1.0e-10);
    }

}
