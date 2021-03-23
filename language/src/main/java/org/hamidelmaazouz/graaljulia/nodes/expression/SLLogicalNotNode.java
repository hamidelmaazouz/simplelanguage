package org.hamidelmaazouz.graaljulia.nodes.expression;

import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import org.hamidelmaazouz.graaljulia.SLException;
import org.hamidelmaazouz.graaljulia.nodes.SLExpressionNode;

/**
 * Example of a simple unary node that uses type specialization. See {@link SLAddNode} for
 * information on specializations.
 */
@NodeChild("valueNode")
@NodeInfo(shortName = "!")
public abstract class SLLogicalNotNode extends SLExpressionNode {

    @Specialization
    protected boolean doBoolean(boolean value) {
        return !value;
    }

    @Fallback
    protected Object typeError(Object value) {
        throw SLException.typeError(this, value);
    }

}
