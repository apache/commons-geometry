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

package org.apache.commons.geometry.spherical;

import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.core.GeometryTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class SphericalCoordinatesTest {

    @Test
    public void testCoordinatesStoC() {
        double piO2 = 0.5 * Math.PI;
        SphericalCoordinates sc1 = new SphericalCoordinates(2.0, 0, piO2);
        Assert.assertEquals(0, sc1.getCartesian().distance(Vector3D.of(2, 0, 0)), 1.0e-10);
        SphericalCoordinates sc2 = new SphericalCoordinates(2.0, piO2, piO2);
        Assert.assertEquals(0, sc2.getCartesian().distance(Vector3D.of(0, 2, 0)), 1.0e-10);
        SphericalCoordinates sc3 = new SphericalCoordinates(2.0, Math.PI, piO2);
        Assert.assertEquals(0, sc3.getCartesian().distance(Vector3D.of(-2, 0, 0)), 1.0e-10);
        SphericalCoordinates sc4 = new SphericalCoordinates(2.0, -piO2, piO2);
        Assert.assertEquals(0, sc4.getCartesian().distance(Vector3D.of(0, -2, 0)), 1.0e-10);
        SphericalCoordinates sc5 = new SphericalCoordinates(2.0, 1.23456, 0);
        Assert.assertEquals(0, sc5.getCartesian().distance(Vector3D.of(0, 0, 2)), 1.0e-10);
        SphericalCoordinates sc6 = new SphericalCoordinates(2.0, 6.54321, Math.PI);
        Assert.assertEquals(0, sc6.getCartesian().distance(Vector3D.of(0, 0, -2)), 1.0e-10);
    }

    @Test
    public void testCoordinatesCtoS() {
        double piO2 = 0.5 * Math.PI;
        SphericalCoordinates sc1 = new SphericalCoordinates(Vector3D.of(2, 0, 0));
        Assert.assertEquals(2,           sc1.getR(),     1.0e-10);
        Assert.assertEquals(0,           sc1.getTheta(), 1.0e-10);
        Assert.assertEquals(piO2,        sc1.getPhi(),   1.0e-10);
        SphericalCoordinates sc2 = new SphericalCoordinates(Vector3D.of(0, 2, 0));
        Assert.assertEquals(2,           sc2.getR(),     1.0e-10);
        Assert.assertEquals(piO2,        sc2.getTheta(), 1.0e-10);
        Assert.assertEquals(piO2,        sc2.getPhi(),   1.0e-10);
        SphericalCoordinates sc3 = new SphericalCoordinates(Vector3D.of(-2, 0, 0));
        Assert.assertEquals(2,           sc3.getR(),     1.0e-10);
        Assert.assertEquals(Math.PI, sc3.getTheta(), 1.0e-10);
        Assert.assertEquals(piO2,        sc3.getPhi(),   1.0e-10);
        SphericalCoordinates sc4 = new SphericalCoordinates(Vector3D.of(0, -2, 0));
        Assert.assertEquals(2,           sc4.getR(),     1.0e-10);
        Assert.assertEquals(-piO2,       sc4.getTheta(), 1.0e-10);
        Assert.assertEquals(piO2,        sc4.getPhi(),   1.0e-10);
        SphericalCoordinates sc5 = new SphericalCoordinates(Vector3D.of(0, 0, 2));
        Assert.assertEquals(2,           sc5.getR(),     1.0e-10);
        //  don't check theta on poles, as it is singular
        Assert.assertEquals(0,           sc5.getPhi(),   1.0e-10);
        SphericalCoordinates sc6 = new SphericalCoordinates(Vector3D.of(0, 0, -2));
        Assert.assertEquals(2,           sc6.getR(),     1.0e-10);
        //  don't check theta on poles, as it is singular
        Assert.assertEquals(Math.PI, sc6.getPhi(),   1.0e-10);
    }

    @Test
    public void testSerialization() {
        SphericalCoordinates a = new SphericalCoordinates(3, 2, 1);
        SphericalCoordinates b = (SphericalCoordinates) GeometryTestUtils.serializeAndRecover(a);
        Assert.assertEquals(0, a.getCartesian().distance(b.getCartesian()), 1.0e-10);
        Assert.assertEquals(a.getR(),     b.getR(),     1.0e-10);
        Assert.assertEquals(a.getTheta(), b.getTheta(), 1.0e-10);
        Assert.assertEquals(a.getPhi(),   b.getPhi(),   1.0e-10);
    }

}
