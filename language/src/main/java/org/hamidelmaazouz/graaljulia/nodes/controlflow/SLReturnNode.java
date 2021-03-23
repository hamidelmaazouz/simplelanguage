package org.hamidelmaazouz.graaljulia.nodes.controlflow;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import org.hamidelmaazouz.graaljulia.nodes.SLExpressionNode;
import org.hamidelmaazouz.graaljulia.nodes.SLStatementNode;
import org.hamidelmaazouz.graaljulia.runtime.SLNull;

/**
 * Implementation of the SL return statement. We need to unwind an unknown number of interpreter
 * frames that are between this {@link SLReturnNode} and the {@link SLFunctionBodyNode} of the
 * method we are exiting. This is done by throwing an {@link SLReturnException exception} that is
 * caught by the {@link SLFunctionBodyNode#executeGeneric function body}. The exception transports
 * the return value.
 */
@NodeInfo(shortName = "return", description = "The node implementing a return statement")
public final class SLReturnNode extends SLStatementNode {

    @Child private SLExpressionNode valueNode;

    public SLReturnNode(SLExpressionNode valueNode) {
        this.valueNode = valueNode;
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        Object result;
        if (valueNode != null) {
            result = valueNode.executeGeneric(frame);
        } else {
            /*
             * Return statement that was not followed by an expression, so return the SL null value.
             */
            result = SLNull.SINGLETON;
        }
        throw new SLReturnException(result);
    }
}
