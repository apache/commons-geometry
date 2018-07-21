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
package org.apache.commons.geometry.core;

import org.junit.Assert;
import org.junit.Test;

public class GeometryTest {

    @Test
    public void testConstants() {
        // arrange
        double eps = 0.0;

        // act/assert
        Assert.assertEquals(Math.PI, Geometry.PI, eps);
        Assert.assertEquals(-Math.PI, Geometry.MINUS_PI, eps);

        Assert.assertEquals(2.0 * Math.PI, Geometry.TWO_PI, eps);
        Assert.assertEquals(-2.0 * Math.PI, Geometry.MINUS_TWO_PI, eps);

        Assert.assertEquals(Math.PI / 2.0, Geometry.HALF_PI, 0.0);
        Assert.assertEquals(-Math.PI / 2.0, Geometry.MINUS_HALF_PI, eps);

        Assert.assertEquals((3.0 * Math.PI) / 2.0, Geometry.THREE_HALVES_PI, eps);
    }

    @Test
    public void testConstants_trigEval() {
        // arrange
        double eps = 1e-15;

        // act/assert
        Assert.assertEquals(0.0, Math.sin(Geometry.PI), eps);
        Assert.assertEquals(-1.0, Math.cos(Geometry.PI), eps);

        Assert.assertEquals(0.0, Math.sin(Geometry.MINUS_PI), eps);
        Assert.assertEquals(-1.0, Math.cos(Geometry.MINUS_PI), eps);

        Assert.assertEquals(0.0, Math.sin(Geometry.TWO_PI), eps);
        Assert.assertEquals(1.0, Math.cos(Geometry.TWO_PI), eps);

        Assert.assertEquals(0.0, Math.sin(Geometry.MINUS_TWO_PI), eps);
        Assert.assertEquals(1.0, Math.cos(Geometry.MINUS_TWO_PI), eps);

        Assert.assertEquals(1.0, Math.sin(Geometry.HALF_PI), eps);
        Assert.assertEquals(0.0, Math.cos(Geometry.HALF_PI), eps);

        Assert.assertEquals(-1.0, Math.sin(Geometry.MINUS_HALF_PI), eps);
        Assert.assertEquals(0.0, Math.cos(Geometry.MINUS_HALF_PI), eps);
    }
}
