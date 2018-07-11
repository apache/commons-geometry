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
package org.apache.commons.geometry.core.internal;

import org.junit.Assert;
import org.junit.Test;

public class SimpleTupleFormatTest {

    private static final double EPS = 1e-10;

    private static final String OPEN_PAREN = "(";
    private static final String CLOSE_PAREN = ")";

    private static DoubleFunction1N<Stub1D> FACTORY_1D = new DoubleFunction1N<Stub1D>() {

        @Override
        public Stub1D apply(double v) {
            Stub1D result = new Stub1D();
            result.v = v;

            return result;
        }
    };

    private static DoubleFunction2N<Stub2D> FACTORY_2D = new DoubleFunction2N<Stub2D>() {

        @Override
        public Stub2D apply(double v1, double v2) {
            Stub2D result = new Stub2D();
            result.v1 = v1;
            result.v2 = v2;

            return result;
        }
    };

    private static DoubleFunction3N<Stub3D> FACTORY_3D = new DoubleFunction3N<Stub3D>() {

        @Override
        public Stub3D apply(double v1, double v2, double v3) {
            Stub3D result = new Stub3D();
            result.v1 = v1;
            result.v2 = v2;
            result.v3 = v3;

            return result;
        }
    };

    @Test
    public void testConstructor() {
        // act
        SimpleTupleFormat formatter = new SimpleTupleFormat("|", "{", "}");

        // assert
        Assert.assertEquals("|", formatter.getSeparator());
        Assert.assertEquals("{", formatter.getPrefix());
        Assert.assertEquals("}", formatter.getSuffix());
    }

    @Test
    public void testConstructor_defaultSeparator() {
        // act
        SimpleTupleFormat formatter = new SimpleTupleFormat("{", "}");

        // assert
        Assert.assertEquals(",", formatter.getSeparator());
        Assert.assertEquals("{", formatter.getPrefix());
        Assert.assertEquals("}", formatter.getSuffix());
    }

    @Test
    public void testFormat1D() {
        // arrange
        SimpleTupleFormat formatter = new SimpleTupleFormat(OPEN_PAREN, CLOSE_PAREN);

        // act/assert
        Assert.assertEquals("(1.0)", formatter.format(1.0));
        Assert.assertEquals("(-1.0)", formatter.format(-1.0));
        Assert.assertEquals("(NaN)", formatter.format(Double.NaN));
        Assert.assertEquals("(-Infinity)", formatter.format(Double.NEGATIVE_INFINITY));
        Assert.assertEquals("(Infinity)", formatter.format(Double.POSITIVE_INFINITY));
    }

    @Test
    public void testFormat1D_noPrefixSuffix() {
        // arrange
        SimpleTupleFormat formatter = new SimpleTupleFormat(null, null);

        // act/assert
        Assert.assertEquals("1.0", formatter.format(1.0));
        Assert.assertEquals("-1.0", formatter.format(-1.0));
        Assert.assertEquals("NaN", formatter.format(Double.NaN));
        Assert.assertEquals("-Infinity", formatter.format(Double.NEGATIVE_INFINITY));
        Assert.assertEquals("Infinity", formatter.format(Double.POSITIVE_INFINITY));
    }

    @Test
    public void testFormat2D() {
        // arrange
        SimpleTupleFormat formatter = new SimpleTupleFormat(OPEN_PAREN, CLOSE_PAREN);

        // act/assert
        Assert.assertEquals("(1.0, -1.0)", formatter.format(1.0, -1.0));
        Assert.assertEquals("(-1.0, 1.0)", formatter.format(-1.0, 1.0));
        Assert.assertEquals("(NaN, -Infinity)", formatter.format(Double.NaN, Double.NEGATIVE_INFINITY));
        Assert.assertEquals("(-Infinity, Infinity)", formatter.format(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
    }

    @Test
    public void testFormat2D_noPrefixSuffix() {
        // arrange
        SimpleTupleFormat formatter = new SimpleTupleFormat(null, null);

        // act/assert
        Assert.assertEquals("1.0, -1.0", formatter.format(1.0, -1.0));
        Assert.assertEquals("-1.0, 1.0", formatter.format(-1.0, 1.0));
        Assert.assertEquals("NaN, -Infinity", formatter.format(Double.NaN, Double.NEGATIVE_INFINITY));
        Assert.assertEquals("-Infinity, Infinity", formatter.format(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
    }

    @Test
    public void testFormat3D() {
        // arrange
        SimpleTupleFormat formatter = new SimpleTupleFormat(OPEN_PAREN, CLOSE_PAREN);

        // act/assert
        Assert.assertEquals("(1.0, 0.0, -1.0)", formatter.format(1.0, 0.0, -1.0));
        Assert.assertEquals("(-1.0, 1.0, 0.0)", formatter.format(-1.0, 1.0, 0.0));
        Assert.assertEquals("(NaN, -Infinity, Infinity)", formatter.format(Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
    }

    @Test
    public void testFormat3D_noPrefixSuffix() {
        // arrange
        SimpleTupleFormat formatter = new SimpleTupleFormat(null, null);

        // act/assert
        Assert.assertEquals("1.0, 0.0, -1.0", formatter.format(1.0, 0.0, -1.0));
        Assert.assertEquals("-1.0, 1.0, 0.0", formatter.format(-1.0, 1.0, 0.0));
        Assert.assertEquals("NaN, -Infinity, Infinity", formatter.format(Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
    }

    @Test
    public void testFormat_longTokens() {
        // arrange
        SimpleTupleFormat formatter = new SimpleTupleFormat("||", "<<", ">>");

        // act/assert
        Assert.assertEquals("<<1.0>>", formatter.format(1.0));
        Assert.assertEquals("<<1.0|| 2.0>>", formatter.format(1.0, 2.0));
        Assert.assertEquals("<<1.0|| 2.0|| 3.0>>", formatter.format(1.0, 2.0, 3.0));
    }

    @Test
    public void testParse1D() {
        // arrange
        SimpleTupleFormat formatter = new SimpleTupleFormat(OPEN_PAREN, CLOSE_PAREN);

        // act/assert
        checkParse1D(formatter, "(1)", 1.0);
        checkParse1D(formatter, "(-1)", -1.0);

        checkParse1D(formatter, "(0.01)", 0.01);
        checkParse1D(formatter, "(-1e-2)", -0.01);

        checkParse1D(formatter, "(100)", 100);
        checkParse1D(formatter, "(-1e2)", -100);

        checkParse1D(formatter, " (\n 1 \t) ", 1);
        checkParse1D(formatter, "\n ( -1 \t)\r\n", -1);

        checkParse1D(formatter, "(1, )", 1.0);
        checkParse1D(formatter, "(-1, )", -1.0);

        checkParse1D(formatter, "(NaN)", Double.NaN);
        checkParse1D(formatter, "(-Infinity)", Double.NEGATIVE_INFINITY);
        checkParse1D(formatter, "(Infinity)", Double.POSITIVE_INFINITY);
    }

    @Test
    public void testParse1D_noPrefixSuffix() {
        // arrange
        SimpleTupleFormat formatter = new SimpleTupleFormat(null, null);

        // act/assert
        checkParse1D(formatter, "1", 1.0);
        checkParse1D(formatter, "-1", -1.0);

        checkParse1D(formatter, "0.01", 0.01);
        checkParse1D(formatter, "-1e-2", -0.01);

        checkParse1D(formatter, "100", 100);
        checkParse1D(formatter, "-1e2", -100);

        checkParse1D(formatter, " \n 1 \t ", 1);
        checkParse1D(formatter, "\n  -1 \t\r\n", -1);

        checkParse1D(formatter, "1, ", 1.0);
        checkParse1D(formatter, "-1, ", -1.0);

        checkParse1D(formatter, "NaN", Double.NaN);
        checkParse1D(formatter, "-Infinity", Double.NEGATIVE_INFINITY);
        checkParse1D(formatter, "Infinity", Double.POSITIVE_INFINITY);
    }

    @Test
    public void testParse1D_failure() {
        // arrange
        SimpleTupleFormat formatter = new SimpleTupleFormat(OPEN_PAREN, CLOSE_PAREN);

        // act/assert
        checkParse1DFailure(formatter, "", "index 0: expected \"(\" but found \"\"");
        checkParse1DFailure(formatter, "(1 ", "index 3: expected \")\" but found \"\"");

        checkParse1DFailure(formatter, "(abc)", "unable to parse number from string \"abc\"");

        checkParse1DFailure(formatter, "(1) 1", "index 4: unexpected content");
    }

    @Test
    public void testParse2D() {
        // arrange
        SimpleTupleFormat formatter = new SimpleTupleFormat(OPEN_PAREN, CLOSE_PAREN);

        // act/assert
        checkParse2D(formatter, "(1,-2)", 1.0, -2.0);
        checkParse2D(formatter, "(2,-1)", 2.0, -1.0);

        checkParse2D(formatter, "(0.01, -0.02)", 0.01, -0.02);
        checkParse2D(formatter, "(-1e-2,2e-2)", -0.01, 0.02);

        checkParse2D(formatter, "(100,  -1e2)", 100, -100);

        checkParse2D(formatter, " (\n 1 , 2 \t) ", 1, 2);
        checkParse2D(formatter, "\n ( -1 , -2 \t)\r\n", -1, -2);

        checkParse2D(formatter, "(1, 2, )", 1.0, 2.0);
        checkParse2D(formatter, "(-1, -2,)", -1.0, -2.0);

        checkParse2D(formatter, "(NaN, -Infinity)", Double.NaN, Double.NEGATIVE_INFINITY);
        checkParse2D(formatter, "(-Infinity, Infinity)", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testParse2D_noPrefixSuffix() {
        // arrange
        SimpleTupleFormat formatter = new SimpleTupleFormat(null, null);

        // act/assert
        checkParse2D(formatter, "1,-2", 1.0, -2.0);
        checkParse2D(formatter, "2,-1", 2.0, -1.0);

        checkParse2D(formatter, "0.01, -0.02", 0.01, -0.02);
        checkParse2D(formatter, "-1e-2,2e-2", -0.01, 0.02);

        checkParse2D(formatter, "100,  -1e2", 100, -100);

        checkParse2D(formatter, " \n 1 , 2 \t ", 1, 2);
        checkParse2D(formatter, "\n  -1 , -2 \t\r\n", -1, -2);

        checkParse2D(formatter, "1, 2, ", 1.0, 2.0);
        checkParse2D(formatter, "-1, -2,", -1.0, -2.0);

        checkParse2D(formatter, "NaN, -Infinity", Double.NaN, Double.NEGATIVE_INFINITY);
        checkParse2D(formatter, "-Infinity, Infinity", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testParse2D_failure() {
        // arrange
        SimpleTupleFormat formatter = new SimpleTupleFormat(OPEN_PAREN, CLOSE_PAREN);

        // act/assert
        checkParse2DFailure(formatter, "", "index 0: expected \"(\" but found \"\"");
        checkParse2DFailure(formatter, "(1, 2 ", "index 6: expected \")\" but found \"\"");

        checkParse2DFailure(formatter, "(0,abc)", "index 3: unable to parse number from string \"abc\"");

        checkParse2DFailure(formatter, "(1, 2) 1", "index 7: unexpected content");
    }

    @Test
    public void testParse3D() {
        // arrange
        SimpleTupleFormat formatter = new SimpleTupleFormat(OPEN_PAREN, CLOSE_PAREN);

        // act/assert
        checkParse3D(formatter, "(1,-2,3)", 1.0, -2.0, 3.0);
        checkParse3D(formatter, "(2,-1,3)", 2.0, -1.0, 3.0);

        checkParse3D(formatter, "(0.01, -0.02, 0.3)", 0.01, -0.02, 0.3);
        checkParse3D(formatter, "(-1e-2,2e-2,-3E-1)", -0.01, 0.02, -0.3);

        checkParse3D(formatter, "(100,  -1e2,2E10)", 100, -100, 2e10);

        checkParse3D(formatter, " (\n 1 , 2 , 3 \t) ", 1, 2, 3);
        checkParse3D(formatter, "\n ( -1 , -2 ,  -3 \t)\r\n", -1, -2, -3);

        checkParse3D(formatter, "(1, 2, 3, )", 1.0, 2.0, 3.0);
        checkParse3D(formatter, "(-1, -2, -3,)", -1.0, -2.0, -3.0);

        checkParse3D(formatter, "(NaN, -Infinity, Infinity)", Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testParse3D_noPrefixSuffix() {
        // arrange
        SimpleTupleFormat formatter = new SimpleTupleFormat(null, null);

        // act/assert
        checkParse3D(formatter, "1,-2,3", 1.0, -2.0, 3.0);
        checkParse3D(formatter, "2,-1,3", 2.0, -1.0, 3.0);

        checkParse3D(formatter, "0.01, -0.02, 0.3", 0.01, -0.02, 0.3);
        checkParse3D(formatter, "-1e-2,2e-2,-3E-1", -0.01, 0.02, -0.3);

        checkParse3D(formatter, "100,  -1e2,2E10", 100, -100, 2e10);

        checkParse3D(formatter, " \n 1 , 2 , 3 \t ", 1, 2, 3);
        checkParse3D(formatter, "\n  -1 , -2 ,  -3 \t\r\n", -1, -2, -3);

        checkParse3D(formatter, "1, 2, 3, ", 1.0, 2.0, 3.0);
        checkParse3D(formatter, "-1, -2, -3,", -1.0, -2.0, -3.0);

        checkParse3D(formatter, "NaN, -Infinity, Infinity", Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testParse3D_failure() {
        // arrange
        SimpleTupleFormat formatter = new SimpleTupleFormat(OPEN_PAREN, CLOSE_PAREN);

        // act/assert
        checkParse3DFailure(formatter, "", "index 0: expected \"(\" but found \"\"");
        checkParse3DFailure(formatter, "(1, 2, 3", "index 8: expected \")\" but found \"\"");

        checkParse3DFailure(formatter, "(0,0,abc)", "index 5: unable to parse number from string \"abc\"");

        checkParse3DFailure(formatter, "(1, 2, 3) 1", "index 10: unexpected content");
    }

    @Test
    public void testParse_longTokens() {
        // arrange
        SimpleTupleFormat formatter = new SimpleTupleFormat("||", "<<", ">>");

        // act/assert
        checkParse1D(formatter, "<<1.0>>", 1.0);
        checkParse2D(formatter, "<<1.0|| 2.0>>", 1.0, 2.0);
        checkParse3D(formatter, "<<1.0|| 2.0|| 3.0>>", 1.0, 2.0, 3.0);
    }

    @Test
    public void testParse_longTokens_failure() {
        // arrange
        SimpleTupleFormat formatter = new SimpleTupleFormat("||", "<<", ">>");

        // act/assert
        checkParse1DFailure(formatter, "<", "index 0: expected \"<<\" but found \"<\"");
        checkParse1DFailure(formatter, "<1.0>>", "index 0: expected \"<<\" but found \"<1\"");
        checkParse2DFailure(formatter, "<<1.0| 2.0>>", "index 2: unable to parse number from string \"1.0| 2.0\"");
        checkParse3DFailure(formatter, "<<1.0|| 2.0|| 3.0>", "index 13: unable to parse number from string \" 3.0>\"");
    }

    @Test
    public void testDefaultInstance() {
        // act
        SimpleTupleFormat formatter = SimpleTupleFormat.getDefault();

        // assert
        Assert.assertEquals(",", formatter.getSeparator());
        Assert.assertEquals("(", formatter.getPrefix());
        Assert.assertEquals(")", formatter.getSuffix());

        Assert.assertEquals("(1.0, 2.0)", formatter.format(1, 2));
    }

    private void checkParse1D(SimpleTupleFormat formatter, String str, double v) {
        Stub1D result = formatter.parse(str, FACTORY_1D);

        Assert.assertEquals(v, result.v, EPS);
    }

    private void checkParse1DFailure(SimpleTupleFormat formatter, String str, String msgSubstr) {
        try {
            formatter.parse(str, FACTORY_1D);
            Assert.fail("Operation should have failed");
        }
        catch (IllegalArgumentException exc) {
            String excMsg = exc.getMessage();
            Assert.assertTrue("Expected message to contain [" + msgSubstr + "] but was [" + excMsg + "]",
                    excMsg.contains(msgSubstr));
        }
    }

    private void checkParse2D(SimpleTupleFormat formatter, String str, double v1, double v2) {
        Stub2D result = formatter.parse(str, FACTORY_2D);

        Assert.assertEquals(v1, result.v1, EPS);
        Assert.assertEquals(v2, result.v2, EPS);
    }

    private void checkParse2DFailure(SimpleTupleFormat formatter, String str, String msgSubstr) {
        try {
            formatter.parse(str, FACTORY_2D);
            Assert.fail("Operation should have failed");
        }
        catch (IllegalArgumentException exc) {
            String excMsg = exc.getMessage();
            Assert.assertTrue("Expected message to contain [" + msgSubstr + "] but was [" + excMsg + "]",
                    excMsg.contains(msgSubstr));
        }
    }

    private void checkParse3D(SimpleTupleFormat formatter, String str, double v1, double v2, double v3) {
        Stub3D result = formatter.parse(str, FACTORY_3D);

        Assert.assertEquals(v1, result.v1, EPS);
        Assert.assertEquals(v2, result.v2, EPS);
        Assert.assertEquals(v3, result.v3, EPS);
    }

    private void checkParse3DFailure(SimpleTupleFormat formatter, String str, String msgSubstr) {
        try {
            formatter.parse(str, FACTORY_3D);
            Assert.fail("Operation should have failed");
        }
        catch (IllegalArgumentException exc) {
            String excMsg = exc.getMessage();
            Assert.assertTrue("Expected message to contain [" + msgSubstr + "] but was [" + excMsg + "]",
                    excMsg.contains(msgSubstr));
        }
    }

    private static class Stub1D {
        public double v;
    }

    private static class Stub2D {
        public double v1;
        public double v2;
    }

    private static class Stub3D {
        public double v1;
        public double v2;
        public double v3;
    }
}
