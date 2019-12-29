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

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.junit.Assert;
import org.junit.Test;

public class LinecastPoint3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final Line3D X_AXIS =
            Line3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION);

    private static final Line3D Y_AXIS =
            Line3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

    @Test
    public void testProperties() {
        // arrange
        Vector3D pt = Vector3D.of(1, 1, 1);
        Vector3D normal = Vector3D.Unit.MINUS_X;

        LinecastPoint3D it = new LinecastPoint3D(pt, normal, X_AXIS);

        // act
        Assert.assertSame(pt, it.getPoint());
        Assert.assertSame(normal, it.getNormal());
        Assert.assertSame(X_AXIS, it.getLine());
        Assert.assertEquals(1.0, it.getAbscissa(), TEST_EPS);
    }

    @Test
    public void testCompareTo() {
        // arrange
        LinecastPoint3D a = new LinecastPoint3D(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X, X_AXIS);

        LinecastPoint3D b = new LinecastPoint3D(Vector3D.of(2, 2, 2), Vector3D.Unit.PLUS_X, X_AXIS);
        LinecastPoint3D c = new LinecastPoint3D(Vector3D.of(-3, 3, 3), Vector3D.Unit.PLUS_X, X_AXIS);
        LinecastPoint3D d = new LinecastPoint3D(Vector3D.of(1, 4, 4), Vector3D.Unit.PLUS_Y, X_AXIS);
        LinecastPoint3D e = new LinecastPoint3D(Vector3D.of(1, 4, 4), Vector3D.Unit.PLUS_X, X_AXIS);

        // act/assert
        Assert.assertEquals(-1, LinecastPoint3D.ABSCISSA_ORDER.compare(a, b));
        Assert.assertEquals(1, LinecastPoint3D.ABSCISSA_ORDER.compare(a, c));
        Assert.assertEquals(1, LinecastPoint3D.ABSCISSA_ORDER.compare(a, d));
        Assert.assertEquals(0, LinecastPoint3D.ABSCISSA_ORDER.compare(a, e));
    }

    @Test
    public void testHashCode() {
        // arrange
        LinecastPoint3D a = new LinecastPoint3D(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X, X_AXIS);
        LinecastPoint3D b = new LinecastPoint3D(Vector3D.of(2, 2, 2), Vector3D.Unit.PLUS_X, X_AXIS);
        LinecastPoint3D c = new LinecastPoint3D(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_Y, X_AXIS);
        LinecastPoint3D d = new LinecastPoint3D(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X, Y_AXIS);
        LinecastPoint3D e = new LinecastPoint3D(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X, X_AXIS);

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
        LinecastPoint3D a = new LinecastPoint3D(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X, X_AXIS);
        LinecastPoint3D b = new LinecastPoint3D(Vector3D.of(2, 2, 2), Vector3D.Unit.PLUS_X, X_AXIS);
        LinecastPoint3D c = new LinecastPoint3D(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_Y, X_AXIS);
        LinecastPoint3D d = new LinecastPoint3D(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X, Y_AXIS);
        LinecastPoint3D e = new LinecastPoint3D(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X, X_AXIS);

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
        LinecastPoint3D it = new LinecastPoint3D(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X, X_AXIS);

        // act
        String str = it.toString();

        // assert
        GeometryTestUtils.assertContains("LinecastPoint3D[point= (1.0, 1.0, 1.0), normal= (1.0, 0.0, 0.0)", str);
    }
}
