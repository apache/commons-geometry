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
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class PlaneTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testContains() {
        Plane p = Plane.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), TEST_PRECISION);
        Assert.assertTrue(p.contains(Vector3D.of(0, 0, 1)));
        Assert.assertTrue(p.contains(Vector3D.of(17, -32, 1)));
        Assert.assertTrue(! p.contains(Vector3D.of(17, -32, 1.001)));
    }

    @Test
    public void testContainsLine() {
        Plane p = Plane.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), TEST_PRECISION);
        Line line = new Line(Vector3D.of(1, 0, 1), Vector3D.of(2, 0, 1), TEST_PRECISION);
        Assert.assertTrue(p.contains(line));
    }
    
    
    @Test
    public void testProjectLine() {
        Plane p = Plane.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), TEST_PRECISION);
        Line line = new Line(Vector3D.of(1, 0, 1), Vector3D.of(2, 0, 2), TEST_PRECISION);
        Line expectedProjection = new Line(Vector3D.of(1, 0, 1),Vector3D.of(2, 0, 1), TEST_PRECISION);
        Assert.assertEquals(expectedProjection, p.project(line));
    }
    
    @Test
    public void testOffset() {
        Vector3D p1 = Vector3D.of(1, 1, 1);
        Plane p = Plane.fromPointAndNormal(p1, Vector3D.of(0.2, 0, 0), TEST_PRECISION);
        Assert.assertEquals(-5.0, p.getOffset(Vector3D.of(-4, 0, 0)), TEST_EPS);
        Assert.assertEquals(+5.0, p.getOffset(Vector3D.of(6, 10, -12)), TEST_EPS);
        Assert.assertEquals(0.3,
                            p.getOffset(Vector3D.linearCombination(1.0, p1, 0.3, p.getNormal())),
                            TEST_EPS);
        Assert.assertEquals(-0.3,
                            p.getOffset(Vector3D.linearCombination(1.0, p1, -0.3, p.getNormal())),
                            TEST_EPS);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testVectorsAreColinear()
    {
      Plane.fromPointAndPlaneVectors(Vector3D.of(1, 1, 1), Vector3D.of(2, 0, 0), Vector3D.of(2,0,0), TEST_PRECISION);
    }

    
    @Test
    public void testVectorsAreNormalizedForSuppliedUAndV() {
        Plane p = Plane.fromPointAndPlaneVectors(Vector3D.of(1, 1, 1), Vector3D.of(2, 0, 0), Vector3D.of(0,2,0), TEST_PRECISION);
        Assert.assertEquals(1.0, p.getNormal().norm(), TEST_EPS);
        Assert.assertEquals(1.0, p.getV().norm(), TEST_EPS);
        Assert.assertEquals(1.0, p.getU().norm(), TEST_EPS);
    }

    
    
    @Test
    public void testVectorsAreNormalized() {
        Plane p = Plane.fromPointAndNormal(Vector3D.of(2, -3, 1), Vector3D.of(1, 4, 9), TEST_PRECISION);
        Assert.assertEquals(1.0, p.getNormal().norm(), TEST_EPS);
        Assert.assertEquals(1.0, p.getV().norm(), TEST_EPS);
        Assert.assertEquals(1.0, p.getU().norm(), TEST_EPS);
    }

    
    @Test
    public void testPoint() {
        Plane p = Plane.fromPointAndNormal(Vector3D.of(2, -3, 1), Vector3D.of(1, 4, 9), TEST_PRECISION);
        Assert.assertTrue(p.contains(p.getOrigin()));
    }

    @Test
    public void testThreePoints() {
        Vector3D p1 = Vector3D.of(1.2, 3.4, -5.8);
        Vector3D p2 = Vector3D.of(3.4, -5.8, 1.2);
        Vector3D p3 = Vector3D.of(-2.0, 4.3, 0.7);
        Plane    p  = Plane.fromPoints(p1, p2, p3, TEST_PRECISION);
        Assert.assertTrue(p.contains(p1));
        Assert.assertTrue(p.contains(p2));
        Assert.assertTrue(p.contains(p3));
    }

    @Test
    public void testRotate() {
        Vector3D p1 = Vector3D.of(1.2, 3.4, -5.8);
        Vector3D p2 = Vector3D.of(3.4, -5.8, 1.2);
        Vector3D p3 = Vector3D.of(-2.0, 4.3, 0.7);
        Plane    p  = Plane.fromPoints(p1, p2, p3, TEST_PRECISION);
        Vector3D oldNormal = p.getNormal();

        p = p.rotate(p2, QuaternionRotation.fromAxisAngle(p2.subtract(p1), 1.7));
        Assert.assertTrue(p.contains(p1));
        Assert.assertTrue(p.contains(p2));
        Assert.assertTrue(! p.contains(p3));

        p = p.rotate(p2, QuaternionRotation.fromAxisAngle(oldNormal, 0.1));
        Assert.assertTrue(! p.contains(p1));
        Assert.assertTrue(p.contains(p2));
        Assert.assertTrue(! p.contains(p3));

        p = p.rotate(p1, QuaternionRotation.fromAxisAngle(oldNormal, 0.1));
        Assert.assertTrue(! p.contains(p1));
        Assert.assertTrue(! p.contains(p2));
        Assert.assertTrue(! p.contains(p3));

    }

    @Test
    public void testTranslate() {
        Vector3D p1 = Vector3D.of(1.2, 3.4, -5.8);
        Vector3D p2 = Vector3D.of(3.4, -5.8, 1.2);
        Vector3D p3 = Vector3D.of(-2.0, 4.3, 0.7);
        Plane    p  = Plane.fromPoints(p1, p2, p3, TEST_PRECISION);

        p = p.translate(Vector3D.linearCombination(2.0, p.getU(), -1.5, p.getV()));
        Assert.assertTrue(p.contains(p1));
        Assert.assertTrue(p.contains(p2));
        Assert.assertTrue(p.contains(p3));

        p = p.translate(Vector3D.linearCombination(-1.2, p.getNormal()));
        Assert.assertTrue(! p.contains(p1));
        Assert.assertTrue(! p.contains(p2));
        Assert.assertTrue(! p.contains(p3));

        p = p.translate(Vector3D.linearCombination(+1.2, p.getNormal()));
        Assert.assertTrue(p.contains(p1));
        Assert.assertTrue(p.contains(p2));
        Assert.assertTrue(p.contains(p3));

    }

    @Test
    public void testIntersection() {
        Plane p = Plane.fromPointAndNormal(Vector3D.of(1, 2, 3), Vector3D.of(-4, 1, -5), TEST_PRECISION);
        Line  l = new Line(Vector3D.of(0.2, -3.5, 0.7), Vector3D.of(1.2, -2.5, -0.3), TEST_PRECISION);
        Vector3D point = p.intersection(l);
        Assert.assertTrue(p.contains(point));
        Assert.assertTrue(l.contains(point));
        Assert.assertNull(p.intersection(new Line(Vector3D.of(10, 10, 10),
                                                  Vector3D.of(10, 10, 10).add(p.getNormal().orthogonal()),
                                                  TEST_PRECISION)));
    }

    @Test
    public void testIntersection2() {
        Vector3D p1  = Vector3D.of(1.2, 3.4, -5.8);
        Vector3D p2  = Vector3D.of(3.4, -5.8, 1.2);
        Plane    pA  = Plane.fromPoints(p1, p2, Vector3D.of(-2.0, 4.3, 0.7), TEST_PRECISION);
        Plane    pB  = Plane.fromPoints(p1, Vector3D.of(11.4, -3.8, 5.1), p2, TEST_PRECISION);
        Line     l   = pA.intersection(pB);
        Assert.assertTrue(l.contains(p1));
        Assert.assertTrue(l.contains(p2));
        Assert.assertNull(pA.intersection(pA));
    }

    @Test
    public void testIntersection3() {
        Vector3D reference = Vector3D.of(1.2, 3.4, -5.8);
        Plane p1 = Plane.fromPointAndNormal(reference, Vector3D.of(1, 3, 3), TEST_PRECISION);
        Plane p2 = Plane.fromPointAndNormal(reference, Vector3D.of(-2, 4, 0), TEST_PRECISION);
        Plane p3 = Plane.fromPointAndNormal(reference, Vector3D.of(7, 0, -4), TEST_PRECISION);
        Vector3D p = Plane.intersection(p1, p2, p3);
        Assert.assertEquals(reference.getX(), p.getX(), TEST_EPS);
        Assert.assertEquals(reference.getY(), p.getY(), TEST_EPS);
        Assert.assertEquals(reference.getZ(), p.getZ(), TEST_EPS);
    }

    @Test
    public void testSimilar() {
        Vector3D p1  = Vector3D.of(1.2, 3.4, -5.8);
        Vector3D p2  = Vector3D.of(3.4, -5.8, 1.2);
        Vector3D p3  = Vector3D.of(-2.0, 4.3, 0.7);
        Plane    pA  = Plane.fromPoints(p1, p2, p3, TEST_PRECISION);
        Plane    pB  = Plane.fromPoints(p1, Vector3D.of(11.4, -3.8, 5.1), p2, TEST_PRECISION);
        Assert.assertTrue(! pA.contains(pB));
        Assert.assertTrue(pA.contains(pA));
        Assert.assertTrue(pA.contains(Plane.fromPoints(p1, p3, p2, TEST_PRECISION)));
        Vector3D shift = Vector3D.linearCombination(0.3, pA.getNormal());
        Assert.assertTrue(! pA.contains(Plane.fromPoints(p1.add(shift),
                                                     p3.add(shift),
                                                     p2.add(shift),
                                                     TEST_PRECISION)));
    }

}
