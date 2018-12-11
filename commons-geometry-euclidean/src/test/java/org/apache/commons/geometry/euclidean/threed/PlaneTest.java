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

import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.junit.Assert;
import org.junit.Test;

public class PlaneTest {

    @Test
    public void testContains() {
        Plane p = new Plane(Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), 1.0e-10);
        Assert.assertTrue(p.contains(Vector3D.of(0, 0, 1)));
        Assert.assertTrue(p.contains(Vector3D.of(17, -32, 1)));
        Assert.assertTrue(! p.contains(Vector3D.of(17, -32, 1.001)));
    }

    @Test
    public void testOffset() {
        Vector3D p1 = Vector3D.of(1, 1, 1);
        Plane p = new Plane(p1, Vector3D.of(0.2, 0, 0), 1.0e-10);
        Assert.assertEquals(-5.0, p.getOffset(Vector3D.of(-4, 0, 0)), 1.0e-10);
        Assert.assertEquals(+5.0, p.getOffset(Vector3D.of(6, 10, -12)), 1.0e-10);
        Assert.assertEquals(0.3,
                            p.getOffset(Vector3D.linearCombination(1.0, p1, 0.3, p.getNormal())),
                            1.0e-10);
        Assert.assertEquals(-0.3,
                            p.getOffset(Vector3D.linearCombination(1.0, p1, -0.3, p.getNormal())),
                            1.0e-10);
    }

    @Test
    public void testPoint() {
        Plane p = new Plane(Vector3D.of(2, -3, 1), Vector3D.of(1, 4, 9), 1.0e-10);
        Assert.assertTrue(p.contains(p.getOrigin()));
    }

    @Test
    public void testThreePoints() {
        Vector3D p1 = Vector3D.of(1.2, 3.4, -5.8);
        Vector3D p2 = Vector3D.of(3.4, -5.8, 1.2);
        Vector3D p3 = Vector3D.of(-2.0, 4.3, 0.7);
        Plane    p  = new Plane(p1, p2, p3, 1.0e-10);
        Assert.assertTrue(p.contains(p1));
        Assert.assertTrue(p.contains(p2));
        Assert.assertTrue(p.contains(p3));
    }

    @Test
    public void testRotate() {
        Vector3D p1 = Vector3D.of(1.2, 3.4, -5.8);
        Vector3D p2 = Vector3D.of(3.4, -5.8, 1.2);
        Vector3D p3 = Vector3D.of(-2.0, 4.3, 0.7);
        Plane    p  = new Plane(p1, p2, p3, 1.0e-10);
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
        Plane    p  = new Plane(p1, p2, p3, 1.0e-10);

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
        Plane p = new Plane(Vector3D.of(1, 2, 3), Vector3D.of(-4, 1, -5), 1.0e-10);
        Line  l = new Line(Vector3D.of(0.2, -3.5, 0.7), Vector3D.of(1.2, -2.5, -0.3), 1.0e-10);
        Vector3D point = p.intersection(l);
        Assert.assertTrue(p.contains(point));
        Assert.assertTrue(l.contains(point));
        Assert.assertNull(p.intersection(new Line(Vector3D.of(10, 10, 10),
                                                  Vector3D.of(10, 10, 10).add(p.getNormal().orthogonal()),
                                                  1.0e-10)));
    }

    @Test
    public void testIntersection2() {
        Vector3D p1  = Vector3D.of(1.2, 3.4, -5.8);
        Vector3D p2  = Vector3D.of(3.4, -5.8, 1.2);
        Plane    pA  = new Plane(p1, p2, Vector3D.of(-2.0, 4.3, 0.7), 1.0e-10);
        Plane    pB  = new Plane(p1, Vector3D.of(11.4, -3.8, 5.1), p2, 1.0e-10);
        Line     l   = pA.intersection(pB);
        Assert.assertTrue(l.contains(p1));
        Assert.assertTrue(l.contains(p2));
        Assert.assertNull(pA.intersection(pA));
    }

    @Test
    public void testIntersection3() {
        Vector3D reference = Vector3D.of(1.2, 3.4, -5.8);
        Plane p1 = new Plane(reference, Vector3D.of(1, 3, 3), 1.0e-10);
        Plane p2 = new Plane(reference, Vector3D.of(-2, 4, 0), 1.0e-10);
        Plane p3 = new Plane(reference, Vector3D.of(7, 0, -4), 1.0e-10);
        Vector3D p = Plane.intersection(p1, p2, p3);
        Assert.assertEquals(reference.getX(), p.getX(), 1.0e-10);
        Assert.assertEquals(reference.getY(), p.getY(), 1.0e-10);
        Assert.assertEquals(reference.getZ(), p.getZ(), 1.0e-10);
    }

    @Test
    public void testSimilar() {
        Vector3D p1  = Vector3D.of(1.2, 3.4, -5.8);
        Vector3D p2  = Vector3D.of(3.4, -5.8, 1.2);
        Vector3D p3  = Vector3D.of(-2.0, 4.3, 0.7);
        Plane    pA  = new Plane(p1, p2, p3, 1.0e-10);
        Plane    pB  = new Plane(p1, Vector3D.of(11.4, -3.8, 5.1), p2, 1.0e-10);
        Assert.assertTrue(! pA.isSimilarTo(pB));
        Assert.assertTrue(pA.isSimilarTo(pA));
        Assert.assertTrue(pA.isSimilarTo(new Plane(p1, p3, p2, 1.0e-10)));
        Vector3D shift = Vector3D.linearCombination(0.3, pA.getNormal());
        Assert.assertTrue(! pA.isSimilarTo(new Plane(p1.add(shift),
                                                     p3.add(shift),
                                                     p2.add(shift),
                                                     1.0e-10)));
    }

}
