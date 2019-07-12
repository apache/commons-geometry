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

import org.apache.commons.geometry.core.Geometry;

/** Line segment connector subclass that selects between multiple connection
 * options based on the resulting interior angle. An interior angle in this
 * case is the angle created between an incoming segment and an outgoing segment
 * as measured on the minus (interior) side of the incoming line. If looking
 * along the direction of the incoming line segment, smaller interior angles
 * point more to the left and larger ones point more to the right.
 *
 * <p>This class provides two concrete implementations: {@link Maximize} and
 * {@link Minimize}, which choose connections with the largest or smallest interior
 * angles respectively.
 * </p>
 */
public abstract class InteriorAngleSegmentConnector extends AbstractSegmentConnector {

    /** Serializable UID */
    private static final long serialVersionUID = 20190530L;

    /** {@inheritDoc} */
    @Override
    protected ConnectorEntry selectConnection(ConnectorEntry incoming, List<ConnectorEntry> outgoing) {

        // search for the best connection
        final Line segmentLine = incoming.getSegment().getLine();

        double selectedInteriorAngle = Double.POSITIVE_INFINITY;
        ConnectorEntry selected = null;

        for (ConnectorEntry candidate : outgoing) {
            double interiorAngle = Geometry.PI - segmentLine.angle(candidate.getSegment().getLine());

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
     * @return
     */
    protected abstract boolean isBetterAngle(final double newAngle, final double previousAngle);

    /** Convenience method for connecting a set of line segments with interior angles maximized
     * when possible. This method is equivalent to {@code new Maximize().connect(segments)}.
     * @param segments line segments to connect
     * @return a list of connected line segment paths
     * @see Maximize
     */
    public static List<SegmentPath> connectMaximized(final Collection<Segment> segments) {
        return new Maximize().getPaths(segments);
    }

    /** Convenience method for connecting a set of line segments with interior angles minimized
     * when possible. This method is equivalent to {@code new Minimize().connect(segments)}.
     * @param segments line segments to connect
     * @return a list of connected line segment paths
     * @see Minimize
     */
    public static List<SegmentPath> connectMinimized(final Collection<Segment> segments) {
        return new Minimize().getPaths(segments);
    }

    /** Implementation of {@link InteriorAngleSegmentConnector} that chooses line segment
     * connections that produce the largest interior angles. Another way to visualize this is
     * that when presented multiple connection options for a given line segment, this class will
     * choose the option that points most to the right when viewed in the direction of the incoming
     * line segment.
     */
    public static class Maximize extends InteriorAngleSegmentConnector {

        /** Serializable UID */
        private static final long serialVersionUID = 20190530L;

        /** {@inheritDoc} */
        @Override
        protected boolean isBetterAngle(double newAngle, double previousAngle) {
            return newAngle > previousAngle;
        }
    }

    /** Implementation of {@link InteriorAngleSegmentConnector} that chooses line segment
     * connections that produce the smallest interior angles. Another way to visualize this is
     * that when presented multiple connection options for a given line segment, this class will
     * choose the option that points most to the left when viewed in the direction of the incoming
     * line segment.
     */
    public static class Minimize extends InteriorAngleSegmentConnector {

        /** Serializable UID */
        private static final long serialVersionUID = 20190530L;

        /** {@inheritDoc} */
        @Override
        protected boolean isBetterAngle(double newAngle, double previousAngle) {
            return newAngle < previousAngle;
        }
    }
}
