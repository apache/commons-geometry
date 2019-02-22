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
package org.apache.commons.geometry.core.partition.test;

import org.apache.commons.geometry.core.Point;

/** Stub 1D point class for use in testing.
 */
public class StubPoint implements Point<StubPoint> {

    private final double x;

    public StubPoint(final double x) {
        this.x = x;
    }

    public double getX() {
        return x;
    }

    @Override
    public int getDimension() {
        return 1;
    }

    @Override
    public boolean isNaN() {
        return Double.isNaN(x);
    }

    @Override
    public boolean isInfinite() {
        return Double.isInfinite(x);
    }

    @Override
    public double distance(StubPoint p) {
        return Math.abs(p.x - x);
    }

    @Override
    public String toString() {
        return "(" + x + ")";
    }
}
