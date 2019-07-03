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
package org.apache.commons.geometry.euclidean.threed;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partition.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partition.Hyperplane;
import org.apache.commons.geometry.core.partition.Split;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;

public final class ConvexSubPlane extends AbstractSubPlane<ConvexArea>
    implements ConvexSubHyperplane<Vector3D>  {

    private ConvexSubPlane(Plane plane) {
        super(plane);
    }

    /** {@inheritDoc} */
    @Override
    public List<ConvexSubPlane> toConvex() {
        return Arrays.asList(this);
    }

    /** {@inheritDoc} */
    @Override
    public Split<ConvexSubPlane> split(Hyperplane<Vector3D> splitter) {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public ConvexSubPlane transform(Transform<Vector3D> transform) {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public ConvexArea getSubspaceRegion() {
        // TODO Auto-generated method stub
        return null;
    }
}
