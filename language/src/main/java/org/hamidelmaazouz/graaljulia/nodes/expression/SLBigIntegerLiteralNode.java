package org.hamidelmaazouz.graaljulia.nodes.expression;

import java.math.BigInteger;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import org.hamidelmaazouz.graaljulia.nodes.SLExpressionNode;
import org.hamidelmaazouz.graaljulia.runtime.SLBigNumber;

/**
 * Constant literal for a arbitrary-precision number that exceeds the range of
 * {@link SLLongLiteralNode}.
 */
@NodeInfo(shortName = "const")
public final class SLBigIntegerLiteralNode extends SLExpressionNode {

    private final SLBigNumber value;

    public SLBigIntegerLiteralNode(BigInteger value) {
        this.value = new SLBigNumber(value);
    }

    @Override
    public SLBigNumber executeGeneric(VirtualFrame frame) {
        return value;
    }
}
