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
package org.apache.commons.geometry.euclidean.twod.path;

import java.util.Collection;
import java.util.List;

import org.apache.commons.geometry.euclidean.twod.Line;
import org.apache.commons.geometry.euclidean.twod.LineConvexSubset;
import org.apache.commons.numbers.angle.PlaneAngleRadians;

/** Line subset connector that selects between multiple connection options
 * based on the resulting interior angle. An interior angle in this
 * case is the angle created between an incoming line subset and an outgoing
 * line subset as measured on the minus (interior) side of the incoming line.
 * If looking along the direction of the incoming line subset, smaller interior
 * angles point more to the left and larger ones point more to the right.
 *
 * <p>This class provides two concrete implementations: {@link Maximize} and
 * {@link Minimize}, which choose connections with the largest or smallest interior
 * angles respectively.
 * </p>
 */
public abstract class InteriorAngleLinePathConnector extends AbstractLinePathConnector {
    /** {@inheritDoc} */
    @Override
    protected ConnectableLineSubset selectConnection(final ConnectableLineSubset incoming,
            final List<ConnectableLineSubset> outgoing) {

        // search for the best connection
        final Line incomingLine = incoming.getLineSubset().getLine();

        double selectedInteriorAngle = Double.POSITIVE_INFINITY;
        ConnectableLineSubset selected = null;

        for (final ConnectableLineSubset candidate : outgoing) {
            final double interiorAngle =
                    PlaneAngleRadians.PI - incomingLine.angle(candidate.getLineSubset().getLine());

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

    /** Convenience method for connecting a collection of line subsets with interior angles
     * maximized when possible. This method is equivalent to {@code new Maximize().connect(subsets)}.
     * @param subsets line subsets to connect
     * @return a list of connected line subset paths
     * @see Maximize
     */
    public static List<LinePath> connectMaximized(final Collection<LineConvexSubset> subsets) {
        return new Maximize().connectAll(subsets);
    }

    /** Convenience method for connecting a collection of line subsets with interior angles minimized
     * when possible. This method is equivalent to {@code new Minimize().connect(subsets)}.
     * @param subsets line subsets to connect
     * @return a list of connected line subset paths
     * @see Minimize
     */
    public static List<LinePath> connectMinimized(final Collection<LineConvexSubset> subsets) {
        return new Minimize().connectAll(subsets);
    }

    /** Implementation of {@link InteriorAngleLinePathConnector} that chooses line subset
     * connections that produce the largest interior angles. Another way to visualize this is
     * that when presented multiple connection options for a given line subset, this class will
     * choose the option that points most to the right when viewed in the direction of the incoming
     * line subset.
     */
    public static final class Maximize extends InteriorAngleLinePathConnector {
        /** {@inheritDoc} */
        @Override
        protected boolean isBetterAngle(final double newAngle, final double previousAngle) {
            return newAngle > previousAngle;
        }
    }

    /** Implementation of {@link InteriorAngleLinePathConnector} that chooses line subset
     * connections that produce the smallest interior angles. Another way to visualize this is
     * that when presented multiple connection options for a given line subset, this class will
     * choose the option that points most to the left when viewed in the direction of the incoming
     * line subset.
     */
    public static final class Minimize extends InteriorAngleLinePathConnector {
        /** {@inheritDoc} */
        @Override
        protected boolean isBetterAngle(final double newAngle, final double previousAngle) {
            return newAngle < previousAngle;
        }
    }
}
