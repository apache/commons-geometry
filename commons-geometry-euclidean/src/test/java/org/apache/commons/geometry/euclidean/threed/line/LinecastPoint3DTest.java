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
package org.apache.commons.geometry.euclidean.threed.line;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LinecastPoint3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION = Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    private static final Line3D X_AXIS = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.Unit.PLUS_X,
            TEST_PRECISION);

    private static final Line3D Y_AXIS = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.Unit.PLUS_Y,
            TEST_PRECISION);

    @Test
    public void testProperties() {
        // arrange
        final Vector3D pt = Vector3D.of(1, 1, 1);
        final Vector3D normal = Vector3D.Unit.MINUS_X;

        final LinecastPoint3D it = new LinecastPoint3D(pt, normal, X_AXIS);

        // act
        Assertions.assertSame(pt, it.getPoint());
        Assertions.assertSame(normal, it.getNormal());
        Assertions.assertSame(X_AXIS, it.getLine());
        Assertions.assertEquals(1.0, it.getAbscissa(), TEST_EPS);
    }

    @Test
    public void testCompareTo() {
        // arrange
        final LinecastPoint3D a = new LinecastPoint3D(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X, X_AXIS);

        final LinecastPoint3D b = new LinecastPoint3D(Vector3D.of(2, 2, 2), Vector3D.Unit.PLUS_X, X_AXIS);
        final LinecastPoint3D c = new LinecastPoint3D(Vector3D.of(-3, 3, 3), Vector3D.Unit.PLUS_X, X_AXIS);
        final LinecastPoint3D d = new LinecastPoint3D(Vector3D.of(1, 4, 4), Vector3D.Unit.PLUS_Y, X_AXIS);
        final LinecastPoint3D e = new LinecastPoint3D(Vector3D.of(1, 4, 4), Vector3D.Unit.PLUS_X, X_AXIS);

        // act/assert
        Assertions.assertEquals(-1, LinecastPoint3D.ABSCISSA_ORDER.compare(a, b));
        Assertions.assertEquals(1, LinecastPoint3D.ABSCISSA_ORDER.compare(a, c));
        Assertions.assertEquals(1, LinecastPoint3D.ABSCISSA_ORDER.compare(a, d));
        Assertions.assertEquals(0, LinecastPoint3D.ABSCISSA_ORDER.compare(a, e));
    }

    @Test
    public void testHashCode() {
        // arrange
        final LinecastPoint3D a = new LinecastPoint3D(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X, X_AXIS);
        final LinecastPoint3D b = new LinecastPoint3D(Vector3D.of(2, 2, 2), Vector3D.Unit.PLUS_X, X_AXIS);
        final LinecastPoint3D c = new LinecastPoint3D(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_Y, X_AXIS);
        final LinecastPoint3D d = new LinecastPoint3D(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X, Y_AXIS);
        final LinecastPoint3D e = new LinecastPoint3D(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X, X_AXIS);

        // act
        final int hash = a.hashCode();

        // assert
        Assertions.assertEquals(hash, a.hashCode());

        Assertions.assertNotEquals(hash, b.hashCode());
        Assertions.assertNotEquals(hash, c.hashCode());
        Assertions.assertNotEquals(hash, d.hashCode());

        Assertions.assertEquals(hash, e.hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        final LinecastPoint3D a = new LinecastPoint3D(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X, X_AXIS);
        final LinecastPoint3D b = new LinecastPoint3D(Vector3D.of(2, 2, 2), Vector3D.Unit.PLUS_X, X_AXIS);
        final LinecastPoint3D c = new LinecastPoint3D(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_Y, X_AXIS);
        final LinecastPoint3D d = new LinecastPoint3D(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X, Y_AXIS);
        final LinecastPoint3D e = new LinecastPoint3D(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X, X_AXIS);

        // act/assert
        GeometryTestUtils.assertSimpleEqualsCases(a);

        Assertions.assertNotEquals(a, b);
        Assertions.assertNotEquals(a, c);
        Assertions.assertNotEquals(a, d);

        Assertions.assertEquals(a, e);
        Assertions.assertEquals(e, a);
    }

    @Test
    public void testEq() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-2);

        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.Unit.PLUS_X, precision);
        final Line3D otherLine = Lines3D.fromPointAndDirection(Vector3D.of(1e-4, 1e-4, 1e-4), Vector3D.Unit.PLUS_X,
                precision);

        final LinecastPoint3D a = new LinecastPoint3D(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X, line);

        final LinecastPoint3D b = new LinecastPoint3D(Vector3D.of(2, 2, 2), Vector3D.Unit.PLUS_X, line);
        final LinecastPoint3D c = new LinecastPoint3D(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_Y, line);

        final LinecastPoint3D d = new LinecastPoint3D(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X, line);
        final LinecastPoint3D e = new LinecastPoint3D(Vector3D.of(1 + 1e-3, 1 + 1e-3, 1 + 1e-3),
                Vector3D.Unit.from(1 + 1e-3, 1e-3, 1e-3), otherLine);

        // act/assert
        Assertions.assertTrue(a.eq(a, precision));

        Assertions.assertFalse(a.eq(b, precision));
        Assertions.assertFalse(a.eq(c, precision));

        Assertions.assertTrue(a.eq(d, precision));
        Assertions.assertTrue(a.eq(e, precision));
    }

    @Test
    public void testToString() {
        // arrange
        final LinecastPoint3D it = new LinecastPoint3D(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X, X_AXIS);

        // act
        final String str = it.toString();

        // assert
        GeometryTestUtils.assertContains("LinecastPoint3D[point= (1.0, 1.0, 1.0), normal= (1.0, 0.0, 0.0)", str);
    }

    @Test
    public void testSortAndFilter_empty() {
        // arrange
        final List<LinecastPoint3D> pts = new ArrayList<>();

        // act
        LinecastPoint3D.sortAndFilter(pts);

        // assert
        Assertions.assertEquals(0, pts.size());
    }

    @Test
    public void testSortAndFilter() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-2);

        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.Unit.PLUS_X, precision);
        final Line3D eqLine = Lines3D.fromPointAndDirection(Vector3D.of(1e-3, 1e-3, 1e-3), Vector3D.Unit.PLUS_X,
                precision);
        final Line3D diffLine = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.Unit.PLUS_Y, precision);

        final LinecastPoint3D a = new LinecastPoint3D(Vector3D.ZERO, Vector3D.Unit.MINUS_Y, line);
        final LinecastPoint3D aDup1 = new LinecastPoint3D(Vector3D.of(1e-3, 0, 0), Vector3D.Unit.MINUS_Y, line);
        final LinecastPoint3D aDup2 = new LinecastPoint3D(Vector3D.of(1e-3, 1e-3, 1e-3), Vector3D.of(1e-3, -1, 0),
                eqLine);

        final LinecastPoint3D b = new LinecastPoint3D(Vector3D.ZERO, Vector3D.Unit.MINUS_X, diffLine);
        final LinecastPoint3D bDup = new LinecastPoint3D(Vector3D.of(-1e-3, 1e-4, 1e-4), Vector3D.Unit.MINUS_X,
                diffLine);

        final LinecastPoint3D c = new LinecastPoint3D(Vector3D.of(0.5, 0, 0), Vector3D.Unit.MINUS_Y, line);

        final LinecastPoint3D d = new LinecastPoint3D(Vector3D.of(1, 0, 0), Vector3D.Unit.MINUS_Y, line);

        final List<LinecastPoint3D> list = new ArrayList<>(Arrays.asList(d, aDup1, bDup, b, c, a, aDup2));

        // act
        LinecastPoint3D.sortAndFilter(list);

        // assert
        Assertions.assertEquals(4, list.size());

        Assertions.assertSame(b, list.get(0));
        Assertions.assertSame(a, list.get(1));
        Assertions.assertSame(c, list.get(2));
        Assertions.assertSame(d, list.get(3));
    }
}
