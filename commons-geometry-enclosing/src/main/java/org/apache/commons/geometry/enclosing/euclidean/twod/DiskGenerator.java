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
package org.apache.commons.geometry.enclosing.euclidean.twod;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.enclosing.EnclosingBall;
import org.apache.commons.geometry.enclosing.SupportBallGenerator;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.fraction.BigFraction;

/** Class generating a disk from its support points.
 */
public class DiskGenerator implements SupportBallGenerator<Vector2D> {

    /** Create an instance. */
    public DiskGenerator() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public EnclosingBall<Vector2D> ballOnSupport(final List<Vector2D> support) {
        if (support.isEmpty()) {
            return new EnclosingBall<>(Vector2D.ZERO, Double.NEGATIVE_INFINITY, Collections.emptyList());
        }
        final Vector2D vA = support.get(0);
        if (support.size() < 2) {
            return new EnclosingBall<>(vA, 0, Collections.singletonList(vA));
        }
        final Vector2D vB = support.get(1);
        if (support.size() < 3) {
            return new EnclosingBall<>(vA.lerp(vB, 0.5),
                                       0.5 * vA.distance(vB),
                                       Arrays.asList(vA, vB));
        }
        final Vector2D vC = support.get(2);
        // a disk is 2D can be defined as:
        // (1)   (x - x_0)^2 + (y - y_0)^2 = r^2
        // which can be written:
        // (2)   (x^2 + y^2) - 2 x_0 x - 2 y_0 y + (x_0^2 + y_0^2 - r^2) = 0
        // or simply:
        // (3)   (x^2 + y^2) + a x + b y + c = 0
        // with disk center coordinates -a/2, -b/2
        // If the disk exists, a, b and c are a non-zero solution to
        // [ (x^2  + y^2 )   x    y   1 ]   [ 1 ]   [ 0 ]
        // [ (xA^2 + yA^2)   xA   yA  1 ]   [ a ]   [ 0 ]
        // [ (xB^2 + yB^2)   xB   yB  1 ] * [ b ] = [ 0 ]
        // [ (xC^2 + yC^2)   xC   yC  1 ]   [ c ]   [ 0 ]
        // So the determinant of the matrix is zero. Computing this determinant
        // by expanding it using the minors m_ij of first row leads to
        // (4)   m_11 (x^2 + y^2) - m_12 x + m_13 y - m_14 = 0
        // So by identifying equations (2) and (4) we get the coordinates
        // of center as:
        //      x_0 = +m_12 / (2 m_11)
        //      y_0 = -m_13 / (2 m_11)
        // Note that the minors m_11, m_12 and m_13 all have the last column
        // filled with 1.0, hence simplifying the computation
        final BigFraction[] c2 = {
            BigFraction.from(vA.getX()), BigFraction.from(vB.getX()), BigFraction.from(vC.getX())
        };
        final BigFraction[] c3 = {
            BigFraction.from(vA.getY()), BigFraction.from(vB.getY()), BigFraction.from(vC.getY())
        };
        final BigFraction[] c1 = {
            c2[0].multiply(c2[0]).add(c3[0].multiply(c3[0])),
            c2[1].multiply(c2[1]).add(c3[1].multiply(c3[1])),
            c2[2].multiply(c2[2]).add(c3[2].multiply(c3[2]))
        };
        final BigFraction twoM11 = minor(c2, c3).multiply(2);
        final BigFraction m12 = minor(c1, c3);
        final BigFraction m13 = minor(c1, c2);
        final BigFraction centerX = m12.divide(twoM11);
        final BigFraction centerY = m13.divide(twoM11).negate();
        final BigFraction dx = c2[0].subtract(centerX);
        final BigFraction dy = c3[0].subtract(centerY);
        final BigFraction r2 = dx.multiply(dx).add(dy.multiply(dy));
        return new EnclosingBall<>(Vector2D.of(centerX.doubleValue(),
                                               centerY.doubleValue()),
                                   Math.sqrt(r2.doubleValue()),
                                   Arrays.asList(vA, vB, vC));
    }

    /** Compute a dimension 3 minor, when 3<sup>d</sup> column is known to be filled with 1.0.
     * @param c1 first column
     * @param c2 second column
     * @return value of the minor computed has an exact fraction
     */
    private BigFraction minor(final BigFraction[] c1, final BigFraction[] c2) {
        return c2[0].multiply(c1[2].subtract(c1[1])).
            add(c2[1].multiply(c1[0].subtract(c1[2]))).
            add(c2[2].multiply(c1[1].subtract(c1[0])));
    }
}
