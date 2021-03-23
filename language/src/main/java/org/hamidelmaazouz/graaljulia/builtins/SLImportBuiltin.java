package org.hamidelmaazouz.graaljulia.builtins;

import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import org.hamidelmaazouz.graaljulia.SLException;
import org.hamidelmaazouz.graaljulia.SLLanguage;
import org.hamidelmaazouz.graaljulia.runtime.SLContext;
import org.hamidelmaazouz.graaljulia.runtime.SLNull;

/**
 * Built-in function that goes through to import a symbol from the polyglot bindings.
 */
@NodeInfo(shortName = "import")
public abstract class SLImportBuiltin extends SLBuiltinNode {

    @Specialization
    public Object importSymbol(String symbol,
                    @CachedLibrary(limit = "3") InteropLibrary arrays,
                    @CachedContext(SLLanguage.class) SLContext context) {
        try {
            return arrays.readMember(context.getPolyglotBindings(), symbol);
        } catch (UnsupportedMessageException | UnknownIdentifierException e) {
            return SLNull.SINGLETON;
        } catch (SecurityException e) {
            throw new SLException("No polyglot access allowed.", this);
        }
    }

}
