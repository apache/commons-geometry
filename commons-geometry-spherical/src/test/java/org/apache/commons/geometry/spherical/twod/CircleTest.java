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
package org.apache.commons.geometry.spherical.twod;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.partitioning.Transform;
import org.apache.commons.geometry.euclidean.threed.Rotation;
import org.apache.commons.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.oned.Arc;
import org.apache.commons.geometry.spherical.oned.LimitAngle;
import org.apache.commons.geometry.spherical.oned.S1Point;
import org.apache.commons.geometry.spherical.oned.SubLimitAngle;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.UnitSphereSampler;
import org.apache.commons.rng.simple.RandomSource;
import org.junit.Assert;
import org.junit.Test;


public class CircleTest {

    @Test
    public void testEquator() {
        Circle circle = new Circle(Vector3D.of(0, 0, 1000), 1.0e-10).copySelf();
        Assert.assertEquals(Vector3D.PLUS_Z, circle.getPole());
        Assert.assertEquals(1.0e-10, circle.getTolerance(), 1.0e-20);
        circle.revertSelf();
        Assert.assertEquals(Vector3D.MINUS_Z, circle.getPole());
        Assert.assertEquals(Vector3D.PLUS_Z, circle.getReverse().getPole());
        Assert.assertEquals(Vector3D.MINUS_Z, circle.getPole());
    }

    @Test
    public void testXY() {
        Circle circle = new Circle(S2Point.of(1.2, 2.5), S2Point.of(-4.3, 0), 1.0e-10);
        Assert.assertEquals(0.0, circle.getPointAt(0).distance(circle.getXAxis()), 1.0e-10);
        Assert.assertEquals(0.0, circle.getPointAt(0.5 * Math.PI).distance(circle.getYAxis()), 1.0e-10);
        Assert.assertEquals(0.5 * Math.PI, circle.getXAxis().angle(circle.getYAxis()), 1.0e-10);
        Assert.assertEquals(0.5 * Math.PI, circle.getXAxis().angle(circle.getPole()), 1.0e-10);
        Assert.assertEquals(0.5 * Math.PI, circle.getPole().angle(circle.getYAxis()), 1.0e-10);
        Assert.assertEquals(0.0,
                            circle.getPole().distance(circle.getXAxis().crossProduct(circle.getYAxis())),
                            1.0e-10);
    }

    @Test
    public void testReverse() {
        Circle circle = new Circle(S2Point.of(1.2, 2.5), S2Point.of(-4.3, 0), 1.0e-10);
        Circle reversed = circle.getReverse();
        Assert.assertEquals(0.0, reversed.getPointAt(0).distance(reversed.getXAxis()), 1.0e-10);
        Assert.assertEquals(0.0, reversed.getPointAt(0.5 * Math.PI).distance(reversed.getYAxis()), 1.0e-10);
        Assert.assertEquals(0.5 * Math.PI, reversed.getXAxis().angle(reversed.getYAxis()), 1.0e-10);
        Assert.assertEquals(0.5 * Math.PI, reversed.getXAxis().angle(reversed.getPole()), 1.0e-10);
        Assert.assertEquals(0.5 * Math.PI, reversed.getPole().angle(reversed.getYAxis()), 1.0e-10);
        Assert.assertEquals(0.0,
                            reversed.getPole().distance(reversed.getXAxis().crossProduct(reversed.getYAxis())),
                            1.0e-10);

        Assert.assertEquals(0, circle.getXAxis().angle(reversed.getXAxis()), 1.0e-10);
        Assert.assertEquals(Math.PI, circle.getYAxis().angle(reversed.getYAxis()), 1.0e-10);
        Assert.assertEquals(Math.PI, circle.getPole().angle(reversed.getPole()), 1.0e-10);

        Assert.assertTrue(circle.sameOrientationAs(circle));
        Assert.assertFalse(circle.sameOrientationAs(reversed));
    }

    @Test
    public void testPhase() {
        Circle circle = new Circle(S2Point.of(1.2, 2.5), S2Point.of(-4.3, 0), 1.0e-10);
        Vector3D p = Vector3D.of(1, 2, -4);
        Vector3D samePhase = circle.getPointAt(circle.getPhase(p));
        Assert.assertEquals(0.0,
                            circle.getPole().crossProduct(p).angle(
                                           circle.getPole().crossProduct(samePhase)),
                            1.0e-10);
        Assert.assertEquals(0.5 * Math.PI, circle.getPole().angle(samePhase), 1.0e-10);
        Assert.assertEquals(circle.getPhase(p), circle.getPhase(samePhase), 1.0e-10);
        Assert.assertEquals(0.0, circle.getPhase(circle.getXAxis()), 1.0e-10);
        Assert.assertEquals(0.5 * Math.PI, circle.getPhase(circle.getYAxis()), 1.0e-10);
    }

    @Test
    public void testSubSpace() {
        Circle circle = new Circle(S2Point.of(1.2, 2.5), S2Point.of(-4.3, 0), 1.0e-10);
        Assert.assertEquals(0.0, circle.toSubSpace(S2Point.ofVector(circle.getXAxis())).getAzimuth(), 1.0e-10);
        Assert.assertEquals(0.5 * Math.PI, circle.toSubSpace(S2Point.ofVector(circle.getYAxis())).getAzimuth(), 1.0e-10);
        Vector3D p = Vector3D.of(1, 2, -4);
        Assert.assertEquals(circle.getPhase(p), circle.toSubSpace(S2Point.ofVector(p)).getAzimuth(), 1.0e-10);
    }

    @Test
    public void testSpace() {
        Circle circle = new Circle(S2Point.of(1.2, 2.5), S2Point.of(-4.3, 0), 1.0e-10);
        for (double alpha = 0; alpha < Geometry.TWO_PI; alpha += 0.1) {
            Vector3D p = Vector3D.linearCombination(Math.cos(alpha), circle.getXAxis(),
                                      Math.sin(alpha), circle.getYAxis());
            Vector3D q = circle.toSpace(S1Point.of(alpha)).getVector();
            Assert.assertEquals(0.0, p.distance(q), 1.0e-10);
            Assert.assertEquals(0.5 * Math.PI, circle.getPole().angle(q), 1.0e-10);
        }
    }

    @Test
    public void testOffset() {
        Circle circle = new Circle(Vector3D.PLUS_Z, 1.0e-10);
        Assert.assertEquals(0.0,                circle.getOffset(S2Point.ofVector(Vector3D.PLUS_X)),  1.0e-10);
        Assert.assertEquals(0.0,                circle.getOffset(S2Point.ofVector(Vector3D.MINUS_X)), 1.0e-10);
        Assert.assertEquals(0.0,                circle.getOffset(S2Point.ofVector(Vector3D.PLUS_Y)),  1.0e-10);
        Assert.assertEquals(0.0,                circle.getOffset(S2Point.ofVector(Vector3D.MINUS_Y)), 1.0e-10);
        Assert.assertEquals(-0.5 * Math.PI, circle.getOffset(S2Point.ofVector(Vector3D.PLUS_Z)),  1.0e-10);
        Assert.assertEquals(0.5 * Math.PI, circle.getOffset(S2Point.ofVector(Vector3D.MINUS_Z)), 1.0e-10);

    }

    @Test
    public void testInsideArc() {
        UnitSphereSampler sphRandom = new UnitSphereSampler(3, RandomSource.create(RandomSource.WELL_1024_A,
                                                                                   0xbfd34e92231bbcfel));
        for (int i = 0; i < 100; ++i) {
            Circle c1 = new Circle(Vector3D.ofArray(sphRandom.nextVector()), 1.0e-10);
            Circle c2 = new Circle(Vector3D.ofArray(sphRandom.nextVector()), 1.0e-10);
            checkArcIsInside(c1, c2);
            checkArcIsInside(c2, c1);
        }
    }

    private void checkArcIsInside(final Circle arcCircle, final Circle otherCircle) {
        Arc arc = arcCircle.getInsideArc(otherCircle);
        Assert.assertEquals(Math.PI, arc.getSize(), 1.0e-10);
        for (double alpha = arc.getInf(); alpha < arc.getSup(); alpha += 0.1) {
            Assert.assertTrue(otherCircle.getOffset(arcCircle.getPointAt(alpha)) <= 2.0e-15);
        }
        for (double alpha = arc.getSup(); alpha < arc.getInf() + Geometry.TWO_PI; alpha += 0.1) {
            Assert.assertTrue(otherCircle.getOffset(arcCircle.getPointAt(alpha)) >= -2.0e-15);
        }
    }

    @Test
    public void testTransform() {
        UniformRandomProvider random = RandomSource.create(RandomSource.WELL_1024_A,
                                                           0x16992fc4294bf2f1l);
        UnitSphereSampler sphRandom = new UnitSphereSampler(3, random);
        for (int i = 0; i < 100; ++i) {

            Rotation r = new Rotation(Vector3D.ofArray(sphRandom.nextVector()),
                                      Math.PI * random.nextDouble(),
                                      RotationConvention.VECTOR_OPERATOR);
            Transform<S2Point, S1Point> t = Circle.getTransform(r);

            S2Point  p = S2Point.ofVector(Vector3D.ofArray(sphRandom.nextVector()));
            S2Point tp = t.apply(p);
            Assert.assertEquals(0.0, r.applyTo(p.getVector()).distance(tp.getVector()), 1.0e-10);

            Circle  c = new Circle(Vector3D.ofArray(sphRandom.nextVector()), 1.0e-10);
            Circle tc = (Circle) t.apply(c);
            Assert.assertEquals(0.0, r.applyTo(c.getPole()).distance(tc.getPole()),   1.0e-10);
            Assert.assertEquals(0.0, r.applyTo(c.getXAxis()).distance(tc.getXAxis()), 1.0e-10);
            Assert.assertEquals(0.0, r.applyTo(c.getYAxis()).distance(tc.getYAxis()), 1.0e-10);
            Assert.assertEquals(c.getTolerance(), ((Circle) t.apply(c)).getTolerance(), 1.0e-10);

            SubLimitAngle  sub = new LimitAngle(S1Point.of(Geometry.TWO_PI * random.nextDouble()),
                                                random.nextBoolean(), 1.0e-10).wholeHyperplane();
            Vector3D psub = c.getPointAt(((LimitAngle) sub.getHyperplane()).getLocation().getAzimuth());
            SubLimitAngle tsub = (SubLimitAngle) t.apply(sub, c, tc);
            Vector3D ptsub = tc.getPointAt(((LimitAngle) tsub.getHyperplane()).getLocation().getAzimuth());
            Assert.assertEquals(0.0, r.applyTo(psub).distance(ptsub), 1.0e-10);

        }
    }

}
