package org.hamidelmaazouz.graaljulia.runtime;

import static com.oracle.truffle.api.CompilerDirectives.shouldNotReachHere;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleLanguage.Env;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.instrumentation.AllocationReporter;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.source.Source;
import org.hamidelmaazouz.graaljulia.SLLanguage;
import org.hamidelmaazouz.graaljulia.builtins.SLBuiltinNode;
import org.hamidelmaazouz.graaljulia.builtins.SLDefineFunctionBuiltinFactory;
import org.hamidelmaazouz.graaljulia.builtins.SLEvalBuiltinFactory;
import org.hamidelmaazouz.graaljulia.builtins.SLGetSizeBuiltinFactory;
import org.hamidelmaazouz.graaljulia.builtins.SLHasSizeBuiltinFactory;
import org.hamidelmaazouz.graaljulia.builtins.SLHelloEqualsWorldBuiltinFactory;
import org.hamidelmaazouz.graaljulia.builtins.SLImportBuiltinFactory;
import org.hamidelmaazouz.graaljulia.builtins.SLIsExecutableBuiltinFactory;
import org.hamidelmaazouz.graaljulia.builtins.SLIsInstanceBuiltinFactory;
import org.hamidelmaazouz.graaljulia.builtins.SLIsNullBuiltinFactory;
import org.hamidelmaazouz.graaljulia.builtins.SLNanoTimeBuiltinFactory;
import org.hamidelmaazouz.graaljulia.builtins.SLNewObjectBuiltinFactory;
import org.hamidelmaazouz.graaljulia.builtins.SLPrintlnBuiltin;
import org.hamidelmaazouz.graaljulia.builtins.SLPrintlnBuiltinFactory;
import org.hamidelmaazouz.graaljulia.builtins.SLReadlnBuiltin;
import org.hamidelmaazouz.graaljulia.builtins.SLReadlnBuiltinFactory;
import org.hamidelmaazouz.graaljulia.builtins.SLStackTraceBuiltinFactory;
import org.hamidelmaazouz.graaljulia.builtins.SLTypeOfBuiltinFactory;
import org.hamidelmaazouz.graaljulia.builtins.SLWrapPrimitiveBuiltinFactory;

/**
 * The run-time state of SL during execution. The context is created by the {@link SLLanguage}. It
 * is used, for example, by {@link SLBuiltinNode#getContext() builtin functions}.
 * <p>
 * It would be an error to have two different context instances during the execution of one script.
 * However, if two separate scripts run in one Java VM at the same time, they have a different
 * context. Therefore, the context is not a singleton.
 */
public final class SLContext {

    private final SLLanguage language;
    private final Env env;
    private final BufferedReader input;
    private final PrintWriter output;
    private final SLFunctionRegistry functionRegistry;
    private final AllocationReporter allocationReporter;

    public SLContext(SLLanguage language, TruffleLanguage.Env env, List<NodeFactory<? extends SLBuiltinNode>> externalBuiltins) {
        this.env = env;
        this.input = new BufferedReader(new InputStreamReader(env.in()));
        this.output = new PrintWriter(env.out(), true);
        this.language = language;
        this.allocationReporter = env.lookup(AllocationReporter.class);
        this.functionRegistry = new SLFunctionRegistry(language);
        installBuiltins();
        for (NodeFactory<? extends SLBuiltinNode> builtin : externalBuiltins) {
            installBuiltin(builtin);
        }
    }

    /**
     * Return the current Truffle environment.
     */
    public Env getEnv() {
        return env;
    }

    /**
     * Returns the default input, i.e., the source for the {@link SLReadlnBuiltin}. To allow unit
     * testing, we do not use {@link System#in} directly.
     */
    public BufferedReader getInput() {
        return input;
    }

    /**
     * The default default, i.e., the output for the {@link SLPrintlnBuiltin}. To allow unit
     * testing, we do not use {@link System#out} directly.
     */
    public PrintWriter getOutput() {
        return output;
    }

    /**
     * Returns the registry of all functions that are currently defined.
     */
    public SLFunctionRegistry getFunctionRegistry() {
        return functionRegistry;
    }

    /**
     * Adds all builtin functions to the {@link SLFunctionRegistry}. This method lists all
     * {@link SLBuiltinNode builtin implementation classes}.
     */
    private void installBuiltins() {
        installBuiltin(SLReadlnBuiltinFactory.getInstance());
        installBuiltin(SLPrintlnBuiltinFactory.getInstance());
        installBuiltin(SLNanoTimeBuiltinFactory.getInstance());
        installBuiltin(SLDefineFunctionBuiltinFactory.getInstance());
        installBuiltin(SLStackTraceBuiltinFactory.getInstance());
        installBuiltin(SLHelloEqualsWorldBuiltinFactory.getInstance());
        installBuiltin(SLNewObjectBuiltinFactory.getInstance());
        installBuiltin(SLEvalBuiltinFactory.getInstance());
        installBuiltin(SLImportBuiltinFactory.getInstance());
        installBuiltin(SLGetSizeBuiltinFactory.getInstance());
        installBuiltin(SLHasSizeBuiltinFactory.getInstance());
        installBuiltin(SLIsExecutableBuiltinFactory.getInstance());
        installBuiltin(SLIsNullBuiltinFactory.getInstance());
        installBuiltin(SLWrapPrimitiveBuiltinFactory.getInstance());
        installBuiltin(SLTypeOfBuiltinFactory.getInstance());
        installBuiltin(SLIsInstanceBuiltinFactory.getInstance());
    }

    public void installBuiltin(NodeFactory<? extends SLBuiltinNode> factory) {
        /* Register the builtin function in our function registry. */
        RootCallTarget target = language.lookupBuiltin(factory);
        String rootName = target.getRootNode().getName();
        getFunctionRegistry().register(rootName, target);
    }

    /*
     * Methods for object creation / object property access.
     */
    public AllocationReporter getAllocationReporter() {
        return allocationReporter;
    }

    /*
     * Methods for language interoperability.
     */
    public static Object fromForeignValue(Object a) {
        if (a instanceof Long || a instanceof SLBigNumber || a instanceof String || a instanceof Boolean) {
            return a;
        } else if (a instanceof Character) {
            return fromForeignCharacter((Character) a);
        } else if (a instanceof Number) {
            return fromForeignNumber(a);
        } else if (a instanceof TruffleObject) {
            return a;
        } else if (a instanceof SLContext) {
            return a;
        }
        throw shouldNotReachHere("Value is not a truffle value.");
    }

    @TruffleBoundary
    private static long fromForeignNumber(Object a) {
        return ((Number) a).longValue();
    }

    @TruffleBoundary
    private static String fromForeignCharacter(char c) {
        return String.valueOf(c);
    }

    public CallTarget parse(Source source) {
        return env.parsePublic(source);
    }

    /**
     * Returns an object that contains bindings that were exported across all used languages. To
     * read or write from this object the {@link TruffleObject interop} API can be used.
     */
    public TruffleObject getPolyglotBindings() {
        return (TruffleObject) env.getPolyglotBindings();
    }

    public static SLContext getCurrent() {
        return SLLanguage.getCurrentContext();
    }

}
