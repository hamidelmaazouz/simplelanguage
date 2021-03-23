package org.hamidelmaazouz.graaljulia.nodes.expression;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import org.hamidelmaazouz.graaljulia.SLException;
import org.hamidelmaazouz.graaljulia.nodes.SLBinaryNode;
import org.hamidelmaazouz.graaljulia.runtime.SLBigNumber;

/**
 * This class is similar to the extensively documented {@link SLAddNode}.
 */
@NodeInfo(shortName = "*")
public abstract class SLMulNode extends SLBinaryNode {

    @Specialization(rewriteOn = ArithmeticException.class)
    protected long mul(long left, long right) {
        return Math.multiplyExact(left, right);
    }

    @Specialization
    @TruffleBoundary
    protected SLBigNumber mul(SLBigNumber left, SLBigNumber right) {
        return new SLBigNumber(left.getValue().multiply(right.getValue()));
    }

    @Fallback
    protected Object typeError(Object left, Object right) {
        throw SLException.typeError(this, left, right);
    }

}
