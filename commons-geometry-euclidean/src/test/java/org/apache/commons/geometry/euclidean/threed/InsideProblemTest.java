package org.apache.commons.geometry.euclidean.threed;



import static org.apache.commons.geometry.euclidean.threed.PolyhedronsSet.minXyzPredicate;
import static org.apache.commons.geometry.euclidean.threed.PolyhedronsSet.vertexOnConvexHull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.partitioning.Region.Location;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.PolyhedronsSet;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.junit.Test;


public class InsideProblemTest
{


  @Test
  public void testMinXyZPredicate()
  {
    DoublePrecisionContext precision = new EpsilonDoublePrecisionContext( 1e-10 );

    assertTrue(minXyzPredicate(Vector3D.of( 0, 0, 0 ), Vector3D.of( 1, 0, 0 ), precision));
    assertTrue(minXyzPredicate(Vector3D.of( 0, 0, 0 ), Vector3D.of( 0, 1, 0 ), precision));
    assertTrue(minXyzPredicate(Vector3D.of( 0, 0, 0 ), Vector3D.of( 0, 0, 1 ), precision));

    assertFalse(minXyzPredicate(Vector3D.of( 1, 0, 0 ), Vector3D.of( 0, 0, 0 ), precision));
    assertFalse(minXyzPredicate(Vector3D.of( 0, 1, 0 ), Vector3D.of( 0, 0, 0 ), precision));
    assertFalse(minXyzPredicate(Vector3D.of( 0, 0, 1 ), Vector3D.of( 0, 0, 0 ), precision));
  }  

  @Test
  public void testFindPointOnConvexHull()
  {
    List<Vector3D> vertices = Arrays.asList(
        Vector3D.of( 0, 0, 0 ),
        Vector3D.of( 4, 0, 0 ),
        Vector3D.of( 3, 2, 1 ),
        Vector3D.of( 4, 4, 2 ),
        Vector3D.of( 0, 4, 2 ),
        Vector3D.of( 1, 2, 1 )
    );

    int facets[][] = {
        { 0, 1, 2, 3, 4, 5 },
        { 1, 2, 3, 4, 5, 0 },
        { 2, 3, 4, 5, 0, 1 },
        { 3, 4, 5, 0, 1, 2 },
        { 4, 5, 0, 1, 2, 3 },
        { 5, 0, 1, 2, 3, 4 },
   };

    DoublePrecisionContext precision = new EpsilonDoublePrecisionContext( 1e-10 );

    assertEquals(0 , vertexOnConvexHull( vertices, facets[0], precision ));
    assertEquals(5 , vertexOnConvexHull( vertices, facets[1], precision ));
    assertEquals(4 , vertexOnConvexHull( vertices, facets[2], precision ));
    assertEquals(3 , vertexOnConvexHull( vertices, facets[3], precision ));
    assertEquals(2 , vertexOnConvexHull( vertices, facets[4], precision ));
    assertEquals(1 , vertexOnConvexHull( vertices, facets[5], precision ));
  }

  //    +
  //   / \
  //  /   \
  // +--+--+
  // A prism with an almost triangular base.
  // One edge has been modified to make it concave.

  @Test
  public void testSlightlyConcavePrism()
  {
    Vector3D vertices[] = {
        Vector3D.of( 0, 0, 0 ),
        Vector3D.of( 2, 1e-7, 0 ),
        Vector3D.of( 4, 0, 0 ),
        Vector3D.of( 2, 2, 0 ),
        Vector3D.of( 0, 0, 2 ),
        Vector3D.of( 2, 1e-7, 2 ),
        Vector3D.of( 4, 0, 2 ),
        Vector3D.of( 2, 2, 2 )
    };

    int facets[][] = {
        { 4, 5, 6, 7 },
        { 3, 2, 1, 0 },
        { 0, 1, 5, 4 },
        { 1, 2, 6, 5 },
        { 2, 3, 7, 6 },
        { 3, 0, 4, 7 }
    };

    PolyhedronsSet prism = new PolyhedronsSet(
      Arrays.asList( vertices ),
      Arrays.asList( facets ),
      new EpsilonDoublePrecisionContext( 1e-10 ) );

    // A point above the prism is computed as inside.
    // When the prism is modified to have a convex base, the result is as expected.
    assertEquals( Location.OUTSIDE, prism.checkPoint( Vector3D.of( 2, 1, 3 ) ) );
  }


}