/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.geometry.enclosing.euclidean.threed;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.enclosing.EnclosingBall;
import org.apache.commons.geometry.enclosing.SupportBallGenerator;
import org.apache.commons.geometry.enclosing.euclidean.twod.DiskGenerator;
import org.apache.commons.geometry.euclidean.threed.EmbeddingPlane;
import org.apache.commons.geometry.euclidean.threed.Planes;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.core.Precision;
import org.apache.commons.numbers.fraction.BigFraction;

/** Class generating a sphere from its support points.
 */
public class SphereGenerator implements SupportBallGenerator<Vector3D> {

    /** Precision context used to compare floating point numbers. */
    private final Precision.DoubleEquivalence precision;

    /** Construct a new instance with the given precision context.
     * @param precision precision context used to compare floating point numbers
     */
    public SphereGenerator(final Precision.DoubleEquivalence precision) {
        this.precision = precision;
    }

    /** {@inheritDoc} */
    @Override
    public EnclosingBall<Vector3D> ballOnSupport(final List<Vector3D> support) {
        if (support.isEmpty()) {
            return new EnclosingBall<>(Vector3D.ZERO, Double.NEGATIVE_INFINITY, Collections.emptyList());
        }
        final Vector3D vA = support.get(0);
        if (support.size() < 2) {
            return new EnclosingBall<>(vA, 0, Collections.singletonList(vA));
        }
        final Vector3D vB = support.get(1);
        if (support.size() < 3) {
            return new EnclosingBall<>(vA.lerp(vB, 0.5),
                                       0.5 * vA.distance(vB),
                                       Arrays.asList(vA, vB));
        }
        final Vector3D vC = support.get(2);
        if (support.size() < 4) {
            final EmbeddingPlane p = Planes.fromPoints(vA, vB, vC, precision).getEmbedding();
            final EnclosingBall<Vector2D> disk =
                    new DiskGenerator().ballOnSupport(Arrays.asList(p.toSubspace(vA),
                                                                    p.toSubspace(vB),
                                                                    p.toSubspace(vC)));

            // convert back to 3D
            return new EnclosingBall<>(p.toSpace(disk.getCenter()),
                                                            disk.getRadius(),
                                                            Arrays.asList(vA, vB, vC));

        }
        final Vector3D vD = support.get(3);
        // a sphere is 3D can be defined as:
        // (1)   (x - x_0)^2 + (y - y_0)^2 + (z - z_0)^2 = r^2
        // which can be written:
        // (2)   (x^2 + y^2 + z^2) - 2 x_0 x - 2 y_0 y - 2 z_0 z + (x_0^2 + y_0^2 + z_0^2 - r^2) = 0
        // or simply:
        // (3)   (x^2 + y^2 + z^2) + a x + b y + c z + d = 0
        // with sphere center coordinates -a/2, -b/2, -c/2
        // If the sphere exists, a b, c and d are a non zero solution to
        // [ (x^2  + y^2  + z^2)    x    y   z    1 ]   [ 1 ]   [ 0 ]
        // [ (xA^2 + yA^2 + zA^2)   xA   yA  zA   1 ]   [ a ]   [ 0 ]
        // [ (xB^2 + yB^2 + zB^2)   xB   yB  zB   1 ] * [ b ] = [ 0 ]
        // [ (xC^2 + yC^2 + zC^2)   xC   yC  zC   1 ]   [ c ]   [ 0 ]
        // [ (xD^2 + yD^2 + zD^2)   xD   yD  zD   1 ]   [ d ]   [ 0 ]
        // So the determinant of the matrix is zero. Computing this determinant
        // by expanding it using the minors m_ij of first row leads to
        // (4)   m_11 (x^2 + y^2 + z^2) - m_12 x + m_13 y - m_14 z + m_15 = 0
        // So by identifying equations (2) and (4) we get the coordinates
        // of center as:
        //      x_0 = +m_12 / (2 m_11)
        //      y_0 = -m_13 / (2 m_11)
        //      z_0 = +m_14 / (2 m_11)
        // Note that the minors m_11, m_12, m_13 and m_14 all have the last column
        // filled with 1.0, hence simplifying the computation
        final BigFraction[] c2 = {
            BigFraction.from(vA.getX()), BigFraction.from(vB.getX()),
            BigFraction.from(vC.getX()), BigFraction.from(vD.getX())
        };
        final BigFraction[] c3 = {
            BigFraction.from(vA.getY()), BigFraction.from(vB.getY()),
            BigFraction.from(vC.getY()), BigFraction.from(vD.getY())
        };
        final BigFraction[] c4 = {
            BigFraction.from(vA.getZ()), BigFraction.from(vB.getZ()),
            BigFraction.from(vC.getZ()), BigFraction.from(vD.getZ())
        };
        final BigFraction[] c1 = {
            c2[0].multiply(c2[0]).add(c3[0].multiply(c3[0])).add(c4[0].multiply(c4[0])),
            c2[1].multiply(c2[1]).add(c3[1].multiply(c3[1])).add(c4[1].multiply(c4[1])),
            c2[2].multiply(c2[2]).add(c3[2].multiply(c3[2])).add(c4[2].multiply(c4[2])),
            c2[3].multiply(c2[3]).add(c3[3].multiply(c3[3])).add(c4[3].multiply(c4[3]))
        };
        final BigFraction twoM11 = minor(c2, c3, c4).multiply(2);
        final BigFraction m12 = minor(c1, c3, c4);
        final BigFraction m13 = minor(c1, c2, c4);
        final BigFraction m14 = minor(c1, c2, c3);
        final BigFraction centerX = m12.divide(twoM11);
        final BigFraction centerY = m13.divide(twoM11).negate();
        final BigFraction centerZ = m14.divide(twoM11);
        final BigFraction dx = c2[0].subtract(centerX);
        final BigFraction dy = c3[0].subtract(centerY);
        final BigFraction dz = c4[0].subtract(centerZ);
        final BigFraction r2 = dx.multiply(dx).add(dy.multiply(dy)).add(dz.multiply(dz));
        return new EnclosingBall<>(Vector3D.of(centerX.doubleValue(),
                                               centerY.doubleValue(),
                                               centerZ.doubleValue()),
                                   Math.sqrt(r2.doubleValue()),
                                   Arrays.asList(vA, vB, vC, vD));
    }

    /** Compute a dimension 4 minor, when 4<sup>th</sup> column is known to be filled with 1.0.
     * @param c1 first column
     * @param c2 second column
     * @param c3 third column
     * @return value of the minor computed has an exact fraction
     */
    private BigFraction minor(final BigFraction[] c1, final BigFraction[] c2, final BigFraction[] c3) {
        return c2[0].multiply(c3[1]).multiply(c1[2].subtract(c1[3])).
            add(c2[0].multiply(c3[2]).multiply(c1[3].subtract(c1[1]))).
            add(c2[0].multiply(c3[3]).multiply(c1[1].subtract(c1[2]))).
            add(c2[1].multiply(c3[0]).multiply(c1[3].subtract(c1[2]))).
            add(c2[1].multiply(c3[2]).multiply(c1[0].subtract(c1[3]))).
            add(c2[1].multiply(c3[3]).multiply(c1[2].subtract(c1[0]))).
            add(c2[2].multiply(c3[0]).multiply(c1[1].subtract(c1[3]))).
            add(c2[2].multiply(c3[1]).multiply(c1[3].subtract(c1[0]))).
            add(c2[2].multiply(c3[3]).multiply(c1[0].subtract(c1[1]))).
            add(c2[3].multiply(c3[0]).multiply(c1[2].subtract(c1[1]))).
            add(c2[3].multiply(c3[1]).multiply(c1[0].subtract(c1[2]))).
            add(c2[3].multiply(c3[2]).multiply(c1[1].subtract(c1[0])));
    }
}
