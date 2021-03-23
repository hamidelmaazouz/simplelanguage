package org.hamidelmaazouz.graaljulia.nodes;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import org.hamidelmaazouz.graaljulia.SLLanguage;
import org.hamidelmaazouz.graaljulia.runtime.SLFunction;
import org.hamidelmaazouz.graaljulia.runtime.SLUndefinedNameException;

/**
 * The initial {@link RootNode} of {@link SLFunction functions} when they are created, i.e., when
 * they are still undefined. Executing it throws an
 * {@link SLUndefinedNameException#undefinedFunction exception}.
 */
public class SLUndefinedFunctionRootNode extends SLRootNode {
    public SLUndefinedFunctionRootNode(SLLanguage language, String name) {
        super(language, null, null, null, name);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        throw SLUndefinedNameException.undefinedFunction(null, getName());
    }
}
