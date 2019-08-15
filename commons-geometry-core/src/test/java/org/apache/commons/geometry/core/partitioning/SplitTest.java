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
package org.apache.commons.geometry.core.partitioning;

import org.junit.Test;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.junit.Assert;

public class SplitTest {

    @Test
    public void testProperties() {
        // arrange
        Object a = new Object();
        Object b = new Object();

        // act
        Split<Object> split = new Split<>(a, b);

        // assert
        Assert.assertSame(a, split.getMinus());
        Assert.assertSame(b,  split.getPlus());
    }

    @Test
    public void testGetLocation() {
        // arrange
        Object a = new Object();
        Object b = new Object();

        // act/assert
        Assert.assertEquals(SplitLocation.NEITHER, new Split<Object>(null, null).getLocation());
        Assert.assertEquals(SplitLocation.MINUS, new Split<Object>(a, null).getLocation());
        Assert.assertEquals(SplitLocation.PLUS, new Split<Object>(null, b).getLocation());
        Assert.assertEquals(SplitLocation.BOTH, new Split<Object>(a, b).getLocation());
    }

    @Test
    public void testToString() {
        // arrange
        Split<String> split = new Split<>("a", "b");

        // act
        String str = split.toString();

        // assert
        Assert.assertEquals("Split[location= BOTH, minus= a, plus= b]", str);
    }
}
