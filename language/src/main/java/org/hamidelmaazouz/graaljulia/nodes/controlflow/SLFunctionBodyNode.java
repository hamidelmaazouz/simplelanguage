package org.hamidelmaazouz.graaljulia.nodes.controlflow;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.hamidelmaazouz.graaljulia.nodes.SLExpressionNode;
import org.hamidelmaazouz.graaljulia.nodes.SLRootNode;
import org.hamidelmaazouz.graaljulia.nodes.SLStatementNode;
import org.hamidelmaazouz.graaljulia.runtime.SLNull;

/**
 * The body of a user-defined SL function. This is the node referenced by a {@link SLRootNode} for
 * user-defined functions. It handles the return value of a function: the {@link SLReturnNode return
 * statement} throws an {@link SLReturnException exception} with the return value. This node catches
 * the exception. If the method ends without an explicit {@code return}, return the
 * {@link SLNull#SINGLETON default null value}.
 */
@NodeInfo(shortName = "body")
public final class SLFunctionBodyNode extends SLExpressionNode {

    /** The body of the function. */
    @Child private SLStatementNode bodyNode;

    /**
     * Profiling information, collected by the interpreter, capturing whether the function had an
     * {@link SLReturnNode explicit return statement}. This allows the compiler to generate better
     * code.
     */
    private final BranchProfile exceptionTaken = BranchProfile.create();
    private final BranchProfile nullTaken = BranchProfile.create();

    public SLFunctionBodyNode(SLStatementNode bodyNode) {
        this.bodyNode = bodyNode;
        addRootTag();
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        try {
            /* Execute the function body. */
            bodyNode.executeVoid(frame);

        } catch (SLReturnException ex) {
            /*
             * In the interpreter, record profiling information that the function has an explicit
             * return.
             */
            exceptionTaken.enter();
            /* The exception transports the actual return value. */
            return ex.getResult();
        }

        /*
         * In the interpreter, record profiling information that the function ends without an
         * explicit return.
         */
        nullTaken.enter();
        /* Return the default null value. */
        return SLNull.SINGLETON;
    }
}
