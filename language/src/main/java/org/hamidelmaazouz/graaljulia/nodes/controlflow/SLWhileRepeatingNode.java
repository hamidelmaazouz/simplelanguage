package org.hamidelmaazouz.graaljulia.nodes.controlflow;

import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RepeatingNode;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.hamidelmaazouz.graaljulia.nodes.SLExpressionNode;
import org.hamidelmaazouz.graaljulia.nodes.SLStatementNode;
import org.hamidelmaazouz.graaljulia.nodes.util.SLUnboxNodeGen;

/**
 * The loop body of a {@link SLWhileNode while loop}. A Truffle framework {@link LoopNode} between
 * the {@link SLWhileNode} and {@link SLWhileRepeatingNode} allows Truffle to perform loop
 * optimizations, for example, compile just the loop body for long running loops.
 */
public final class SLWhileRepeatingNode extends Node implements RepeatingNode {

    /**
     * The condition of the loop. This in a {@link SLExpressionNode} because we require a result
     * value. We do not have a node type that can only return a {@code boolean} value, so
     * {@link #evaluateCondition executing the condition} can lead to a type error.
     */
    @Child private SLExpressionNode conditionNode;

    /** Statement (or {@link SLBlockNode block}) executed as long as the condition is true. */
    @Child private SLStatementNode bodyNode;

    /**
     * Profiling information, collected by the interpreter, capturing whether a {@code continue}
     * statement was used in this loop. This allows the compiler to generate better code for loops
     * without a {@code continue}.
     */
    private final BranchProfile continueTaken = BranchProfile.create();
    private final BranchProfile breakTaken = BranchProfile.create();

    public SLWhileRepeatingNode(SLExpressionNode conditionNode, SLStatementNode bodyNode) {
        this.conditionNode = SLUnboxNodeGen.create(conditionNode);
        this.bodyNode = bodyNode;
    }

    @Override
    public boolean executeRepeating(VirtualFrame frame) {
        if (!evaluateCondition(frame)) {
            /* Normal exit of the loop when loop condition is false. */
            return false;
        }

        try {
            /* Execute the loop body. */
            bodyNode.executeVoid(frame);
            /* Continue with next loop iteration. */
            return true;

        } catch (SLContinueException ex) {
            /* In the interpreter, record profiling information that the loop uses continue. */
            continueTaken.enter();
            /* Continue with next loop iteration. */
            return true;

        } catch (SLBreakException ex) {
            /* In the interpreter, record profiling information that the loop uses break. */
            breakTaken.enter();
            /* Break out of the loop. */
            return false;
        }
    }

    private boolean evaluateCondition(VirtualFrame frame) {
        try {
            /*
             * The condition must evaluate to a boolean value, so we call the boolean-specialized
             * execute method.
             */
            return conditionNode.executeBoolean(frame);
        } catch (UnexpectedResultException ex) {
            /*
             * The condition evaluated to a non-boolean result. This is a type error in the SL
             * program. We report it with the same exception that Truffle DSL generated nodes use to
             * report type errors.
             */
            throw new UnsupportedSpecializationException(this, new Node[]{conditionNode}, ex.getResult());
        }
    }

    @Override
    public String toString() {
        return SLStatementNode.formatSourceSection(this);
    }

}
