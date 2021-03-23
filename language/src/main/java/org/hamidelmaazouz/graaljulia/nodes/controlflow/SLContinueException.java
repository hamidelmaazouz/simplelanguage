package org.hamidelmaazouz.graaljulia.nodes.controlflow;

import com.oracle.truffle.api.nodes.ControlFlowException;

/**
 * Exception thrown by the {@link SLContinueNode continue statement} and caught by the
 * {@link SLWhileNode loop statement}. Since the exception is stateless, i.e., has no instance
 * fields, we can use a {@link #SINGLETON} to avoid memory allocation during interpretation.
 */
public final class SLContinueException extends ControlFlowException {

    public static final SLContinueException SINGLETON = new SLContinueException();

    private static final long serialVersionUID = 5329687983726237188L;

    /* Prevent instantiation from outside. */
    private SLContinueException() {
    }
}
