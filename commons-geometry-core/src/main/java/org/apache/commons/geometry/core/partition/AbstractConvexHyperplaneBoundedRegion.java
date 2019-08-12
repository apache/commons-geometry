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
package org.apache.commons.geometry.core.partition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;

/** Base class for convex hyperplane-bounded regions.
 * @param <P> Point implementation type
 * @param <S> Convex subhyperplane implementation type
 */
public abstract class AbstractConvexHyperplaneBoundedRegion<P extends Point<P>, S extends ConvexSubHyperplane<P>>
    implements ConvexHyperplaneBoundedRegion<P>, Serializable {

    /** Serializable UID */
    private static final long serialVersionUID = 20190812L;

    /** List of boundaries for the region. */
    private final List<S> boundaries;

    /** Simple constructor. Callers are responsible for ensuring that the given list of subhyperplanes
     * represent a valid convex region boundary. No validation is performed.
     * @param boundaries the boundaries of the convex region
     */
    protected AbstractConvexHyperplaneBoundedRegion(final List<S> boundaries) {
        this.boundaries = Collections.unmodifiableList(boundaries);
    }

    /** Get the boundaries of the convex region.
     * @return the boundaries of the convex region.
     */
    public List<S> getBoundaries() {
        return boundaries;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFull() {
        // no boundaries => no outside
        return boundaries.isEmpty();
    }

    /** {@inheritDoc}
     *
     * <p>This method always returns false.</p>
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public double getBoundarySize() {
        double sum = 0.0;
        for (S boundary : boundaries) {
            sum += boundary.getSize();
        }

        return sum;
    }

    /** {@inheritDoc} */
    @Override
    public RegionLocation classify(P pt) {
        boolean isOn = false;

        HyperplaneLocation loc;
        for (S boundary : boundaries) {
            loc = boundary.getHyperplane().classify(pt);

            if (loc == HyperplaneLocation.PLUS) {
                return RegionLocation.OUTSIDE;
            }
            else if (loc == HyperplaneLocation.ON) {
                isOn = true;
            }
        }

        return isOn ? RegionLocation.BOUNDARY : RegionLocation.INSIDE;
    }

    /** {@inheritDoc} */
    @Override
    public P project(P pt) {

        P projected;
        double dist;

        P closestPt = null;
        double closestDist = Double.POSITIVE_INFINITY;

        for (S boundary : boundaries) {
            projected = boundary.closest(pt);
            dist = pt.distance(projected);

            if (projected != null && (closestPt == null || dist < closestDist)) {
                closestPt = projected;
                closestDist = dist;
            }
        }

        return closestPt;
    }

    /** {@inheritDoc} */
    @Override
    public ConvexSubHyperplane<P> trim(final ConvexSubHyperplane<P> convexSubHyperplane) {
        ConvexSubHyperplane<P> remaining = convexSubHyperplane;
        for (S boundary : boundaries) {
            remaining = remaining.split(boundary.getHyperplane()).getMinus();
            if (remaining == null) {
                break;
            }
        }

        return remaining;
    }

    /** Generic, internal transform method. Subclasses should call this from their {@link #transform(Transform)}
     * method.
     * @param transform the transform to apply to the instance
     * @param thisInstance a reference to the current instance; this is passed as
     *      an argument in order to allow it to be a generic type
     * @param factory function used to create new convex region instances
     * @return the result of the transform operation
     */
    protected <T extends AbstractConvexHyperplaneBoundedRegion<P, S>> T transformInteral(final Transform<P> transform,
            final T thisInstance, final Function<List<? extends ConvexSubHyperplane<P>>, T> factory) {

        if (isFull()) {
            return thisInstance;
        }

        final List<S> boundaries = getBoundaries();

        final int size = boundaries.size();
        final List<ConvexSubHyperplane<P>> tBoundaries = new ArrayList<>(size);

        // determine if the hyperplanes should be reversed
        final S boundary = boundaries.get(0);
        ConvexSubHyperplane<P> tBoundary = boundary.transform(transform);

        final P plusPt = boundary.getHyperplane().plusPoint();
        final boolean reverseDirection = tBoundary.getHyperplane().classify(transform.apply(plusPt)) == HyperplaneLocation.MINUS;

        // transform all of the segments
        if (reverseDirection) {
            tBoundary = tBoundary.reverse();
        }
        tBoundaries.add(tBoundary);

        for (int i=1; i<boundaries.size(); ++i) {
            tBoundary = boundaries.get(i).transform(transform);

            if (reverseDirection) {
                tBoundary = tBoundary.reverse();
            }

            tBoundaries.add(tBoundary);
        }

        return factory.apply(tBoundaries);
    }

    /** Generic, internal split method. Subclasses should call this from their {@link #split(Hyperplane)} methods.
     * @param splitter splitting hyperplane
     * @param thisInstance a reference to the current instance; this is passed as
     *      an argument in order to allow it to be a generic type
     * @param factory function used to create new convex region instances
     * @return the result of the split operation
     */
    protected <T extends AbstractConvexHyperplaneBoundedRegion<P, S>> Split<T> splitInternal(final Hyperplane<P> splitter,
            final T thisInstance, final Function<List<? extends ConvexSubHyperplane<P>>, T> factory) {

        if (isFull() ) {
            final T minus = factory.apply(Arrays.asList(splitter.span()));
            final T plus = factory.apply(Arrays.asList(splitter.reverse().span()));

            return new Split<>(minus, plus);
        }
        else {
            final ConvexSubHyperplane<P> trimmedSplitter = trim(splitter.span());

            if (trimmedSplitter == null) {
                // The splitter lies entirely outside of the region; we need
                // to determine whether we lie on the plus or minus side of the splitter.
                // We can use the first boundary to determine this. If the boundary is entirely
                // on the minus side of the splitter or lies directly on the splitter and has
                // the same orientation, then the area lies on the minus side of the splitter.
                // Otherwise, it lies on the plus side.
                ConvexSubHyperplane<P> testSegment = boundaries.get(0);
                SplitLocation testLocation = testSegment.split(splitter).getLocation();

                if (SplitLocation.MINUS == testLocation ||
                        (SplitLocation.NEITHER == testLocation && splitter.similarOrientation(testSegment.getHyperplane()))) {
                    return new Split<>(thisInstance, null);
                }

                return new Split<>(null, thisInstance);
            }

            final List<ConvexSubHyperplane<P>> minusBoundaries = new ArrayList<>();
            final List<ConvexSubHyperplane<P>> plusBoundaries = new ArrayList<>();

            Split<? extends ConvexSubHyperplane<P>> split;
            ConvexSubHyperplane<P> minusBoundary;
            ConvexSubHyperplane<P> plusBoundary;

            for (S boundary : boundaries) {
                split = boundary.split(splitter);

                minusBoundary = split.getMinus();
                plusBoundary = split.getPlus();

                if (minusBoundary != null) {
                    minusBoundaries.add(minusBoundary);
                }

                if (plusBoundary != null) {
                    plusBoundaries.add(plusBoundary);
                }
            }

            minusBoundaries.add(trimmedSplitter);
            plusBoundaries.add(trimmedSplitter.reverse());

            return new Split<>(factory.apply(minusBoundaries), factory.apply(plusBoundaries));
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName())
            .append("[boundaries= ")
            .append(boundaries);

        return sb.toString();
    }
}
