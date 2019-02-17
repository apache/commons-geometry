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
package org.apache.commons.geometry.spherical.oned;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.partitioning.BSPTree_Old;
import org.apache.commons.geometry.core.partitioning.Region;
import org.apache.commons.geometry.core.partitioning.Region.Location;
import org.apache.commons.geometry.core.partitioning.RegionFactory;
import org.apache.commons.geometry.core.partitioning.Side;
import org.apache.commons.geometry.core.partitioning.SubHyperplane;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.numbers.core.Precision;
import org.junit.Assert;
import org.junit.Test;

public class ArcsSetTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testArc() {
        ArcsSet set = new ArcsSet(2.3, 5.7, TEST_PRECISION);
        Assert.assertEquals(3.4, set.getSize(), TEST_EPS);
        Assert.assertSame(TEST_PRECISION, set.getPrecision());
        Assert.assertEquals(Region.Location.BOUNDARY, set.checkPoint(S1Point.of(2.3)));
        Assert.assertEquals(Region.Location.BOUNDARY, set.checkPoint(S1Point.of(5.7)));
        Assert.assertEquals(Region.Location.OUTSIDE,  set.checkPoint(S1Point.of(1.2)));
        Assert.assertEquals(Region.Location.OUTSIDE,  set.checkPoint(S1Point.of(8.5)));
        Assert.assertEquals(Region.Location.INSIDE,   set.checkPoint(S1Point.of(8.7)));
        Assert.assertEquals(Region.Location.INSIDE,   set.checkPoint(S1Point.of(3.0)));
        Assert.assertEquals(1, set.asList().size());
        Assert.assertEquals(2.3, set.asList().get(0).getInf(), TEST_EPS);
        Assert.assertEquals(5.7, set.asList().get(0).getSup(), TEST_EPS);
    }

    @Test
    public void testWrapAround2PiArc() {
        ArcsSet set = new ArcsSet(5.7 - Geometry.TWO_PI, 2.3, TEST_PRECISION);
        Assert.assertEquals(Geometry.TWO_PI - 3.4, set.getSize(), TEST_EPS);
        Assert.assertSame(TEST_PRECISION, set.getPrecision());
        Assert.assertEquals(Region.Location.BOUNDARY, set.checkPoint(S1Point.of(2.3)));
        Assert.assertEquals(Region.Location.BOUNDARY, set.checkPoint(S1Point.of(5.7)));
        Assert.assertEquals(Region.Location.INSIDE,   set.checkPoint(S1Point.of(1.2)));
        Assert.assertEquals(Region.Location.INSIDE,   set.checkPoint(S1Point.of(8.5)));
        Assert.assertEquals(Region.Location.OUTSIDE,  set.checkPoint(S1Point.of(8.7)));
        Assert.assertEquals(Region.Location.OUTSIDE,  set.checkPoint(S1Point.of(3.0)));
        Assert.assertEquals(1, set.asList().size());
        Assert.assertEquals(5.7, set.asList().get(0).getInf(), TEST_EPS);
        Assert.assertEquals(2.3 + Geometry.TWO_PI, set.asList().get(0).getSup(), TEST_EPS);
    }

    @Test
    public void testSplitOver2Pi() {
        ArcsSet set = new ArcsSet(TEST_PRECISION);
        Arc     arc = new Arc(1.5 * Math.PI, 2.5 * Math.PI, TEST_PRECISION);
        ArcsSet.Split split = set.split(arc);
        for (double alpha = 0.0; alpha <= Geometry.TWO_PI; alpha += 0.01) {
            S1Point p = S1Point.of(alpha);
            if (alpha < 0.5 * Math.PI || alpha > 1.5 * Math.PI) {
                Assert.assertEquals(Location.OUTSIDE, split.getPlus().checkPoint(p));
                Assert.assertEquals(Location.INSIDE,  split.getMinus().checkPoint(p));
            } else {
                Assert.assertEquals(Location.INSIDE,  split.getPlus().checkPoint(p));
                Assert.assertEquals(Location.OUTSIDE, split.getMinus().checkPoint(p));
            }
        }
    }

    @Test
    public void testSplitAtEnd() {
        ArcsSet set = new ArcsSet(TEST_PRECISION);
        Arc     arc = new Arc(Math.PI, Geometry.TWO_PI, TEST_PRECISION);
        ArcsSet.Split split = set.split(arc);
        for (double alpha = 0.01; alpha < Geometry.TWO_PI; alpha += 0.01) {
            S1Point p = S1Point.of(alpha);
            if (alpha > Math.PI) {
                Assert.assertEquals(Location.OUTSIDE, split.getPlus().checkPoint(p));
                Assert.assertEquals(Location.INSIDE,  split.getMinus().checkPoint(p));
            } else {
                Assert.assertEquals(Location.INSIDE,  split.getPlus().checkPoint(p));
                Assert.assertEquals(Location.OUTSIDE, split.getMinus().checkPoint(p));
            }
        }

        S1Point zero = S1Point.of(0.0);
        Assert.assertEquals(Location.BOUNDARY,  split.getPlus().checkPoint(zero));
        Assert.assertEquals(Location.BOUNDARY,  split.getMinus().checkPoint(zero));

        S1Point pi = S1Point.of(Math.PI);
        Assert.assertEquals(Location.BOUNDARY,  split.getPlus().checkPoint(pi));
        Assert.assertEquals(Location.BOUNDARY,  split.getMinus().checkPoint(pi));

    }

    @Test(expected=IllegalArgumentException.class)
    public void testWrongInterval() {
        new ArcsSet(1.2, 0.0, TEST_PRECISION);
    }

    @Test
    public void testFullEqualEndPoints() {
        ArcsSet set = new ArcsSet(1.0, 1.0, TEST_PRECISION);
        Assert.assertSame(TEST_PRECISION, set.getPrecision());
        Assert.assertEquals(Region.Location.INSIDE, set.checkPoint(S1Point.of(9.0)));
        for (double alpha = -20.0; alpha <= 20.0; alpha += 0.1) {
            Assert.assertEquals(Region.Location.INSIDE, set.checkPoint(S1Point.of(alpha)));
        }
        Assert.assertEquals(1, set.asList().size());
        Assert.assertEquals(0.0, set.asList().get(0).getInf(), TEST_EPS);
        Assert.assertEquals(2 * Math.PI, set.asList().get(0).getSup(), TEST_EPS);
        Assert.assertEquals(2 * Math.PI, set.getSize(), TEST_EPS);
    }

    @Test
    public void testFullCircle() {
        ArcsSet set = new ArcsSet(TEST_PRECISION);
        Assert.assertSame(TEST_PRECISION, set.getPrecision());
        Assert.assertEquals(Region.Location.INSIDE, set.checkPoint(S1Point.of(9.0)));
        for (double alpha = -20.0; alpha <= 20.0; alpha += 0.1) {
            Assert.assertEquals(Region.Location.INSIDE, set.checkPoint(S1Point.of(alpha)));
        }
        Assert.assertEquals(1, set.asList().size());
        Assert.assertEquals(0.0, set.asList().get(0).getInf(), TEST_EPS);
        Assert.assertEquals(2 * Math.PI, set.asList().get(0).getSup(), TEST_EPS);
        Assert.assertEquals(2 * Math.PI, set.getSize(), TEST_EPS);
    }

    @Test
    public void testEmpty() {
        ArcsSet empty = (ArcsSet) new RegionFactory<S1Point>().getComplement(new ArcsSet(TEST_PRECISION));
        Assert.assertSame(TEST_PRECISION, empty.getPrecision());
        Assert.assertEquals(0.0, empty.getSize(), TEST_EPS);
        Assert.assertTrue(empty.asList().isEmpty());
    }

    @Test
    public void testTiny() {
        ArcsSet tiny = new ArcsSet(0.0, Precision.SAFE_MIN / 2, TEST_PRECISION);
        Assert.assertSame(TEST_PRECISION, tiny.getPrecision());
        Assert.assertEquals(Precision.SAFE_MIN / 2, tiny.getSize(), TEST_EPS);
        Assert.assertEquals(1, tiny.asList().size());
        Assert.assertEquals(0.0, tiny.asList().get(0).getInf(), TEST_EPS);
        Assert.assertEquals(Precision.SAFE_MIN / 2, tiny.asList().get(0).getSup(), TEST_EPS);
    }

    @Test
    public void testSpecialConstruction() {
        List<SubHyperplane<S1Point>> boundary = new ArrayList<>();
        boundary.add(new LimitAngle(S1Point.of(0.0), false, TEST_PRECISION).wholeHyperplane());
        boundary.add(new LimitAngle(S1Point.of(Geometry.TWO_PI - 1.0e-11), true, TEST_PRECISION).wholeHyperplane());
        ArcsSet set = new ArcsSet(boundary, TEST_PRECISION);
        Assert.assertEquals(Geometry.TWO_PI, set.getSize(), TEST_EPS);
        Assert.assertSame(TEST_PRECISION, set.getPrecision());
        Assert.assertEquals(1, set.asList().size());
        Assert.assertEquals(0.0, set.asList().get(0).getInf(), TEST_EPS);
        Assert.assertEquals(Geometry.TWO_PI, set.asList().get(0).getSup(), TEST_EPS);
    }

    @Test
    public void testDifference() {

        ArcsSet a   = new ArcsSet(1.0, 6.0, TEST_PRECISION);
        List<Arc> aList = a.asList();
        Assert.assertEquals(1,   aList.size());
        Assert.assertEquals(1.0, aList.get(0).getInf(), TEST_EPS);
        Assert.assertEquals(6.0, aList.get(0).getSup(), TEST_EPS);

        ArcsSet b   = new ArcsSet(3.0, 5.0, TEST_PRECISION);
        List<Arc> bList = b.asList();
        Assert.assertEquals(1,   bList.size());
        Assert.assertEquals(3.0, bList.get(0).getInf(), TEST_EPS);
        Assert.assertEquals(5.0, bList.get(0).getSup(), TEST_EPS);

        ArcsSet aMb = (ArcsSet) new RegionFactory<S1Point>().difference(a, b);
        for (int k = -2; k < 3; ++k) {
            Assert.assertEquals(Location.OUTSIDE,  aMb.checkPoint(S1Point.of(0.0 + k * Geometry.TWO_PI)));
            Assert.assertEquals(Location.OUTSIDE,  aMb.checkPoint(S1Point.of(0.9 + k * Geometry.TWO_PI)));
            Assert.assertEquals(Location.BOUNDARY, aMb.checkPoint(S1Point.of(1.0 + k * Geometry.TWO_PI)));
            Assert.assertEquals(Location.INSIDE,   aMb.checkPoint(S1Point.of(1.1 + k * Geometry.TWO_PI)));
            Assert.assertEquals(Location.INSIDE,   aMb.checkPoint(S1Point.of(2.9 + k * Geometry.TWO_PI)));
            Assert.assertEquals(Location.BOUNDARY, aMb.checkPoint(S1Point.of(3.0 + k * Geometry.TWO_PI)));
            Assert.assertEquals(Location.OUTSIDE,  aMb.checkPoint(S1Point.of(3.1 + k * Geometry.TWO_PI)));
            Assert.assertEquals(Location.OUTSIDE,  aMb.checkPoint(S1Point.of(4.9 + k * Geometry.TWO_PI)));
            Assert.assertEquals(Location.BOUNDARY, aMb.checkPoint(S1Point.of(5.0 + k * Geometry.TWO_PI)));
            Assert.assertEquals(Location.INSIDE,   aMb.checkPoint(S1Point.of(5.1 + k * Geometry.TWO_PI)));
            Assert.assertEquals(Location.INSIDE,   aMb.checkPoint(S1Point.of(5.9 + k * Geometry.TWO_PI)));
            Assert.assertEquals(Location.BOUNDARY, aMb.checkPoint(S1Point.of(6.0 + k * Geometry.TWO_PI)));
            Assert.assertEquals(Location.OUTSIDE,  aMb.checkPoint(S1Point.of(6.1 + k * Geometry.TWO_PI)));
            Assert.assertEquals(Location.OUTSIDE,  aMb.checkPoint(S1Point.of(6.2 + k * Geometry.TWO_PI)));
        }

        List<Arc> aMbList = aMb.asList();
        Assert.assertEquals(2,   aMbList.size());
        Assert.assertEquals(1.0, aMbList.get(0).getInf(), TEST_EPS);
        Assert.assertEquals(3.0, aMbList.get(0).getSup(), TEST_EPS);
        Assert.assertEquals(5.0, aMbList.get(1).getInf(), TEST_EPS);
        Assert.assertEquals(6.0, aMbList.get(1).getSup(), TEST_EPS);


    }

    @Test
    public void testIntersection() {

        ArcsSet a   = (ArcsSet) new RegionFactory<S1Point>().union(new ArcsSet(1.0, 3.0, TEST_PRECISION),
                                                                    new ArcsSet(5.0, 6.0, TEST_PRECISION));
        List<Arc> aList = a.asList();
        Assert.assertEquals(2,   aList.size());
        Assert.assertEquals(1.0, aList.get(0).getInf(), TEST_EPS);
        Assert.assertEquals(3.0, aList.get(0).getSup(), TEST_EPS);
        Assert.assertEquals(5.0, aList.get(1).getInf(), TEST_EPS);
        Assert.assertEquals(6.0, aList.get(1).getSup(), TEST_EPS);

        ArcsSet b   = new ArcsSet(0.0, 5.5, TEST_PRECISION);
        List<Arc> bList = b.asList();
        Assert.assertEquals(1,   bList.size());
        Assert.assertEquals(0.0, bList.get(0).getInf(), TEST_EPS);
        Assert.assertEquals(5.5, bList.get(0).getSup(), TEST_EPS);

        ArcsSet aMb = (ArcsSet) new RegionFactory<S1Point>().intersection(a, b);
        for (int k = -2; k < 3; ++k) {
            Assert.assertEquals(Location.OUTSIDE,  aMb.checkPoint(S1Point.of(0.0 + k * Geometry.TWO_PI)));
            Assert.assertEquals(Location.BOUNDARY, aMb.checkPoint(S1Point.of(1.0 + k * Geometry.TWO_PI)));
            Assert.assertEquals(Location.INSIDE,   aMb.checkPoint(S1Point.of(1.1 + k * Geometry.TWO_PI)));
            Assert.assertEquals(Location.INSIDE,   aMb.checkPoint(S1Point.of(2.9 + k * Geometry.TWO_PI)));
            Assert.assertEquals(Location.BOUNDARY, aMb.checkPoint(S1Point.of(3.0 + k * Geometry.TWO_PI)));
            Assert.assertEquals(Location.OUTSIDE,  aMb.checkPoint(S1Point.of(3.1 + k * Geometry.TWO_PI)));
            Assert.assertEquals(Location.OUTSIDE,  aMb.checkPoint(S1Point.of(4.9 + k * Geometry.TWO_PI)));
            Assert.assertEquals(Location.BOUNDARY, aMb.checkPoint(S1Point.of(5.0 + k * Geometry.TWO_PI)));
            Assert.assertEquals(Location.INSIDE,   aMb.checkPoint(S1Point.of(5.1 + k * Geometry.TWO_PI)));
            Assert.assertEquals(Location.INSIDE,   aMb.checkPoint(S1Point.of(5.4 + k * Geometry.TWO_PI)));
            Assert.assertEquals(Location.BOUNDARY, aMb.checkPoint(S1Point.of(5.5 + k * Geometry.TWO_PI)));
            Assert.assertEquals(Location.OUTSIDE,  aMb.checkPoint(S1Point.of(5.6 + k * Geometry.TWO_PI)));
            Assert.assertEquals(Location.OUTSIDE,  aMb.checkPoint(S1Point.of(6.2 + k * Geometry.TWO_PI)));
        }

        List<Arc> aMbList = aMb.asList();
        Assert.assertEquals(2,   aMbList.size());
        Assert.assertEquals(1.0, aMbList.get(0).getInf(), TEST_EPS);
        Assert.assertEquals(3.0, aMbList.get(0).getSup(), TEST_EPS);
        Assert.assertEquals(5.0, aMbList.get(1).getInf(), TEST_EPS);
        Assert.assertEquals(5.5, aMbList.get(1).getSup(), TEST_EPS);


    }

    @Test
    public void testMultiple() {
        RegionFactory<S1Point> factory = new RegionFactory<>();
        ArcsSet set = (ArcsSet)
        factory.intersection(factory.union(factory.difference(new ArcsSet(1.0, 6.0, TEST_PRECISION),
                                                              new ArcsSet(3.0, 5.0, TEST_PRECISION)),
                                                              new ArcsSet(0.5, 2.0, TEST_PRECISION)),
                                                              new ArcsSet(0.0, 5.5, TEST_PRECISION));
        Assert.assertEquals(3.0, set.getSize(), TEST_EPS);
        Assert.assertEquals(Region.Location.OUTSIDE,  set.checkPoint(S1Point.of(0.0)));
        Assert.assertEquals(Region.Location.OUTSIDE,  set.checkPoint(S1Point.of(4.0)));
        Assert.assertEquals(Region.Location.OUTSIDE,  set.checkPoint(S1Point.of(6.0)));
        Assert.assertEquals(Region.Location.INSIDE,   set.checkPoint(S1Point.of(1.2)));
        Assert.assertEquals(Region.Location.INSIDE,   set.checkPoint(S1Point.of(5.25)));
        Assert.assertEquals(Region.Location.BOUNDARY, set.checkPoint(S1Point.of(0.5)));
        Assert.assertEquals(Region.Location.BOUNDARY, set.checkPoint(S1Point.of(3.0)));
        Assert.assertEquals(Region.Location.BOUNDARY, set.checkPoint(S1Point.of(5.0)));
        Assert.assertEquals(Region.Location.BOUNDARY, set.checkPoint(S1Point.of(5.5)));

        List<Arc> list = set.asList();
        Assert.assertEquals(2, list.size());
        Assert.assertEquals( 0.5, list.get(0).getInf(), TEST_EPS);
        Assert.assertEquals( 3.0, list.get(0).getSup(), TEST_EPS);
        Assert.assertEquals( 5.0, list.get(1).getInf(), TEST_EPS);
        Assert.assertEquals( 5.5, list.get(1).getSup(), TEST_EPS);

    }

    @Test
    public void testSinglePoint() {
        ArcsSet set = new ArcsSet(1.0, Math.nextAfter(1.0, Double.POSITIVE_INFINITY), TEST_PRECISION);
        Assert.assertEquals(2 * Precision.EPSILON, set.getSize(), Precision.SAFE_MIN);
    }

    @Test
    public void testIteration() {
        ArcsSet set = (ArcsSet) new RegionFactory<S1Point>().difference(new ArcsSet(1.0, 6.0, TEST_PRECISION),
                                                                         new ArcsSet(3.0, 5.0, TEST_PRECISION));
        Iterator<double[]> iterator = set.iterator();
        try {
            iterator.remove();
            Assert.fail("an exception should have been thrown");
        } catch (UnsupportedOperationException uoe) {
            // expected
        }

        Assert.assertTrue(iterator.hasNext());
        double[] a0 = iterator.next();
        Assert.assertEquals(2, a0.length);
        Assert.assertEquals(1.0, a0[0], TEST_EPS);
        Assert.assertEquals(3.0, a0[1], TEST_EPS);

        Assert.assertTrue(iterator.hasNext());
        double[] a1 = iterator.next();
        Assert.assertEquals(2, a1.length);
        Assert.assertEquals(5.0, a1[0], TEST_EPS);
        Assert.assertEquals(6.0, a1[1], TEST_EPS);

        Assert.assertFalse(iterator.hasNext());
        try {
            iterator.next();
            Assert.fail("an exception should have been thrown");
        } catch (NoSuchElementException nsee) {
            // expected
        }

    }

    @Test
    public void testEmptyTree() {
        Assert.assertEquals(Geometry.TWO_PI, new ArcsSet(new BSPTree_Old<S1Point>(Boolean.TRUE), TEST_PRECISION).getSize(), TEST_EPS);
    }

    @Test
    public void testShiftedAngles() {
        for (int k = -2; k < 3; ++k) {
            SubLimitAngle l1  = new LimitAngle(S1Point.of(1.0 + k * Geometry.TWO_PI), false, TEST_PRECISION).wholeHyperplane();
            SubLimitAngle l2  = new LimitAngle(S1Point.of(1.5 + k * Geometry.TWO_PI), true,  TEST_PRECISION).wholeHyperplane();
            ArcsSet set = new ArcsSet(new BSPTree_Old<>(l1,
                                                            new BSPTree_Old<S1Point>(Boolean.FALSE),
                                                            new BSPTree_Old<>(l2,
                                                                                  new BSPTree_Old<S1Point>(Boolean.FALSE),
                                                                                  new BSPTree_Old<S1Point>(Boolean.TRUE),
                                                                                  null),
                                                            null),
                    TEST_PRECISION);
            for (double alpha = 1.0e-6; alpha < Geometry.TWO_PI; alpha += 0.001) {
                if (alpha < 1 || alpha > 1.5) {
                    Assert.assertEquals(Location.OUTSIDE, set.checkPoint(S1Point.of(alpha)));
                } else {
                    Assert.assertEquals(Location.INSIDE,  set.checkPoint(S1Point.of(alpha)));
                }
            }
        }

    }

    @Test(expected=IllegalArgumentException.class)
    public void testInconsistentState() {
        SubLimitAngle l1 = new LimitAngle(S1Point.of(1.0), false, TEST_PRECISION).wholeHyperplane();
        SubLimitAngle l2 = new LimitAngle(S1Point.of(2.0), true,  TEST_PRECISION).wholeHyperplane();
        SubLimitAngle l3 = new LimitAngle(S1Point.of(3.0), false, TEST_PRECISION).wholeHyperplane();
        new ArcsSet(new BSPTree_Old<>(l1,
                                          new BSPTree_Old<S1Point>(Boolean.FALSE),
                                          new BSPTree_Old<>(l2,
                                                                new BSPTree_Old<>(l3,
                                                                                      new BSPTree_Old<S1Point>(Boolean.FALSE),
                                                                                      new BSPTree_Old<S1Point>(Boolean.TRUE),
                                                                                      null),
                                                                new BSPTree_Old<S1Point>(Boolean.TRUE),
                                                                null),
                                          null),
                TEST_PRECISION);
    }

    @Test
    public void testSide() {
        ArcsSet set = (ArcsSet) new RegionFactory<S1Point>().difference(new ArcsSet(1.0, 6.0, TEST_PRECISION),
                                                                         new ArcsSet(3.0, 5.0, TEST_PRECISION));
        for (int k = -2; k < 3; ++k) {
            Assert.assertEquals(Side.MINUS, set.split(new Arc(0.5 + k * Geometry.TWO_PI,
                                                              6.1 + k * Geometry.TWO_PI,
                                                              set.getPrecision())).getSide());
            Assert.assertEquals(Side.PLUS,  set.split(new Arc(0.5 + k * Geometry.TWO_PI,
                                                              0.8 + k * Geometry.TWO_PI,
                                                              set.getPrecision())).getSide());
            Assert.assertEquals(Side.PLUS,  set.split(new Arc(6.2 + k * Geometry.TWO_PI,
                                                              6.3 + k * Geometry.TWO_PI,
                                                              set.getPrecision())).getSide());
            Assert.assertEquals(Side.PLUS,  set.split(new Arc(3.5 + k * Geometry.TWO_PI,
                                                              4.5 + k * Geometry.TWO_PI,
                                                              set.getPrecision())).getSide());
            Assert.assertEquals(Side.BOTH,  set.split(new Arc(2.9 + k * Geometry.TWO_PI,
                                                              4.5 + k * Geometry.TWO_PI,
                                                              set.getPrecision())).getSide());
            Assert.assertEquals(Side.BOTH,  set.split(new Arc(0.5 + k * Geometry.TWO_PI,
                                                              1.2 + k * Geometry.TWO_PI,
                                                              set.getPrecision())).getSide());
            Assert.assertEquals(Side.BOTH,  set.split(new Arc(0.5 + k * Geometry.TWO_PI,
                                                              5.9 + k * Geometry.TWO_PI,
                                                              set.getPrecision())).getSide());
        }
    }

    @Test
    public void testSideEmbedded() {

        ArcsSet s35 = new ArcsSet(3.0, 5.0, TEST_PRECISION);
        ArcsSet s16 = new ArcsSet(1.0, 6.0, TEST_PRECISION);

        Assert.assertEquals(Side.BOTH,  s16.split(new Arc(3.0, 5.0, TEST_PRECISION)).getSide());
        Assert.assertEquals(Side.BOTH,  s16.split(new Arc(5.0, 3.0 + Geometry.TWO_PI, TEST_PRECISION)).getSide());
        Assert.assertEquals(Side.MINUS, s35.split(new Arc(1.0, 6.0, TEST_PRECISION)).getSide());
        Assert.assertEquals(Side.PLUS,  s35.split(new Arc(6.0, 1.0 + Geometry.TWO_PI, TEST_PRECISION)).getSide());

    }

    @Test
    public void testSideOverlapping() {
        ArcsSet s35 = new ArcsSet(3.0, 5.0, TEST_PRECISION);
        ArcsSet s46 = new ArcsSet(4.0, 6.0, TEST_PRECISION);

        Assert.assertEquals(Side.BOTH,  s46.split(new Arc(3.0, 5.0, TEST_PRECISION)).getSide());
        Assert.assertEquals(Side.BOTH,  s46.split(new Arc(5.0, 3.0 + Geometry.TWO_PI, TEST_PRECISION)).getSide());
        Assert.assertEquals(Side.BOTH, s35.split(new Arc(4.0, 6.0, TEST_PRECISION)).getSide());
        Assert.assertEquals(Side.BOTH,  s35.split(new Arc(6.0, 4.0 + Geometry.TWO_PI, TEST_PRECISION)).getSide());
    }

    @Test
    public void testSideHyper() {
        ArcsSet sub = (ArcsSet) new RegionFactory<S1Point>().getComplement(new ArcsSet(TEST_PRECISION));
        Assert.assertTrue(sub.isEmpty());
        Assert.assertEquals(Side.HYPER,  sub.split(new Arc(2.0, 3.0, TEST_PRECISION)).getSide());
    }

    @Test
    public void testSplitEmbedded() {

        ArcsSet s35 = new ArcsSet(3.0, 5.0, TEST_PRECISION);
        ArcsSet s16 = new ArcsSet(1.0, 6.0, TEST_PRECISION);

        ArcsSet.Split split1 = s16.split(new Arc(3.0, 5.0, TEST_PRECISION));
        ArcsSet split1Plus  = split1.getPlus();
        ArcsSet split1Minus = split1.getMinus();
        Assert.assertEquals(3.0, split1Plus.getSize(), TEST_EPS);
        Assert.assertEquals(2,   split1Plus.asList().size());
        Assert.assertEquals(1.0, split1Plus.asList().get(0).getInf(), TEST_EPS);
        Assert.assertEquals(3.0, split1Plus.asList().get(0).getSup(), TEST_EPS);
        Assert.assertEquals(5.0, split1Plus.asList().get(1).getInf(), TEST_EPS);
        Assert.assertEquals(6.0, split1Plus.asList().get(1).getSup(), TEST_EPS);
        Assert.assertEquals(2.0, split1Minus.getSize(), TEST_EPS);
        Assert.assertEquals(1,   split1Minus.asList().size());
        Assert.assertEquals(3.0, split1Minus.asList().get(0).getInf(), TEST_EPS);
        Assert.assertEquals(5.0, split1Minus.asList().get(0).getSup(), TEST_EPS);

        ArcsSet.Split split2 = s16.split(new Arc(5.0, 3.0 + Geometry.TWO_PI, TEST_PRECISION));
        ArcsSet split2Plus  = split2.getPlus();
        ArcsSet split2Minus = split2.getMinus();
        Assert.assertEquals(2.0, split2Plus.getSize(), TEST_EPS);
        Assert.assertEquals(1,   split2Plus.asList().size());
        Assert.assertEquals(3.0, split2Plus.asList().get(0).getInf(), TEST_EPS);
        Assert.assertEquals(5.0, split2Plus.asList().get(0).getSup(), TEST_EPS);
        Assert.assertEquals(3.0, split2Minus.getSize(), TEST_EPS);
        Assert.assertEquals(2,   split2Minus.asList().size());
        Assert.assertEquals(1.0, split2Minus.asList().get(0).getInf(), TEST_EPS);
        Assert.assertEquals(3.0, split2Minus.asList().get(0).getSup(), TEST_EPS);
        Assert.assertEquals(5.0, split2Minus.asList().get(1).getInf(), TEST_EPS);
        Assert.assertEquals(6.0, split2Minus.asList().get(1).getSup(), TEST_EPS);

        ArcsSet.Split split3 = s35.split(new Arc(1.0, 6.0, TEST_PRECISION));
        ArcsSet split3Plus  = split3.getPlus();
        ArcsSet split3Minus = split3.getMinus();
        Assert.assertNull(split3Plus);
        Assert.assertEquals(2.0, split3Minus.getSize(), TEST_EPS);
        Assert.assertEquals(1,   split3Minus.asList().size());
        Assert.assertEquals(3.0, split3Minus.asList().get(0).getInf(), TEST_EPS);
        Assert.assertEquals(5.0, split3Minus.asList().get(0).getSup(), TEST_EPS);

        ArcsSet.Split split4 = s35.split(new Arc(6.0, 1.0 + Geometry.TWO_PI, TEST_PRECISION));
        ArcsSet split4Plus  = split4.getPlus();
        ArcsSet split4Minus = split4.getMinus();
        Assert.assertEquals(2.0, split4Plus.getSize(), TEST_EPS);
        Assert.assertEquals(1,   split4Plus.asList().size());
        Assert.assertEquals(3.0, split4Plus.asList().get(0).getInf(), TEST_EPS);
        Assert.assertEquals(5.0, split4Plus.asList().get(0).getSup(), TEST_EPS);
        Assert.assertNull(split4Minus);

    }

    @Test
    public void testSplitOverlapping() {

        ArcsSet s35 = new ArcsSet(3.0, 5.0, TEST_PRECISION);
        ArcsSet s46 = new ArcsSet(4.0, 6.0, TEST_PRECISION);

        ArcsSet.Split split1 = s46.split(new Arc(3.0, 5.0, TEST_PRECISION));
        ArcsSet split1Plus  = split1.getPlus();
        ArcsSet split1Minus = split1.getMinus();
        Assert.assertEquals(1.0, split1Plus.getSize(), TEST_EPS);
        Assert.assertEquals(1,   split1Plus.asList().size());
        Assert.assertEquals(5.0, split1Plus.asList().get(0).getInf(), TEST_EPS);
        Assert.assertEquals(6.0, split1Plus.asList().get(0).getSup(), TEST_EPS);
        Assert.assertEquals(1.0, split1Minus.getSize(), TEST_EPS);
        Assert.assertEquals(1,   split1Minus.asList().size());
        Assert.assertEquals(4.0, split1Minus.asList().get(0).getInf(), TEST_EPS);
        Assert.assertEquals(5.0, split1Minus.asList().get(0).getSup(), TEST_EPS);

        ArcsSet.Split split2 = s46.split(new Arc(5.0, 3.0 + Geometry.TWO_PI, TEST_PRECISION));
        ArcsSet split2Plus  = split2.getPlus();
        ArcsSet split2Minus = split2.getMinus();
        Assert.assertEquals(1.0, split2Plus.getSize(), TEST_EPS);
        Assert.assertEquals(1,   split2Plus.asList().size());
        Assert.assertEquals(4.0, split2Plus.asList().get(0).getInf(), TEST_EPS);
        Assert.assertEquals(5.0, split2Plus.asList().get(0).getSup(), TEST_EPS);
        Assert.assertEquals(1.0, split2Minus.getSize(), TEST_EPS);
        Assert.assertEquals(1,   split2Minus.asList().size());
        Assert.assertEquals(5.0, split2Minus.asList().get(0).getInf(), TEST_EPS);
        Assert.assertEquals(6.0, split2Minus.asList().get(0).getSup(), TEST_EPS);

        ArcsSet.Split split3 = s35.split(new Arc(4.0, 6.0, TEST_PRECISION));
        ArcsSet split3Plus  = split3.getPlus();
        ArcsSet split3Minus = split3.getMinus();
        Assert.assertEquals(1.0, split3Plus.getSize(), TEST_EPS);
        Assert.assertEquals(1,   split3Plus.asList().size());
        Assert.assertEquals(3.0, split3Plus.asList().get(0).getInf(), TEST_EPS);
        Assert.assertEquals(4.0, split3Plus.asList().get(0).getSup(), TEST_EPS);
        Assert.assertEquals(1.0, split3Minus.getSize(), TEST_EPS);
        Assert.assertEquals(1,   split3Minus.asList().size());
        Assert.assertEquals(4.0, split3Minus.asList().get(0).getInf(), TEST_EPS);
        Assert.assertEquals(5.0, split3Minus.asList().get(0).getSup(), TEST_EPS);

        ArcsSet.Split split4 = s35.split(new Arc(6.0, 4.0 + Geometry.TWO_PI, TEST_PRECISION));
        ArcsSet split4Plus  = split4.getPlus();
        ArcsSet split4Minus = split4.getMinus();
        Assert.assertEquals(1.0, split4Plus.getSize(), TEST_EPS);
        Assert.assertEquals(1,   split4Plus.asList().size());
        Assert.assertEquals(4.0, split4Plus.asList().get(0).getInf(), TEST_EPS);
        Assert.assertEquals(5.0, split4Plus.asList().get(0).getSup(), TEST_EPS);
        Assert.assertEquals(1.0, split4Minus.getSize(), TEST_EPS);
        Assert.assertEquals(1,   split4Minus.asList().size());
        Assert.assertEquals(3.0, split4Minus.asList().get(0).getInf(), TEST_EPS);
        Assert.assertEquals(4.0, split4Minus.asList().get(0).getSup(), TEST_EPS);

    }

    @Test
    public void testFarSplit() {
        ArcsSet set = new ArcsSet(Math.PI, 2.5 * Math.PI, TEST_PRECISION);
        ArcsSet.Split split = set.split(new Arc(0.5 * Math.PI, 1.5 * Math.PI, TEST_PRECISION));
        ArcsSet splitPlus  = split.getPlus();
        ArcsSet splitMinus = split.getMinus();
        Assert.assertEquals(1,   splitMinus.asList().size());
        Assert.assertEquals(1.0 * Math.PI, splitMinus.asList().get(0).getInf(), TEST_EPS);
        Assert.assertEquals(1.5 * Math.PI, splitMinus.asList().get(0).getSup(), TEST_EPS);
        Assert.assertEquals(0.5 * Math.PI, splitMinus.getSize(), TEST_EPS);
        Assert.assertEquals(1,   splitPlus.asList().size());
        Assert.assertEquals(1.5 * Math.PI, splitPlus.asList().get(0).getInf(), TEST_EPS);
        Assert.assertEquals(2.5 * Math.PI, splitPlus.asList().get(0).getSup(), TEST_EPS);
        Assert.assertEquals(1.0 * Math.PI, splitPlus.getSize(), TEST_EPS);

    }

    @Test
    public void testSplitWithinEpsilon() {
        double epsilon = TEST_EPS;
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(epsilon);
        double a = 6.25;
        double b = a - 0.5 * epsilon;
        ArcsSet set = new ArcsSet(a - 1, a, precision);
        Arc arc = new Arc(b, b + Math.PI, precision);
        ArcsSet.Split split = set.split(arc);
        Assert.assertEquals(set.getSize(), split.getPlus().getSize(),  epsilon);
        Assert.assertNull(split.getMinus());
    }

    @Test
    public void testSideSplitConsistency() {
        double  epsilon = 1.0e-6;
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(epsilon);
        double  a       = 4.725;
        ArcsSet set     = new ArcsSet(a, a + 0.5, precision);
        Arc     arc     = new Arc(a + 0.5 * epsilon, a + 1, precision);
        ArcsSet.Split split = set.split(arc);
        Assert.assertNotNull(split.getMinus());
        Assert.assertNull(split.getPlus());
        Assert.assertEquals(Side.MINUS, set.split(arc).getSide());
    }

}
