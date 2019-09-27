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

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.spherical.oned.Arc;
import org.apache.commons.geometry.spherical.oned.ArcsSet;
import org.apache.commons.geometry.spherical.oned.Point1S;
import org.apache.commons.geometry.spherical.partitioning.AbstractSubHyperplane_Old;
import org.apache.commons.geometry.spherical.partitioning.Hyperplane_Old;
import org.apache.commons.geometry.spherical.partitioning.Region_Old;

/** This class represents a sub-Hyperplane_Old for {@link Circle}.
 */
public class SubCircle extends AbstractSubHyperplane_Old<Point2S, Point1S> {

    /** Simple constructor.
     * @param Hyperplane_Old underlying Hyperplane_Old
     * @param remainingRegion remaining region of the Hyperplane_Old
     */
    public SubCircle(final Hyperplane_Old<Point2S> Hyperplane_Old,
                     final Region_Old<Point1S> remainingRegion) {
        super(Hyperplane_Old, remainingRegion);
    }

    /** {@inheritDoc} */
    @Override
    protected AbstractSubHyperplane_Old<Point2S, Point1S> buildNew(final Hyperplane_Old<Point2S> Hyperplane_Old,
                                                                 final Region_Old<Point1S> remainingRegion) {
        return new SubCircle(Hyperplane_Old, remainingRegion);
    }

    /** {@inheritDoc} */
    @Override
    public SplitSubHyperplane<Point2S> split(final Hyperplane_Old<Point2S> Hyperplane_Old) {

        final Circle thisCircle   = (Circle) getHyperplane();
        final Circle otherCircle  = (Circle) Hyperplane_Old;
        final double angle = thisCircle.getPole().angle(otherCircle.getPole());
        final DoublePrecisionContext precision = thisCircle.getPrecision();

        if (precision.eqZero(angle) || precision.compare(angle, Math.PI) >= 0) {
            // the two circles are aligned or opposite
            return new SplitSubHyperplane<>(null, null);
        } else {
            // the two circles intersect each other
            final Arc    arc          = thisCircle.getInsideArc(otherCircle);
            final ArcsSet.Split split = ((ArcsSet) getRemainingRegion()).split(arc);
            final ArcsSet plus        = split.getPlus();
            final ArcsSet minus       = split.getMinus();
            return new SplitSubHyperplane<>(plus  == null ? null : new SubCircle(thisCircle.copySelf(), plus),
                                                    minus == null ? null : new SubCircle(thisCircle.copySelf(), minus));
        }

    }

}
