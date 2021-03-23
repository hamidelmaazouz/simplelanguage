package org.hamidelmaazouz.graaljulia.test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static org.hamidelmaazouz.graaljulia.test.SLExceptionTest.assertGuestFrame;
import static org.hamidelmaazouz.graaljulia.test.SLExceptionTest.assertHostFrame;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.PolyglotException.StackFrame;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.Assert;
import org.junit.Test;

import org.hamidelmaazouz.graaljulia.SLLanguage;

public class SLJavaInteropExceptionTest {
    public static class Validator {
        @HostAccess.Export
        public int validateException() {
            throw new NoSuchElementException();
        }

        @HostAccess.Export
        public void validateNested() throws Exception {
            String sourceText = "function test(validator) {\n" +
                            "  return validator.validateException();\n" +
                            "}";
            try (Context context = Context.newBuilder(SLLanguage.ID).build()) {
                context.eval(Source.newBuilder(SLLanguage.ID, sourceText, "Test").build());
                Value test = context.getBindings(SLLanguage.ID).getMember("test");
                test.execute(Validator.this);
            }
        }

        @HostAccess.Export
        @SuppressWarnings("unchecked")
        public Object validateCallback(int index, Map<?, ?> map) throws Exception {
            Object call = map.get(Integer.toString(index));
            if (call == null) {
                throw new NullPointerException("Nothing to call");
            }
            return ((Function<Object, Object>) call).apply(new Object[]{this, index});
        }

        @HostAccess.Export
        public long validateFunction(Supplier<Long> function) {
            return function.get();
        }

        @HostAccess.Export
        public void validateMap(Map<String, Object> map) {
            Assert.assertNull(map.get(null));
        }
    }

    @Test
    public void testGR7284() throws Exception {
        String sourceText = "function test(validator) {\n" +
                        "  return validator.validateException();\n" +
                        "}";
        try (Context context = Context.newBuilder(SLLanguage.ID).build()) {
            context.eval(Source.newBuilder(SLLanguage.ID, sourceText, "Test").build());
            Value test = context.getBindings(SLLanguage.ID).getMember("test");
            try {
                test.execute(new Validator());
                fail("expected a PolyglotException but did not throw");
            } catch (PolyglotException ex) {
                assertTrue("expected HostException", ex.isHostException());
                assertThat(ex.asHostException(), instanceOf(NoSuchElementException.class));
                assertNoJavaInteropStackFrames(ex);
            }
        }
    }

    @Test
    public void testGR7284GuestHostGuestHost() throws Exception {
        String sourceText = "function test(validator) {\n" +
                        "  return validator.validateNested();\n" +
                        "}";
        try (Context context = Context.newBuilder(SLLanguage.ID).build()) {
            context.eval(Source.newBuilder(SLLanguage.ID, sourceText, "Test").build());
            Value test = context.getBindings(SLLanguage.ID).getMember("test");
            try {
                test.execute(new Validator());
                fail("expected a PolyglotException but did not throw");
            } catch (PolyglotException ex) {
                assertTrue("expected HostException", ex.isHostException());
                assertThat(ex.asHostException(), instanceOf(NoSuchElementException.class));
                assertNoJavaInteropStackFrames(ex);
            }
        }
    }

    @Test
    public void testGuestHostCallbackGuestError() throws Exception {
        String sourceText = "function doMultiCallback(validator, n) {\n" +
                        "    map = new();\n" +
                        "    if (n <= 0) {\n" +
                        "        return error();\n" +
                        "    }\n" +
                        "    map[n] = doCall;\n" +
                        "    validator.validateCallback(n, map);\n" +
                        "}\n" +
                        "function doCall(validator, x) {\n" +
                        "    doMultiCallback(validator, x - 1);\n" +
                        "}";
        try (Context context = Context.newBuilder(SLLanguage.ID).build()) {
            context.eval(Source.newBuilder(SLLanguage.ID, sourceText, "Test").build());
            Value doMultiCallback = context.getBindings(SLLanguage.ID).getMember("doMultiCallback");
            int numCalbacks = 3;
            try {
                doMultiCallback.execute(new Validator(), numCalbacks);
                fail("expected a PolyglotException but did not throw");
            } catch (PolyglotException ex) {
                Iterator<StackFrame> frames = ex.getPolyglotStackTrace().iterator();
                assertGuestFrame(frames, "sl", "error");
                assertGuestFrame(frames, "sl", "doMultiCallback", "Test", 91, 98);
                for (int i = 0; i < numCalbacks; i++) {
                    assertGuestFrame(frames, "sl", "doCall", "Test", 205, 238);
                    assertHostFrame(frames, "com.oracle.truffle.polyglot.PolyglotFunction", "apply");
                    assertHostFrame(frames, Validator.class.getName(), "validateCallback");
                    assertGuestFrame(frames, "sl", "doMultiCallback", "Test", 131, 165);
                }
                assertHostFrame(frames, Value.class.getName(), "execute");
                assertNoJavaInteropStackFrames(ex);
            }
        }
    }

    @Test
    public void testGuestHostCallbackHostError() throws Exception {
        String sourceText = "function doMultiCallback(validator, n) {\n" +
                        "    map = new();\n" +
                        "    if (n <= 0) {\n" +
                        "        return validator.validateCallback(n, map); // will throw error\n" +
                        "    }\n" +
                        "    map[n] = doCall;\n" +
                        "    validator.validateCallback(n, map);\n" +
                        "}\n" +
                        "function doCall(validator, x) {\n" +
                        "    doMultiCallback(validator, x - 1);\n" +
                        "}";
        try (Context context = Context.newBuilder(SLLanguage.ID).build()) {
            context.eval(Source.newBuilder(SLLanguage.ID, sourceText, "Test").build());
            Value doMultiCallback = context.getBindings(SLLanguage.ID).getMember("doMultiCallback");
            int numCalbacks = 3;
            try {
                doMultiCallback.execute(new Validator(), numCalbacks);
                fail("expected a PolyglotException but did not throw");
            } catch (PolyglotException ex) {
                Assert.assertEquals("Nothing to call", ex.getMessage());
                Iterator<StackFrame> frames = ex.getPolyglotStackTrace().iterator();
                assertHostFrame(frames, Validator.class.getName(), "validateCallback");
                assertGuestFrame(frames, "sl", "doMultiCallback", "Test", 91, 125);
                for (int i = 0; i < numCalbacks; i++) {
                    assertGuestFrame(frames, "sl", "doCall", "Test", 252, 285);
                    assertHostFrame(frames, "com.oracle.truffle.polyglot.PolyglotFunction", "apply");
                    assertHostFrame(frames, Validator.class.getName(), "validateCallback");
                    assertGuestFrame(frames, "sl", "doMultiCallback", "Test", 178, 212);
                }
                assertHostFrame(frames, Value.class.getName(), "execute");
                assertNoJavaInteropStackFrames(ex);
            }
        }
    }

    private static void assertNoJavaInteropStackFrames(PolyglotException ex) {
        String javaInteropPackageName = "com.oracle.truffle.api.interop.java";
        assertFalse("expected no java interop stack trace elements", Arrays.stream(ex.getStackTrace()).anyMatch(ste -> ste.getClassName().startsWith(javaInteropPackageName)));
    }

    @Test
    public void testFunctionProxy() throws Exception {
        String javaMethod = "validateFunction";
        String sourceText = "" +
                        "function supplier() {\n" +
                        "  return error();\n" +
                        "}\n" +
                        "function test(validator) {\n" +
                        "  return validator." + javaMethod + "(supplier);\n" +
                        "}";
        try (Context context = Context.newBuilder(SLLanguage.ID).build()) {
            context.eval(Source.newBuilder(SLLanguage.ID, sourceText, "Test").build());
            Value test = context.getBindings(SLLanguage.ID).getMember("test");
            try {
                test.execute(new Validator());
                fail("expected a PolyglotException but did not throw");
            } catch (PolyglotException ex) {
                StackTraceElement last = null;
                boolean found = false;
                for (StackTraceElement curr : ex.getStackTrace()) {
                    if (curr.getMethodName().contains(javaMethod)) {
                        assertNotNull(last);
                        assertThat("expected Proxy stack frame", last.getClassName(), containsString("Proxy"));
                        found = true;
                        break;
                    }
                    last = curr;
                }
                assertTrue(javaMethod + " not found in stack trace", found);
            }
        }
    }

    @Test
    public void testTruffleMap() throws Exception {
        String javaMethod = "validateMap";
        String sourceText = "" +
                        "function test(validator) {\n" +
                        "  return validator." + javaMethod + "(new());\n" +
                        "}";
        try (Context context = Context.newBuilder(SLLanguage.ID).build()) {
            context.eval(Source.newBuilder(SLLanguage.ID, sourceText, "Test").build());
            Value test = context.getBindings(SLLanguage.ID).getMember("test");
            test.execute(new Validator());
        }
    }
}
