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
package org.apache.commons.geometry.euclidean.twod;

import java.util.Collection;
import java.util.List;

import org.apache.commons.numbers.angle.PlaneAngleRadians;

/** Subline connector that selects between multiple connection options
 * based on the resulting interior angle. An interior angle in this
 * case is the angle created between an incoming subline and an outgoing subline
 * as measured on the minus (interior) side of the incoming line. If looking
 * along the direction of the incoming subline, smaller interior angles
 * point more to the left and larger ones point more to the right.
 *
 * <p>This class provides two concrete implementations: {@link Maximize} and
 * {@link Minimize}, which choose connections with the largest or smallest interior
 * angles respectively.
 * </p>
 */
public abstract class InteriorAngleSubLineConnector extends AbstractSubLineConnector {
    /** {@inheritDoc} */
    @Override
    protected ConnectableSubLine selectConnection(ConnectableSubLine incoming, List<ConnectableSubLine> outgoing) {

        // search for the best connection
        final Line incomingLine = incoming.getSubLine().getLine();

        double selectedInteriorAngle = Double.POSITIVE_INFINITY;
        ConnectableSubLine selected = null;

        for (final ConnectableSubLine candidate : outgoing) {
            final double interiorAngle = PlaneAngleRadians.PI - incomingLine.angle(candidate.getSubLine().getLine());

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

    /** Convenience method for connecting a set of sublines with interior angles maximized
     * when possible. This method is equivalent to {@code new Maximize().connect(sublines)}.
     * @param sublines sublines to connect
     * @return a list of connected subline paths
     * @see Maximize
     */
    public static List<Polyline> connectMaximized(final Collection<ConvexSubLine> sublines) {
        return new Maximize().connectAll(sublines);
    }

    /** Convenience method for connecting a set of sublines with interior angles minimized
     * when possible. This method is equivalent to {@code new Minimize().connect(sublines)}.
     * @param sublines sublines to connect
     * @return a list of connected subline paths
     * @see Minimize
     */
    public static List<Polyline> connectMinimized(final Collection<ConvexSubLine> sublines) {
        return new Minimize().connectAll(sublines);
    }

    /** Implementation of {@link InteriorAngleSubLineConnector} that chooses subline
     * connections that produce the largest interior angles. Another way to visualize this is
     * that when presented multiple connection options for a given subline, this class will
     * choose the option that points most to the right when viewed in the direction of the incoming
     * subline.
     */
    public static class Maximize extends InteriorAngleSubLineConnector {
        /** {@inheritDoc} */
        @Override
        protected boolean isBetterAngle(double newAngle, double previousAngle) {
            return newAngle > previousAngle;
        }
    }

    /** Implementation of {@link InteriorAngleSubLineConnector} that chooses subline
     * connections that produce the smallest interior angles. Another way to visualize this is
     * that when presented multiple connection options for a given subline, this class will
     * choose the option that points most to the left when viewed in the direction of the incoming
     * subline.
     */
    public static class Minimize extends InteriorAngleSubLineConnector {
        /** {@inheritDoc} */
        @Override
        protected boolean isBetterAngle(double newAngle, double previousAngle) {
            return newAngle < previousAngle;
        }
    }
}
