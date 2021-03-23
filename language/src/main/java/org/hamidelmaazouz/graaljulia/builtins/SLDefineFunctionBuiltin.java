package org.hamidelmaazouz.graaljulia.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.source.Source;
import org.hamidelmaazouz.graaljulia.SLLanguage;
import org.hamidelmaazouz.graaljulia.runtime.SLContext;

/**
 * Builtin function to define (or redefine) functions. The provided source code is parsed the same
 * way as the initial source of the script, so the same syntax applies.
 */
@NodeInfo(shortName = "defineFunction")
public abstract class SLDefineFunctionBuiltin extends SLBuiltinNode {

    @TruffleBoundary
    @Specialization
    public String defineFunction(String code, @CachedContext(SLLanguage.class) SLContext context) {
        // @formatter:off
        Source source = Source.newBuilder(SLLanguage.ID, code, "[defineFunction]").
            build();
        // @formatter:on
        /* The same parsing code as for parsing the initial source. */
        context.getFunctionRegistry().register(source);

        return code;
    }
}
