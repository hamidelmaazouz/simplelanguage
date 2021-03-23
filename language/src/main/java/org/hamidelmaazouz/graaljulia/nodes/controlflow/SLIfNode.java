package org.hamidelmaazouz.graaljulia.nodes.controlflow;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.hamidelmaazouz.graaljulia.SLException;
import org.hamidelmaazouz.graaljulia.nodes.SLExpressionNode;
import org.hamidelmaazouz.graaljulia.nodes.SLStatementNode;
import org.hamidelmaazouz.graaljulia.nodes.util.SLUnboxNodeGen;

@NodeInfo(shortName = "if", description = "The node implementing a condional statement")
public final class SLIfNode extends SLStatementNode {

    /**
     * The condition of the {@code if}. This in a {@link SLExpressionNode} because we require a
     * result value. We do not have a node type that can only return a {@code boolean} value, so
     * {@link #evaluateCondition executing the condition} can lead to a type error.
     */
    @Child private SLExpressionNode conditionNode;

    /** Statement (or {@link SLBlockNode block}) executed when the condition is true. */
    @Child private SLStatementNode thenPartNode;

    /** Statement (or {@link SLBlockNode block}) executed when the condition is false. */
    @Child private SLStatementNode elsePartNode;

    /**
     * Profiling information, collected by the interpreter, capturing the profiling information of
     * the condition. This allows the compiler to generate better code for conditions that are
     * always true or always false. Additionally the {@link CountingConditionProfile} implementation
     * (as opposed to {@link BinaryConditionProfile} implementation) transmits the probability of
     * the condition to be true to the compiler.
     */
    private final ConditionProfile condition = ConditionProfile.createCountingProfile();

    public SLIfNode(SLExpressionNode conditionNode, SLStatementNode thenPartNode, SLStatementNode elsePartNode) {
        this.conditionNode = SLUnboxNodeGen.create(conditionNode);
        this.thenPartNode = thenPartNode;
        this.elsePartNode = elsePartNode;
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        /*
         * In the interpreter, record profiling information that the condition was executed and with
         * which outcome.
         */
        if (condition.profile(evaluateCondition(frame))) {
            /* Execute the then-branch. */
            thenPartNode.executeVoid(frame);
        } else {
            /* Execute the else-branch (which is optional according to the SL syntax). */
            if (elsePartNode != null) {
                elsePartNode.executeVoid(frame);
            }
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
             * program.
             */
            throw SLException.typeError(this, ex.getResult());
        }
    }
}
