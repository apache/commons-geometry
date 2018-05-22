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
package org.apache.commons.geometry.euclidean.oned;

import org.apache.commons.geometry.core.partitioning.AbstractSubHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Region;

/** This class represents sub-hyperplane for {@link OrientedPoint}.
 * <p>An hyperplane in 1D is a simple point, its orientation being a
 * boolean.</p>
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
public class SubOrientedPoint extends AbstractSubHyperplane<Point1D, Point1D> {

    /** Simple constructor.
     * @param hyperplane underlying hyperplane
     * @param remainingRegion remaining region of the hyperplane
     */
    public SubOrientedPoint(final Hyperplane<Point1D> hyperplane,
                            final Region<Point1D> remainingRegion) {
        super(hyperplane, remainingRegion);
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    protected AbstractSubHyperplane<Point1D, Point1D> buildNew(final Hyperplane<Point1D> hyperplane,
                                                                       final Region<Point1D> remainingRegion) {
        return new SubOrientedPoint(hyperplane, remainingRegion);
    }

    /** {@inheritDoc} */
    @Override
    public SplitSubHyperplane<Point1D> split(final Hyperplane<Point1D> hyperplane) {
        final OrientedPoint thisHyperplane = (OrientedPoint) getHyperplane();
        final double global = hyperplane.getOffset(thisHyperplane.getLocation());

        // use the tolerance value from our parent hyperplane to determine equality
        final double tolerance = thisHyperplane.getTolerance();

        if (global < -tolerance) {
            return new SplitSubHyperplane<Point1D>(null, this);
        } else if (global > tolerance) {
            return new SplitSubHyperplane<Point1D>(this, null);
        } else {
            return new SplitSubHyperplane<Point1D>(null, null);
        }
    }

}
