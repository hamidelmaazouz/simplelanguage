package org.hamidelmaazouz.graaljulia.builtins;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.source.Source;
import org.hamidelmaazouz.graaljulia.SLLanguage;
import org.hamidelmaazouz.graaljulia.runtime.SLContext;

/**
 * Builtin function to evaluate source code in any supported language.
 * <p>
 * The call target is cached against the language id and the source code, so that if they are the
 * same each time then a direct call will be made to a cached AST, allowing it to be compiled and
 * possibly inlined.
 */
@NodeInfo(shortName = "eval")
@SuppressWarnings("unused")
public abstract class SLEvalBuiltin extends SLBuiltinNode {

    static final int LIMIT = 2;

    @Specialization(guards = {"stringsEqual(cachedId, id)", "stringsEqual(cachedCode, code)"}, limit = "LIMIT")
    public Object evalCached(String id, String code,
                    @Cached("id") String cachedId,
                    @Cached("code") String cachedCode,
                    @CachedContext(SLLanguage.class) SLContext context,
                    @Cached("create(parse(id, code, context))") DirectCallNode callNode) {
        return callNode.call(new Object[]{});
    }

    @TruffleBoundary
    @Specialization(replaces = "evalCached")
    public Object evalUncached(String id, String code, @CachedContext(SLLanguage.class) SLContext context) {
        return parse(id, code, context).call();
    }

    protected CallTarget parse(String id, String code, SLContext context) {
        final Source source = Source.newBuilder(id, code, "(eval)").build();
        return context.parse(source);
    }

    /* Work around findbugs warning in generate code. */
    protected static boolean stringsEqual(String a, String b) {
        return a.equals(b);
    }
}
