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
package org.apache.commons.geometry.examples.jmh.euclidean;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ModifiedPointMap3DImplTest {

    private static final Precision.DoubleEquivalence PRECISION =
            Precision.doubleEquivalenceOfEpsilon(1e-6);

    private static final Integer VAL = Integer.valueOf(1);

    @Test
    public void testLine() {
        // arrange
        final ModifiedPointMap3DImpl<Integer> map = new ModifiedPointMap3DImpl<>(PRECISION);

        final int count = 10_000;
        final List<Vector3D> pts = new ArrayList<>(count);

        final Vector3D base = Vector3D.of(2.0, 1.0, 0.5);
        for (int i = 0; i < count; ++i) {
            pts.add(base.multiply(i));
        }

        // act
        final long start = System.currentTimeMillis();

        for (final Vector3D pt : pts) {
            map.put(pt, VAL);
        }

        System.out.println("done in " + (System.currentTimeMillis() - start) + "ms");
        map.printDepth();

        // assert
        Assertions.assertEquals(count, map.size());
    }
}
