package org.hamidelmaazouz.graaljulia.nodes.expression;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import org.hamidelmaazouz.graaljulia.nodes.SLExpressionNode;

/**
 * Constant literal for a String value.
 */
@NodeInfo(shortName = "const")
public final class SLStringLiteralNode extends SLExpressionNode {

    private final String value;

    public SLStringLiteralNode(String value) {
        this.value = value;
    }

    @Override
    public String executeGeneric(VirtualFrame frame) {
        return value;
    }
}
