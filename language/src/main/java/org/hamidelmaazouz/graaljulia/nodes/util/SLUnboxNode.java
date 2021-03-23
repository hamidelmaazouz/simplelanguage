package org.hamidelmaazouz.graaljulia.nodes.util;

import static com.oracle.truffle.api.CompilerDirectives.shouldNotReachHere;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import org.hamidelmaazouz.graaljulia.nodes.SLExpressionNode;
import org.hamidelmaazouz.graaljulia.nodes.SLTypes;
import org.hamidelmaazouz.graaljulia.runtime.SLBigNumber;
import org.hamidelmaazouz.graaljulia.runtime.SLFunction;
import org.hamidelmaazouz.graaljulia.runtime.SLNull;

/**
 * The node to normalize any value to an SL value. This is useful to reduce the number of values
 * expression nodes need to expect.
 */
@TypeSystemReference(SLTypes.class)
@NodeChild
public abstract class SLUnboxNode extends SLExpressionNode {

    static final int LIMIT = 5;

    @Specialization
    protected static String fromString(String value) {
        return value;
    }

    @Specialization
    protected static boolean fromBoolean(boolean value) {
        return value;
    }

    @Specialization
    protected static long fromLong(long value) {
        return value;
    }

    @Specialization
    protected static SLBigNumber fromBigNumber(SLBigNumber value) {
        return value;
    }

    @Specialization
    protected static SLFunction fromFunction(SLFunction value) {
        return value;
    }

    @Specialization
    protected static SLNull fromFunction(SLNull value) {
        return value;
    }

    @Specialization(limit = "LIMIT")
    public static Object fromForeign(Object value, @CachedLibrary("value") InteropLibrary interop) {
        try {
            if (interop.fitsInLong(value)) {
                return interop.asLong(value);
            } else if (interop.fitsInDouble(value)) {
                return (long) interop.asDouble(value);
            } else if (interop.isString(value)) {
                return interop.asString(value);
            } else if (interop.isBoolean(value)) {
                return interop.asBoolean(value);
            } else {
                return value;
            }
        } catch (UnsupportedMessageException e) {
            throw shouldNotReachHere(e);
        }
    }

}
