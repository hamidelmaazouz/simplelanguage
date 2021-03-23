package org.hamidelmaazouz.graaljulia.test;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyInstantiable;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SLInteropObjectTest {
    private Context context;

    @Before
    public void setUp() {
        context = Context.create("sl");
    }

    @After
    public void tearDown() {
        context.close();
        context = null;
    }

    @Test
    public void testObject() {
        final Source src = Source.newBuilder("sl", "function main() {o = new(); o.a = 10; o.b = \"B\"; return o;}", "testObject.sl").buildLiteral();
        final Value obj = context.eval(src);
        Assert.assertTrue(obj.hasMembers());

        Value a = obj.getMember("a");
        Assert.assertNotNull(a);
        Assert.assertTrue(a.isNumber());
        Assert.assertEquals(10, a.asInt());

        Value b = obj.getMember("b");
        Assert.assertNotNull(b);
        Assert.assertTrue(b.isString());
        Assert.assertEquals("B", b.asString());

        obj.putMember("a", b);
        a = obj.getMember("a");
        Assert.assertTrue(a.isString());
        Assert.assertEquals("B", a.asString());

        obj.removeMember("a");
        Assert.assertFalse(obj.hasMember("a"));

        Assert.assertEquals("[b]", obj.getMemberKeys().toString());
    }

    @Test
    public void testNewForeign() {
        final Source src = Source.newBuilder("sl", "function getValue(type) {o = new(type); o.a = 10; return o.value;}", "testObject.sl").buildLiteral();
        context.eval(src);
        Value getValue = context.getBindings("sl").getMember("getValue");
        Value ret = getValue.execute(new TestType());
        Assert.assertEquals(20, ret.asLong());
    }

    private static class TestType implements ProxyInstantiable {

        @Override
        public Object newInstance(Value... arguments) {
            return new TestObject();
        }

    }

    private static class TestObject implements ProxyObject {

        private long value;

        @Override
        public Object getMember(String key) {
            if ("value".equals(key)) {
                return 2 * value;
            }
            return 0;
        }

        @Override
        public Object getMemberKeys() {
            return new String[]{"a", "value"};
        }

        @Override
        public boolean hasMember(String key) {
            switch (key) {
                case "a":
                case "value":
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void putMember(String key, Value v) {
            value += v.asLong();
        }

    }
}
