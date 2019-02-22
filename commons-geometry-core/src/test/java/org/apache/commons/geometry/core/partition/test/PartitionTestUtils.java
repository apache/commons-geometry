package org.apache.commons.geometry.core.partition.test;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;

public class PartitionTestUtils {

    public static final double EPS = 1e-6;

    public static final DoublePrecisionContext PRECISION =
            new EpsilonDoublePrecisionContext(EPS);
}
