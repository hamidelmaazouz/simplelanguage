package org.hamidelmaazouz.graaljulia.nodes.expression;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import org.hamidelmaazouz.graaljulia.nodes.SLExpressionNode;
import org.hamidelmaazouz.graaljulia.nodes.util.SLToMemberNode;
import org.hamidelmaazouz.graaljulia.runtime.SLUndefinedNameException;

/**
 * The node for writing a property of an object. When executed, this node:
 * <ol>
 * <li>evaluates the object expression on the left hand side of the object access operator</li>
 * <li>evaluates the property name</li>
 * <li>evaluates the value expression on the right hand side of the assignment operator</li>
 * <li>writes the named property</li>
 * <li>returns the written value</li>
 * </ol>
 */
@NodeInfo(shortName = ".=")
@NodeChild("receiverNode")
@NodeChild("nameNode")
@NodeChild("valueNode")
public abstract class SLWritePropertyNode extends SLExpressionNode {

    static final int LIBRARY_LIMIT = 3;

    @Specialization(guards = "arrays.hasArrayElements(receiver)", limit = "LIBRARY_LIMIT")
    protected Object writeArray(Object receiver, Object index, Object value,
                    @CachedLibrary("receiver") InteropLibrary arrays,
                    @CachedLibrary("index") InteropLibrary numbers) {
        try {
            arrays.writeArrayElement(receiver, numbers.asLong(index), value);
        } catch (UnsupportedMessageException | UnsupportedTypeException | InvalidArrayIndexException e) {
            // read was not successful. In SL we only have basic support for errors.
            throw SLUndefinedNameException.undefinedProperty(this, index);
        }
        return value;
    }

    @Specialization(limit = "LIBRARY_LIMIT")
    protected Object writeObject(Object receiver, Object name, Object value,
                    @CachedLibrary("receiver") InteropLibrary objectLibrary,
                    @Cached SLToMemberNode asMember) {
        try {
            objectLibrary.writeMember(receiver, asMember.execute(name), value);
        } catch (UnsupportedMessageException | UnknownIdentifierException | UnsupportedTypeException e) {
            // write was not successful. In SL we only have basic support for errors.
            throw SLUndefinedNameException.undefinedProperty(this, name);
        }
        return value;
    }

}
