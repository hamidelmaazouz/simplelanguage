package org.hamidelmaazouz.graaljulia.nodes.controlflow;

import com.oracle.truffle.api.nodes.ControlFlowException;

/**
 * Exception thrown by the {@link SLReturnNode return statement} and caught by the
 * {@link SLFunctionBodyNode function body}. The exception transports the return value in its
 * {@link #result} field.
 */
public final class SLReturnException extends ControlFlowException {

    private static final long serialVersionUID = 4073191346281369231L;

    private final Object result;

    public SLReturnException(Object result) {
        this.result = result;
    }

    public Object getResult() {
        return result;
    }
}
