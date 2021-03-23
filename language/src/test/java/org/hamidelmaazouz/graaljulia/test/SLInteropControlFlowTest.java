package org.hamidelmaazouz.graaljulia.test;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SLInteropControlFlowTest {
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
    public void testWhile() {
        final Source src = Source.newBuilder("sl", "function testWhile(a) {while(a) {break;}} function main() {return testWhile;}", "testWhile.sl").buildLiteral();
        final Value fnc = context.eval(src);
        Assert.assertTrue(fnc.canExecute());
        fnc.execute(false);
    }

    @Test
    public void testIf() {
        final Source src = Source.newBuilder("sl", "function testIf(a) {if(a) {return 1;} else {return 0;}} function main() {return testIf;}", "testIf.sl").buildLiteral();
        final Value fnc = context.eval(src);
        fnc.execute(false);
    }
}
