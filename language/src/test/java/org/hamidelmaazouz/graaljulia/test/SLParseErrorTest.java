package org.hamidelmaazouz.graaljulia.test;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SLParseErrorTest {
    private Context context;

    @Before
    public void setUp() {
        context = Context.create("sl");
    }

    @After
    public void tearDown() {
        context = null;
    }

    @Test
    public void testParseError() {
        try {
            final Source src = Source.newBuilder("sl", "function testSyntaxError(a) {break;} function main() {return testSyntaxError;}", "testSyntaxError.sl").buildLiteral();
            context.eval(src);
            Assert.assertTrue("Should not reach here.", false);
        } catch (PolyglotException e) {
            Assert.assertTrue("Should be a syntax error.", e.isSyntaxError());
            Assert.assertNotNull("Should have source section.", e.getSourceLocation());
        }
    }

    @Test
    public void testParseErrorEmpty() {
        try {
            final Source src = Source.newBuilder("sl", "", "testSyntaxErrorEmpty.sl").buildLiteral();
            context.eval(src);
            Assert.assertTrue("Should not reach here.", false);
        } catch (PolyglotException e) {
            Assert.assertTrue("Should be a syntax error.", e.isSyntaxError());
            Assert.assertNotNull("Should have source section.", e.getSourceLocation());
        }
    }

    @Test
    public void testParseErrorEOF1() {
        try {
            final Source src = Source.newBuilder("sl", "function main", "testSyntaxErrorEOF1.sl").buildLiteral();
            context.eval(src);
            Assert.assertTrue("Should not reach here.", false);
        } catch (PolyglotException e) {
            Assert.assertTrue("Should be a syntax error.", e.isSyntaxError());
            Assert.assertNotNull("Should have source section.", e.getSourceLocation());
        }
    }

    @Test
    public void testParseErrorEOF2() {
        try {
            final Source src = Source.newBuilder("sl", "function\n", "testSyntaxErrorEOF2.sl").buildLiteral();
            context.eval(src);
            Assert.assertTrue("Should not reach here.", false);
        } catch (PolyglotException e) {
            Assert.assertTrue("Should be a syntax error.", e.isSyntaxError());
            Assert.assertNotNull("Should have source section.", e.getSourceLocation());
        }
    }
}
