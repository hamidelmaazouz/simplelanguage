package org.hamidelmaazouz.graaljulia.runtime;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.Node;
import org.hamidelmaazouz.graaljulia.SLException;

public final class SLUndefinedNameException extends SLException {

    private static final long serialVersionUID = 1L;

    @TruffleBoundary
    public static SLUndefinedNameException undefinedFunction(Node location, Object name) {
        throw new SLUndefinedNameException("Undefined function: " + name, location);
    }

    @TruffleBoundary
    public static SLUndefinedNameException undefinedProperty(Node location, Object name) {
        throw new SLUndefinedNameException("Undefined property: " + name, location);
    }

    private SLUndefinedNameException(String message, Node node) {
        super(message, node);
    }
}
