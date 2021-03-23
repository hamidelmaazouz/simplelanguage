package org.hamidelmaazouz.graaljulia.nodes.expression;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import org.hamidelmaazouz.graaljulia.nodes.SLExpressionNode;

/**
 * Constant literal for a primitive {@code long} value. The unboxed value can be returned when the
 * parent expects a long value and calls {@link SLLongLiteralNode#executeLong}. In the generic case,
 * the primitive value is automatically boxed by Java.
 */
@NodeInfo(shortName = "const")
public final class SLLongLiteralNode extends SLExpressionNode {

    private final long value;

    public SLLongLiteralNode(long value) {
        this.value = value;
    }

    @Override
    public long executeLong(VirtualFrame frame) throws UnexpectedResultException {
        return value;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return value;
    }
}
