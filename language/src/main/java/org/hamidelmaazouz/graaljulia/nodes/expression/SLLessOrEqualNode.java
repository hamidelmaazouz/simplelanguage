package org.hamidelmaazouz.graaljulia.nodes.expression;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import org.hamidelmaazouz.graaljulia.SLException;
import org.hamidelmaazouz.graaljulia.nodes.SLBinaryNode;
import org.hamidelmaazouz.graaljulia.runtime.SLBigNumber;

/**
 * This class is similar to the {@link SLLessThanNode}.
 */
@NodeInfo(shortName = "<=")
public abstract class SLLessOrEqualNode extends SLBinaryNode {

    @Specialization
    protected boolean lessOrEqual(long left, long right) {
        return left <= right;
    }

    @Specialization
    @TruffleBoundary
    protected boolean lessOrEqual(SLBigNumber left, SLBigNumber right) {
        return left.compareTo(right) <= 0;
    }

    @Fallback
    protected Object typeError(Object left, Object right) {
        throw SLException.typeError(this, left, right);
    }
}
