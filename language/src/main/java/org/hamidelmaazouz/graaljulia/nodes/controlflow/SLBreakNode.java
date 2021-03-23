package org.hamidelmaazouz.graaljulia.nodes.controlflow;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import org.hamidelmaazouz.graaljulia.nodes.SLStatementNode;

/**
 * Implementation of the SL break statement. We need to unwind an unknown number of interpreter
 * frames that are between this {@link SLBreakNode} and the {@link SLWhileNode} of the loop we are
 * breaking out. This is done by throwing an {@link SLBreakException exception} that is caught by
 * the {@link SLWhileNode#executeVoid loop node}.
 */
@NodeInfo(shortName = "break", description = "The node implementing a break statement")
public final class SLBreakNode extends SLStatementNode {

    @Override
    public void executeVoid(VirtualFrame frame) {
        throw SLBreakException.SINGLETON;
    }
}
