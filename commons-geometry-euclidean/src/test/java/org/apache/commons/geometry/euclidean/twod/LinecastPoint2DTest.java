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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.junit.Assert;
import org.junit.Test;

public class LinecastPoint2DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final Line X_AXIS =
            Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);

    private static final Line Y_AXIS =
            Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_Y, TEST_PRECISION);

    @Test
    public void testProperties() {
        // arrange
        final Vector2D pt = Vector2D.of(1, 1);
        final Vector2D normal = Vector2D.Unit.PLUS_X;

        final LinecastPoint2D it = new LinecastPoint2D(pt, normal, X_AXIS);

        // act
        Assert.assertSame(pt, it.getPoint());
        Assert.assertSame(normal, it.getNormal());
        Assert.assertSame(X_AXIS, it.getLine());
        Assert.assertEquals(1.0, it.getAbscissa(), TEST_EPS);
    }

    @Test
    public void testAbscissaOrder() {
        // arrange
        final LinecastPoint2D a = new LinecastPoint2D(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X, X_AXIS);

        final LinecastPoint2D b = new LinecastPoint2D(Vector2D.of(2, 2), Vector2D.Unit.PLUS_X, X_AXIS);
        final LinecastPoint2D c = new LinecastPoint2D(Vector2D.of(-3, 3), Vector2D.Unit.PLUS_Y, X_AXIS);
        final LinecastPoint2D d = new LinecastPoint2D(Vector2D.of(1, 4), Vector2D.Unit.PLUS_Y, X_AXIS);
        final LinecastPoint2D e = new LinecastPoint2D(Vector2D.of(1, 4), Vector2D.Unit.PLUS_X, X_AXIS);

        // act/assert
        Assert.assertEquals(-1, LinecastPoint2D.ABSCISSA_ORDER.compare(a, b));
        Assert.assertEquals(1, LinecastPoint2D.ABSCISSA_ORDER.compare(a, c));
        Assert.assertEquals(1, LinecastPoint2D.ABSCISSA_ORDER.compare(a, d));
        Assert.assertEquals(0, LinecastPoint2D.ABSCISSA_ORDER.compare(a, e));
    }

    @Test
    public void testHashCode() {
        // arrange
        final LinecastPoint2D a = new LinecastPoint2D(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X, X_AXIS);
        final LinecastPoint2D b = new LinecastPoint2D(Vector2D.of(2, 2), Vector2D.Unit.PLUS_X, X_AXIS);
        final LinecastPoint2D c = new LinecastPoint2D(Vector2D.of(1, 1), Vector2D.Unit.PLUS_Y, X_AXIS);
        final LinecastPoint2D d = new LinecastPoint2D(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X, Y_AXIS);
        final LinecastPoint2D e = new LinecastPoint2D(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X, X_AXIS);

        // act
        final int hash = a.hashCode();

        // assert
        Assert.assertEquals(hash, a.hashCode());

        Assert.assertNotEquals(hash, b.hashCode());
        Assert.assertNotEquals(hash, c.hashCode());
        Assert.assertNotEquals(hash, d.hashCode());

        Assert.assertEquals(hash, e.hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        final LinecastPoint2D a = new LinecastPoint2D(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X, X_AXIS);
        final LinecastPoint2D b = new LinecastPoint2D(Vector2D.of(2, 2), Vector2D.Unit.PLUS_X, X_AXIS);
        final LinecastPoint2D c = new LinecastPoint2D(Vector2D.of(1, 1), Vector2D.Unit.PLUS_Y, X_AXIS);
        final LinecastPoint2D d = new LinecastPoint2D(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X, Y_AXIS);
        final LinecastPoint2D e = new LinecastPoint2D(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X, X_AXIS);

        // act/assert
        GeometryTestUtils.assertSimpleEqualsCases(a);

        Assert.assertNotEquals(a, b);
        Assert.assertNotEquals(a, c);
        Assert.assertNotEquals(a, d);

        Assert.assertEquals(a, e);
        Assert.assertEquals(e, a);
    }

    @Test
    public void testEq() {
        // arrange
        final DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);

        final Line line = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, precision);
        final Line otherLine = Lines.fromPointAndDirection(Vector2D.of(1e-4, 1e-4), Vector2D.Unit.PLUS_X, precision);

        final LinecastPoint2D a = new LinecastPoint2D(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X, line);

        final LinecastPoint2D b = new LinecastPoint2D(Vector2D.of(2, 2), Vector2D.Unit.PLUS_X, line);
        final LinecastPoint2D c = new LinecastPoint2D(Vector2D.of(1, 1), Vector2D.Unit.PLUS_Y, line);

        final LinecastPoint2D d = new LinecastPoint2D(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X, line);
        final LinecastPoint2D e = new LinecastPoint2D(
                Vector2D.of(1 + 1e-3, 1 + 1e-3), Vector2D.Unit.from(1 + 1e-3, 1e-3), otherLine);

        // act/assert
        Assert.assertTrue(a.eq(a, precision));

        Assert.assertFalse(a.eq(b, precision));
        Assert.assertFalse(a.eq(c, precision));

        Assert.assertTrue(a.eq(d, precision));
        Assert.assertTrue(a.eq(e, precision));
    }

    @Test
    public void testToString() {
        // arrange
        final LinecastPoint2D it = new LinecastPoint2D(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X, X_AXIS);

        // act
        final String str = it.toString();

        // assert
        GeometryTestUtils.assertContains("LinecastPoint2D[point= (1.0, 1.0), normal= (1.0, 0.0)", str);
    }

    @Test
    public void testSortAndFilter_empty() {
        // arrange
        final List<LinecastPoint2D> pts = new ArrayList<>();

        // act
        LinecastPoint2D.sortAndFilter(pts);

        // assert
        Assert.assertEquals(0, pts.size());
    }

    @Test
    public void testSortAndFilter() {
        // arrange
        final DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);

        final Line line = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, precision);
        final Line eqLine = Lines.fromPointAndDirection(Vector2D.of(1e-3, 1e-3), Vector2D.Unit.PLUS_X, precision);
        final Line diffLine = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_Y, precision);

        final LinecastPoint2D a = new LinecastPoint2D(Vector2D.ZERO, Vector2D.Unit.MINUS_Y, line);
        final LinecastPoint2D aDup1 = new LinecastPoint2D(Vector2D.of(1e-3, 0), Vector2D.Unit.MINUS_Y, line);
        final LinecastPoint2D aDup2 = new LinecastPoint2D(Vector2D.of(1e-3, 1e-3), Vector2D.of(1e-3, -1), eqLine);

        final LinecastPoint2D b = new LinecastPoint2D(Vector2D.ZERO, Vector2D.Unit.MINUS_X, diffLine);
        final LinecastPoint2D bDup = new LinecastPoint2D(Vector2D.of(-1e-3, 1e-4), Vector2D.Unit.MINUS_X, diffLine);

        final LinecastPoint2D c = new LinecastPoint2D(Vector2D.of(0.5, 0), Vector2D.Unit.MINUS_Y, line);

        final LinecastPoint2D d = new LinecastPoint2D(Vector2D.of(1, 0), Vector2D.Unit.MINUS_Y, line);

        final List<LinecastPoint2D> list = new ArrayList<>(Arrays.asList(d, aDup1, bDup, b, c, a, aDup2));

        // act
        LinecastPoint2D.sortAndFilter(list);

        // assert
        Assert.assertEquals(4, list.size());

        Assert.assertSame(b, list.get(0));
        Assert.assertSame(a, list.get(1));
        Assert.assertSame(c, list.get(2));
        Assert.assertSame(d, list.get(3));
    }
}
