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
package org.apache.commons.geometry.enclosing;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.enclosing.euclidean.threed.WelzlEncloser3D;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** This class contains code listed as examples in the user guide and other documentation.
 * If any portion of this code changes, the corresponding examples in the documentation <em>must</em> be updated.
 */
public class DocumentationExamplesTest {

    private static final double TEST_EPS = 1e-10;

    @Test
    public void testWelzlEncloser3DExample() {
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-10);

        final List<Vector3D> points = Arrays.asList(
                    Vector3D.of(0, 0, 1),
                    Vector3D.of(0.75, 0, 1),
                    Vector3D.of(2, 0, 1),
                    Vector3D.of(1, 0, 2)
                );

        // compute the enclosing ball
        final WelzlEncloser3D encloser = new WelzlEncloser3D(precision);

        final EnclosingBall<Vector3D> sphere = encloser.enclose(points);

        // check the generated ball
        final Vector3D center = sphere.getCenter(); // (1, 0, 1)
        final double radius = sphere.getRadius(); // 1.0
        final boolean containsCenter = sphere.contains(center); // true
        final boolean containsOrigin = sphere.contains(Vector3D.ZERO); // false

        // ----------
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 0, 1), center, TEST_EPS);
        Assertions.assertEquals(1.0, radius, TEST_EPS);
        Assertions.assertTrue(containsCenter);
        Assertions.assertFalse(containsOrigin);
    }
}
