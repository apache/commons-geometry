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
package org.apache.commons.geometry.euclidean.threed.rotation;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AxisAngleSequenceTest {

    @Test
    public void testConstructor() {
        // act
        final AxisAngleSequence seq = new AxisAngleSequence(AxisReferenceFrame.RELATIVE, AxisSequence.XYZ, 1, 2, 3);

        // assert
        Assertions.assertEquals(AxisReferenceFrame.RELATIVE, seq.getReferenceFrame());
        Assertions.assertEquals(AxisSequence.XYZ, seq.getAxisSequence());
        Assertions.assertEquals(1, seq.getAngle1(), 0.0);
        Assertions.assertEquals(2, seq.getAngle2(), 0.0);
        Assertions.assertEquals(3, seq.getAngle3(), 0.0);
    }

    @Test
    public void testGetAngles() {
        // arrange
        final AxisAngleSequence seq = new AxisAngleSequence(AxisReferenceFrame.RELATIVE, AxisSequence.XYZ, 1, 2, 3);

        // act
        final double[] angles = seq.getAngles();

        // assert
        Assertions.assertArrayEquals(new double[] {1, 2, 3}, angles, 0.0);
    }

    @Test
    public void testHashCode() {
        // arrange
        final AxisAngleSequence seq = new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.XYZ, 1, 2, 3);

        // act/assert
        Assertions.assertNotEquals(seq.hashCode(), new AxisAngleSequence(AxisReferenceFrame.RELATIVE, AxisSequence.XYZ, 1, 2, 3).hashCode());
        Assertions.assertNotEquals(seq.hashCode(), new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.ZYX, 1, 2, 3).hashCode());
        Assertions.assertNotEquals(seq.hashCode(), new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.XYZ, 9, 2, 3).hashCode());
        Assertions.assertNotEquals(seq.hashCode(), new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.XYZ, 1, 9, 3).hashCode());
        Assertions.assertNotEquals(seq.hashCode(), new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.XYZ, 1, 2, 9).hashCode());

        Assertions.assertEquals(seq.hashCode(), new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.XYZ, 1, 2, 3).hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        final AxisAngleSequence seq = new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.XYZ, 1, 2, 3);

        // act/assert
        GeometryTestUtils.assertSimpleEqualsCases(seq);

        Assertions.assertNotEquals(seq, new AxisAngleSequence(AxisReferenceFrame.RELATIVE, AxisSequence.XYZ, 1, 2, 3));
        Assertions.assertNotEquals(seq, new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.ZYX, 1, 2, 3));
        Assertions.assertNotEquals(seq, new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.XYZ, 9, 2, 3));
        Assertions.assertNotEquals(seq, new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.XYZ, 1, 9, 3));
        Assertions.assertNotEquals(seq, new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.XYZ, 1, 2, 9));

        Assertions.assertEquals(seq, new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.XYZ, 1, 2, 3));
    }

    @Test
    public void testEqualsAndHashCode_signedZeroConsistency() {
        // arrange
        final AxisAngleSequence a = new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.XYZ,
                0.0, -0.0, 0.0);
        final AxisAngleSequence b = new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.XYZ,
                -0.0, 0.0, -0.0);
        final AxisAngleSequence c = new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.XYZ,
                0.0, -0.0, 0.0);
        final AxisAngleSequence d = new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.XYZ,
                -0.0, 0.0, -0.0);

        // act/assert
        Assertions.assertFalse(a.equals(b));
        Assertions.assertNotEquals(a.hashCode(), b.hashCode());

        Assertions.assertTrue(a.equals(c));
        Assertions.assertEquals(a.hashCode(), c.hashCode());

        Assertions.assertTrue(b.equals(d));
        Assertions.assertEquals(b.hashCode(), d.hashCode());
    }

    @Test
    public void testToString() {
        // arrange
        final AxisAngleSequence seq = new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.XYZ, 1, 2, 3);

        // act
        final String str = seq.toString();

        // assert
        Assertions.assertTrue(str.contains("ABSOLUTE"));
        Assertions.assertTrue(str.contains("XYZ"));
        Assertions.assertTrue(str.contains("1"));
        Assertions.assertTrue(str.contains("2"));
        Assertions.assertTrue(str.contains("3"));
    }

    @Test
    public void testCreateRelative() {
        // act
        final AxisAngleSequence seq = AxisAngleSequence.createRelative(AxisSequence.XYZ, 1, 2, 3);

        // assert
        Assertions.assertEquals(AxisReferenceFrame.RELATIVE, seq.getReferenceFrame());
        Assertions.assertEquals(AxisSequence.XYZ, seq.getAxisSequence());
        Assertions.assertEquals(1, seq.getAngle1(), 0.0);
        Assertions.assertEquals(2, seq.getAngle2(), 0.0);
        Assertions.assertEquals(3, seq.getAngle3(), 0.0);
    }

    @Test
    public void testCreateAbsolute() {
        // act
        final AxisAngleSequence seq = AxisAngleSequence.createAbsolute(AxisSequence.XYZ, 1, 2, 3);

        // assert
        Assertions.assertEquals(AxisReferenceFrame.ABSOLUTE, seq.getReferenceFrame());
        Assertions.assertEquals(AxisSequence.XYZ, seq.getAxisSequence());
        Assertions.assertEquals(1, seq.getAngle1(), 0.0);
        Assertions.assertEquals(2, seq.getAngle2(), 0.0);
        Assertions.assertEquals(3, seq.getAngle3(), 0.0);
    }
}
