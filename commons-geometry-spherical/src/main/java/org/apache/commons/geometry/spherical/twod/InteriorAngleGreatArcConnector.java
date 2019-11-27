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
package org.apache.commons.geometry.spherical.twod;

import java.util.Collection;
import java.util.List;

import org.apache.commons.geometry.core.Geometry;

/** Great arc connector that selects between multiple connection options
 * based on the resulting interior angle. An interior angle in this
 * case is the angle created between an incoming arc and an outgoing arc
 * as measured on the minus (interior) side of the incoming arc. If looking
 * along the direction of the incoming arc, smaller interior angles
 * point more to the left and larger ones point more to the right.
 *
 * <p>This class provides two concrete implementations: {@link Maximize} and
 * {@link Minimize}, which choose connections with the largest or smallest interior
 * angles respectively.
 * </p>
 */
public abstract class InteriorAngleGreatArcConnector extends AbstractGreatArcConnector {
    /** {@inheritDoc} */
    @Override
    protected ConnectableGreatArc selectConnection(final ConnectableGreatArc incoming,
            final List<ConnectableGreatArc> outgoing) {

        // search for the best connection
        final GreatCircle circle = incoming.getArc().getCircle();

        double selectedInteriorAngle = Double.POSITIVE_INFINITY;
        ConnectableGreatArc selected = null;

        for (ConnectableGreatArc candidate : outgoing) {
            double interiorAngle = Geometry.PI - circle.angle(candidate.getArc().getCircle(),
                    incoming.getArc().getEndPoint());

            if (selected == null || isBetterAngle(interiorAngle, selectedInteriorAngle)) {
                selectedInteriorAngle = interiorAngle;
                selected = candidate;
            }
        }

        return selected;
    }

    /** Return true if {@code newAngle} represents a better interior angle than {@code previousAngle}.
     * @param newAngle the new angle under consideration
     * @param previousAngle the previous best angle
     * @return true if {@code newAngle} represents a better interior angle than {@code previousAngle}
     */
    protected abstract boolean isBetterAngle(double newAngle, double previousAngle);

    /** Convenience method for connecting a set of arcs with interior angles maximized
     * when possible. This method is equivalent to {@code new Maximize().connect(segments)}.
     * @param arcs arcs to connect
     * @return a list of connected arc paths
     * @see Maximize
     */
    public static List<GreatArcPath> connectMaximized(final Collection<GreatArc> arcs) {
        return new Maximize().connectAll(arcs);
    }

    /** Convenience method for connecting a set of line segments with interior angles minimized
     * when possible. This method is equivalent to {@code new Minimize().connect(segments)}.
     * @param arcs arcs to connect
     * @return a list of connected arc paths
     * @see Minimize
     */
    public static List<GreatArcPath> connectMinimized(final Collection<GreatArc> arcs) {
        return new Minimize().connectAll(arcs);
    }

    /** Implementation of {@link InteriorAngleGreatArcConnector} that chooses arc
     * connections that produce the largest interior angles. Another way to visualize this is
     * that when presented multiple connection options for a given arc, this class will
     * choose the option that points most to the right when viewed in the direction of the incoming
     * arc.
     */
    public static class Maximize extends InteriorAngleGreatArcConnector {
        /** {@inheritDoc} */
        @Override
        protected boolean isBetterAngle(double newAngle, double previousAngle) {
            return newAngle > previousAngle;
        }
    }

    /** Implementation of {@link InteriorAngleGreatArcConnector} that chooses arc
     * connections that produce the smallest interior angles. Another way to visualize this is
     * that when presented multiple connection options for a given arc, this class will
     * choose the option that points most to the left when viewed in the direction of the incoming
     * arc.
     */
    public static class Minimize extends InteriorAngleGreatArcConnector {
        /** {@inheritDoc} */
        @Override
        protected boolean isBetterAngle(double newAngle, double previousAngle) {
            return newAngle < previousAngle;
        }
    }
}
