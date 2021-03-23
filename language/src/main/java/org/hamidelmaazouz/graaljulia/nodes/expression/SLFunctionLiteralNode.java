package org.hamidelmaazouz.graaljulia.nodes.expression;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.TruffleLanguage.ContextPolicy;
import com.oracle.truffle.api.TruffleLanguage.ContextReference;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import org.hamidelmaazouz.graaljulia.SLLanguage;
import org.hamidelmaazouz.graaljulia.nodes.SLExpressionNode;
import org.hamidelmaazouz.graaljulia.runtime.SLContext;
import org.hamidelmaazouz.graaljulia.runtime.SLFunction;
import org.hamidelmaazouz.graaljulia.runtime.SLFunctionRegistry;

/**
 * Constant literal for a {@link SLFunction function} value, created when a function name occurs as
 * a literal in SL source code. Note that function redefinition can change the {@link CallTarget
 * call target} that is executed when calling the function, but the {@link SLFunction} for a name
 * never changes. This is guaranteed by the {@link SLFunctionRegistry}.
 */
@NodeInfo(shortName = "func")
public final class SLFunctionLiteralNode extends SLExpressionNode {

    /** The name of the function. */
    private final String functionName;

    /**
     * The resolved function. During parsing (in the constructor of this node), we do not have the
     * {@link SLContext} available yet, so the lookup can only be done at {@link #executeGeneric
     * first execution}. The {@link CompilationFinal} annotation ensures that the function can still
     * be constant folded during compilation.
     */
    @CompilationFinal private SLFunction cachedFunction;

    /**
     * The stored context reference. Caching the context reference in a field like this always
     * ensures the most efficient context lookup. The {@link SLContext} must not be stored in the
     * AST in the multi-context case.
     */
    @CompilationFinal private ContextReference<SLContext> contextRef;

    /**
     * It is always safe to store the language in the AST if the language supports
     * {@link ContextPolicy#SHARED shared}.
     */
    @CompilationFinal private SLLanguage language;

    public SLFunctionLiteralNode(String functionName) {
        this.functionName = functionName;
    }

    @Override
    public SLFunction executeGeneric(VirtualFrame frame) {
        ContextReference<SLContext> contextReference = contextRef;
        if (contextReference == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            contextReference = contextRef = lookupContextReference(SLLanguage.class);
        }
        SLLanguage l = language;
        if (l == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            l = language = lookupLanguageReference(SLLanguage.class).get();
        }
        CompilerAsserts.partialEvaluationConstant(l);

        SLFunction function;
        if (l.isSingleContext()) {
            function = this.cachedFunction;
            if (function == null) {
                /* We are about to change a @CompilationFinal field. */
                CompilerDirectives.transferToInterpreterAndInvalidate();
                /* First execution of the node: lookup the function in the function registry. */
                this.cachedFunction = function = contextReference.get().getFunctionRegistry().lookup(functionName, true);
            }
        } else {
            /*
             * We need to rest the cached function otherwise it might cause a memory leak.
             */
            if (this.cachedFunction != null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                this.cachedFunction = null;
            }
            // in the multi-context case we are not allowed to store
            // SLFunction objects in the AST. Instead we always perform the lookup in the hash map.
            function = contextReference.get().getFunctionRegistry().lookup(functionName, true);
        }
        return function;
    }

}
