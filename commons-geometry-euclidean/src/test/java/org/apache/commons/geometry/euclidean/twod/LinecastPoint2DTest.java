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
            Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);

    private static final Line Y_AXIS =
            Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_Y, TEST_PRECISION);

    @Test
    public void testProperties() {
        // arrange
        Vector2D pt = Vector2D.of(1, 1);
        Vector2D normal = Vector2D.Unit.PLUS_X;

        LinecastPoint2D it = new LinecastPoint2D(pt, normal, X_AXIS);

        // act
        Assert.assertSame(pt, it.getPoint());
        Assert.assertSame(normal, it.getNormal());
        Assert.assertSame(X_AXIS, it.getLine());
        Assert.assertEquals(1.0, it.getAbscissa(), TEST_EPS);
    }

    @Test
    public void testAbscissaOrder() {
        // arrange
        LinecastPoint2D a = new LinecastPoint2D(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X, X_AXIS);

        LinecastPoint2D b = new LinecastPoint2D(Vector2D.of(2, 2), Vector2D.Unit.PLUS_X, X_AXIS);
        LinecastPoint2D c = new LinecastPoint2D(Vector2D.of(-3, 3), Vector2D.Unit.PLUS_Y, X_AXIS);
        LinecastPoint2D d = new LinecastPoint2D(Vector2D.of(1, 4), Vector2D.Unit.PLUS_Y, X_AXIS);
        LinecastPoint2D e = new LinecastPoint2D(Vector2D.of(1, 4), Vector2D.Unit.PLUS_X, X_AXIS);

        // act/assert
        Assert.assertEquals(-1, LinecastPoint2D.ABSCISSA_ORDER.compare(a, b));
        Assert.assertEquals(1, LinecastPoint2D.ABSCISSA_ORDER.compare(a, c));
        Assert.assertEquals(1, LinecastPoint2D.ABSCISSA_ORDER.compare(a, d));
        Assert.assertEquals(0, LinecastPoint2D.ABSCISSA_ORDER.compare(a, e));
    }

    @Test
    public void testHashCode() {
        // arrange
        LinecastPoint2D a = new LinecastPoint2D(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X, X_AXIS);
        LinecastPoint2D b = new LinecastPoint2D(Vector2D.of(2, 2), Vector2D.Unit.PLUS_X, X_AXIS);
        LinecastPoint2D c = new LinecastPoint2D(Vector2D.of(1, 1), Vector2D.Unit.PLUS_Y, X_AXIS);
        LinecastPoint2D d = new LinecastPoint2D(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X, Y_AXIS);
        LinecastPoint2D e = new LinecastPoint2D(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X, X_AXIS);

        // act
        int hash = a.hashCode();

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
        LinecastPoint2D a = new LinecastPoint2D(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X, X_AXIS);
        LinecastPoint2D b = new LinecastPoint2D(Vector2D.of(2, 2), Vector2D.Unit.PLUS_X, X_AXIS);
        LinecastPoint2D c = new LinecastPoint2D(Vector2D.of(1, 1), Vector2D.Unit.PLUS_Y, X_AXIS);
        LinecastPoint2D d = new LinecastPoint2D(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X, Y_AXIS);
        LinecastPoint2D e = new LinecastPoint2D(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X, X_AXIS);

        // act/assert
        Assert.assertTrue(a.equals(a));

        Assert.assertFalse(a.equals(null));
        Assert.assertFalse(a.equals(new Object()));

        Assert.assertFalse(a.equals(b));
        Assert.assertFalse(a.equals(c));
        Assert.assertFalse(a.equals(d));

        Assert.assertTrue(a.equals(e));
        Assert.assertTrue(e.equals(a));
    }

    @Test
    public void testToString() {
        // arrange
        LinecastPoint2D it = new LinecastPoint2D(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X, X_AXIS);

        // act
        String str = it.toString();

        // assert
        GeometryTestUtils.assertContains("LinecastPoint2D[point= (1.0, 1.0), normal= (1.0, 0.0)", str);
    }
}
