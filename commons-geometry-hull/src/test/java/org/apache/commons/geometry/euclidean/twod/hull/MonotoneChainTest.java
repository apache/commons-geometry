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
package org.apache.commons.geometry.euclidean.twod.hull;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.geometry.euclidean.twod.Point2D;
import org.junit.Test;

/**
 * Test class for MonotoneChain.
 */
public class MonotoneChainTest extends ConvexHullGenerator2DAbstractTest {

    @Override
    protected ConvexHullGenerator2D createConvexHullGenerator(boolean includeCollinearPoints) {
        return new MonotoneChain(includeCollinearPoints);
    }

    // ------------------------------------------------------------------------------

    @Test(expected=IllegalStateException.class)
    public void testConvergenceException() {
        final Collection<Point2D> points = new ArrayList<>();

        points.add(Point2D.of(1, 1));
        points.add(Point2D.of(1, 5));
        points.add(Point2D.of(0, 7));
        points.add(Point2D.of(1, 10));
        points.add(Point2D.of(1, 20));
        points.add(Point2D.of(20, 20));
        points.add(Point2D.of(20, 40));
        points.add(Point2D.of(40, 1));

        @SuppressWarnings("unused")
        final ConvexHull2D hull = new MonotoneChain(true, 2).generate(points);
    }

}
