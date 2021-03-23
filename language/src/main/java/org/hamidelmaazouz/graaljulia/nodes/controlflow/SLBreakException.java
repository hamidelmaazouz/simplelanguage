package org.hamidelmaazouz.graaljulia.nodes.controlflow;

import com.oracle.truffle.api.nodes.ControlFlowException;

/**
 * Exception thrown by the {@link SLBreakNode break statement} and caught by the {@link SLWhileNode
 * loop statement}. Since the exception is stateless, i.e., has no instance fields, we can use a
 * {@link #SINGLETON} to avoid memory allocation during interpretation.
 */
public final class SLBreakException extends ControlFlowException {

    public static final SLBreakException SINGLETON = new SLBreakException();

    private static final long serialVersionUID = -91013036379258890L;

    /* Prevent instantiation from outside. */
    private SLBreakException() {
    }
}
