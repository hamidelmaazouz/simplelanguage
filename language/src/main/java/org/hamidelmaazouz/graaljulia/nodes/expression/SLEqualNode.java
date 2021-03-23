package org.hamidelmaazouz.graaljulia.nodes.expression;

import static com.oracle.truffle.api.CompilerDirectives.shouldNotReachHere;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import org.hamidelmaazouz.graaljulia.nodes.SLBinaryNode;
import org.hamidelmaazouz.graaljulia.runtime.SLBigNumber;
import org.hamidelmaazouz.graaljulia.runtime.SLFunction;
import org.hamidelmaazouz.graaljulia.runtime.SLNull;

/**
 * The {@code ==} operator of SL is defined on all types. Therefore, we need a
 * {@link #equal(Object, Object) implementation} that can handle all possible types including
 * interop types.
 * <p>
 * Note that we do not need the analogous {@code !=} operator, because we can just
 * {@link SLLogicalNotNode negate} the {@code ==} operator.
 */
@NodeInfo(shortName = "==")
public abstract class SLEqualNode extends SLBinaryNode {

    @Specialization
    protected boolean doLong(long left, long right) {
        return left == right;
    }

    @Specialization
    @TruffleBoundary
    protected boolean doBigNumber(SLBigNumber left, SLBigNumber right) {
        return left.equals(right);
    }

    @Specialization
    protected boolean doBoolean(boolean left, boolean right) {
        return left == right;
    }

    @Specialization
    protected boolean doString(String left, String right) {
        return left.equals(right);
    }

    @Specialization
    protected boolean doNull(SLNull left, SLNull right) {
        /* There is only the singleton instance of SLNull, so we do not need equals(). */
        return left == right;
    }

    @Specialization
    protected boolean doFunction(SLFunction left, Object right) {
        /*
         * Our function registry maintains one canonical SLFunction object per function name, so we
         * do not need equals().
         */
        return left == right;
    }

    /*
     * This is a generic specialization of equality operation. Since it is generic this
     * specialization covers the entire semantics. One can see this by having no method guards set
     * and the types for the left and right value are Object. The previous specializations are only
     * here for interpreter performance and footprint reasons. They could be removed and this
     * operation be semantically equivalent.
     *
     * We cache four combinations of interop values until we fallback to the uncached version of
     * this specialization. This limit is set arbitrary and for a real language should be set to the
     * minimal possible value, for a set of given benchmarks.
     *
     * This specialization is generic and handles all the cases, but in this case we decided to not
     * replace the previous specializations, as they are still more efficient in the interpeter.
     */
    @Specialization(limit = "4")
    public boolean doGeneric(Object left, Object right,
                    @CachedLibrary("left") InteropLibrary leftInterop,
                    @CachedLibrary("right") InteropLibrary rightInterop) {
        /*
         * This method looks very inefficient. In practice most of these branches fold as the
         * interop type checks typically return a constant when using a cached library.
         *
         * Exercise: Try looking at what happens to this method during partial evaluation in IGV.
         * Tip: comment out all the previous @Specialization annotations to make it easier to
         * activate this specialization.
         */
        try {
            if (leftInterop.isBoolean(left) && rightInterop.isBoolean(right)) {
                return doBoolean(leftInterop.asBoolean(left), rightInterop.asBoolean(right));
            } else if (leftInterop.isString(left) && rightInterop.isString(right)) {
                return doString(leftInterop.asString(left), (rightInterop.asString(right)));
            } else if (leftInterop.isNull(left) && rightInterop.isNull(right)) {
                return true;
            } else if (leftInterop.fitsInLong(left) && rightInterop.fitsInLong(right)) {
                return doLong(leftInterop.asLong(left), (rightInterop.asLong(right)));
            } else if (left instanceof SLBigNumber && right instanceof SLBigNumber) {
                return doBigNumber((SLBigNumber) left, (SLBigNumber) right);
            } else if (leftInterop.hasIdentity(left) && rightInterop.hasIdentity(right)) {
                return leftInterop.isIdentical(left, right, rightInterop);
            } else {
                /*
                 * We return false in good dynamic language manner. Stricter languages might throw
                 * an error here.
                 */
                return false;
            }
        } catch (UnsupportedMessageException e) {
            // this case must not happen as we always check interop types before converting
            throw shouldNotReachHere(e);
        }
    }

}
