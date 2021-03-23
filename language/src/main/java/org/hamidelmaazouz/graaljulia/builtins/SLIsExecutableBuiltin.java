package org.hamidelmaazouz.graaljulia.builtins;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;

/**
 * Built-in function that queries if the foreign object is executable. See
 * <link>Messages.IS_EXECUTABLE</link>.
 */
@NodeInfo(shortName = "isExecutable")
public abstract class SLIsExecutableBuiltin extends SLBuiltinNode {

    @Specialization(limit = "3")
    public boolean isExecutable(Object obj, @CachedLibrary("obj") InteropLibrary executables) {
        return executables.isExecutable(obj);
    }
}
