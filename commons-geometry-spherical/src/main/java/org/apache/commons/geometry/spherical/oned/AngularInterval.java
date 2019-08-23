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
package org.apache.commons.geometry.spherical.oned;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.ConvexHyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partitioning.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

public class AngularInterval implements ConvexHyperplaneBoundedRegion<Point1S>, Serializable {

    /** Serializable UID */
    private static final long serialVersionUID = 20190817L;

    private final double min;

    private final double max;

    private final double mid;

    private AngularInterval(final double min, final double max, final double mid) {
        this.min = min;
        this.max = max;
        this.mid = mid;
    }

    @Override
    public List<? extends ConvexHyperplaneBoundedRegion<Point1S>> toConvex() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isFull() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public double getSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getBoundarySize() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Point1S getBarycenter() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RegionLocation classify(Point1S pt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Point1S project(Point1S pt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConvexHyperplaneBoundedRegion<Point1S> transform(Transform<Point1S> transform) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConvexSubHyperplane<Point1S> trim(ConvexSubHyperplane<Point1S> convexSubHyperplane) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Split<? extends ConvexHyperplaneBoundedRegion<Point1S>> split(Hyperplane<Point1S> splitter) {
        // TODO Auto-generated method stub
        return null;
    }

    public static AngularInterval full() {
        return null;
    }

    public static AngularInterval of(final double start, final double end, final DoublePrecisionContext precision) {
        return null;
    }
}
