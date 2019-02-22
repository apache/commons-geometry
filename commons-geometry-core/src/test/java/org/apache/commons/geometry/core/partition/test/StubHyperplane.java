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

import org.apache.commons.geometry.core.partition.Hyperplane;
import org.apache.commons.geometry.core.partition.Side;

/** Stub 1D hyperplane for testing purposes.
 */
public class StubHyperplane implements Hyperplane<StubPoint> {

    private final boolean direction;

    private final StubPoint location;

    public StubHyperplane(final StubPoint location, final boolean direction) {
        this.location = location;
        this.direction = direction;
    }

    public StubPoint getLocation() {
        return location;
    }

    public boolean getDirection() {
        return direction;
    }

    @Override
    public double offset(StubPoint point) {
        double offset = this.location.getX() - point.getX();
        return direction ? offset : -1 * offset;
    }

    @Override
    public Side classify(StubPoint point) {
        double offset = offset(point);

        if (PartitionTestUtils.PRECISION.eqZero(offset)) {
            return Side.HYPER;
        }
        else if (offset > 0) {
            return Side.MINUS;
        }

        return Side.PLUS;
    }

    @Override
    public StubPoint project(StubPoint point) {
        return this.location;
    }

    @Override
    public boolean sameOrientation(Hyperplane<StubPoint> other) {
        return direction == ((StubHyperplane) other).direction;
    }

    @Override
    public String toString() {
        return "hyperplane[location=" + location + ", direction= " + direction + "]";
    }
}
