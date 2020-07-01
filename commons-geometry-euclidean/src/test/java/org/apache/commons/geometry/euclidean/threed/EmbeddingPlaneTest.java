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
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.EmbeddingPlane.SubspaceTransform;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.geometry.euclidean.twod.AffineTransformMatrix2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class EmbeddingPlaneTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFromPointAndPlaneVectors() {
        // arrange
        final Vector3D pt = Vector3D.of(1, 2, 3);

        // act/assert
        checkPlane(Planes.fromPointAndPlaneVectors(pt, Vector3D.of(2, 0, 0), Vector3D.of(3, 0.1, 0),  TEST_PRECISION),
                Vector3D.of(0, 0, 3), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y);

        checkPlane(Planes.fromPointAndPlaneVectors(pt, Vector3D.of(2, 0, 0), Vector3D.of(3, -0.1, 0),  TEST_PRECISION),
                Vector3D.of(0, 0, 3), Vector3D.Unit.PLUS_X, Vector3D.Unit.MINUS_Y);

        checkPlane(Planes.fromPointAndPlaneVectors(pt, Vector3D.of(0, 0.1, 0), Vector3D.of(0, -3, 1),  TEST_PRECISION),
                Vector3D.of(1, 0, 0), Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_Z);
    }

    @Test
    public void testFromPointAndPlaneVectors_illegalArguments() {
        // arrange
        final Vector3D pt = Vector3D.of(1, 2, 3);

        // act/assert

        // identical vectors
        GeometryTestUtils.assertThrows(() -> {
            Planes.fromPointAndPlaneVectors(pt, Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), TEST_PRECISION);
        }, IllegalArgumentException.class);

        // zero vector
        GeometryTestUtils.assertThrows(() -> {
            Planes.fromPointAndPlaneVectors(pt, Vector3D.of(0, 0, 1), Vector3D.ZERO, TEST_PRECISION);
        }, IllegalArgumentException.class);

        // collinear vectors
        GeometryTestUtils.assertThrows(() -> {
            Planes.fromPointAndPlaneVectors(pt, Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 2), TEST_PRECISION);
        }, IllegalArgumentException.class);

        // collinear vectors - reversed
        GeometryTestUtils.assertThrows(() -> {
            Planes.fromPointAndPlaneVectors(pt, Vector3D.of(0, 0, 1), Vector3D.of(0, 0, -2), TEST_PRECISION);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testGetEmbedding() {
        // arrange
        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(Vector3D.ZERO,
                Vector3D.Unit.PLUS_X, Vector3D.Unit.MINUS_Y, TEST_PRECISION);

        // act/assert
        Assert.assertSame(plane, plane.getEmbedding());
    }

    @Test
    public void testPointAt() {
        // arrange
        final Vector3D pt = Vector3D.of(0, 0, 1);
        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(pt,
                Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_X, TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(pt, plane.pointAt(Vector2D.ZERO, 0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, plane.pointAt(Vector2D.ZERO, -1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, -1), plane.pointAt(Vector2D.ZERO, -2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 2), plane.pointAt(Vector2D.ZERO, 1), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 2, 1), plane.pointAt(Vector2D.of(2, 1), 0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(4, -3, 6), plane.pointAt(Vector2D.of(-3, -4), 5), TEST_EPS);
    }

    @Test
    public void testReverse() {
        // arrange
        final Vector3D pt = Vector3D.of(0, 0, 1);
        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(pt,
                Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        // act
        final EmbeddingPlane reversed = plane.reverse();

        // assert
        checkPlane(reversed, pt, Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_X);

        Assert.assertTrue(reversed.contains(Vector3D.of(1, 1, 1)));
        Assert.assertTrue(reversed.contains(Vector3D.of(-1, -1, 1)));
        Assert.assertFalse(reversed.contains(Vector3D.ZERO));

        Assert.assertEquals(1.0, reversed.offset(Vector3D.ZERO), TEST_EPS);
    }

    @Test
    public void testTransform_rotationAroundPoint() {
        // arrange
        final Vector3D pt = Vector3D.of(0, 0, 1);
        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(pt, Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_X, TEST_PRECISION);

        final AffineTransformMatrix3D mat = AffineTransformMatrix3D.createRotation(pt,
                QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, PlaneAngleRadians.PI_OVER_TWO));

        // act
        final EmbeddingPlane result = plane.transform(mat);

        // assert
        checkPlane(result, Vector3D.ZERO, Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_Z);
    }

    @Test
    public void testTransform_asymmetricScaling() {
        // arrange
        final Vector3D pt = Vector3D.of(0, 1, 0);
        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(pt, Vector3D.Unit.MINUS_Z, Vector3D.of(-1, 1, 0), TEST_PRECISION);

        final AffineTransformMatrix3D mat = AffineTransformMatrix3D.createScale(2, 1, 1);

        // act
        final EmbeddingPlane result = plane.transform(mat);

        // assert
        final Vector3D expectedU = Vector3D.Unit.MINUS_Z;
        final Vector3D expectedV = Vector3D.Unit.of(-2, 1, 0);
        final Vector3D expectedNormal = Vector3D.Unit.of(1, 2, 0);

        final Vector3D transformedPt = mat.apply(plane.getOrigin());
        final Vector3D expectedOrigin = transformedPt.project(expectedNormal);

        checkPlane(result, expectedOrigin, expectedU, expectedV);

        Assert.assertTrue(result.contains(transformedPt));
        Assert.assertFalse(plane.contains(transformedPt));
    }

    @Test
    public void testTransform_negateOneComponent() {
        // arrange
        final Vector3D pt = Vector3D.of(0, 0, 1);
        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(pt, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.from(v -> Vector3D.of(-v.getX(), v.getY(), v.getZ()));

        // act
        final EmbeddingPlane result = plane.transform(transform);

        // assert
        checkPlane(result, Vector3D.of(0, 0, 1), Vector3D.Unit.MINUS_X, Vector3D.Unit.PLUS_Y);
    }

    @Test
    public void testTransform_negateTwoComponents() {
        // arrange
        final Vector3D pt = Vector3D.of(0, 0, 1);
        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(pt, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.from(v -> Vector3D.of(-v.getX(), -v.getY(), v.getZ()));

        // act
        final EmbeddingPlane result = plane.transform(transform);

        // assert
        checkPlane(result, Vector3D.of(0, 0, 1), Vector3D.Unit.MINUS_X, Vector3D.Unit.MINUS_Y);
    }

    @Test
    public void testTransform_negateAllComponents() {
        // arrange
        final Vector3D pt = Vector3D.of(0, 0, 1);
        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(pt,
                Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.from(Vector3D::negate);

        // act
        final EmbeddingPlane result = plane.transform(transform);

        // assert
        checkPlane(result, Vector3D.of(0, 0, -1), Vector3D.Unit.MINUS_X, Vector3D.Unit.MINUS_Y);
    }

    @Test
    public void testTransform_consistency() {
        // arrange
        final Vector3D pt = Vector3D.of(1, 2, 3);
        final Vector3D normal = Vector3D.Unit.from(1, 1, 1);
        final Vector3D u = normal.orthogonal(Vector3D.Unit.PLUS_X);
        final Vector3D v = normal.cross(u).normalize();

        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(pt, u, v, TEST_PRECISION);

        final Vector3D p1 = plane.project(Vector3D.of(4, 5, 6));
        final Vector3D p2 = plane.project(Vector3D.of(-7, -8, -9));
        final Vector3D p3 = plane.project(Vector3D.of(10, -11, 12));

        final Vector3D notOnPlane1 = plane.getOrigin().add(plane.getNormal());
        final Vector3D notOnPlane2 = plane.getOrigin().subtract(plane.getNormal());

        EuclideanTestUtils.permuteSkipZero(-4, 4, 1, (a, b, c) -> {
            final AffineTransformMatrix3D t = AffineTransformMatrix3D.identity()
                    .rotate(Vector3D.of(-1, 2, 3),
                            QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_X, 0.3 * a))
                    .scale(Math.max(a, 1), Math.max(b, 1), Math.max(c, 1))
                    .translate(c, b, a);

            // act
            final EmbeddingPlane result = plane.transform(t);

            // assert
            Vector3D expectedNormal = t.normalTransform().apply(plane.getNormal()).normalize();
            if (!t.preservesOrientation()) {
                expectedNormal = expectedNormal.negate();
            }

            EuclideanTestUtils.assertCoordinatesEqual(expectedNormal, result.getNormal(), TEST_EPS);

            Assert.assertTrue(result.contains(t.apply(p1)));
            Assert.assertTrue(result.contains(t.apply(p2)));
            Assert.assertTrue(result.contains(t.apply(p3)));

            Assert.assertFalse(result.contains(t.apply(notOnPlane1)));
            Assert.assertFalse(result.contains(t.apply(notOnPlane2)));
        });
    }

    @Test
    public void testRotate() {
        // arrange
        final Vector3D p1 = Vector3D.of(1.2, 3.4, -5.8);
        final Vector3D p2 = Vector3D.of(3.4, -5.8, 1.2);
        final Vector3D p3 = Vector3D.of(-2.0, 4.3, 0.7);
        EmbeddingPlane plane  = Planes.fromPoints(p1, p2, p3, TEST_PRECISION).getEmbedding();
        final Vector3D oldNormal = plane.getNormal();

        // act/assert
        plane = plane.rotate(p2, QuaternionRotation.fromAxisAngle(p2.subtract(p1), 1.7));
        Assert.assertTrue(plane.contains(p1));
        Assert.assertTrue(plane.contains(p2));
        Assert.assertFalse(plane.contains(p3));

        plane = plane.rotate(p2, QuaternionRotation.fromAxisAngle(oldNormal, 0.1));
        Assert.assertFalse(plane.contains(p1));
        Assert.assertTrue(plane.contains(p2));
        Assert.assertFalse(plane.contains(p3));

        plane = plane.rotate(p1, QuaternionRotation.fromAxisAngle(oldNormal, 0.1));
        Assert.assertFalse(plane.contains(p1));
        Assert.assertFalse(plane.contains(p2));
        Assert.assertFalse(plane.contains(p3));
    }

    @Test
    public void testTranslate() {
        // arrange
        final Vector3D p1 = Vector3D.of(1.2, 3.4, -5.8);
        final Vector3D p2 = Vector3D.of(3.4, -5.8, 1.2);
        final Vector3D p3 = Vector3D.of(-2.0, 4.3, 0.7);
        EmbeddingPlane plane  = Planes.fromPoints(p1, p2, p3, TEST_PRECISION).getEmbedding();

        // act/assert
        plane = plane.translate(Vector3D.linearCombination(2.0, plane.getU(), -1.5, plane.getV()));
        Assert.assertTrue(plane.contains(p1));
        Assert.assertTrue(plane.contains(p2));
        Assert.assertTrue(plane.contains(p3));

        plane = plane.translate(Vector3D.linearCombination(-1.2, plane.getNormal()));
        Assert.assertFalse(plane.contains(p1));
        Assert.assertFalse(plane.contains(p2));
        Assert.assertFalse(plane.contains(p3));

        plane = plane.translate(Vector3D.linearCombination(+1.2, plane.getNormal()));
        Assert.assertTrue(plane.contains(p1));
        Assert.assertTrue(plane.contains(p2));
        Assert.assertTrue(plane.contains(p3));
    }

    @Test
    public void testSubspaceTransform() {
        // arrange
        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1),
                Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        // act/assert
        checkSubspaceTransform(plane.subspaceTransform(AffineTransformMatrix3D.createScale(2, 3, 4)),
                Vector3D.of(0, 0, 4), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y,
                Vector3D.of(0, 0, 4), Vector3D.of(2, 0, 4), Vector3D.of(0, 3, 4));

        checkSubspaceTransform(plane.subspaceTransform(AffineTransformMatrix3D.createTranslation(2, 3, 4)),
                Vector3D.of(0, 0, 5), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y,
                Vector3D.of(2, 3, 5), Vector3D.of(3, 3, 5), Vector3D.of(2, 4, 5));

        checkSubspaceTransform(plane.subspaceTransform(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, PlaneAngleRadians.PI_OVER_TWO)),
                Vector3D.of(1, 0, 0), Vector3D.Unit.MINUS_Z, Vector3D.Unit.PLUS_Y,
                Vector3D.of(1, 0, 0), Vector3D.of(1, 0, -1), Vector3D.of(1, 1, 0));
    }

    private void checkSubspaceTransform(final SubspaceTransform st,
                                        final Vector3D origin, final Vector3D u, final Vector3D v,
                                        final Vector3D tOrigin, final Vector3D tU, final Vector3D tV) {

        final EmbeddingPlane plane = st.getPlane();
        final AffineTransformMatrix2D transform = st.getTransform();

        checkPlane(plane, origin, u, v);

        EuclideanTestUtils.assertCoordinatesEqual(tOrigin, plane.toSpace(transform.apply(Vector2D.ZERO)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(tU, plane.toSpace(transform.apply(Vector2D.Unit.PLUS_X)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(tV, plane.toSpace(transform.apply(Vector2D.Unit.PLUS_Y)), TEST_EPS);
    }

    @Test
    public void testSubspaceTransform_transformsPointsCorrectly() {
        // arrange
        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(Vector3D.of(1, 2, 3),
                Vector3D.of(-1, -1, 1), Vector3D.of(-1, 1, 1), TEST_PRECISION);

        EuclideanTestUtils.permuteSkipZero(-2, 2, 0.5, (a, b, c) -> {
            // create a somewhat complicate transform to try to hit all of the edge cases
            final AffineTransformMatrix3D transform = AffineTransformMatrix3D.createTranslation(Vector3D.of(a, b, c))
                    .rotate(QuaternionRotation.fromAxisAngle(Vector3D.of(b, c, a), PlaneAngleRadians.PI * c))
                    .scale(0.1, 4, 8);

            // act
            final SubspaceTransform st = plane.subspaceTransform(transform);

            // assert
            EuclideanTestUtils.permute(-5, 5, 1, (x, y) -> {
                final Vector2D subPt = Vector2D.of(x, y);
                final Vector3D expected = transform.apply(plane.toSpace(subPt));
                final Vector3D actual = st.getPlane().toSpace(
                        st.getTransform().apply(subPt));

                EuclideanTestUtils.assertCoordinatesEqual(expected, actual, TEST_EPS);
            });
        });
    }

    @Test
    public void testEq_stdAndEmbedding() {
        // arrange
        final Plane stdPlane = Planes.fromPointAndNormal(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        final EmbeddingPlane embeddingPlane = Planes.fromPointAndPlaneVectors(Vector3D.of(1, 1, 1),
                Vector3D.of(1, 1, 0), Vector3D.of(-1, 1, 0), TEST_PRECISION);

        final EmbeddingPlane nonEqEmbeddingPlane = Planes.fromPointAndPlaneVectors(Vector3D.of(1, 1, 1),
                Vector3D.of(1, 1, 1), Vector3D.of(-1, 1, 1), TEST_PRECISION);

        // act/assert
        Assert.assertTrue(stdPlane.eq(embeddingPlane, TEST_PRECISION));
        Assert.assertTrue(embeddingPlane.eq(stdPlane, TEST_PRECISION));

        Assert.assertFalse(stdPlane.eq(nonEqEmbeddingPlane, TEST_PRECISION));
        Assert.assertFalse(nonEqEmbeddingPlane.eq(stdPlane, TEST_PRECISION));
    }

    @Test
    public void testSimilarOrientation_stdAndEmbedding() {
        // arrange
        final Plane stdPlane = Planes.fromPointAndNormal(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        final EmbeddingPlane embeddingPlane = Planes.fromPointAndPlaneVectors(Vector3D.of(1, 1, 1),
                Vector3D.of(1, 1, 1), Vector3D.of(-1, 1, 1), TEST_PRECISION);

        final EmbeddingPlane nonSimilarEmbeddingPlane = Planes.fromPointAndPlaneVectors(Vector3D.of(1, 1, 1),
                Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act/assert
        Assert.assertTrue(stdPlane.similarOrientation(embeddingPlane));
        Assert.assertTrue(embeddingPlane.similarOrientation(stdPlane));

        Assert.assertFalse(stdPlane.similarOrientation(nonSimilarEmbeddingPlane));
        Assert.assertFalse(nonSimilarEmbeddingPlane.similarOrientation(stdPlane));
    }

    @Test
    public void testHashCode() {
        // arrange
        final Vector3D pt = Vector3D.of(1, 2, 3);
        final Vector3D u = Vector3D.Unit.PLUS_X;
        final Vector3D v = Vector3D.Unit.PLUS_Y;

        final EmbeddingPlane a = Planes.fromPointAndPlaneVectors(pt, u, v, TEST_PRECISION);
        final EmbeddingPlane b = Planes.fromPointAndPlaneVectors(Vector3D.of(1, 2, 4), u, v, TEST_PRECISION);
        final EmbeddingPlane c = Planes.fromPointAndPlaneVectors(pt, Vector3D.of(1, 1, 0), v, TEST_PRECISION);
        final EmbeddingPlane d = Planes.fromPointAndPlaneVectors(pt, u, Vector3D.Unit.MINUS_Y, TEST_PRECISION);
        final EmbeddingPlane e = Planes.fromPointAndPlaneVectors(pt, u, v, new EpsilonDoublePrecisionContext(1e-8));
        final EmbeddingPlane f = Planes.fromPointAndPlaneVectors(pt, u, v, TEST_PRECISION);

        // act/assert
        final int hash = a.hashCode();

        Assert.assertEquals(hash, a.hashCode());

        Assert.assertNotEquals(hash, b.hashCode());
        Assert.assertNotEquals(hash, c.hashCode());
        Assert.assertNotEquals(hash, d.hashCode());
        Assert.assertNotEquals(hash, e.hashCode());

        Assert.assertEquals(hash, f.hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        final Vector3D pt = Vector3D.of(1, 2, 3);
        final Vector3D u = Vector3D.Unit.PLUS_X;
        final Vector3D v = Vector3D.Unit.PLUS_Y;

        final EmbeddingPlane a = Planes.fromPointAndPlaneVectors(pt, u, v, TEST_PRECISION);
        final EmbeddingPlane b = Planes.fromPointAndPlaneVectors(Vector3D.of(1, 2, 4), u, v, TEST_PRECISION);
        final EmbeddingPlane c = Planes.fromPointAndPlaneVectors(pt, Vector3D.Unit.MINUS_X, v, TEST_PRECISION);
        final EmbeddingPlane d = Planes.fromPointAndPlaneVectors(pt, u, Vector3D.Unit.MINUS_Y, TEST_PRECISION);
        final EmbeddingPlane e = Planes.fromPointAndPlaneVectors(pt, u, v, new EpsilonDoublePrecisionContext(1e-8));
        final EmbeddingPlane f = Planes.fromPointAndPlaneVectors(pt, u, v, TEST_PRECISION);

        final Plane stdPlane = Planes.fromPointAndNormal(pt, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act/assert
        Assert.assertEquals(a, a);

        Assert.assertFalse(a.equals(null));
        Assert.assertFalse(a.equals(new Object()));

        Assert.assertNotEquals(a, b);
        Assert.assertNotEquals(a, c);
        Assert.assertNotEquals(a, d);
        Assert.assertNotEquals(a, e);

        Assert.assertEquals(a, f);
        Assert.assertEquals(f, a);

        Assert.assertNotEquals(a, stdPlane);
    }

    @Test
    public void testToString() {
        // arrange
        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(Vector3D.ZERO,
                Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        // act
        final String str = plane.toString();

        // assert
        Assert.assertTrue(str.startsWith("EmbeddingPlane["));
        Assert.assertTrue(str.matches(".*origin= \\(0(\\.0)?, 0(\\.0)?\\, 0(\\.0)?\\).*"));
        Assert.assertTrue(str.matches(".*u= \\(1(\\.0)?, 0(\\.0)?\\, 0(\\.0)?\\).*"));
        Assert.assertTrue(str.matches(".*v= \\(0(\\.0)?, 1(\\.0)?\\, 0(\\.0)?\\).*"));
        Assert.assertTrue(str.matches(".*w= \\(0(\\.0)?, 0(\\.0)?\\, 1(\\.0)?\\).*"));
    }

    private static void checkPlane(final EmbeddingPlane plane, final Vector3D origin, Vector3D u, Vector3D v) {
        u = u.normalize();
        v = v.normalize();
        final Vector3D w = u.cross(v);

        EuclideanTestUtils.assertCoordinatesEqual(origin, plane.getOrigin(), TEST_EPS);
        Assert.assertTrue(plane.contains(origin));

        EuclideanTestUtils.assertCoordinatesEqual(u, plane.getU(), TEST_EPS);
        Assert.assertEquals(1.0, plane.getU().norm(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(v, plane.getV(), TEST_EPS);
        Assert.assertEquals(1.0, plane.getV().norm(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(w, plane.getW(), TEST_EPS);
        Assert.assertEquals(1.0, plane.getW().norm(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(w, plane.getNormal(), TEST_EPS);
        Assert.assertEquals(1.0, plane.getNormal().norm(), TEST_EPS);

        final double offset = plane.getOriginOffset();
        Assert.assertEquals(Vector3D.ZERO.distance(plane.getOrigin()), Math.abs(offset), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(origin, plane.getNormal().multiply(-offset), TEST_EPS);
    }
}
