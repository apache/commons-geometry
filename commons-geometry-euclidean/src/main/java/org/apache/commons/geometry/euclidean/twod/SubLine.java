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

import java.util.List;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partition.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partition.Hyperplane;
import org.apache.commons.geometry.core.partition.SubHyperplane;;

public class SubLine implements SubHyperplane<Vector2D> {

    @Override
    public Hyperplane<Vector2D> getHyperplane() {
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
    public boolean isInfinite() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public double getSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public RegionLocation classify(Vector2D point) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Vector2D closest(Vector2D point) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Builder<Vector2D> builder() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<? extends ConvexSubHyperplane<Vector2D>> toConvex() {
        // TODO Auto-generated method stub
        return null;
    }

}
