package org.hamidelmaazouz.graaljulia.nodes.expression;

import com.oracle.truffle.api.nodes.NodeInfo;
import org.hamidelmaazouz.graaljulia.nodes.SLExpressionNode;

/**
 * Logical conjunction node with short circuit evaluation.
 */
@NodeInfo(shortName = "&&")
public final class SLLogicalAndNode extends SLShortCircuitNode {

    public SLLogicalAndNode(SLExpressionNode left, SLExpressionNode right) {
        super(left, right);
    }

    /**
     * The right value does not need to be evaluated if the left value is already <code>false</code>
     * .
     */
    @Override
    protected boolean isEvaluateRight(boolean left) {
        return left;
    }

    /**
     * Only if left and right value are true the result of the logical and is <code>true</code>. If
     * the second parameter is not evaluated, <code>false</code> is provided.
     */
    @Override
    protected boolean execute(boolean left, boolean right) {
        return left && right;
    }

}
