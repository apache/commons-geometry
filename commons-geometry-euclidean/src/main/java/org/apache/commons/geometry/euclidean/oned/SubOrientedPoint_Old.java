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

import org.apache.commons.geometry.core.partitioning.AbstractSubHyperplane_Old;
import org.apache.commons.geometry.core.partitioning.Hyperplane_Old;
import org.apache.commons.geometry.core.partitioning.Region_Old;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

/** This class represents sub-hyperplane for {@link OrientedPoint_Old}.
 * <p>An hyperplane in 1D is a simple point, its orientation being a
 * boolean.</p>
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
public class SubOrientedPoint_Old extends AbstractSubHyperplane_Old<Vector1D, Vector1D> {

    /** Simple constructor.
     * @param hyperplane underlying hyperplane
     * @param remainingRegion remaining region of the hyperplane
     */
    public SubOrientedPoint_Old(final Hyperplane_Old<Vector1D> hyperplane,
                            final Region_Old<Vector1D> remainingRegion) {
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
    protected AbstractSubHyperplane_Old<Vector1D, Vector1D> buildNew(final Hyperplane_Old<Vector1D> hyperplane,
                                                                       final Region_Old<Vector1D> remainingRegion) {
        return new SubOrientedPoint_Old(hyperplane, remainingRegion);
    }

    /** {@inheritDoc} */
    @Override
    public SplitSubHyperplane<Vector1D> split(final Hyperplane_Old<Vector1D> hyperplane) {
        final OrientedPoint_Old thisHyperplane = (OrientedPoint_Old) getHyperplane();
        final double global = hyperplane.getOffset(thisHyperplane.getLocation());

        // use the precision context from our parent hyperplane to determine equality
        final DoublePrecisionContext precision = thisHyperplane.getPrecision();

        int comparison = precision.compare(global, 0.0);

        if (comparison < 0) {
            return new SplitSubHyperplane<Vector1D>(null, this);
        } else if (comparison > 0) {
            return new SplitSubHyperplane<Vector1D>(this, null);
        } else {
            return new SplitSubHyperplane<Vector1D>(null, null);
        }
    }

}
