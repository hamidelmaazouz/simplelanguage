package org.hamidelmaazouz.graaljulia.nodes.expression;

import com.oracle.truffle.api.nodes.NodeInfo;
import org.hamidelmaazouz.graaljulia.nodes.SLExpressionNode;

/**
 * Logical disjunction node with short circuit evaluation.
 */
@NodeInfo(shortName = "||")
public final class SLLogicalOrNode extends SLShortCircuitNode {

    public SLLogicalOrNode(SLExpressionNode left, SLExpressionNode right) {
        super(left, right);
    }

    @Override
    protected boolean isEvaluateRight(boolean left) {
        return !left;
    }

    @Override
    protected boolean execute(boolean left, boolean right) {
        return left || right;
    }

}
