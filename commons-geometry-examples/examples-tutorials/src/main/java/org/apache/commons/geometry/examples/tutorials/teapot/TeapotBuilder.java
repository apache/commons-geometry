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
package org.apache.commons.geometry.examples.tutorials.teapot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.DoubleFunction;
import java.util.function.UnaryOperator;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.AffineTransformMatrix3D;
import org.apache.commons.geometry.euclidean.threed.Bounds3D;
import org.apache.commons.geometry.euclidean.threed.Plane;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.Planes;
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.mesh.SimpleTriangleMesh;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.geometry.euclidean.threed.shape.Sphere;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.geometry.io.euclidean.threed.IO3D;
import org.apache.commons.geometry.io.euclidean.threed.obj.ObjWriter;
import org.apache.commons.numbers.angle.PlaneAngleRadians;

/** Class used to construct a simple 3D teapot shape using the
 * {@code commons-geometry-euclidean} module.
 */
public class TeapotBuilder {

    /** Name used to identify the teapot body geometry. */
    private static final String BODY_NAME = "body";

    /** Name used to identify the teapot lid geometry. */
    private static final String LID_NAME = "lid";

    /** Name used to identify the teapot handle geometry. */
    private static final String HANDLE_NAME = "handle";

    /** Name used to identify the teapot spout geometry. */
    private static final String SPOUT_NAME = "spout";

    /** Precision context used during region construction. */
    private final DoublePrecisionContext precision;

    /** Construct a new build instance.
     * @param precision precision context to use during region construction
     */
    public TeapotBuilder(final DoublePrecisionContext precision) {
        this.precision = precision;
    }

    /** Build a teapot as a {@link RegionBSPTree3D}.
     * @return teapot as a BSP tree
     */
    public RegionBSPTree3D buildTeapot() {
        return buildTeapot(null);
    }

    /** Build a teapot as a {@link RegionBSPTree3D}.
     * @param debugOutputs if not null, important geometries used during the construction of the
     *      teapot will be placed in this map, keyed by part name
     * @return teapot as a BSP tree
     */
    public RegionBSPTree3D buildTeapot(final Map<String, RegionBSPTree3D> debugOutputs) {
        // build the parts
        final RegionBSPTree3D body = buildBody(1);
        final RegionBSPTree3D lid = buildLid(body);
        final RegionBSPTree3D handle = buildHandle();
        final RegionBSPTree3D spout = buildSpout(1);

        // combine into the final region
        final RegionBSPTree3D teapot = RegionBSPTree3D.empty();
        teapot.union(body, lid);
        teapot.union(handle);
        teapot.union(spout);

        // subtract scaled-down versions of the body and spout to
        // create the hollow interior
        teapot.difference(buildBody(0.9));
        teapot.difference(buildSpout(0.8));

        // add debug outputs if needed
        if (debugOutputs != null) {
            debugOutputs.put(BODY_NAME, body);
            debugOutputs.put(LID_NAME, lid);
            debugOutputs.put(HANDLE_NAME, handle);
            debugOutputs.put(SPOUT_NAME, spout);
        }

        return teapot;
    }

    /** Build a teapot separated into its component parts. The keys of the returned map are
     * the component names and the values are the regions.
     * @return map of teapot component names to regions
     */
    public Map<String, RegionBSPTree3D> buildSeparatedTeapot() {
        // construct the single-piece teapot
        final RegionBSPTree3D teapot = buildTeapot();

        // create a region to extract the lid
        final AffineTransformMatrix3D innerCylinderTransform = AffineTransformMatrix3D.createScale(0.4, 0.4, 1)
                .translate(0, 0, 0.5);
        final RegionBSPTree3D innerCylinder = buildUnitCylinderMesh(1, 20, innerCylinderTransform).toTree();

        final AffineTransformMatrix3D outerCylinderTransform = AffineTransformMatrix3D.createScale(0.5, 0.5, 10);
        final RegionBSPTree3D outerCylinder = buildUnitCylinderMesh(1, 20, outerCylinderTransform).toTree();

        final Plane step = Planes.fromPointAndNormal(Vector3D.of(0, 0, 0.63), Vector3D.Unit.MINUS_Z, precision);

        final RegionBSPTree3D extractor = RegionBSPTree3D.from(Arrays.asList(step.span()));
        extractor.union(innerCylinder);
        extractor.intersection(outerCylinder);

        // extract the lid
        final RegionBSPTree3D lid = RegionBSPTree3D.empty();
        lid.intersection(teapot, extractor);

        // remove the lid from the body
        final RegionBSPTree3D body = RegionBSPTree3D.empty();
        body.difference(teapot, extractor);

        // build the output
        final Map<String, RegionBSPTree3D> result = new LinkedHashMap<>();
        result.put(LID_NAME, lid);
        result.put(BODY_NAME, body);

        return result;
    }

    /** Build the teapot body.
     * @param initialRadius radius of the sphere used as the first step in body construction
     * @return teapot body
     */
    private RegionBSPTree3D buildBody(final double initialRadius) {
        // construct a BSP tree sphere approximation
        final Sphere sphere = Sphere.from(Vector3D.ZERO, initialRadius, precision);
        final RegionBSPTree3D body = sphere.toTree(4);

        // squash it a little bit along the z-axis
        final AffineTransformMatrix3D t = AffineTransformMatrix3D.createScale(1, 1, 0.75);
        body.transform(t);

        // cut off part of the bottom to make it flat
        final Plane bottomPlane = Planes.fromPointAndNormal(
                Vector3D.of(0, 0, -0.6 * initialRadius),
                Vector3D.Unit.PLUS_Z,
                precision);
        final PlaneConvexSubset bottom = bottomPlane.span();
        body.difference(RegionBSPTree3D.from(Arrays.asList(bottom)));

        return body;
    }

    /** Build the lid of the teapot.
     * @param body region representing the teapot lid
     * @return teapot lid
     */
    private RegionBSPTree3D buildLid(final RegionBSPTree3D body) {
        // make a copy of the body so that we match its curve exactly
        final RegionBSPTree3D lid = body.copy();

        // translate the lid to be above the body
        final AffineTransformMatrix3D t = AffineTransformMatrix3D.createTranslation(0, 0, 0.03);
        lid.transform(t);

        // intersect the translated body with a cylinder
        final TriangleMesh cylinder =
                buildUnitCylinderMesh(1, 20, AffineTransformMatrix3D.createScale(0.5, 0.5, 10));
        lid.intersection(cylinder.toTree());

        // add a small squashed sphere on top; use the bounds of the top in order to place
        // the sphere at the correct position
        final Sphere sphere = Sphere.from(Vector3D.of(0, 0, 0), 0.15, precision);
        final RegionBSPTree3D sphereTree = sphere.toTree(2);

        final Bounds3D lidBounds = lid.getBounds();
        final double sphereZ = lidBounds.getMax().getZ() + 0.075;
        sphereTree.transform(AffineTransformMatrix3D.createScale(1, 1, 0.75)
                .translate(0, 0, sphereZ));

        // make the small sphere a part of the top
        lid.union(sphereTree);

        return lid;
    }

    /** Build the handle of the teapot.
     * @return teapot handle
     */
    private RegionBSPTree3D buildHandle() {
        final double handleRadius = 0.1;
        final double height = 1 - (2 * handleRadius);

        final AffineTransformMatrix3D scale =
                AffineTransformMatrix3D.createScale(handleRadius, handleRadius, height);

        final QuaternionRotation startRotation =
                QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, -PlaneAngleRadians.PI_OVER_TWO);
        final QuaternionRotation endRotation =
                QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, PlaneAngleRadians.PI_OVER_TWO);
        final DoubleFunction<QuaternionRotation> slerp = startRotation.slerp(endRotation);

        final Vector3D curveCenter = Vector3D.of(0.5 * height, 0, 0);

        final AffineTransformMatrix3D translation =
                AffineTransformMatrix3D.createTranslation(Vector3D.of(-1.38, 0, 0));

        final UnaryOperator<Vector3D> vertexTransform = v -> {
            final double t = v.getZ();

            final Vector3D scaled = scale.apply(v);

            final QuaternionRotation rot = slerp.apply(t);
            final AffineTransformMatrix3D mat = AffineTransformMatrix3D.createRotation(curveCenter, rot);

            final Vector3D rotated = mat.apply(Vector3D.of(scaled.getX(), scaled.getY(), 0));

            final Vector3D result = (t > 0 && t < 1) ?
                    rotated :
                    rotated.add(Vector3D.Unit.PLUS_X);

            return translation.apply(result);
        };

        return buildUnitCylinderMesh(10, 14, vertexTransform).toTree();
    }

    /** Build the teapot spout.
     * @param initialRadius radius of the cylinder used as the first step in spout
     *      construction
     * @return teapot spout
     */
    private RegionBSPTree3D buildSpout(final double initialRadius) {
        final Vector2D baseScale = Vector2D.of(0.4, 0.2).multiply(initialRadius);
        final Vector2D topScale = baseScale.multiply(0.6);
        final double shearZ = 0.9;

        final AffineTransformMatrix3D translation =
                AffineTransformMatrix3D.createTranslation(Vector3D.of(0.25, 0, -0.4));

        final UnaryOperator<Vector3D> vertexTransform = v -> {
            final Vector2D scale = baseScale.lerp(topScale, v.getZ());

            final Vector3D tv = Vector3D.of(
                        (v.getX() * scale.getX()) + (v.getZ() * shearZ),
                        v.getY() * scale.getY(),
                        v.getZ()
                    );

            return translation.apply(tv);
        };

        return buildUnitCylinderMesh(1, 14, vertexTransform).toTree();
    }

    /** Construct a triangle mesh approximating a cylinder of radius one and length one oriented with its
     * based on the origin and extending along the positive z-axis. The vertices may be transformed by
     * {@code vertexTransform} during construction, meaning that the resulting mesh may no longer represent
     * a cylinder.
     * @param segments number of vertical segments used in the cylinder
     * @param circleVertexCount number of vertices used to approximate the outside circle
     * @param vertexTransform function used to transform each computed vertex from its position on the unit
     *      cylinder to its position in the returned mesh
     * @return triangle mesh
     */
    private TriangleMesh buildUnitCylinderMesh(final int segments, final int circleVertexCount,
            final UnaryOperator<Vector3D> vertexTransform) {

        final SimpleTriangleMesh.Builder builder = SimpleTriangleMesh.builder(precision);

        // add the cylinder vertices
        final double zDelta = 1.0 / segments;
        double zValue;

        final double azDelta = PlaneAngleRadians.TWO_PI / circleVertexCount;
        double az;

        Vector3D vertex;
        for (int i = 0; i <= segments; ++i) {
            zValue = i * zDelta;

            for (int v = 0; v < circleVertexCount; ++v) {
                az = v * azDelta;

                vertex = Vector3D.of(
                        Math.cos(az),
                        Math.sin(az),
                        zValue);
                builder.addVertex(vertexTransform.apply(vertex));
            }
        }

        // add the bottom faces using a triangle fan, making sure
        // that the triangles are oriented so that the face normal
        // points down
        for (int i = 1; i < circleVertexCount - 1; ++i) {
            builder.addFace(0, i + 1, i);
        }

        // add the side faces
        int circleStart;
        int v1;
        int v2;
        int v3;
        int v4;
        for (int s = 0; s < segments; ++s) {
            circleStart = s * circleVertexCount;

            for (int i = 0; i < circleVertexCount; ++i) {
                v1 = i + circleStart;
                v2 = ((i + 1) % circleVertexCount) + circleStart;
                v3 = v2 + circleVertexCount;
                v4 = v1 + circleVertexCount;

                builder
                    .addFace(v1, v2, v3)
                    .addFace(v1, v3, v4);
            }
        }

        // add the top faces using a triangle fan
        final int lastCircleStart = circleVertexCount * segments;
        for (int i = 1 + lastCircleStart; i < builder.getVertexCount() - 1; ++i) {
            builder.addFace(lastCircleStart, i, i + 1);
        }

        return builder.build();
    }

    /** Entry point for command-line execution of the {@link TeapotBuilder} class. Two positional
     * arguments are supported:
     * <ol>
     *  <li><em>outputFile</em> - File to write the constructed teapot to.</li>
     *  <li><em>debugDir</em> - (Optional) Directory to write geometry files for important
     *      components used in the construction of the teapot.</li>
     * </ol>
     * @param args argument array
     * @throws IOException if an I/O error occurs
     */
    public static void main(final String[] args) throws IOException {
        if (args.length < 1) {
            throw new IllegalArgumentException("Output file argument is required");
        }

        final Path outputFile = Paths.get(args[0]);
        final DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-10);

        final TeapotBuilder builder = new TeapotBuilder(precision);

        final Map<String, RegionBSPTree3D> debugOutputs = new HashMap<>();

        final RegionBSPTree3D teapot = builder.buildTeapot(debugOutputs);

        IO3D.write(teapot, outputFile);

        if (args.length > 1) {
            // write additional files to the debug dir
            final Path debugDir = Paths.get(args[1]);
            Files.createDirectories(debugDir);

            // build and write teapot components
            final Map<String, RegionBSPTree3D> partMap = builder.buildSeparatedTeapot();
            try (ObjWriter writer = new ObjWriter(Files.newBufferedWriter(debugDir.resolve("separated-teapot.obj")))) {

                for (Map.Entry<String, RegionBSPTree3D> entry : partMap.entrySet()) {
                    writer.writeObjectName(entry.getKey());
                    writer.writeBoundaries(entry.getValue());
                }
            }

            // write debug outputs
            for (Map.Entry<String, RegionBSPTree3D> entry : debugOutputs.entrySet()) {
                IO3D.write(entry.getValue(), debugDir.resolve(entry.getKey() + ".stl"));
            }
        }
    }
}
