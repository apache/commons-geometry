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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.partitioning.Region.Location;
import org.apache.commons.geometry.core.partitioning.RegionFactory;
import org.apache.commons.geometry.core.partitioning.SubHyperplane;
import org.apache.commons.geometry.enclosing.EnclosingBall;
import org.apache.commons.geometry.euclidean.threed.Rotation;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.oned.ArcsSet;
import org.apache.commons.geometry.spherical.oned.S1Point;
import org.apache.commons.rng.sampling.UnitSphereSampler;
import org.apache.commons.rng.simple.RandomSource;
import org.junit.Assert;
import org.junit.Test;

public class SphericalPolygonsSetTest {

    @Test
    public void testFullSphere() {
        SphericalPolygonsSet full = new SphericalPolygonsSet(1.0e-10);
        UnitSphereSampler random =
                new UnitSphereSampler(3, RandomSource.create(RandomSource.WELL_1024_A,
                                                             0x852fd2a0ed8d2f6dl));
        for (int i = 0; i < 1000; ++i) {
            Vector3D v = Vector3D.of(random.nextVector());
            Assert.assertEquals(Location.INSIDE, full.checkPoint(S2Point.of(v)));
        }
        Assert.assertEquals(4 * Math.PI, new SphericalPolygonsSet(0.01, new S2Point[0]).getSize(), 1.0e-10);
        Assert.assertEquals(0, new SphericalPolygonsSet(0.01, new S2Point[0]).getBoundarySize(), 1.0e-10);
        Assert.assertEquals(0, full.getBoundaryLoops().size());
        Assert.assertTrue(full.getEnclosingCap().getRadius() > 0);
        Assert.assertTrue(Double.isInfinite(full.getEnclosingCap().getRadius()));
    }

    @Test
    public void testEmpty() {
        SphericalPolygonsSet empty =
            (SphericalPolygonsSet) new RegionFactory<S2Point>().getComplement(new SphericalPolygonsSet(1.0e-10));
        UnitSphereSampler random =
                new UnitSphereSampler(3, RandomSource.create(RandomSource.WELL_1024_A,
                                                             0x76d9205d6167b6ddl));
        for (int i = 0; i < 1000; ++i) {
            Vector3D v = Vector3D.of(random.nextVector());
            Assert.assertEquals(Location.OUTSIDE, empty.checkPoint(S2Point.of(v)));
        }
        Assert.assertEquals(0, empty.getSize(), 1.0e-10);
        Assert.assertEquals(0, empty.getBoundarySize(), 1.0e-10);
        Assert.assertEquals(0, empty.getBoundaryLoops().size());
        Assert.assertTrue(empty.getEnclosingCap().getRadius() < 0);
        Assert.assertTrue(Double.isInfinite(empty.getEnclosingCap().getRadius()));
    }

    @Test
    public void testSouthHemisphere() {
        double tol = 0.01;
        double sinTol = Math.sin(tol);
        SphericalPolygonsSet south = new SphericalPolygonsSet(Vector3D.MINUS_Z, tol);
        UnitSphereSampler random =
                new UnitSphereSampler(3, RandomSource.create(RandomSource.WELL_1024_A,
                                                             0x6b9d4a6ad90d7b0bl));
        for (int i = 0; i < 1000; ++i) {
            Vector3D v = Vector3D.of(random.nextVector());
            if (v.getZ() < -sinTol) {
                Assert.assertEquals(Location.INSIDE, south.checkPoint(S2Point.of(v)));
            } else if (v.getZ() > sinTol) {
                Assert.assertEquals(Location.OUTSIDE, south.checkPoint(S2Point.of(v)));
            } else {
                Assert.assertEquals(Location.BOUNDARY, south.checkPoint(S2Point.of(v)));
            }
        }
        Assert.assertEquals(1, south.getBoundaryLoops().size());

        EnclosingBall<S2Point> southCap = south.getEnclosingCap();
        Assert.assertEquals(0.0, S2Point.MINUS_K.distance(southCap.getCenter()), 1.0e-10);
        Assert.assertEquals(0.5 * Math.PI, southCap.getRadius(), 1.0e-10);

        EnclosingBall<S2Point> northCap =
                ((SphericalPolygonsSet) new RegionFactory<S2Point>().getComplement(south)).getEnclosingCap();
        Assert.assertEquals(0.0, S2Point.PLUS_K.distance(northCap.getCenter()), 1.0e-10);
        Assert.assertEquals(0.5 * Math.PI, northCap.getRadius(), 1.0e-10);

    }

    @Test
    public void testPositiveOctantByIntersection() {
        double tol = 0.01;
        double sinTol = Math.sin(tol);
        RegionFactory<S2Point> factory = new RegionFactory<>();
        SphericalPolygonsSet plusX = new SphericalPolygonsSet(Vector3D.PLUS_X, tol);
        SphericalPolygonsSet plusY = new SphericalPolygonsSet(Vector3D.PLUS_Y, tol);
        SphericalPolygonsSet plusZ = new SphericalPolygonsSet(Vector3D.PLUS_Z, tol);
        SphericalPolygonsSet octant =
                (SphericalPolygonsSet) factory.intersection(factory.intersection(plusX, plusY), plusZ);
        UnitSphereSampler random =
                new UnitSphereSampler(3, RandomSource.create(RandomSource.WELL_1024_A,
                                                             0x9c9802fde3cbcf25l));
        for (int i = 0; i < 1000; ++i) {
            Vector3D v = Vector3D.of(random.nextVector());
            if ((v.getX() > sinTol) && (v.getY() > sinTol) && (v.getZ() > sinTol)) {
                Assert.assertEquals(Location.INSIDE, octant.checkPoint(S2Point.of(v)));
            } else if ((v.getX() < -sinTol) || (v.getY() < -sinTol) || (v.getZ() < -sinTol)) {
                Assert.assertEquals(Location.OUTSIDE, octant.checkPoint(S2Point.of(v)));
            } else {
                Assert.assertEquals(Location.BOUNDARY, octant.checkPoint(S2Point.of(v)));
            }
        }

        List<Vertex> loops = octant.getBoundaryLoops();
        Assert.assertEquals(1, loops.size());
        boolean xPFound = false;
        boolean yPFound = false;
        boolean zPFound = false;
        boolean xVFound = false;
        boolean yVFound = false;
        boolean zVFound = false;
        Vertex first = loops.get(0);
        int count = 0;
        for (Vertex v = first; count == 0 || v != first; v = v.getOutgoing().getEnd()) {
            ++count;
            Edge e = v.getIncoming();
            Assert.assertTrue(v == e.getStart().getOutgoing().getEnd());
            xPFound = xPFound || e.getCircle().getPole().distance(Vector3D.PLUS_X) < 1.0e-10;
            yPFound = yPFound || e.getCircle().getPole().distance(Vector3D.PLUS_Y) < 1.0e-10;
            zPFound = zPFound || e.getCircle().getPole().distance(Vector3D.PLUS_Z) < 1.0e-10;
            Assert.assertEquals(0.5 * Math.PI, e.getLength(), 1.0e-10);
            xVFound = xVFound || v.getLocation().getVector().distance(Vector3D.PLUS_X) < 1.0e-10;
            yVFound = yVFound || v.getLocation().getVector().distance(Vector3D.PLUS_Y) < 1.0e-10;
            zVFound = zVFound || v.getLocation().getVector().distance(Vector3D.PLUS_Z) < 1.0e-10;
        }
        Assert.assertTrue(xPFound);
        Assert.assertTrue(yPFound);
        Assert.assertTrue(zPFound);
        Assert.assertTrue(xVFound);
        Assert.assertTrue(yVFound);
        Assert.assertTrue(zVFound);
        Assert.assertEquals(3, count);

        Assert.assertEquals(0.0,
                            octant.getBarycenter().distance(S2Point.of(Vector3D.of(1, 1, 1))),
                            1.0e-10);
        Assert.assertEquals(0.5 * Math.PI, octant.getSize(), 1.0e-10);

        EnclosingBall<S2Point> cap = octant.getEnclosingCap();
        Assert.assertEquals(0.0, octant.getBarycenter().distance(cap.getCenter()), 1.0e-10);
        Assert.assertEquals(Math.acos(1.0 / Math.sqrt(3)), cap.getRadius(), 1.0e-10);

        EnclosingBall<S2Point> reversedCap =
                ((SphericalPolygonsSet) factory.getComplement(octant)).getEnclosingCap();
        Assert.assertEquals(0, reversedCap.getCenter().distance(S2Point.of(Vector3D.of(-1, -1, -1))), 1.0e-10);
        Assert.assertEquals(Math.PI - Math.asin(1.0 / Math.sqrt(3)), reversedCap.getRadius(), 1.0e-10);

    }

    @Test
    public void testPositiveOctantByVertices() {
        double tol = 0.01;
        double sinTol = Math.sin(tol);
        SphericalPolygonsSet octant = new SphericalPolygonsSet(tol, S2Point.PLUS_I, S2Point.PLUS_J, S2Point.PLUS_K);
        UnitSphereSampler random =
                new UnitSphereSampler(3, RandomSource.create(RandomSource.WELL_1024_A,
                                                             0xb8fc5acc91044308l));
        for (int i = 0; i < 1000; ++i) {
            Vector3D v = Vector3D.of(random.nextVector());
            if ((v.getX() > sinTol) && (v.getY() > sinTol) && (v.getZ() > sinTol)) {
                Assert.assertEquals(Location.INSIDE, octant.checkPoint(S2Point.of(v)));
            } else if ((v.getX() < -sinTol) || (v.getY() < -sinTol) || (v.getZ() < -sinTol)) {
                Assert.assertEquals(Location.OUTSIDE, octant.checkPoint(S2Point.of(v)));
            } else {
                Assert.assertEquals(Location.BOUNDARY, octant.checkPoint(S2Point.of(v)));
            }
        }
    }

    @Test
    public void testNonConvex() {
        double tol = 0.01;
        double sinTol = Math.sin(tol);
        RegionFactory<S2Point> factory = new RegionFactory<>();
        SphericalPolygonsSet plusX = new SphericalPolygonsSet(Vector3D.PLUS_X, tol);
        SphericalPolygonsSet plusY = new SphericalPolygonsSet(Vector3D.PLUS_Y, tol);
        SphericalPolygonsSet plusZ = new SphericalPolygonsSet(Vector3D.PLUS_Z, tol);
        SphericalPolygonsSet threeOctants =
                (SphericalPolygonsSet) factory.difference(plusZ, factory.intersection(plusX, plusY));

        UnitSphereSampler random =
                new UnitSphereSampler(3, RandomSource.create(RandomSource.WELL_1024_A,
                                                             0x9c9802fde3cbcf25l));
        for (int i = 0; i < 1000; ++i) {
            Vector3D v = Vector3D.of(random.nextVector());
            if (((v.getX() < -sinTol) || (v.getY() < -sinTol)) && (v.getZ() > sinTol)) {
                Assert.assertEquals(Location.INSIDE, threeOctants.checkPoint(S2Point.of(v)));
            } else if (((v.getX() > sinTol) && (v.getY() > sinTol)) || (v.getZ() < -sinTol)) {
                Assert.assertEquals(Location.OUTSIDE, threeOctants.checkPoint(S2Point.of(v)));
            } else {
                Assert.assertEquals(Location.BOUNDARY, threeOctants.checkPoint(S2Point.of(v)));
            }
        }

        List<Vertex> loops = threeOctants.getBoundaryLoops();
        Assert.assertEquals(1, loops.size());
        boolean xPFound = false;
        boolean yPFound = false;
        boolean zPFound = false;
        boolean xVFound = false;
        boolean yVFound = false;
        boolean zVFound = false;
        Vertex first = loops.get(0);
        int count = 0;
        double sumPoleX = 0;
        double sumPoleY = 0;
        double sumPoleZ = 0;
        for (Vertex v = first; count == 0 || v != first; v = v.getOutgoing().getEnd()) {
            ++count;
            Edge e = v.getIncoming();
            Assert.assertTrue(v == e.getStart().getOutgoing().getEnd());
            if (e.getCircle().getPole().distance(Vector3D.MINUS_X) < 1.0e-10) {
                xPFound = true;
                sumPoleX += e.getLength();
            } else if (e.getCircle().getPole().distance(Vector3D.MINUS_Y) < 1.0e-10) {
                yPFound = true;
                sumPoleY += e.getLength();
            } else {
                Assert.assertEquals(0.0, e.getCircle().getPole().distance(Vector3D.PLUS_Z), 1.0e-10);
                zPFound = true;
                sumPoleZ += e.getLength();
            }
            xVFound = xVFound || v.getLocation().getVector().distance(Vector3D.PLUS_X) < 1.0e-10;
            yVFound = yVFound || v.getLocation().getVector().distance(Vector3D.PLUS_Y) < 1.0e-10;
            zVFound = zVFound || v.getLocation().getVector().distance(Vector3D.PLUS_Z) < 1.0e-10;
        }
        Assert.assertTrue(xPFound);
        Assert.assertTrue(yPFound);
        Assert.assertTrue(zPFound);
        Assert.assertTrue(xVFound);
        Assert.assertTrue(yVFound);
        Assert.assertTrue(zVFound);
        Assert.assertEquals(0.5 * Math.PI, sumPoleX, 1.0e-10);
        Assert.assertEquals(0.5 * Math.PI, sumPoleY, 1.0e-10);
        Assert.assertEquals(1.5 * Math.PI, sumPoleZ, 1.0e-10);

        Assert.assertEquals(1.5 * Math.PI, threeOctants.getSize(), 1.0e-10);

    }

    @Test
    public void testModeratlyComplexShape() {
        double tol = 0.01;
        List<SubHyperplane<S2Point>> boundary = new ArrayList<>();
        boundary.add(create(Vector3D.MINUS_Y, Vector3D.PLUS_X,  Vector3D.PLUS_Z,  tol, 0.0, 0.5 * Math.PI));
        boundary.add(create(Vector3D.MINUS_X, Vector3D.PLUS_Z,  Vector3D.PLUS_Y,  tol, 0.0, 0.5 * Math.PI));
        boundary.add(create(Vector3D.PLUS_Z,  Vector3D.PLUS_Y,  Vector3D.MINUS_X, tol, 0.0, 0.5 * Math.PI));
        boundary.add(create(Vector3D.MINUS_Y, Vector3D.MINUS_X, Vector3D.MINUS_Z, tol, 0.0, 0.5 * Math.PI));
        boundary.add(create(Vector3D.MINUS_X, Vector3D.MINUS_Z, Vector3D.MINUS_Y, tol, 0.0, 0.5 * Math.PI));
        boundary.add(create(Vector3D.PLUS_Z,  Vector3D.MINUS_Y, Vector3D.PLUS_X,  tol, 0.0, 0.5 * Math.PI));
        SphericalPolygonsSet polygon = new SphericalPolygonsSet(boundary, tol);

        Assert.assertEquals(Location.OUTSIDE, polygon.checkPoint(S2Point.of(Vector3D.of( 1,  1,  1).normalize())));
        Assert.assertEquals(Location.INSIDE,  polygon.checkPoint(S2Point.of(Vector3D.of(-1,  1,  1).normalize())));
        Assert.assertEquals(Location.INSIDE,  polygon.checkPoint(S2Point.of(Vector3D.of(-1, -1,  1).normalize())));
        Assert.assertEquals(Location.INSIDE,  polygon.checkPoint(S2Point.of(Vector3D.of( 1, -1,  1).normalize())));
        Assert.assertEquals(Location.OUTSIDE, polygon.checkPoint(S2Point.of(Vector3D.of( 1,  1, -1).normalize())));
        Assert.assertEquals(Location.OUTSIDE, polygon.checkPoint(S2Point.of(Vector3D.of(-1,  1, -1).normalize())));
        Assert.assertEquals(Location.INSIDE,  polygon.checkPoint(S2Point.of(Vector3D.of(-1, -1, -1).normalize())));
        Assert.assertEquals(Location.OUTSIDE, polygon.checkPoint(S2Point.of(Vector3D.of( 1, -1, -1).normalize())));

        Assert.assertEquals(Geometry.TWO_PI, polygon.getSize(), 1.0e-10);
        Assert.assertEquals(3 * Math.PI, polygon.getBoundarySize(), 1.0e-10);

        List<Vertex> loops = polygon.getBoundaryLoops();
        Assert.assertEquals(1, loops.size());
        boolean pXFound = false;
        boolean mXFound = false;
        boolean pYFound = false;
        boolean mYFound = false;
        boolean pZFound = false;
        boolean mZFound = false;
        Vertex first = loops.get(0);
        int count = 0;
        for (Vertex v = first; count == 0 || v != first; v = v.getOutgoing().getEnd()) {
            ++count;
            Edge e = v.getIncoming();
            Assert.assertTrue(v == e.getStart().getOutgoing().getEnd());
            pXFound = pXFound || v.getLocation().getVector().distance(Vector3D.PLUS_X)  < 1.0e-10;
            mXFound = mXFound || v.getLocation().getVector().distance(Vector3D.MINUS_X) < 1.0e-10;
            pYFound = pYFound || v.getLocation().getVector().distance(Vector3D.PLUS_Y)  < 1.0e-10;
            mYFound = mYFound || v.getLocation().getVector().distance(Vector3D.MINUS_Y) < 1.0e-10;
            pZFound = pZFound || v.getLocation().getVector().distance(Vector3D.PLUS_Z)  < 1.0e-10;
            mZFound = mZFound || v.getLocation().getVector().distance(Vector3D.MINUS_Z) < 1.0e-10;
            Assert.assertEquals(0.5 * Math.PI, e.getLength(), 1.0e-10);
        }
        Assert.assertTrue(pXFound);
        Assert.assertTrue(mXFound);
        Assert.assertTrue(pYFound);
        Assert.assertTrue(mYFound);
        Assert.assertTrue(pZFound);
        Assert.assertTrue(mZFound);
        Assert.assertEquals(6, count);

    }

    @Test
    public void testSeveralParts() {
        double tol = 0.01;
        double sinTol = Math.sin(tol);
        List<SubHyperplane<S2Point>> boundary = new ArrayList<>();

        // first part: +X, +Y, +Z octant
        boundary.add(create(Vector3D.PLUS_Y,  Vector3D.PLUS_Z,  Vector3D.PLUS_X,  tol, 0.0, 0.5 * Math.PI));
        boundary.add(create(Vector3D.PLUS_Z,  Vector3D.PLUS_X,  Vector3D.PLUS_Y,  tol, 0.0, 0.5 * Math.PI));
        boundary.add(create(Vector3D.PLUS_X,  Vector3D.PLUS_Y,  Vector3D.PLUS_Z,  tol, 0.0, 0.5 * Math.PI));

        // first part: -X, -Y, -Z octant
        boundary.add(create(Vector3D.MINUS_Y, Vector3D.MINUS_X, Vector3D.MINUS_Z, tol, 0.0, 0.5 * Math.PI));
        boundary.add(create(Vector3D.MINUS_X, Vector3D.MINUS_Z, Vector3D.MINUS_Y, tol, 0.0, 0.5 * Math.PI));
        boundary.add(create(Vector3D.MINUS_Z, Vector3D.MINUS_Y, Vector3D.MINUS_X,  tol, 0.0, 0.5 * Math.PI));

        SphericalPolygonsSet polygon = new SphericalPolygonsSet(boundary, tol);

        UnitSphereSampler random =
                new UnitSphereSampler(3, RandomSource.create(RandomSource.WELL_1024_A,
                                                             0xcc5ce49949e0d3ecl));
        for (int i = 0; i < 1000; ++i) {
            Vector3D v = Vector3D.of(random.nextVector());
            if ((v.getX() < -sinTol) && (v.getY() < -sinTol) && (v.getZ() < -sinTol)) {
                Assert.assertEquals(Location.INSIDE, polygon.checkPoint(S2Point.of(v)));
            } else if ((v.getX() < sinTol) && (v.getY() < sinTol) && (v.getZ() < sinTol)) {
                Assert.assertEquals(Location.BOUNDARY, polygon.checkPoint(S2Point.of(v)));
            } else if ((v.getX() > sinTol) && (v.getY() > sinTol) && (v.getZ() > sinTol)) {
                Assert.assertEquals(Location.INSIDE, polygon.checkPoint(S2Point.of(v)));
            } else if ((v.getX() > -sinTol) && (v.getY() > -sinTol) && (v.getZ() > -sinTol)) {
                Assert.assertEquals(Location.BOUNDARY, polygon.checkPoint(S2Point.of(v)));
            } else {
                Assert.assertEquals(Location.OUTSIDE, polygon.checkPoint(S2Point.of(v)));
            }
        }

        Assert.assertEquals(Math.PI, polygon.getSize(), 1.0e-10);
        Assert.assertEquals(3 * Math.PI, polygon.getBoundarySize(), 1.0e-10);

        // there should be two separate boundary loops
        Assert.assertEquals(2, polygon.getBoundaryLoops().size());

    }

    @Test
    public void testPartWithHole() {
        double tol = 0.01;
        double alpha = 0.7;
        S2Point center = S2Point.of(Vector3D.of(1, 1, 1));
        SphericalPolygonsSet hexa = new SphericalPolygonsSet(center.getVector(), Vector3D.PLUS_Z, alpha, 6, tol);
        SphericalPolygonsSet hole  = new SphericalPolygonsSet(tol,
                                                              S2Point.of(Math.PI / 6, Math.PI / 3),
                                                              S2Point.of(Math.PI / 3, Math.PI / 3),
                                                              S2Point.of(Math.PI / 4, Math.PI / 6));
        SphericalPolygonsSet hexaWithHole =
                (SphericalPolygonsSet) new RegionFactory<S2Point>().difference(hexa, hole);

        for (double phi = center.getPhi() - alpha + 0.1; phi < center.getPhi() + alpha - 0.1; phi += 0.07) {
            Location l = hexaWithHole.checkPoint(S2Point.of(Math.PI / 4, phi));
            if (phi < Math.PI / 6 || phi > Math.PI / 3) {
                Assert.assertEquals(Location.INSIDE,  l);
            } else {
                Assert.assertEquals(Location.OUTSIDE, l);
            }
        }

        // there should be two separate boundary loops
        Assert.assertEquals(2, hexaWithHole.getBoundaryLoops().size());

        Assert.assertEquals(hexa.getBoundarySize() + hole.getBoundarySize(), hexaWithHole.getBoundarySize(), 1.0e-10);
        Assert.assertEquals(hexa.getSize() - hole.getSize(), hexaWithHole.getSize(), 1.0e-10);

    }

    @Test
    public void testConcentricSubParts() {
        double tol = 0.001;
        Vector3D center = Vector3D.of(1, 1, 1);
        SphericalPolygonsSet hexaOut   = new SphericalPolygonsSet(center, Vector3D.PLUS_Z, 0.9,  6, tol);
        SphericalPolygonsSet hexaIn    = new SphericalPolygonsSet(center, Vector3D.PLUS_Z, 0.8,  6, tol);
        SphericalPolygonsSet pentaOut  = new SphericalPolygonsSet(center, Vector3D.PLUS_Z, 0.7,  5, tol);
        SphericalPolygonsSet pentaIn   = new SphericalPolygonsSet(center, Vector3D.PLUS_Z, 0.6,  5, tol);
        SphericalPolygonsSet quadriOut = new SphericalPolygonsSet(center, Vector3D.PLUS_Z, 0.5,  4, tol);
        SphericalPolygonsSet quadriIn  = new SphericalPolygonsSet(center, Vector3D.PLUS_Z, 0.4,  4, tol);
        SphericalPolygonsSet triOut    = new SphericalPolygonsSet(center, Vector3D.PLUS_Z, 0.25, 3, tol);
        SphericalPolygonsSet triIn     = new SphericalPolygonsSet(center, Vector3D.PLUS_Z, 0.15, 3, tol);

        RegionFactory<S2Point> factory = new RegionFactory<>();
        SphericalPolygonsSet hexa   = (SphericalPolygonsSet) factory.difference(hexaOut,   hexaIn);
        SphericalPolygonsSet penta  = (SphericalPolygonsSet) factory.difference(pentaOut,  pentaIn);
        SphericalPolygonsSet quadri = (SphericalPolygonsSet) factory.difference(quadriOut, quadriIn);
        SphericalPolygonsSet tri    = (SphericalPolygonsSet) factory.difference(triOut,    triIn);
        SphericalPolygonsSet concentric =
                (SphericalPolygonsSet) factory.union(factory.union(hexa, penta), factory.union(quadri, tri));

        // there should be two separate boundary loops
        Assert.assertEquals(8, concentric.getBoundaryLoops().size());

        Assert.assertEquals(hexaOut.getBoundarySize()   + hexaIn.getBoundarySize()   +
                            pentaOut.getBoundarySize()  + pentaIn.getBoundarySize()  +
                            quadriOut.getBoundarySize() + quadriIn.getBoundarySize() +
                            triOut.getBoundarySize()    + triIn.getBoundarySize(),
                            concentric.getBoundarySize(), 1.0e-10);
        Assert.assertEquals(hexaOut.getSize()   - hexaIn.getSize()   +
                            pentaOut.getSize()  - pentaIn.getSize()  +
                            quadriOut.getSize() - quadriIn.getSize() +
                            triOut.getSize()    - triIn.getSize(),
                            concentric.getSize(), 1.0e-10);

        // we expect lots of sign changes as we traverse all concentric rings
        double phi = S2Point.of(center).getPhi();
        Assert.assertEquals(+0.207, concentric.projectToBoundary(S2Point.of(-0.60,  phi)).getOffset(), 0.01);
        Assert.assertEquals(-0.048, concentric.projectToBoundary(S2Point.of(-0.21,  phi)).getOffset(), 0.01);
        Assert.assertEquals(+0.027, concentric.projectToBoundary(S2Point.of(-0.10,  phi)).getOffset(), 0.01);
        Assert.assertEquals(-0.041, concentric.projectToBoundary(S2Point.of( 0.01,  phi)).getOffset(), 0.01);
        Assert.assertEquals(+0.049, concentric.projectToBoundary(S2Point.of( 0.16,  phi)).getOffset(), 0.01);
        Assert.assertEquals(-0.038, concentric.projectToBoundary(S2Point.of( 0.29,  phi)).getOffset(), 0.01);
        Assert.assertEquals(+0.097, concentric.projectToBoundary(S2Point.of( 0.48,  phi)).getOffset(), 0.01);
        Assert.assertEquals(-0.022, concentric.projectToBoundary(S2Point.of( 0.64,  phi)).getOffset(), 0.01);
        Assert.assertEquals(+0.072, concentric.projectToBoundary(S2Point.of( 0.79,  phi)).getOffset(), 0.01);
        Assert.assertEquals(-0.022, concentric.projectToBoundary(S2Point.of( 0.93,  phi)).getOffset(), 0.01);
        Assert.assertEquals(+0.091, concentric.projectToBoundary(S2Point.of( 1.08,  phi)).getOffset(), 0.01);
        Assert.assertEquals(-0.037, concentric.projectToBoundary(S2Point.of( 1.28,  phi)).getOffset(), 0.01);
        Assert.assertEquals(+0.051, concentric.projectToBoundary(S2Point.of( 1.40,  phi)).getOffset(), 0.01);
        Assert.assertEquals(-0.041, concentric.projectToBoundary(S2Point.of( 1.55,  phi)).getOffset(), 0.01);
        Assert.assertEquals(+0.027, concentric.projectToBoundary(S2Point.of( 1.67,  phi)).getOffset(), 0.01);
        Assert.assertEquals(-0.044, concentric.projectToBoundary(S2Point.of( 1.79,  phi)).getOffset(), 0.01);
        Assert.assertEquals(+0.201, concentric.projectToBoundary(S2Point.of( 2.16,  phi)).getOffset(), 0.01);

    }

    @Test
    public void testGeographicalMap() {

        SphericalPolygonsSet continental = buildSimpleZone(new double[][] {
          { 51.14850,  2.51357 }, { 50.94660,  1.63900 }, { 50.12717,  1.33876 }, { 49.34737, -0.98946 },
          { 49.77634, -1.93349 }, { 48.64442, -1.61651 }, { 48.90169, -3.29581 }, { 48.68416, -4.59234 },
          { 47.95495, -4.49155 }, { 47.57032, -2.96327 }, { 46.01491, -1.19379 }, { 44.02261, -1.38422 },
          { 43.42280, -1.90135 }, { 43.03401, -1.50277 }, { 42.34338,  1.82679 }, { 42.47301,  2.98599 },
          { 43.07520,  3.10041 }, { 43.39965,  4.55696 }, { 43.12889,  6.52924 }, { 43.69384,  7.43518 },
          { 44.12790,  7.54959 }, { 45.02851,  6.74995 }, { 45.33309,  7.09665 }, { 46.42967,  6.50009 },
          { 46.27298,  6.02260 }, { 46.72577,  6.03738 }, { 47.62058,  7.46675 }, { 49.01778,  8.09927 },
          { 49.20195,  6.65822 }, { 49.44266,  5.89775 }, { 49.98537,  4.79922 }
        });
        SphericalPolygonsSet corsica = buildSimpleZone(new double[][] {
          { 42.15249,  9.56001 }, { 43.00998,  9.39000 }, { 42.62812,  8.74600 }, { 42.25651,  8.54421 },
          { 41.58361,  8.77572 }, { 41.38000,  9.22975 }
        });
        RegionFactory<S2Point> factory = new RegionFactory<>();
        SphericalPolygonsSet zone = (SphericalPolygonsSet) factory.union(continental, corsica);
        EnclosingBall<S2Point> enclosing = zone.getEnclosingCap();
        Vector3D enclosingCenter = enclosing.getCenter().getVector();

        double step = Math.toRadians(0.1);
        for (Vertex loopStart : zone.getBoundaryLoops()) {
            int count = 0;
            for (Vertex v = loopStart; count == 0 || v != loopStart; v = v.getOutgoing().getEnd()) {
                ++count;
                for (int i = 0; i < Math.ceil(v.getOutgoing().getLength() / step); ++i) {
                    Vector3D p = v.getOutgoing().getPointAt(i * step);
                    Assert.assertTrue(Vector3D.angle(p, enclosingCenter) <= enclosing.getRadius());
                }
            }
        }

        S2Point supportPointA = s2Point(48.68416, -4.59234);
        S2Point supportPointB = s2Point(41.38000,  9.22975);
        Assert.assertEquals(enclosing.getRadius(), supportPointA.distance(enclosing.getCenter()), 1.0e-10);
        Assert.assertEquals(enclosing.getRadius(), supportPointB.distance(enclosing.getCenter()), 1.0e-10);
        Assert.assertEquals(0.5 * supportPointA.distance(supportPointB), enclosing.getRadius(), 1.0e-10);
        Assert.assertEquals(2, enclosing.getSupportSize());

        EnclosingBall<S2Point> continentalInscribed =
                ((SphericalPolygonsSet) factory.getComplement(continental)).getEnclosingCap();
        Vector3D continentalCenter = continentalInscribed.getCenter().getVector();
        Assert.assertEquals(2.2, Math.toDegrees(Math.PI - continentalInscribed.getRadius()), 0.1);
        for (Vertex loopStart : continental.getBoundaryLoops()) {
            int count = 0;
            for (Vertex v = loopStart; count == 0 || v != loopStart; v = v.getOutgoing().getEnd()) {
                ++count;
                for (int i = 0; i < Math.ceil(v.getOutgoing().getLength() / step); ++i) {
                    Vector3D p = v.getOutgoing().getPointAt(i * step);
                    Assert.assertTrue(Vector3D.angle(p, continentalCenter) <= continentalInscribed.getRadius());
                }
            }
        }

        EnclosingBall<S2Point> corsicaInscribed =
                ((SphericalPolygonsSet) factory.getComplement(corsica)).getEnclosingCap();
        Vector3D corsicaCenter = corsicaInscribed.getCenter().getVector();
        Assert.assertEquals(0.34, Math.toDegrees(Math.PI - corsicaInscribed.getRadius()), 0.01);
        for (Vertex loopStart : corsica.getBoundaryLoops()) {
            int count = 0;
            for (Vertex v = loopStart; count == 0 || v != loopStart; v = v.getOutgoing().getEnd()) {
                ++count;
                for (int i = 0; i < Math.ceil(v.getOutgoing().getLength() / step); ++i) {
                    Vector3D p = v.getOutgoing().getPointAt(i * step);
                    Assert.assertTrue(Vector3D.angle(p, corsicaCenter) <= corsicaInscribed.getRadius());
                }
            }
        }

    }

    private SubCircle create(Vector3D pole, Vector3D x, Vector3D y,
                             double tolerance, double ... limits) {
        RegionFactory<S1Point> factory = new RegionFactory<>();
        Circle circle = new Circle(pole, tolerance);
        Circle phased =
                (Circle) Circle.getTransform(new Rotation(circle.getXAxis(), circle.getYAxis(), x, y)).apply(circle);
        ArcsSet set = (ArcsSet) factory.getComplement(new ArcsSet(tolerance));
        for (int i = 0; i < limits.length; i += 2) {
            set = (ArcsSet) factory.union(set, new ArcsSet(limits[i], limits[i + 1], tolerance));
        }
        return new SubCircle(phased, set);
    }

    private SphericalPolygonsSet buildSimpleZone(double[][] points) {
        final S2Point[] vertices = new S2Point[points.length];
        for (int i = 0; i < points.length; ++i) {
            vertices[i] = s2Point(points[i][0], points[i][1]);
        }
        return new SphericalPolygonsSet(1.0e-10, vertices);
    }

    private S2Point s2Point(double latitude, double longitude) {
        return S2Point.of(Math.toRadians(longitude), Math.toRadians(90.0 - latitude));
    }

}
