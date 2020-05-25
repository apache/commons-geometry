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

/** Simple implementation of {@link Triangle3D}.
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
final class SimpleTriangle3D extends AbstractConvexPolygon3D implements Triangle3D {

    /** First point in the triangle. */
    private final Vector3D p1;

    /** Second point in the triangle. */
    private final Vector3D p2;

    /** Third point in the triangle. */
    private final Vector3D p3;

    /** Construct a new instance from a plane and 3 points. Callers are responsible for ensuring that
     * the points lie on the plane and define a triangle. No validation is performed.
     * @param plane the plane containing the triangle
     * @param p1 first point in the triangle
     * @param p2 second point in the triangle
     * @param p3 third point in the triangle
     */
    SimpleTriangle3D(final Plane plane, final Vector3D p1, final Vector3D p2, final Vector3D p3) {
        super(plane);

        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D getPoint1() {
        return p1;
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D getPoint2() {
        return p2;
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D getPoint3() {
        return p3;
    }

    /** {@inheritDoc} */
    @Override
    public List<Vector3D> getVertices() {
        return Arrays.asList(p1, p2, p3);
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        final Vector3D v1 = p1.vectorTo(p2);
        final Vector3D v2 = p1.vectorTo(p3);
        return 0.5 * v1.cross(v2).norm();
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D getCentroid() {
        return Vector3D.centroid(p1, p2, p3);
    }

    /** {@inheritDoc} */
    @Override
    public SimpleTriangle3D reverse() {
        final Plane rPlane = getPlane().reverse();

        return new SimpleTriangle3D(rPlane, p1, p3, p2); // reverse point ordering
    }

    /** {@inheritDoc} */
    @Override
    public SimpleTriangle3D transform(final Transform<Vector3D> transform) {
        final Plane tPlane = getPlane().transform(transform);
        final Vector3D t1 = transform.apply(p1);
        final Vector3D t2 = transform.apply(p2);
        final Vector3D t3 = transform.apply(p3);

        return new SimpleTriangle3D(tPlane, t1, t2, t3);
    }
}
