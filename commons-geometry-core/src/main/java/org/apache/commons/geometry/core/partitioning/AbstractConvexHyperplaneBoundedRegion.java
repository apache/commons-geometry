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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;

/** Base class for convex hyperplane-bounded regions. This class provides generic implementations of many
 * algorithms related to convex regions.
 * @param <P> Point implementation type
 * @param <S> Hyperplane convex subset implementation type
 */
public abstract class AbstractConvexHyperplaneBoundedRegion<P extends Point<P>, S extends HyperplaneConvexSubset<P>>
    implements HyperplaneBoundedRegion<P> {
    /** List of boundaries for the region. */
    private final List<S> boundaries;

    /** Simple constructor. Callers are responsible for ensuring that the given list of boundaries
     * define a convex region. No validation is performed.
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
    public RegionLocation classify(final P pt) {
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
    public P project(final P pt) {

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

    /** Trim the given hyperplane subset to the portion contained inside this instance.
     * @param sub hyperplane subset to trim. Null is returned if the subset does not intersect the instance.
     * @return portion of the argument that lies entirely inside the region represented by
     *      this instance, or null if it does not intersect.
     */
    public HyperplaneConvexSubset<P> trim(final HyperplaneConvexSubset<P> sub) {
        HyperplaneConvexSubset<P> remaining = sub;
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
     * @param boundaryType the type used for the boundary hyperplane subsets
     * @param factory function used to create new convex region instances
     * @param <R> Region implementation type
     * @return the result of the transform operation
     */
    protected <R extends AbstractConvexHyperplaneBoundedRegion<P, S>> R transformInternal(
            final Transform<P> transform, final R thisInstance, final Class<S> boundaryType,
            final Function<List<S>, R> factory) {

        if (isFull()) {
            return thisInstance;
        }

        final List<S> origBoundaries = getBoundaries();

        final int size = origBoundaries.size();
        final List<S> tBoundaries = new ArrayList<>(size);

        // determine if the hyperplanes should be reversed
        final S boundary = origBoundaries.get(0);
        HyperplaneConvexSubset<P> tBoundary = boundary.transform(transform);

        final boolean reverseDirection = swapsInsideOutside(transform);

        // transform all of the segments
        if (reverseDirection) {
            tBoundary = tBoundary.reverse();
        }
        tBoundaries.add(boundaryType.cast(tBoundary));

        for (int i = 1; i < origBoundaries.size(); ++i) {
            tBoundary = origBoundaries.get(i).transform(transform);

            if (reverseDirection) {
                tBoundary = tBoundary.reverse();
            }

            tBoundaries.add(boundaryType.cast(tBoundary));
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
     * @param boundaryType the type used for the boundary hyperplane subsets
     * @param factory function used to create new convex region instances
     * @param <R> Region implementation type
     * @return the result of the split operation
     */
    protected <R extends AbstractConvexHyperplaneBoundedRegion<P, S>> Split<R> splitInternal(
            final Hyperplane<P> splitter, final R thisInstance, final Class<S> boundaryType,
            final Function<List<S>, R> factory) {

        return isFull() ?
                splitInternalFull(splitter, boundaryType, factory) :
                splitInternalNonFull(splitter, thisInstance, boundaryType, factory);
    }

    /** Internal split method for use with full regions, i.e. regions that cover the entire space.
     * @param splitter splitting hyperplane
     * @param boundaryType the type used for the boundary hyperplane subsets
     * @param factory function used to create new convex region instances
     * @param <R> Region implementation type
     * @return the result of the split operation
     */
    private <R extends AbstractConvexHyperplaneBoundedRegion<P, S>> Split<R> splitInternalFull(
            final Hyperplane<P> splitter, final Class<S> boundaryType, final Function<List<S>, R> factory) {

        final R minus = factory.apply(Collections.singletonList(boundaryType.cast(splitter.span())));
        final R plus = factory.apply(Collections.singletonList(boundaryType.cast(splitter.reverse().span())));

        return new Split<>(minus, plus);
    }

    /** Internal split method for use with non-full regions, i.e. regions that do not cover the entire space.
     * @param splitter splitting hyperplane
     * @param thisInstance a reference to the current instance; this is passed as
     *      an argument in order to allow it to be a generic type
     * @param boundaryType the type used for the boundary hyperplane subsets
     * @param factory function used to create new convex region instances
     * @param <R> Region implementation type
     * @return the result of the split operation
     */
    private <R extends AbstractConvexHyperplaneBoundedRegion<P, S>> Split<R> splitInternalNonFull(
            final Hyperplane<P> splitter, final R thisInstance, final Class<S> boundaryType,
            final Function<List<S>, R> factory) {

        final HyperplaneConvexSubset<P> trimmedSplitter = trim(splitter.span());

        if (trimmedSplitter == null) {
            // The splitter lies entirely outside of the region; we need
            // to determine whether we lie on the plus or minus side of the splitter.

            final SplitLocation regionLoc = determineRegionPlusMinusLocation(splitter);
            return regionLoc == SplitLocation.MINUS ?
                    new Split<>(thisInstance, null) :
                    new Split<>(null, thisInstance);
        }

        // the splitter passes through the region; split the other region boundaries
        // by the splitter
        final ArrayList<S> minusBoundaries = new ArrayList<>();
        final ArrayList<S> plusBoundaries = new ArrayList<>();

        splitBoundaries(splitter, boundaryType, minusBoundaries, plusBoundaries);

        // if the splitter was trimmed by the region boundaries, double-check that the split boundaries
        // actually lie on both sides of the splitter; this is another case where floating point errors
        // can cause a discrepancy between the results of splitting the splitter by the boundaries and
        // splitting the boundaries by the splitter
        if (!trimmedSplitter.isFull()) {
            if (minusBoundaries.isEmpty()) {
                if (plusBoundaries.isEmpty()) {
                    return new Split<>(null, null);
                }
                return new Split<>(null, thisInstance);
            } else if (plusBoundaries.isEmpty()) {
                return new Split<>(thisInstance, null);
            }
        }

        // we have a consistent region split; create the new plus and minus regions
        minusBoundaries.add(boundaryType.cast(trimmedSplitter));
        plusBoundaries.add(boundaryType.cast(trimmedSplitter.reverse()));

        minusBoundaries.trimToSize();
        plusBoundaries.trimToSize();

        return new Split<>(factory.apply(minusBoundaries), factory.apply(plusBoundaries));
    }

    /** Determine whether the region lies on the plus or minus side of the given splitter. It is assumed
     * that (1) the region is not full, and (2) the given splitter does not pass through the region.
     *
     * <p>In theory, this is a very simple operation: one need only test a single region boundary
     * to see if it lies on the plus or minus side of the splitter. In practice, however, accumulated
     * floating point errors can cause discrepancies between the splitting operations, causing
     * boundaries to be classified as lying on both sides of the splitter when they should only lie on one.
     * Therefore, this method examines as many boundaries as needed in order to determine the best response.
     * The algorithm proceeds as follows:
     * <ol>
     *  <li>If any boundary lies completely on the minus or plus side of the splitter, then
     *      {@link SplitLocation#MINUS MINUS} or {@link SplitLocation#PLUS PLUS} is returned, respectively.</li>
     *  <li>If any boundary is coincident with the splitter ({@link SplitLocation#NEITHER NEITHER}), then
     *      {@link SplitLocation#MINUS MINUS} is returned if the boundary hyperplane has the same orientation
     *      as the splitter, otherwise {@link SplitLocation#PLUS PLUS}.</li>
     *  <li>If no boundaries match the above conditions, then the sizes of the split boundaries are compared. If
     *      the sum of the sizes of the boundaries on the minus side is greater than the sum of the sizes of
     *      the boundaries on the plus size, then {@link SplitLocation#MINUS MINUS} is returned. Otherwise,
     *      {@link SplitLocation#PLUS PLUS} is returned.
     * </ol>
     * @param splitter splitter to classify the region against; the splitter is assumed to lie
     *      completely outside of the region
     * @return {@link SplitLocation#MINUS} if the region lies on the minus side of the splitter and
     *      {@link SplitLocation#PLUS} if the region lies on the plus side of the splitter
     */
    private SplitLocation determineRegionPlusMinusLocation(final Hyperplane<P> splitter) {
        double minusSize = 0;
        double plusSize = 0;

        Split<? extends HyperplaneConvexSubset<P>> split;
        SplitLocation loc;

        for (final S boundary : boundaries) {
            split = boundary.split(splitter);
            loc = split.getLocation();

            if (loc == SplitLocation.MINUS || loc == SplitLocation.PLUS) {
                return loc;
            } else if (loc == SplitLocation.NEITHER) {
                return splitter.similarOrientation(boundary.getHyperplane()) ?
                        SplitLocation.MINUS :
                        SplitLocation.PLUS;
            } else {
                minusSize += split.getMinus().getSize();
                plusSize += split.getPlus().getSize();
            }
        }

        return minusSize > plusSize ? SplitLocation.MINUS : SplitLocation.PLUS;
    }

    /** Split the boundaries of the region by the given hyperplane, adding the split parts into the
     * corresponding lists.
     * @param splitter splitting hyperplane
     * @param boundaryType the type used for the boundary hyperplane subsets
     * @param minusBoundaries list that will contain the portions of the boundaries on the minus side
     *      of the splitting hyperplane
     * @param plusBoundaries list that will contain the portions of the boundaries on the plus side of
     *      the splitting hyperplane
     */
    private void splitBoundaries(final Hyperplane<P> splitter, final Class<S> boundaryType,
            final List<S> minusBoundaries, final List<S> plusBoundaries) {

        Split<? extends HyperplaneConvexSubset<P>> split;
        HyperplaneConvexSubset<P> minusBoundary;
        HyperplaneConvexSubset<P> plusBoundary;

        for (final S boundary : boundaries) {
            split = boundary.split(splitter);

            minusBoundary = split.getMinus();
            plusBoundary = split.getPlus();

            if (minusBoundary != null) {
                minusBoundaries.add(boundaryType.cast(minusBoundary));
            }

            if (plusBoundary != null) {
                plusBoundaries.add(boundaryType.cast(plusBoundary));
            }
        }
    }

    /** Internal class encapsulating the logic for building convex region boundaries from collections of hyperplanes.
     * @param <P> Point implementation type
     * @param <S> Hyperplane convex subset implementation type
     */
    protected static class ConvexRegionBoundaryBuilder<P extends Point<P>, S extends HyperplaneConvexSubset<P>> {

        /** Hyperplane convex subset implementation type. */
        private final Class<S> subsetType;

        /** Construct a new instance for building convex region boundaries with the given hyperplane
         * convex subset implementation type.
         * @param subsetType Hyperplane convex subset implementation type
         */
        public ConvexRegionBoundaryBuilder(final Class<S> subsetType) {
            this.subsetType = subsetType;
        }

        /** Compute a list of hyperplane convex subsets representing the boundaries of the convex region
         * bounded by the given collection of hyperplanes.
         * @param bounds hyperplanes defining the convex region
         * @return a list of hyperplane convex subsets representing the boundaries of the convex region
         * @throws IllegalArgumentException if the given hyperplanes do not form a convex region
         */
        public List<S> build(final Iterable<? extends Hyperplane<P>> bounds) {

            final List<S> boundaries = new ArrayList<>();

            // cut each hyperplane by every other hyperplane in order to get the region boundaries
            int boundIdx = -1;
            HyperplaneConvexSubset<P> boundary;

            for (final Hyperplane<P> currentBound : bounds) {
                ++boundIdx;

                boundary = splitBound(currentBound, bounds, boundIdx);
                if (boundary != null) {
                    boundaries.add(subsetType.cast(boundary));
                }
            }

            if (boundIdx > 0 && boundaries.isEmpty()) {
                // nothing was added
                throw nonConvexException(bounds);
            }

            return boundaries;
        }

        /** Split the given bounding hyperplane by all of the other hyperplanes in the given collection, returning the
         * remaining hyperplane subset.
         * @param currentBound the bound to split; this value is assumed to have come from {@code bounds}
         * @param bounds collection of bounds to use to split {@code currentBound}
         * @param currentBoundIdx the index of {@code currentBound} in {@code bounds}
         * @return the part of {@code currentBound}'s hyperplane subset that lies on the minus side of all of the
         *      splitting hyperplanes
         * @throws IllegalArgumentException if the hyperplanes do not form a convex region
         */
        private HyperplaneConvexSubset<P> splitBound(final Hyperplane<P> currentBound,
                final Iterable<? extends Hyperplane<P>> bounds, final int currentBoundIdx) {

            HyperplaneConvexSubset<P> boundary = currentBound.span();

            final Iterator<? extends Hyperplane<P>> boundsIt = bounds.iterator();

            Hyperplane<P> splitter;
            int splitterIdx = -1;

            while (boundsIt.hasNext() && boundary != null) {
                splitter = boundsIt.next();
                ++splitterIdx;

                if (currentBound == splitter) {
                    // do not split the bound with itself

                    if (currentBoundIdx > splitterIdx) {
                        // this hyperplane is duplicated in the list; skip all but the
                        // first insertion of its hyperplane subset
                        return null;
                    }
                } else {
                    // split the boundary
                    final Split<? extends HyperplaneConvexSubset<P>> split = boundary.split(splitter);

                    if (split.getLocation() != SplitLocation.NEITHER) {
                        // retain the minus portion of the split
                        boundary = split.getMinus();
                    } else if (!currentBound.similarOrientation(splitter)) {
                        // two or more splitters are coincident and have opposite
                        // orientations, meaning that no area is on the minus side
                        // of both
                        throw nonConvexException(bounds);
                    } else if (currentBoundIdx > splitterIdx) {
                        // two or more hyperplanes are equivalent; only use the boundary
                        // from the first one and return null for this one
                        return null;
                    }
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
