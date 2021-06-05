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
package org.apache.commons.geometry.examples.tutorials.bsp;

import org.junit.jupiter.api.Test;

class BSPTreeTutorialsTest {

    private static final String[] ARGS = {"target"};

    @Test
    void testBottomUp() {
        BottomUpBSPTreeConstruction.main(ARGS);
    }

    @Test
    void testTopDown() {
        TopDownBSPTreeConstruction.main(ARGS);
    }

    @Test
    void testHexagonUnbalanced() {
        HexagonUnbalanced.main(ARGS);
    }

    @Test
    void testHexagonStructuralCut() {
        HexagonStructuralCut.main(ARGS);
    }

    @Test
    void testHexagonPartitionedRegion() {
        HexagonPartitionedRegion.main(ARGS);
    }

    @Test
    void testUnion() {
        BSPTreeUnion.main(ARGS);
    }

    @Test
    void testXor() {
        BSPTreeXor.main(ARGS);
    }
}
