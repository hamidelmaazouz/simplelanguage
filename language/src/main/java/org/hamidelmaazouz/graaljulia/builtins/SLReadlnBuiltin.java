
package org.hamidelmaazouz.graaljulia.builtins;

import java.io.BufferedReader;
import java.io.IOException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import org.hamidelmaazouz.graaljulia.SLException;
import org.hamidelmaazouz.graaljulia.SLLanguage;
import org.hamidelmaazouz.graaljulia.runtime.SLContext;

/**
 * Builtin function that reads a String from the {@link SLContext#getInput() standard input}.
 */
@NodeInfo(shortName = "readln")
public abstract class SLReadlnBuiltin extends SLBuiltinNode {

    @Specialization
    public String readln(@CachedContext(SLLanguage.class) SLContext context) {
        String result = doRead(context.getInput());
        if (result == null) {
            /*
             * We do not have a sophisticated end of file handling, so returning an empty string is
             * a reasonable alternative. Note that the Java null value should never be used, since
             * it can interfere with the specialization logic in generated source code.
             */
            result = "";
        }
        return result;
    }

    @TruffleBoundary
    private String doRead(BufferedReader in) {
        try {
            return in.readLine();
        } catch (IOException ex) {
            throw new SLException(ex.getMessage(), this);
        }
    }
}
