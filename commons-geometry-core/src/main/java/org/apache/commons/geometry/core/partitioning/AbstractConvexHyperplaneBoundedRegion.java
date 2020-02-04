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
package org.apache.commons.geometry.core.partitioning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;

/** Base class for convex hyperplane-bounded regions. This class provides generic implementations of many
 * algorithms related to convex regions.
 * @param <P> Point implementation type
 * @param <S> Convex subhyperplane implementation type
 */
public abstract class AbstractConvexHyperplaneBoundedRegion<P extends Point<P>, S extends ConvexSubHyperplane<P>>
    implements HyperplaneBoundedRegion<P> {
    /** List of boundaries for the region. */
    private final List<S> boundaries;

    /** Simple constructor. Callers are responsible for ensuring that the given list of subhyperplanes
     * represent a valid convex region boundary. No validation is performed.
     * @param boundaries the boundaries of the convex region
     */
    protected AbstractConvexHyperplaneBoundedRegion(final List<S> boundaries) {
        this.boundaries = Collections.unmodifiableList(boundaries);
    }

    /** Get the boundaries of the convex region. The exact ordering of the boundaries
     * is not guaranteed.
     * @return the boundaries of the convex region
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
        for (final S boundary : boundaries) {
            sum += boundary.getSize();
        }

        return sum;
    }

    /** {@inheritDoc} */
    @Override
    public RegionLocation classify(P pt) {
        boolean isOn = false;

        HyperplaneLocation loc;
        for (final S boundary : boundaries) {
            loc = boundary.getHyperplane().classify(pt);

            if (loc == HyperplaneLocation.PLUS) {
                return RegionLocation.OUTSIDE;
            } else if (loc == HyperplaneLocation.ON) {
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

        for (final S boundary : boundaries) {
            projected = boundary.closest(pt);
            dist = pt.distance(projected);

            if (projected != null && (closestPt == null || dist < closestDist)) {
                closestPt = projected;
                closestDist = dist;
            }
        }

        return closestPt;
    }

    /** Trim the given convex subhyperplane to the portion contained inside this instance.
     * @param convexSubHyperplane convex subhyperplane to trim. Null is returned if the subhyperplane
     * does not intersect the instance.
     * @return portion of the argument that lies entirely inside the region represented by
     *      this instance, or null if it does not intersect.
     */
    public ConvexSubHyperplane<P> trim(final ConvexSubHyperplane<P> convexSubHyperplane) {
        ConvexSubHyperplane<P> remaining = convexSubHyperplane;
        for (final S boundary : boundaries) {
            remaining = remaining.split(boundary.getHyperplane()).getMinus();
            if (remaining == null) {
                break;
            }
        }

        return remaining;
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

    /** Generic, internal transform method. Subclasses should use this to implement their own transform methods.
     * @param transform the transform to apply to the instance
     * @param thisInstance a reference to the current instance; this is passed as
     *      an argument in order to allow it to be a generic type
     * @param subhpType the type used for the boundary subhyperplanes
     * @param factory function used to create new convex region instances
     * @param <R> Region implementation type
     * @return the result of the transform operation
     */
    protected <R extends AbstractConvexHyperplaneBoundedRegion<P, S>> R transformInternal(
            final Transform<P> transform, final R thisInstance, final Class<S> subhpType,
            final Function<List<S>, R> factory) {

        if (isFull()) {
            return thisInstance;
        }

        final List<S> origBoundaries = getBoundaries();

        final int size = origBoundaries.size();
        final List<S> tBoundaries = new ArrayList<>(size);

        // determine if the hyperplanes should be reversed
        final S boundary = origBoundaries.get(0);
        ConvexSubHyperplane<P> tBoundary = boundary.transform(transform);

        final boolean reverseDirection = swapsInsideOutside(transform);

        // transform all of the segments
        if (reverseDirection) {
            tBoundary = tBoundary.reverse();
        }
        tBoundaries.add(subhpType.cast(tBoundary));

        for (int i = 1; i < origBoundaries.size(); ++i) {
            tBoundary = origBoundaries.get(i).transform(transform);

            if (reverseDirection) {
                tBoundary = tBoundary.reverse();
            }

            tBoundaries.add(subhpType.cast(tBoundary));
        }

        return factory.apply(tBoundaries);
    }

    /** Return true if the given transform swaps the inside and outside of
     * the region.
     *
     * <p>The default behavior of this method is to return true if the transform
     * does not preserve spatial orientation (ie, {@link Transform#preservesOrientation()}
     * is false). Subclasses may need to override this method to implement the correct
     * behavior for their space and dimension.</p>
     * @param transform transform to check
     * @return true if the given transform swaps the interior and exterior of
     *      the region
     */
    protected boolean swapsInsideOutside(final Transform<P> transform) {
        return !transform.preservesOrientation();
    }

    /** Generic, internal split method. Subclasses should call this from their {@link #split(Hyperplane)} methods.
     * @param splitter splitting hyperplane
     * @param thisInstance a reference to the current instance; this is passed as
     *      an argument in order to allow it to be a generic type
     * @param subhpType the type used for the boundary subhyperplanes
     * @param factory function used to create new convex region instances
     * @param <R> Region implementation type
     * @return the result of the split operation
     */
    protected <R extends AbstractConvexHyperplaneBoundedRegion<P, S>> Split<R> splitInternal(
            final Hyperplane<P> splitter, final R thisInstance, final Class<S> subhpType,
            final Function<List<S>, R> factory) {

        if (isFull()) {
            final R minus = factory.apply(Arrays.asList(subhpType.cast(splitter.span())));
            final R plus = factory.apply(Arrays.asList(subhpType.cast(splitter.reverse().span())));

            return new Split<>(minus, plus);
        } else {
            final ConvexSubHyperplane<P> trimmedSplitter = trim(splitter.span());

            if (trimmedSplitter == null) {
                // The splitter lies entirely outside of the region; we need
                // to determine whether we lie on the plus or minus side of the splitter.
                // We can use the first boundary to determine this. If the boundary is entirely
                // on the minus side of the splitter or lies directly on the splitter and has
                // the same orientation, then the area lies on the minus side of the splitter.
                // Otherwise, it lies on the plus side.
                final ConvexSubHyperplane<P> testSegment = boundaries.get(0);
                final SplitLocation testLocation = testSegment.split(splitter).getLocation();

                if (SplitLocation.MINUS == testLocation ||
                        (SplitLocation.NEITHER == testLocation &&
                            splitter.similarOrientation(testSegment.getHyperplane()))) {
                    return new Split<>(thisInstance, null);
                }

                return new Split<>(null, thisInstance);
            }

            final List<S> minusBoundaries = new ArrayList<>();
            final List<S> plusBoundaries = new ArrayList<>();

            splitBoundaries(splitter, subhpType, minusBoundaries, plusBoundaries);

            minusBoundaries.add(subhpType.cast(trimmedSplitter));
            plusBoundaries.add(subhpType.cast(trimmedSplitter.reverse()));

            return new Split<>(factory.apply(minusBoundaries), factory.apply(plusBoundaries));
        }
    }

    /** Split the boundaries of the region by the given hyperplane, adding the split parts into the
     * corresponding lists.
     * @param splitter splitting hyperplane
     * @param subhpType the type used for the boundary subhyperplanes
     * @param minusBoundaries list that will contain the portions of the boundaries on the minus side
     *      of the splitting hyperplane
     * @param plusBoundaries list that will contain the portions of the boundaries on the plus side of
     *      the splitting hyperplane
     */
    private void splitBoundaries(final Hyperplane<P> splitter, final Class<S> subhpType,
            final List<S> minusBoundaries, final List<S> plusBoundaries) {

        Split<? extends ConvexSubHyperplane<P>> split;
        ConvexSubHyperplane<P> minusBoundary;
        ConvexSubHyperplane<P> plusBoundary;

        for (final S boundary : boundaries) {
            split = boundary.split(splitter);

            minusBoundary = split.getMinus();
            plusBoundary = split.getPlus();

            if (minusBoundary != null) {
                minusBoundaries.add(subhpType.cast(minusBoundary));
            }

            if (plusBoundary != null) {
                plusBoundaries.add(subhpType.cast(plusBoundary));
            }
        }
    }

    /** Internal class encapsulating the logic for building convex region boundaries from collections of
     * hyperplanes.
     * @param <P> Point implementation type
     * @param <S> ConvexSubHyperplane implementation type
     */
    protected static class ConvexRegionBoundaryBuilder<P extends Point<P>, S extends ConvexSubHyperplane<P>> {

        /** Convex subhyperplane implementation type. */
        private final Class<S> subhyperplaneType;

        /** Construct a new instance for building convex region boundaries with the given convex subhyperplane
         * implementation type.
         * @param subhyperplaneType Convex subhyperplane implementation type
         */
        public ConvexRegionBoundaryBuilder(final Class<S> subhyperplaneType) {
            this.subhyperplaneType = subhyperplaneType;
        }

        /** Compute a list of convex subhyperplanes representing the boundaries of the convex region
         * bounded by the given collection of hyperplanes.
         * @param bounds hyperplanes defining the convex region
         * @return a list of convex subhyperplanes representing the boundaries of the convex region
         * @throws IllegalArgumentException if the given hyperplanes do not form a convex region
         */
        public List<S> build(final Iterable<? extends Hyperplane<P>> bounds) {

            final List<S> boundaries = new ArrayList<>();

            // cut each hyperplane by every other hyperplane in order to get the region boundaries
            int boundIdx = 0;
            ConvexSubHyperplane<P> boundary;

            for (final Hyperplane<P> currentBound : bounds) {
                ++boundIdx;

                boundary = splitBound(currentBound, bounds, boundIdx);
                if (boundary != null) {
                    boundaries.add(subhyperplaneType.cast(boundary));
                }
            }

            if (boundIdx > 0 && boundaries.isEmpty()) {
                // nothing was added
                throw nonConvexException(bounds);
            }

            return boundaries;
        }

        /** Split the given bounding hyperplane by all of the other hyperplanes in the given collection, returning the
         * remaining subhyperplane.
         * @param currentBound the bound to split; this value is assumed to have come from {@code bounds}
         * @param bounds collection of bounds to use to split {@code currentBound}
         * @param currentBoundIdx the index of {@code currentBound} in {@code bounds}
         * @return the part of {@code currentBound}'s subhyperplane that lies on the minus side of all of the
         *      splitting hyperplanes
         * @throws IllegalArgumentException if the hyperplanes do not form a convex region
         */
        private ConvexSubHyperplane<P> splitBound(final Hyperplane<P> currentBound,
                final Iterable<? extends Hyperplane<P>> bounds, final int currentBoundIdx) {

            ConvexSubHyperplane<P> boundary = currentBound.span();

            int splitterIdx = 0;
            for (final Hyperplane<P> splitter : bounds) {
                ++splitterIdx;

                if (currentBound == splitter) {
                    // do not split the bound with itself

                    if (currentBoundIdx > splitterIdx) {
                        // this hyperplane is duplicated in the list; skip all but the
                        // first insertion of its subhyperplane
                        return null;
                    }
                } else {
                    // split the subhyperplane
                    final Split<? extends ConvexSubHyperplane<P>> split = boundary.split(splitter);

                    if (split.getLocation() == SplitLocation.NEITHER) {
                        // the subhyperplane lies directly on the splitter

                        if (!currentBound.similarOrientation(splitter)) {
                            // two or more splitters are coincident and have opposite
                            // orientations, meaning that no area is on the minus side
                            // of both
                            throw nonConvexException(bounds);
                        } else if (currentBoundIdx > splitterIdx) {
                         // two or more hyperplanes are equivalent; only use the boundary
                            // from the first one and return null for this one
                            return null;
                        }
                    } else {
                        // retain the minus portion of the split
                        boundary = split.getMinus();
                    }
                }

                if (boundary == null) {
                    break;
                }
            }

            return boundary;
        }

        /** Return an exception indicating that the given collection of hyperplanes do not produce a convex region.
         * @param bounds collection of hyperplanes
         * @return an exception indicating that the given collection of hyperplanes do not produce a convex region
         */
        private IllegalArgumentException nonConvexException(final Iterable<? extends Hyperplane<P>> bounds) {
            return new IllegalArgumentException("Bounding hyperplanes do not produce a convex region: " + bounds);
        }
    }
}
