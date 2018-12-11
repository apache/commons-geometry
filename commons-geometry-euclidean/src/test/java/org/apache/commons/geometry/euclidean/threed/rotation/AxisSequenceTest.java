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

import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.rotation.AxisSequence;
import org.junit.Assert;
import org.junit.Test;

public class AxisSequenceTest {

    @Test
    public void testAxes() {
        // act/assert
        for (AxisSequence axes : AxisSequence.values()) {
            checkAxes(axes);
        }
    }

    private void checkAxes(AxisSequence axes) {
        // make sure that the name of the enum value matches
        // the axes it contains
        String name = axes.toString();

        Vector3D a1 = getAxisForName(name.substring(0, 1));
        Vector3D a2 = getAxisForName(name.substring(1, 2));
        Vector3D a3 = getAxisForName(name.substring(2, 3));

        // assert
        Assert.assertEquals(a1, axes.getAxis1());
        Assert.assertEquals(a2, axes.getAxis2());
        Assert.assertEquals(a3, axes.getAxis3());

        Assert.assertArrayEquals(new Vector3D[] { a1, a2, a3 }, axes.toArray());
    }

    private Vector3D getAxisForName(String name) {
        if ("X".equals(name)) {
            return Vector3D.PLUS_X;
        }
        if ("Y".equals(name)) {
            return Vector3D.PLUS_Y;
        }
        if ("Z".equals(name)) {
            return Vector3D.PLUS_Z;
        }
        throw new IllegalArgumentException("Unknown axis: " + name);
    }
}
