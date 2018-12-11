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

import org.junit.Assert;
import org.junit.Test;

public class AxisAngleSequenceTest {

    @Test
    public void testConstructor() {
        // act
        AxisAngleSequence seq = new AxisAngleSequence(AxisReferenceFrame.RELATIVE, AxisSequence.XYZ, 1, 2, 3);

        // assert
        Assert.assertEquals(AxisReferenceFrame.RELATIVE, seq.getReferenceFrame());
        Assert.assertEquals(AxisSequence.XYZ, seq.getAxisSequence());
        Assert.assertEquals(1, seq.getAngle1(), 0.0);
        Assert.assertEquals(2, seq.getAngle2(), 0.0);
        Assert.assertEquals(3, seq.getAngle3(), 0.0);
    }

    @Test
    public void testGetAngles() {
        // arrange
        AxisAngleSequence seq = new AxisAngleSequence(AxisReferenceFrame.RELATIVE, AxisSequence.XYZ, 1, 2, 3);

        // act
        double[] angles = seq.getAngles();

        // assert
        Assert.assertArrayEquals(new double[] { 1, 2, 3 }, angles, 0.0);
    }

    @Test
    public void testHashCode() {
        // arrange
        AxisAngleSequence seq = new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.XYZ, 1, 2, 3);

        // act/assert
        Assert.assertNotEquals(seq.hashCode(), new AxisAngleSequence(AxisReferenceFrame.RELATIVE, AxisSequence.XYZ, 1, 2, 3).hashCode());
        Assert.assertNotEquals(seq.hashCode(), new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.ZYX, 1, 2, 3).hashCode());
        Assert.assertNotEquals(seq.hashCode(), new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.XYZ, 9, 2, 3).hashCode());
        Assert.assertNotEquals(seq.hashCode(), new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.XYZ, 1, 9, 3).hashCode());
        Assert.assertNotEquals(seq.hashCode(), new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.XYZ, 1, 2, 9).hashCode());

        Assert.assertEquals(seq.hashCode(), new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.XYZ, 1, 2, 3).hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        AxisAngleSequence seq = new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.XYZ, 1, 2, 3);

        // act/assert
        Assert.assertFalse(seq.equals(null));
        Assert.assertFalse(seq.equals(new Object()));

        Assert.assertFalse(seq.equals(new AxisAngleSequence(AxisReferenceFrame.RELATIVE, AxisSequence.XYZ, 1, 2, 3)));
        Assert.assertFalse(seq.equals(new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.ZYX, 1, 2, 3)));
        Assert.assertFalse(seq.equals(new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.XYZ, 9, 2, 3)));
        Assert.assertFalse(seq.equals(new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.XYZ, 1, 9, 3)));
        Assert.assertFalse(seq.equals(new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.XYZ, 1, 2, 9)));

        Assert.assertTrue(seq.equals(seq));
        Assert.assertTrue(seq.equals(new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.XYZ, 1, 2, 3)));
    }

    @Test
    public void testToString() {
        // arrange
        AxisAngleSequence seq = new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, AxisSequence.XYZ, 1, 2, 3);

        // act
        String str = seq.toString();

        // assert
        Assert.assertTrue(str.contains("ABSOLUTE"));
        Assert.assertTrue(str.contains("XYZ"));
        Assert.assertTrue(str.contains("1"));
        Assert.assertTrue(str.contains("2"));
        Assert.assertTrue(str.contains("3"));
    }

    @Test
    public void testCreateRelative() {
        // act
        AxisAngleSequence seq = AxisAngleSequence.createRelative(AxisSequence.XYZ, 1, 2, 3);

        // assert
        Assert.assertEquals(AxisReferenceFrame.RELATIVE, seq.getReferenceFrame());
        Assert.assertEquals(AxisSequence.XYZ, seq.getAxisSequence());
        Assert.assertEquals(1, seq.getAngle1(), 0.0);
        Assert.assertEquals(2, seq.getAngle2(), 0.0);
        Assert.assertEquals(3, seq.getAngle3(), 0.0);
    }

    @Test
    public void testCreateAbsolute() {
        // act
        AxisAngleSequence seq = AxisAngleSequence.createAbsolute(AxisSequence.XYZ, 1, 2, 3);

        // assert
        Assert.assertEquals(AxisReferenceFrame.ABSOLUTE, seq.getReferenceFrame());
        Assert.assertEquals(AxisSequence.XYZ, seq.getAxisSequence());
        Assert.assertEquals(1, seq.getAngle1(), 0.0);
        Assert.assertEquals(2, seq.getAngle2(), 0.0);
        Assert.assertEquals(3, seq.getAngle3(), 0.0);
    }
}
