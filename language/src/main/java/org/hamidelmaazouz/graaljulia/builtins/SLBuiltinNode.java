package org.hamidelmaazouz.graaljulia.builtins;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import org.hamidelmaazouz.graaljulia.SLException;
import org.hamidelmaazouz.graaljulia.nodes.SLExpressionNode;
import org.hamidelmaazouz.graaljulia.runtime.SLContext;
import org.hamidelmaazouz.graaljulia.runtime.SLFunctionRegistry;

/**
 * Base class for all builtin functions. It contains the Truffle DSL annotation {@link NodeChild}
 * that defines the function arguments.<br>
 * The builtin functions are registered in {@link SLContext#installBuiltins}. Every builtin node
 * subclass is instantiated there, wrapped into a function, and added to the
 * {@link SLFunctionRegistry}. This ensures that builtin functions can be called like user-defined
 * functions; there is no special function lookup or call node for builtin functions.
 */
@NodeChild(value = "arguments", type = SLExpressionNode[].class)
@GenerateNodeFactory
public abstract class SLBuiltinNode extends SLExpressionNode {

    @Override
    public final Object executeGeneric(VirtualFrame frame) {
        try {
            return execute(frame);
        } catch (UnsupportedSpecializationException e) {
            throw SLException.typeError(e.getNode(), e.getSuppliedValues());
        }
    }

    @Override
    public final boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
        return super.executeBoolean(frame);
    }

    @Override
    public final long executeLong(VirtualFrame frame) throws UnexpectedResultException {
        return super.executeLong(frame);
    }

    @Override
    public final void executeVoid(VirtualFrame frame) {
        super.executeVoid(frame);
    }

    protected abstract Object execute(VirtualFrame frame);
}
