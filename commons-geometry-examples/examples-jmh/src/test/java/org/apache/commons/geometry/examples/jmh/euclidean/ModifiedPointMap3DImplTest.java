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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Test;

class ModifiedPointMap3DImplTest {

    private static final double EPS = 1e-6;

    private static final Precision.DoubleEquivalence PRECISION =
            Precision.doubleEquivalenceOfEpsilon(EPS);

    private static final Integer VAL = Integer.valueOf(1);

    @Test
    public void testLine() {
        // arrange
        final int count = 10_000;
        final List<Vector3D> pts = new ArrayList<>(count);
        final List<Vector3D> otherPts = new ArrayList<>(count);

        final Vector3D base = Vector3D.of(2.0, 1.0, 0.5);
        final Vector3D eps = Vector3D.of(EPS, -EPS, EPS).multiply(2);
        for (int i = 0; i < count; ++i) {
            final Vector3D pt = base.multiply(i);

            pts.add(pt);

            otherPts.add(pt.add(eps));
        }

        // act
        runPutAndGet(32, pts, otherPts);
//        int bestEntryCount = Integer.MAX_VALUE;
//        int bestMillis = Integer.MAX_VALUE;
//
//        for (int i = 50; i <= 500; i += 20) {
//            int millis = runPutAndGet(i, pts, otherPts);
//
//            System.out.println(i + ": " + millis);
//
//            if (millis < bestMillis) {
//                bestMillis = millis;
//                bestEntryCount = i;
//            }
//        }
//
//        System.out.println();
//        System.out.println("Best entry count: " + bestEntryCount + " (" + bestMillis + "ms)");
    }

    private int runPutAndGet(
            final int maxEntriesPerNode,
            final List<Vector3D> pts,
            final List<Vector3D> otherPts) {
        final ModifiedPointMap3DImpl<Integer> map = new ModifiedPointMap3DImpl<>(maxEntriesPerNode, PRECISION);

        final long start = System.currentTimeMillis();

        for (final Vector3D pt : pts) {
            map.put(pt, VAL);
        }

        for (final Vector3D pt : pts) {
            assertEquals(VAL, map.get(pt));
        }

        for (final Vector3D pt : otherPts) {
            assertNull(map.get(pt));
        }

        map.printListCreateCounts();

        return (int) (System.currentTimeMillis() - start);
    }
}
