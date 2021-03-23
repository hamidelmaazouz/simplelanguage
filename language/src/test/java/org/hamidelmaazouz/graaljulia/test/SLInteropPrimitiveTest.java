package org.hamidelmaazouz.graaljulia.test;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SLInteropPrimitiveTest {
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
    public void testBoolean() {
        final Source src = Source.newBuilder("sl", "function testBoolean(a,b) {return a == b;} function main() {return testBoolean;}", "testBoolean.sl").buildLiteral();
        final Value fnc = context.eval(src);
        Assert.assertTrue(fnc.canExecute());
        fnc.execute(true, false);
    }

    @Test
    public void testChar() {
        final Source src = Source.newBuilder("sl", "function testChar(a,b) {return a == b;} function main() {return testChar;}", "testChar.sl").buildLiteral();
        final Value fnc = context.eval(src);
        Assert.assertTrue(fnc.canExecute());
        fnc.execute('a', 'b');
    }
}
