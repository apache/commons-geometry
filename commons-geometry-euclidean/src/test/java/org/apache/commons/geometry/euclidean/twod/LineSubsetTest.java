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
package org.apache.commons.geometry.euclidean.twod;

import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Test;

class LineSubsetTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    @Test
    void testToSubspaceAndToSpace() {
        // arrange
        final LineSubset subset = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION)
                .span();

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 1), subset.toSpace(Vector1D.of(2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(2), subset.toSubspace(Vector2D.of(2, 1)), TEST_EPS);
    }
}
