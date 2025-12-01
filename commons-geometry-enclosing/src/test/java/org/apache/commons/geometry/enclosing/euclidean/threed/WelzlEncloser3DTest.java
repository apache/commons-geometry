/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.geometry.enclosing.euclidean.threed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.enclosing.EnclosingBall;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.core.Precision;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.UnitSphereSampler;
import org.apache.commons.rng.simple.RandomSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WelzlEncloser3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    private final WelzlEncloser3D encloser = new WelzlEncloser3D(TEST_PRECISION);

    @Test
    void testNoPoints() {
        // arrange
        final String msg = "Unable to generate enclosing ball: no points given";

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            encloser.enclose(null);
        }, IllegalArgumentException.class, msg);

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            encloser.enclose(new ArrayList<>());
        }, IllegalArgumentException.class, msg);
    }

    @Test
    void testReducingBall() {
        // arrange
        final List<Vector3D> list =
                Arrays.asList(Vector3D.of(-7.140397329568118, -16.571661242582177, 11.714458961735405),
                              Vector3D.of(-7.137986707455888, -16.570767323375720, 11.708602108715928),
                              Vector3D.of(-7.139185068549035, -16.570891204702250, 11.715554057357394),
                              Vector3D.of(-7.142682716997507, -16.571609818234290, 11.710787934580328),
                              Vector3D.of(-7.139018392423351, -16.574405614157020, 11.710518716711425),
                              Vector3D.of(-7.140870659936730, -16.567993074240455, 11.710914678204503),
                              Vector3D.of(-7.136350173659562, -16.570498228820930, 11.713965225900928),
                              Vector3D.of(-7.141675762759172, -16.572852471407028, 11.714033471449508),
                              Vector3D.of(-7.140453077221105, -16.570212820780647, 11.708624578004980),
                              Vector3D.of(-7.140322188726825, -16.574152894557717, 11.710305611121410),
                              Vector3D.of(-7.141116131477088, -16.574061164624560, 11.712938509321699));

        // act
        final EnclosingBall<Vector3D> ball = encloser.enclose(list);

        // assert
        Assertions.assertTrue(ball.getRadius() > 0);
    }

    @Test
    void testInfiniteLoop() {
        // arrange
        // this test used to generate an infinite loop
        final List<Vector3D> list =
                Arrays.asList(Vector3D.of(-0.89227075512164380, -2.89317694645713900, 14.84572323743355500),
                              Vector3D.of(-0.92099498940693580, -2.31086108263908940, 12.92071026467688300),
                              Vector3D.of(-0.85227999411005200, -3.06314731441320730, 15.40163831651287000),
                              Vector3D.of(-1.77399413020785970, -3.65630391378114260, 14.13190097751873400),
                              Vector3D.of(0.33157833272465354, -2.22813591757792160, 14.21225234159008200),
                              Vector3D.of(-1.53065579165484400, -1.65692084770139570, 14.61483055714788500),
                              Vector3D.of(-1.08457093941217140, -1.96100325935602980, 13.09265170575555000),
                              Vector3D.of(0.30029469589708850, -3.05470831395667370, 14.56352400426342600),
                              Vector3D.of(-0.95007443938638460, -1.86810946486118360, 15.14491234340057000),
                              Vector3D.of(-1.89661503804130830, -2.17004080885185860, 14.81235128513927000),
                              Vector3D.of(-0.72193328761607530, -1.44513142833618270, 14.52355724218561800),
                              Vector3D.of(-0.26895980939606550, -3.69512371522084140, 14.72272846327652000),
                              Vector3D.of(-1.53501693431786170, -3.25055166611021900, 15.15509062584274800),
                              Vector3D.of(-0.71727553535519410, -3.62284279460799100, 13.26256700929380700),
                              Vector3D.of(-0.30220950676137365, -3.25410412500779070, 13.13682612771606000),
                              Vector3D.of(-0.04543996608267075, -1.93081853923797750, 14.79497997883171400),
                              Vector3D.of(-1.53348892951571640, -3.66688919703524900, 14.73095600812074200),
                              Vector3D.of(-0.98034899533935820, -3.34004481162763960, 13.03245014017556800));

        // act
        final EnclosingBall<Vector3D> ball = encloser.enclose(list);

        // assert
        Assertions.assertTrue(ball.getRadius() > 0);
    }

    @Test
    void testLargeSamples() {
        // arrange
        final UniformRandomProvider random = RandomSource.XO_SHI_RO_256_PP.create(0x35ddecfc78131e1dL);
        final UnitSphereSampler sr = UnitSphereSampler.of(random, 3);
        for (int k = 0; k < 50; ++k) {

            // define the reference sphere we want to compute
            final double d = 25 * random.nextDouble();
            final double refRadius = 10 * random.nextDouble();
            final Vector3D refCenter = Vector3D.of(sr.sample()).multiply(d);
            // set up a large sample inside the reference sphere
            final int nbPoints = random.nextInt(1000);

            final List<Vector3D> points = new ArrayList<>();
            for (int i = 0; i < nbPoints; ++i) {
                final double r = refRadius * random.nextDouble();
                points.add(Vector3D.Sum.of(refCenter).addScaled(r, Vector3D.of(sr.sample())).get());
            }

            // act/assert
            // test we find a sphere at most as large as the one used for random drawings
            checkSphere(points, refRadius);
        }
    }

    private void checkSphere(final List<Vector3D> points, final double refRadius) {

        final EnclosingBall<Vector3D> sphere = checkSphere(points);

        // compare computed sphere with bounding sphere
        Assertions.assertTrue(sphere.getRadius() <= refRadius);

        // check removing any point of the support Sphere fails to enclose the point
        for (int i = 0; i < sphere.getSupportSize(); ++i) {
            final List<Vector3D> reducedSupport = new ArrayList<>();
            int count = 0;
            for (final Vector3D s : sphere.getSupport()) {
                if (count++ != i) {
                    reducedSupport.add(s);
                }
            }
            final EnclosingBall<Vector3D> reducedSphere = new SphereGenerator(TEST_PRECISION)
                    .ballOnSupport(reducedSupport);
            boolean foundOutside = false;
            for (int j = 0; j < points.size() && !foundOutside; ++j) {
                if (!reducedSphere.contains(points.get(j), TEST_PRECISION)) {
                    foundOutside = true;
                }
            }
            Assertions.assertTrue(foundOutside);
        }
    }

    private EnclosingBall<Vector3D> checkSphere(final List<Vector3D> points) {

        final EnclosingBall<Vector3D> sphere = encloser.enclose(points);

        // all points are enclosed
        for (final Vector3D v : points) {
            Assertions.assertTrue(sphere.contains(v, TEST_PRECISION));
        }

        // all support points are on the boundary
        final Vector3D center = sphere.getCenter();
        final double radius = sphere.getRadius();

        for (final Vector3D s : sphere.getSupport()) {
            Assertions.assertTrue(TEST_PRECISION.eqZero(center.distance(s) - radius));
        }

        return sphere;
    }
}
