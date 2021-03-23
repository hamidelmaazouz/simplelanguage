package org.hamidelmaazouz.graaljulia.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Source;
import org.junit.Test;

public class SLCodeSharingTest {

    private static Source createFib() {
        return Source.newBuilder("sl", "" +
                        "function fib(n) {\n" +
                        "  if (n == 1 || n == 2) {\n" +
                        "    return 1;\n" +
                        "  }\n" +
                        "  return fib(n - 1) + fib(n - 2);\n" +
                        "}\n",
                        "fib.sl").buildLiteral();
    }

    @Test
    public void testFibSharing() throws Exception {
        Source fib = createFib();
        try (Engine engine = Engine.create()) {
            try (Context context = Context.newBuilder().engine(engine).build()) {
                assertEquals(0, engine.getCachedSources().size());
                context.eval(fib);
                assertEquals(1, engine.getCachedSources().size());
                assertTrue(engine.getCachedSources().contains(fib));
            }
            try (Context context = Context.newBuilder().engine(engine).build()) {
                assertEquals(1, engine.getCachedSources().size());
                assertTrue(engine.getCachedSources().contains(fib));
                context.eval(fib);
                assertEquals(1, engine.getCachedSources().size());
                assertTrue(engine.getCachedSources().contains(fib));
            }
        }
    }

}
