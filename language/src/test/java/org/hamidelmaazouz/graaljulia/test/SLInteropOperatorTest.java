package org.hamidelmaazouz.graaljulia.test;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SLInteropOperatorTest {
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
    public void testAdd() {
        final Source src = Source.newBuilder("sl", "function testAdd(a,b) {return a + b;} function main() {return testAdd;}", "testAdd.sl").buildLiteral();
        final Value fnc = context.eval(src);
        Assert.assertTrue(fnc.canExecute());
        final Value res = fnc.execute(1, 2);
        Assert.assertTrue(res.isNumber());
        Assert.assertEquals(3, res.asInt());
    }

    @Test
    public void testSub() {
        final Source src = Source.newBuilder("sl", "function testSub(a,b) {return a - b;} function main() {return testSub;}", "testSub.sl").buildLiteral();
        final Value fnc = context.eval(src);
        final Value res = fnc.execute(1, 2);
        Assert.assertTrue(res.isNumber());
        Assert.assertEquals(-1, res.asInt());
    }
}
