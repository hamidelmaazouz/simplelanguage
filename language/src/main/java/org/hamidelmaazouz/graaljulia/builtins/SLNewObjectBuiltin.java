package org.hamidelmaazouz.graaljulia.builtins;

import com.oracle.truffle.api.TruffleLanguage.ContextReference;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.CachedLanguage;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.instrumentation.AllocationReporter;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import org.hamidelmaazouz.graaljulia.SLLanguage;
import org.hamidelmaazouz.graaljulia.runtime.SLContext;
import org.hamidelmaazouz.graaljulia.runtime.SLNull;
import org.hamidelmaazouz.graaljulia.runtime.SLUndefinedNameException;

/**
 * Built-in function to create a new object. Objects in SL are simply made up of name/value pairs.
 */
@NodeInfo(shortName = "new")
public abstract class SLNewObjectBuiltin extends SLBuiltinNode {

    @Specialization
    @SuppressWarnings("unused")
    public Object newObject(SLNull o,
                    @CachedLanguage SLLanguage language,
                    @CachedContext(SLLanguage.class) ContextReference<SLContext> contextRef,
                    @Cached("contextRef.get().getAllocationReporter()") AllocationReporter reporter) {
        return language.createObject(reporter);
    }

    @Specialization(guards = "!values.isNull(obj)", limit = "3")
    public Object newObject(Object obj, @CachedLibrary("obj") InteropLibrary values) {
        try {
            return values.instantiate(obj);
        } catch (UnsupportedTypeException | ArityException | UnsupportedMessageException e) {
            /* Foreign access was not successful. */
            throw SLUndefinedNameException.undefinedFunction(this, obj);
        }
    }
}
