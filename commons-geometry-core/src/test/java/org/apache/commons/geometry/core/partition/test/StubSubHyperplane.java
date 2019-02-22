package org.apache.commons.geometry.core.partition.test;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.partition.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partition.Hyperplane;
import org.apache.commons.geometry.core.partition.SplitConvexSubHyperplane;

public class StubSubHyperplane implements ConvexSubHyperplane<StubPoint> {

    private final StubHyperplane hyperplane;

    public StubSubHyperplane(final StubHyperplane hyperplane) {
        this.hyperplane = hyperplane;
    }

    @Override
    public Hyperplane<StubPoint> getHyperplane() {
        return hyperplane;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isInfinite() {
        return false;
    }

    @Override
    public double size() {
        return 0;
    }

    @Override
    public List<ConvexSubHyperplane<StubPoint>> toConvex() {
        return Arrays.asList(this);
    }

    @Override
    public SplitConvexSubHyperplane<StubPoint> split(Hyperplane<StubPoint> splitter) {
        final double offset = splitter.offset(hyperplane.getLocation());

        int comparison = PartitionTestUtils.PRECISION.compare(offset, 0.0);

        if (comparison < 0) {
            return new SplitConvexSubHyperplane<StubPoint>(splitter, null, this);
        } else if (comparison > 0) {
            return new SplitConvexSubHyperplane<StubPoint>(splitter, this, null);
        } else {
            return new SplitConvexSubHyperplane<StubPoint>(splitter, null, null);
        }
    }

    @Override
    public String toString() {
        return "subhyperplane[hyperplane=" + hyperplane + "]";
    }
}
