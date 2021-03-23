package org.hamidelmaazouz.graaljulia.builtins;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.NodeInfo;
import org.hamidelmaazouz.graaljulia.runtime.SLNull;
import org.hamidelmaazouz.graaljulia.runtime.SLType;

/**
 * Built-in function that returns the type of a guest language value.
 */
@NodeInfo(shortName = "typeOf")
@SuppressWarnings("unused")
public abstract class SLTypeOfBuiltin extends SLBuiltinNode {

    /*
     * This returns the SL type for a particular operand value.
     */
    @Specialization(limit = "3")
    @ExplodeLoop
    public Object doDefault(Object operand,
                    @CachedLibrary("operand") InteropLibrary interop) {
        for (SLType type : SLType.PRECEDENCE) {
            if (type.isInstance(operand, interop)) {
                return type;
            }
        }
        return SLNull.SINGLETON;
    }

}
