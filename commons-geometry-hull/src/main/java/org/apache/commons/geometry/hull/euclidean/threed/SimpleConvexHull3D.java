package org.apache.commons.geometry.hull.euclidean.threed;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.geometry.euclidean.threed.ConvexPolygon3D;
import org.apache.commons.geometry.euclidean.threed.ConvexVolume;
import org.apache.commons.geometry.euclidean.threed.Vector3D;

/**
 * This class represents a convex hull in three dimensions.
 */
public class SimpleConvexHull3D implements ConvexHull3D {

    /**
     * The vertices of the convex hull.
     */
    private List<Vector3D> vertices;

    /**
     * The region defined by the hull.
     */
    private ConvexVolume region;

    private Collection<ConvexPolygon3D> facets;

    /**
     * Simple constructor. No validation is performed.
     *
     * @param facets the facets of the hull.s
     */
    SimpleConvexHull3D(Collection<? extends ConvexPolygon3D> facets) {
        vertices = Collections
                .unmodifiableList(facets.stream().flatMap(f -> f.getVertices().stream()).collect(Collectors.toList()));
        region = ConvexVolume.fromBounds(() -> facets.stream().map(ConvexPolygon3D::getPlane).iterator());
        this.facets = Collections.unmodifiableSet(new HashSet<>(facets));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Vector3D> getVertices() {
        return vertices;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConvexVolume getRegion() {
        return region;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<? extends ConvexPolygon3D> getFacets() {
        return facets;
    }

}
