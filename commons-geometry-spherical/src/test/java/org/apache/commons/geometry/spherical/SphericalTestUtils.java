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

import java.io.IOException;
import java.text.ParseException;

import org.apache.commons.geometry.core.partitioning.Hyperplane_Old;
import org.apache.commons.geometry.core.partitioning.TreeBuilder;
import org.apache.commons.geometry.core.partitioning.TreeDumper;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.oned.ArcsSet;
import org.apache.commons.geometry.spherical.oned.LimitAngle;
import org.apache.commons.geometry.spherical.oned.S1Point;
import org.apache.commons.geometry.spherical.twod.Circle;
import org.apache.commons.geometry.spherical.twod.S2Point;
import org.apache.commons.geometry.spherical.twod.SphericalPolygonsSet;

/** Test utilities for spherical spaces.
 */
public class SphericalTestUtils {

    /** Get a string representation of an {@link ArcsSet}.
     * @param arcsSet region to dump
     * @return string representation of the region
     */
    public static String dump(final ArcsSet arcsSet) {
        final TreeDumper<S1Point> visitor = new TreeDumper<S1Point>("ArcsSet") {

            /** {@inheritDoc} */
            @Override
            protected void formatHyperplane(final Hyperplane_Old<S1Point> Hyperplane_Old) {
                final LimitAngle h = (LimitAngle) Hyperplane_Old;
                getFormatter().format("%22.15e %b %s",
                                      h.getLocation().getAzimuth(), h.isDirect(), h.getPrecision().toString());
            }

        };
        arcsSet.getTree(false).visit(visitor);
        return visitor.getDump();
    }

    /** Get a string representation of a {@link SphericalPolygonsSet}.
     * @param sphericalPolygonsSet region to dump
     * @return string representation of the region
     */
    public static String dump(final SphericalPolygonsSet sphericalPolygonsSet) {
        final TreeDumper<S2Point> visitor = new TreeDumper<S2Point>("SphericalPolygonsSet") {

            /** {@inheritDoc} */
            @Override
            protected void formatHyperplane(final Hyperplane_Old<S2Point> Hyperplane_Old) {
                final Circle h = (Circle) Hyperplane_Old;
                getFormatter().format("%22.15e %22.15e %22.15e %s",
                                      h.getPole().getX(), h.getPole().getY(), h.getPole().getZ(),
                                      h.getPrecision().toString());
            }

        };
        sphericalPolygonsSet.getTree(false).visit(visitor);
        return visitor.getDump();
    }

    /** Parse a string representation of an {@link ArcsSet}.
     * @param s string to parse
     * @return parsed region
     * @exception IOException if the string cannot be read
     * @exception ParseException if the string cannot be parsed
     */
    public static ArcsSet parseArcsSet(final String s)
        throws IOException, ParseException {
        final TreeBuilder<S1Point> builder = new TreeBuilder<S1Point>("ArcsSet", s) {

            /** {@inheritDoc} */
            @Override
            protected LimitAngle parseHyperplane()
                throws ParseException {
                return new LimitAngle(S1Point.of(getNumber()), getBoolean(), getPrecision());
            }

        };
        return new ArcsSet(builder.getTree(), builder.getPrecision());
    }

    /** Parse a string representation of a {@link SphericalPolygonsSet}.
     * @param s string to parse
     * @return parsed region
     * @exception IOException if the string cannot be read
     * @exception ParseException if the string cannot be parsed
     */
    public static SphericalPolygonsSet parseSphericalPolygonsSet(final String s)
        throws IOException, ParseException {
        final TreeBuilder<S2Point> builder = new TreeBuilder<S2Point>("SphericalPolygonsSet", s) {

            /** {@inheritDoc} */
            @Override
            public Circle parseHyperplane()
                throws ParseException {
                return new Circle(Vector3D.of(getNumber(), getNumber(), getNumber()), getPrecision());
            }

        };
        return new SphericalPolygonsSet(builder.getTree(), builder.getPrecision());
    }

}
