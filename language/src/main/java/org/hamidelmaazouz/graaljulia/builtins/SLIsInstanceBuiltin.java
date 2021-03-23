package org.hamidelmaazouz.graaljulia.builtins;

import static com.oracle.truffle.api.CompilerDirectives.shouldNotReachHere;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;

/**
 * Built-in function that returns true if the given operand is of a given meta-object. Meta-objects
 * may be values of the current or a foreign value.
 */
@NodeInfo(shortName = "isInstance")
@SuppressWarnings("unused")
public abstract class SLIsInstanceBuiltin extends SLBuiltinNode {

    @Specialization(limit = "3", guards = "metaLib.isMetaObject(metaObject)")
    public Object doDefault(Object metaObject, Object value,
                    @CachedLibrary("metaObject") InteropLibrary metaLib) {
        try {
            return metaLib.isMetaInstance(metaObject, value);
        } catch (UnsupportedMessageException e) {
            throw shouldNotReachHere(e);
        }
    }

}
