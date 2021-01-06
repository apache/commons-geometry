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
package org.apache.commons.geometry.core.partitioning.bsp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.test.PartitionTestUtils;
import org.apache.commons.geometry.core.partitioning.test.TestPoint2D;
import org.apache.commons.geometry.core.partitioning.test.TestRegionBSPTree;
import org.junit.jupiter.api.Assertions;

/** Helper class with a fluent API used to construct assert conditions on tree merge operations.
 */
class MergeChecker {

    /** Helper interface used when testing tree merge operations.
     */
    @FunctionalInterface
    public interface Operation {
        TestRegionBSPTree apply(TestRegionBSPTree tree1, TestRegionBSPTree tree2);
    }

    /** First tree in the merge operation */
    private final Supplier<TestRegionBSPTree> tree1Factory;

    /** Second tree in the merge operation */
    private final Supplier<TestRegionBSPTree> tree2Factory;

    /** Merge operation that does not modify either input tree */
    private final Operation constOperation;

    /** Merge operation that stores the result in the first input tree
     * and leaves the second one unmodified.
     */
    private final Operation inPlaceOperation;

    /** The expected node count of the merged tree */
    private int expectedCount = -1;

    /** The expected full state of the merged tree */
    private boolean expectedFull;

    /** The expected empty state of the merged tree */
    private boolean expectedEmpty;

    /** Points expected to lie in the inside of the region */
    private final List<TestPoint2D> insidePoints = new ArrayList<>();

    /** Points expected to lie on the outside of the region */
    private final List<TestPoint2D> outsidePoints = new ArrayList<>();

    /** Points expected to lie on the  boundary of the region */
    private final List<TestPoint2D> boundaryPoints = new ArrayList<>();

    /** Construct a new instance that will verify the output of performing the given merge operation
     * on the input trees.
     * @param tree1Factory first tree factory in the merge operation
     * @param tree2Factory second tree factory in the merge operation
     * @param constOperation object that performs the merge operation in a form that
     *      leaves both argument unmodified
     * @param inPlaceOperation object that performs the merge operation in a form
     *      that stores the result in the first input tree and leaves the second
     *      input unchanged.
     */
    MergeChecker(
            final Supplier<TestRegionBSPTree> tree1Factory,
            final Supplier<TestRegionBSPTree> tree2Factory,
            final Operation constOperation,
            final Operation inPlaceOperation) {

        this.tree1Factory = tree1Factory;
        this.tree2Factory = tree2Factory;
        this.constOperation = constOperation;
        this.inPlaceOperation = inPlaceOperation;
    }

    /** Set the expected node count of the merged tree
     * @param expectedCountVal the expected node count of the merged tree
     * @return this instance
     */
    public MergeChecker count(final int expectedCountVal) {
        this.expectedCount = expectedCountVal;
        return this;
    }

    /** Set the expected full state of the merged tree.
     * @param expectedFullVal the expected full state of the merged tree.
     * @return this instance
     */
    public MergeChecker full(final boolean expectedFullVal) {
        this.expectedFull = expectedFullVal;
        return this;
    }

    /** Set the expected empty state of the merged tree.
     * @param expectedEmptyVal the expected empty state of the merged tree.
     * @return this instance
     */
    public MergeChecker empty(final boolean expectedEmptyVal) {
        this.expectedEmpty = expectedEmptyVal;
        return this;
    }

    /** Add points expected to be on the inside of the merged region.
     * @param points point expected to be on the inside of the merged
     *      region
     * @return this instance
     */
    public MergeChecker inside(final TestPoint2D... points) {
        insidePoints.addAll(Arrays.asList(points));
        return this;
    }

    /** Add points expected to be on the outside of the merged region.
     * @param points point expected to be on the outside of the merged
     *      region
     * @return this instance
     */
    public MergeChecker outside(final TestPoint2D... points) {
        outsidePoints.addAll(Arrays.asList(points));
        return this;
    }

    /** Add points expected to be on the boundary of the merged region.
     * @param points point expected to be on the boundary of the merged
     *      region
     * @return this instance
     */
    public MergeChecker boundary(final TestPoint2D... points) {
        boundaryPoints.addAll(Arrays.asList(points));
        return this;
    }

    /** Perform the merge operation and verify the output.
     */
    public void check() {
        check(null);
    }

    /** Perform the merge operation and verify the output. The given consumer
     * is passed the merge result and can be used to perform extra assertions.
     * @param assertions consumer that will be passed the merge result; may
     *      be null
     */
    public void check(final Consumer<TestRegionBSPTree> assertions) {
        checkConst(assertions);
        checkInPlace(assertions);
    }

    private void checkConst(final Consumer<TestRegionBSPTree> assertions) {
        checkInternal(false, constOperation, assertions);
    }

    private void checkInPlace(final Consumer<TestRegionBSPTree> assertions) {
        checkInternal(true, inPlaceOperation, assertions);
    }

    private void checkInternal(final boolean inPlace, final Operation operation,
            final Consumer<? super TestRegionBSPTree> assertions) {

        final TestRegionBSPTree tree1 = tree1Factory.get();
        final TestRegionBSPTree tree2 = tree2Factory.get();

        // store the number of nodes in each tree before the operation
        final int tree1BeforeCount = tree1.count();
        final int tree2BeforeCount = tree2.count();

        // perform the operation
        final TestRegionBSPTree result = operation.apply(tree1, tree2);

        // verify the internal consistency of all of the involved trees
        PartitionTestUtils.assertTreeStructure(tree1);
        PartitionTestUtils.assertTreeStructure(tree2);
        PartitionTestUtils.assertTreeStructure(result);

        // check full/empty status
        Assertions.assertEquals(expectedFull, result.isFull(), "Unexpected tree 'full' property");
        Assertions.assertEquals(expectedEmpty, result.isEmpty(), "Unexpected tree 'empty' property");

        // check the node count
        if (expectedCount > -1) {
            Assertions.assertEquals(expectedCount, result.count(), "Unexpected node count");
        }

        // check in place or not
        if (inPlace) {
            Assertions.assertSame(tree1, result, "Expected merge operation to be in place");
        } else {
            Assertions.assertNotSame(tree1, result, "Expected merge operation to return a new instance");

            // make sure that tree1 wasn't modified
            Assertions.assertEquals(tree1BeforeCount, tree1.count(), "Tree 1 node count should not have changed");
        }

        // make sure that tree2 wasn't modified
        Assertions.assertEquals(tree2BeforeCount, tree2.count(), "Tree 2 node count should not have changed");

        // check region point locations
        PartitionTestUtils.assertPointLocations(result, RegionLocation.INSIDE, insidePoints);
        PartitionTestUtils.assertPointLocations(result, RegionLocation.OUTSIDE, outsidePoints);
        PartitionTestUtils.assertPointLocations(result, RegionLocation.BOUNDARY, boundaryPoints);

        // pass the result to the given function for any additional assertions
        if (assertions != null) {
            assertions.accept(result);
        }
    }
}
