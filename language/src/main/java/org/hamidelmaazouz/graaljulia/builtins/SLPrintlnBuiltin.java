package org.hamidelmaazouz.graaljulia.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import org.hamidelmaazouz.graaljulia.SLLanguage;
import org.hamidelmaazouz.graaljulia.runtime.SLContext;
import org.hamidelmaazouz.graaljulia.runtime.SLLanguageView;

/**
 * Builtin function to write a value to the {@link SLContext#getOutput() standard output}. The
 * different specialization leverage the typed {@code println} methods available in Java, i.e.,
 * primitive values are printed without converting them to a {@link String} first.
 * <p>
 * Printing involves a lot of Java code, so we need to tell the optimizing system that it should not
 * unconditionally inline everything reachable from the println() method. This is done via the
 * {@link TruffleBoundary} annotations.
 */
@NodeInfo(shortName = "println")
public abstract class SLPrintlnBuiltin extends SLBuiltinNode {

    @Specialization
    @TruffleBoundary
    public Object println(Object value,
                    @CachedLibrary(limit = "3") InteropLibrary interop,
                    @CachedContext(SLLanguage.class) SLContext context) {
        context.getOutput().println(interop.toDisplayString(SLLanguageView.forValue(value)));
        return value;
    }

}
