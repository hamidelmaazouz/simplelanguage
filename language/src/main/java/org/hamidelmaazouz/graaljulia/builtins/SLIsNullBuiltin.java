package org.hamidelmaazouz.graaljulia.builtins;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;

/**
 * Built-in function that queries if the foreign object is a null value. See
 * <link>Messages.IS_NULL</link>.
 */
@NodeInfo(shortName = "isNull")
public abstract class SLIsNullBuiltin extends SLBuiltinNode {

    @Specialization(limit = "3")
    public boolean isExecutable(Object obj, @CachedLibrary("obj") InteropLibrary values) {
        return values.isNull(obj);
    }
}
