package org.hamidelmaazouz.graaljulia.nodes.controlflow;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.NodeInfo;
import org.hamidelmaazouz.graaljulia.nodes.SLExpressionNode;
import org.hamidelmaazouz.graaljulia.nodes.SLStatementNode;

@NodeInfo(shortName = "while", description = "The node implementing a while loop")
public final class SLWhileNode extends SLStatementNode {

    @Child private LoopNode loopNode;

    public SLWhileNode(SLExpressionNode conditionNode, SLStatementNode bodyNode) {
        this.loopNode = Truffle.getRuntime().createLoopNode(new SLWhileRepeatingNode(conditionNode, bodyNode));
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        loopNode.execute(frame);
    }

}
