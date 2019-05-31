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

import java.util.List;

/** Line segment connector subclass that selects between multiple connection
 * options based on the resulting interior angle. An interior angle in this
 * case is the angle created between an incoming segment and an outgoing segment
 * as measured on the minus (interior) side of the incoming line. If looking
 * down along the direction of the incoming line segment, smaller interior angles
 * point more to the left and larger ones point more to the right.
 *
 * <p>This class provides two concrete implementations: {@link Maximize} and
 * {@link Minimize}, which choose connections with the largest or smallest interior
 * angles respectively.
 * </p>
 */
public abstract class InteriorAngleLineSegmentConnector extends AbstractLineSegmentConnector {

    /** Serializable UID */
    private static final long serialVersionUID = 20190530L;

    /** {@inheritDoc} */
    @Override
    protected LineSegment selectConnection(LineSegment segment, List<LineSegment> connections,
            boolean forward) {

        // simple case first
        if (connections.size() == 1) {
            return connections.get(0);
        }

        // search for the best connection
        final double segmentAngle = segment.getLine().getAngle();

        double selectedDiffAngle = Double.POSITIVE_INFINITY;
        LineSegment selected = null;

        for (LineSegment connection : connections) {
            double connectionAngle = connection.getLine().getAngle();
            double diffAngle = forward ?
                    connectionAngle - segmentAngle :
                    segmentAngle - connectionAngle;

            if (selected == null || isBetterAngle(diffAngle, selectedDiffAngle)) {
                selectedDiffAngle = diffAngle;
                selected = connection;
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

    /** Implementation of {@link InteriorAngleLineSegmentConnector} that chooses line segment
     * connections that produce the largest interior angles. Another way to visualize this is
     * that when presented multiple connection options for a given line segment, this class will
     * choose the option that points most to the right when viewed in the direction of the incoming
     * line segment.
     */
    public static class Maximize extends InteriorAngleLineSegmentConnector {

        /** Serializable UID */
        private static final long serialVersionUID = 20190530L;

        /** {@inheritDoc} */
        @Override
        protected boolean isBetterAngle(double newAngle, double previousAngle) {
            return newAngle > previousAngle;
        }
    }

    /** Implementation of {@link InteriorAngleLineSegmentConnector} that chooses line segment
     * connections that produce the smallest interior angles. Another way to visualize this is
     * that when presented multiple connection options for a given line segment, this class will
     * choose the option that points most to the left when viewed in the direction of the incoming
     * line segment.
     */
    public static class Minimize extends InteriorAngleLineSegmentConnector {

        /** Serializable UID */
        private static final long serialVersionUID = 20190530L;

        /** {@inheritDoc} */
        @Override
        protected boolean isBetterAngle(double newAngle, double previousAngle) {
            return newAngle < previousAngle;
        }
    }
}
