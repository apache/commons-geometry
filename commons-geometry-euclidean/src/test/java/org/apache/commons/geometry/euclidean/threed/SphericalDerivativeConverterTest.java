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
package org.apache.commons.geometry.euclidean.threed;

import org.apache.commons.geometry.core.Geometry;
import org.junit.Assert;
import org.junit.Test;

public class SphericalDerivativeConverterTest {

    private static final double EPS = 1e-10;

    @Test
    public void testConstructor() {
        // arrange
        SphericalCoordinates sc = SphericalCoordinates.of(2, Geometry.PI, Geometry.HALF_PI);

        // act
        SphericalDerivativeConverter conv = new SphericalDerivativeConverter(sc);

        // assert
        checkSpherical(conv.getSpherical(), 2, Geometry.PI, Geometry.HALF_PI);
        checkVector(conv.getVector(), -2, 0, 0);
    }

    @Test
    public void testToCartesianGradient() {
        // NOTE: The following set of test data is taken from the original test for this code in commons-math.
        // The test in that project generated and checked the inputs on the fly using the commons-math differentiation
        // classes. However, since we don't have the benefit of those here, we're using some selected data points
        // from that test.

        // act/assert
        checkToCartesianGradient(
                SphericalCoordinates.of(0.2, 0.1, 0.1),
                new double[] { 3.1274095413292105E-4, -1.724542757978006E-6, 1.5102449769881866E-4 },
                new double[] { 0.0007872851, -0.0000078127, 0.0002357921 });

        checkToCartesianGradient(
                SphericalCoordinates.of(0.2, 0.1, 1.6),
                new double[] { -7.825830329191124E-8, 7.798528724837122E-10, -4.027286034178383E-7 },
                new double[] { -0.0000000197, 0.0000000019, 0.0000020151 });

        checkToCartesianGradient(
                SphericalCoordinates.of(0.2, 1.6, 0.1),
                new double[] { -9.075903886546823E-6, -1.5573157416535893E-5, -4.352284221940998E-6 },
                new double[] { 0.0007802833, 0.0000002252, -0.000006858 });

        checkToCartesianGradient(
                SphericalCoordinates.of(0.2, 2.4, 2.4),
                new double[] { 6.045188551967462E-4, 2.944844493772992E-5, 5.207279563401837E-5 },
                new double[] { -0.0003067696, -0.0000146129, -0.0006216347 });

        checkToCartesianGradient(
                SphericalCoordinates.of(9.2, 5.5, 2.4),
                new double[] { 27.09285722408859, 327.829199283976, 422.53939642005736 },
                new double[] { 26.1884919572, 48.3685006936, -51.0009075025 });
    }

    private void checkToCartesianGradient(SphericalCoordinates spherical, double[] sGradient, double[] cGradient) {
        SphericalDerivativeConverter conv = new SphericalDerivativeConverter(spherical);

        double[] result = conv.toCartesianGradient(sGradient);

        Assert.assertArrayEquals(cGradient, result, EPS);
    }

    @Test
    public void testToCartesianHessian() {
        // NOTE: The following set of test data is taken from the original test for this code in commons-math.
        // The test in that project generated and checked the inputs on the fly using the commons-math differentiation
        // classes. However, since we don't have the benefit of those here, we're using some selected data points
        // from that test.
        //
        // The NaN values in the input spherical Hessians are only present to ensure that the upper-right
        // part of the matrix is not used in the calculation.

        // act/assert
        checkToCartesianHessian(
                SphericalCoordinates.of(0.2, 0.0, 0.1),
                new double[] { 3.147028015595093E-4, -1.5708927954007288E-7, 1.5209020574753025E-4 },
                new double[][] {
                    { 0.004720542023392639, Double.NaN, Double.NaN },
                    { -3.927231988501822E-6, -1.5732003526076452E-5, Double.NaN },
                    { 0.0030418041149506037, -3.0840214797113795E-6, -1.56400962465978E-4 }
                },
                new double[][] {
                    { 0.0, -3.940348984959686E-4, 0.011880399467047453 },
                    { -3.940348984959686E-4, 7.867570038987733E-6, -1.1860608699245036E-4 },
                    { 0.011880399467047453, -1.1860608699245036E-4, 0.002384031969540735 }
                });

        checkToCartesianHessian(
                SphericalCoordinates.of(0.2, 0.2, 1.7),
                new double[] { -6.492205616890373E-6, 9.721055406032577E-8, -7.490005649457144E-6 },
                new double[][] {
                    { -9.660140526063848E-5, Double.NaN, Double.NaN },
                    { 2.087263937942704E-6, 3.0135301759512823E-7, Double.NaN },
                    { -1.4908056742242714E-4, 2.228225255291761E-6, -1.1271700251178201E-4 }
                },
                new double[][] {
                    { 0.0, 8.228328248729827E-7, 1.9536195257978514E-4 },
                    { 8.228328248729827E-7, -1.568516517220037E-7, -1.862033454396115E-5 },
                    { 1.9536195257978514E-4, -1.862033454396115E-5, -0.0029473017314775615 }
                });

        checkToCartesianHessian(
                SphericalCoordinates.of(0.2, 1.6, 0.1),
                new double[] { -9.075903886546686E-6, -1.5573157416535897E-5, -4.352284221940931E-6 },
                new double[][] {
                    { -1.3557892633841054E-4, Double.NaN, Double.NaN },
                    { -3.106944464923055E-4, 4.4143436330613375E-7, Double.NaN },
                    { -8.660889278565699E-5, -1.489922640116937E-4, 5.374400993902801E-6 }
                },
                new double[][] {
                    { 0.0, -3.862868527078941E-4, 0.011763015339492582 },
                    { -3.862868527078941E-4, -2.229868350965674E-7, 3.395142163599996E-6 },
                    { 0.011763015339492582, 3.395142163599996E-6, -6.892478835391066E-5 }
                });

        checkToCartesianHessian(
                SphericalCoordinates.of(0.2, 2.4, 2.5),
                new double[] { 6.911538590806891E-4, 3.344602742543664E-5, 3.330643810411849E-5 },
                new double[][] {
                    { 0.010200457858547542, Double.NaN, Double.NaN },
                    { 6.695363800209198E-4, -3.070347513695088E-5, Double.NaN },
                    { 6.68380906286568E-4, 3.001744637007274E-5, -2.273032055462482E-4 }
                },
                new double[][] {
                    { 0.0, 1.9000713243497378E-4, 0.007402721147059207 },
                    { 1.9000713243497378E-4, 1.6118798431431763E-5, 3.139960286869248E-4 },
                    { 0.007402721147059207, 3.139960286869248E-4, 0.008155571186075681 }
                });

        checkToCartesianHessian(
                SphericalCoordinates.of(9.2, 5.6, 2.5),
                new double[] { 41.42645719593436, 859.1407583470807, 939.7112322238082 },
                new double[][] {
                    { 11.642163255436742, Double.NaN, Double.NaN },
                    { 54.8154280776715, 5286.1651942531325, Double.NaN },
                    { 60.370567966140726, 4700.570567363823, 4929.996883244262 }
                },
                new double[][] {
                    { 0.0, 36.772022140868714, -22.087375306566134 },
                    { 36.772022140868714, 212.8111723550033, -63.91326828897971 },
                    { -22.087375306566134, -63.91326828897971, 25.593304575600133 }
                });
    }

    private void checkToCartesianHessian(SphericalCoordinates spherical, double[] sGradient,
            double[][] sHessian, double[][] cHessian) {
        SphericalDerivativeConverter conv = new SphericalDerivativeConverter(spherical);

        double[][] result = conv.toCartesianHessian(sHessian, sGradient);

        Assert.assertEquals(cHessian.length, result.length);
        for (int i=0; i<cHessian.length; ++i) {
            Assert.assertArrayEquals("Hessians differ at row " + i, cHessian[i], result[i], EPS);
        }
    }

    private void checkSpherical(SphericalCoordinates c, double radius, double azimuth, double polar) {
        Assert.assertEquals(radius, c.getRadius(), EPS);
        Assert.assertEquals(azimuth, c.getAzimuth(), EPS);
        Assert.assertEquals(polar, c.getPolar(), EPS);
    }

    private void checkVector(Vector3D v, double x, double y, double z) {
        Assert.assertEquals(x, v.getX(), EPS);
        Assert.assertEquals(y, v.getY(), EPS);
        Assert.assertEquals(z, v.getZ(), EPS);
    }
}
