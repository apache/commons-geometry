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

import org.apache.commons.geometry.core.partitioning.AbstractSubHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Region;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.oned.Arc;
import org.apache.commons.geometry.spherical.oned.ArcsSet;
import org.apache.commons.geometry.spherical.oned.S1Point;

/** This class represents a sub-hyperplane for {@link Circle}.
 */
public class SubCircle extends AbstractSubHyperplane<S2Point, S1Point> {

    /** Simple constructor.
     * @param hyperplane underlying hyperplane
     * @param remainingRegion remaining region of the hyperplane
     */
    public SubCircle(final Hyperplane<S2Point> hyperplane,
                     final Region<S1Point> remainingRegion) {
        super(hyperplane, remainingRegion);
    }

    /** {@inheritDoc} */
    @Override
    protected AbstractSubHyperplane<S2Point, S1Point> buildNew(final Hyperplane<S2Point> hyperplane,
                                                                 final Region<S1Point> remainingRegion) {
        return new SubCircle(hyperplane, remainingRegion);
    }

    /** {@inheritDoc} */
    @Override
    public SplitSubHyperplane<S2Point> split(final Hyperplane<S2Point> hyperplane) {

        final Circle thisCircle   = (Circle) getHyperplane();
        final Circle otherCircle  = (Circle) hyperplane;
        final double angle = Vector3D.angle(thisCircle.getPole(), otherCircle.getPole());

        if (angle < thisCircle.getTolerance() || angle > Math.PI - thisCircle.getTolerance()) {
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
