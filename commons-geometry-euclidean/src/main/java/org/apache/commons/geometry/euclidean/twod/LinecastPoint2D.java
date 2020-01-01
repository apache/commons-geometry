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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.geometry.core.internal.Equivalency;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.AbstractLinecastPoint;

/** Class representing intersections resulting from linecast operations in Euclidean
 * 2D space. This class contains the intersection point along with the boundary normal
 * of the target at the point of intersection.
 * @see Linecastable2D
 */
public class LinecastPoint2D extends AbstractLinecastPoint<Vector2D, Vector2D.Unit, Line>
    implements Equivalency<LinecastPoint2D> {

    /** Comparator that sorts intersection instances by increasing abscissa order. If two abscissa
     * values are equal, the comparison uses {@link Vector2D#COORDINATE_ASCENDING_ORDER} with the
     * intersection normals.
     */
    public static final Comparator<LinecastPoint2D> ABSCISSA_ORDER = (a, b) -> {
        int cmp = Double.compare(a.getAbscissa(), b.getAbscissa());
        if (cmp == 0) {
            cmp = Vector2D.COORDINATE_ASCENDING_ORDER.compare(a.getNormal(), b.getNormal());
        }
        return cmp;
    };

    /** Construct a new instance from its components.
     * @param point the linecast intersection point
     * @param normal the surface of the linecast target at the intersection point
     * @param line intersecting line
     */
    public LinecastPoint2D(final Vector2D point, final Vector2D normal, final Line line) {
        super(point, normal.normalize(), line);
    }

    /** {@inheritDoc}
     *
     * <p>
     * Instances are considered equivalent if they have equivalent points, normals, and lines.
     * </p>
     */
    @Override
    public boolean eq(final LinecastPoint2D other) {
        if (this == other) {
            return true;
        }

        final DoublePrecisionContext precision = getLine().getPrecision();

        return getLine().eq(other.getLine()) &&
                getPoint().eq(other.getPoint(), precision) &&
                getNormal().eq(other.getNormal(), precision);
    }

    /** Sort the given list of linecast points by increasing abscissa value and filter
     * to remove duplicate entries (as determined by the {@link #eq(LinecastPoint2D)} method).
     * The argument is modified.
     * @param pts list of points to sort and filter
     */
    public static void sortAndFilter(final List<LinecastPoint2D> pts) {
        Collections.sort(pts, ABSCISSA_ORDER);

        double currentAbscissa = Double.POSITIVE_INFINITY;
        final List<LinecastPoint2D> abscissaList = new ArrayList<>();

        final ListIterator<LinecastPoint2D> it = pts.listIterator();
        LinecastPoint2D pt;
        while (it.hasNext()) {
            pt = it.next();
            if (!pt.getLine().getPrecision().eq(currentAbscissa, pt.getAbscissa())) {
                // new abscissa value
                currentAbscissa = pt.getAbscissa();
                abscissaList.clear();

                abscissaList.add(pt);
            } else if (containsEq(pt, abscissaList)) {
                // duplicate found for this abscissa value
                it.remove();
            } else {
                // not a duplicate
                abscissaList.add(pt);
            }
        }
    }

    /** Return true if the given linecast point is equivalent to any of those in the given list.
     * @param pt point to test
     * @param list list to test against
     * @return true if the given linecast point is equivalent to any of those in the given list
     */
    private static boolean containsEq(final LinecastPoint2D pt, final List<LinecastPoint2D> list) {
        for (LinecastPoint2D listPt : list) {
            if (listPt.eq(pt)) {
                return true;
            }
        }

        return false;
    }
}
