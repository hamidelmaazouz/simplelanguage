package org.hamidelmaazouz.graaljulia.nodes.controlflow;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import org.hamidelmaazouz.graaljulia.nodes.SLStatementNode;

/**
 * Implementation of the SL continue statement. We need to unwind an unknown number of interpreter
 * frames that are between this {@link SLContinueNode} and the {@link SLWhileNode} of the loop we
 * are continuing. This is done by throwing an {@link SLContinueException exception} that is caught
 * by the {@link SLWhileNode#executeVoid loop node}.
 */
@NodeInfo(shortName = "continue", description = "The node implementing a continue statement")
public final class SLContinueNode extends SLStatementNode {

    @Override
    public void executeVoid(VirtualFrame frame) {
        throw SLContinueException.SINGLETON;
    }
}
