package org.hamidelmaazouz.graaljulia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleLanguage.ContextPolicy;
import com.oracle.truffle.api.debug.DebuggerTags;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.instrumentation.AllocationReporter;
import com.oracle.truffle.api.instrumentation.ProvidedTags;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.source.Source;
import org.hamidelmaazouz.graaljulia.builtins.SLBuiltinNode;
import org.hamidelmaazouz.graaljulia.builtins.SLDefineFunctionBuiltin;
import org.hamidelmaazouz.graaljulia.builtins.SLNanoTimeBuiltin;
import org.hamidelmaazouz.graaljulia.builtins.SLPrintlnBuiltin;
import org.hamidelmaazouz.graaljulia.builtins.SLReadlnBuiltin;
import org.hamidelmaazouz.graaljulia.builtins.SLStackTraceBuiltin;
import org.hamidelmaazouz.graaljulia.nodes.SLEvalRootNode;
import org.hamidelmaazouz.graaljulia.nodes.SLExpressionNode;
import org.hamidelmaazouz.graaljulia.nodes.SLRootNode;
import org.hamidelmaazouz.graaljulia.nodes.SLTypes;
import org.hamidelmaazouz.graaljulia.nodes.SLUndefinedFunctionRootNode;
import org.hamidelmaazouz.graaljulia.nodes.controlflow.SLBlockNode;
import org.hamidelmaazouz.graaljulia.nodes.controlflow.SLBreakNode;
import org.hamidelmaazouz.graaljulia.nodes.controlflow.SLContinueNode;
import org.hamidelmaazouz.graaljulia.nodes.controlflow.SLDebuggerNode;
import org.hamidelmaazouz.graaljulia.nodes.controlflow.SLIfNode;
import org.hamidelmaazouz.graaljulia.nodes.controlflow.SLReturnNode;
import org.hamidelmaazouz.graaljulia.nodes.controlflow.SLWhileNode;
import org.hamidelmaazouz.graaljulia.nodes.expression.SLAddNode;
import org.hamidelmaazouz.graaljulia.nodes.expression.SLBigIntegerLiteralNode;
import org.hamidelmaazouz.graaljulia.nodes.expression.SLDivNode;
import org.hamidelmaazouz.graaljulia.nodes.expression.SLEqualNode;
import org.hamidelmaazouz.graaljulia.nodes.expression.SLFunctionLiteralNode;
import org.hamidelmaazouz.graaljulia.nodes.expression.SLInvokeNode;
import org.hamidelmaazouz.graaljulia.nodes.expression.SLLessOrEqualNode;
import org.hamidelmaazouz.graaljulia.nodes.expression.SLLessThanNode;
import org.hamidelmaazouz.graaljulia.nodes.expression.SLLogicalAndNode;
import org.hamidelmaazouz.graaljulia.nodes.expression.SLLogicalOrNode;
import org.hamidelmaazouz.graaljulia.nodes.expression.SLMulNode;
import org.hamidelmaazouz.graaljulia.nodes.expression.SLReadPropertyNode;
import org.hamidelmaazouz.graaljulia.nodes.expression.SLStringLiteralNode;
import org.hamidelmaazouz.graaljulia.nodes.expression.SLSubNode;
import org.hamidelmaazouz.graaljulia.nodes.expression.SLWritePropertyNode;
import org.hamidelmaazouz.graaljulia.nodes.local.SLReadArgumentNode;
import org.hamidelmaazouz.graaljulia.nodes.local.SLReadLocalVariableNode;
import org.hamidelmaazouz.graaljulia.nodes.local.SLWriteLocalVariableNode;
import org.hamidelmaazouz.graaljulia.parser.SLNodeFactory;
import org.hamidelmaazouz.graaljulia.parser.SimpleLanguageLexer;
import org.hamidelmaazouz.graaljulia.parser.SimpleLanguageParser;
import org.hamidelmaazouz.graaljulia.runtime.SLBigNumber;
import org.hamidelmaazouz.graaljulia.runtime.SLContext;
import org.hamidelmaazouz.graaljulia.runtime.SLFunction;
import org.hamidelmaazouz.graaljulia.runtime.SLFunctionRegistry;
import org.hamidelmaazouz.graaljulia.runtime.SLLanguageView;
import org.hamidelmaazouz.graaljulia.runtime.SLNull;
import org.hamidelmaazouz.graaljulia.runtime.SLObject;

/**
 * SL is a simple language to demonstrate and showcase features of Truffle. The implementation is as
 * simple and clean as possible in order to help understanding the ideas and concepts of Truffle.
 * The language has first class functions, and objects are key-value stores.
 * <p>
 * SL is dynamically typed, i.e., there are no type names specified by the programmer. SL is
 * strongly typed, i.e., there is no automatic conversion between types. If an operation is not
 * available for the types encountered at run time, a type error is reported and execution is
 * stopped. For example, {@code 4 - "2"} results in a type error because subtraction is only defined
 * for numbers.
 *
 * <p>
 * <b>Types:</b>
 * <ul>
 * <li>Number: arbitrary precision integer numbers. The implementation uses the Java primitive type
 * {@code long} to represent numbers that fit into the 64 bit range, and {@link SLBigNumber} for
 * numbers that exceed the range. Using a primitive type such as {@code long} is crucial for
 * performance.
 * <li>Boolean: implemented as the Java primitive type {@code boolean}.
 * <li>String: implemented as the Java standard type {@link String}.
 * <li>Function: implementation type {@link SLFunction}.
 * <li>Object: efficient implementation using the object model provided by Truffle. The
 * implementation type of objects is a subclass of {@link DynamicObject}.
 * <li>Null (with only one value {@code null}): implemented as the singleton
 * {@link SLNull#SINGLETON}.
 * </ul>
 * The class {@link SLTypes} lists these types for the Truffle DSL, i.e., for type-specialized
 * operations that are specified using Truffle DSL annotations.
 *
 * <p>
 * <b>Language concepts:</b>
 * <ul>
 * <li>Literals for {@link SLBigIntegerLiteralNode numbers} , {@link SLStringLiteralNode strings},
 * and {@link SLFunctionLiteralNode functions}.
 * <li>Basic arithmetic, logical, and comparison operations: {@link SLAddNode +}, {@link SLSubNode
 * -}, {@link SLMulNode *}, {@link SLDivNode /}, {@link SLLogicalAndNode logical and},
 * {@link SLLogicalOrNode logical or}, {@link SLEqualNode ==}, !=, {@link SLLessThanNode &lt;},
 * {@link SLLessOrEqualNode &le;}, &gt;, &ge;.
 * <li>Local variables: local variables must be defined (via a {@link SLWriteLocalVariableNode
 * write}) before they can be used (by a {@link SLReadLocalVariableNode read}). Local variables are
 * not visible outside of the block where they were first defined.
 * <li>Basic control flow statements: {@link SLBlockNode blocks}, {@link SLIfNode if},
 * {@link SLWhileNode while} with {@link SLBreakNode break} and {@link SLContinueNode continue},
 * {@link SLReturnNode return}.
 * <li>Debugging control: {@link SLDebuggerNode debugger} statement uses
 * {@link DebuggerTags#AlwaysHalt} tag to halt the execution when run under the debugger.
 * <li>Function calls: {@link SLInvokeNode invocations} are efficiently implemented with
 * {@link SLDispatchNode polymorphic inline caches}.
 * <li>Object access: {@link SLReadPropertyNode} and {@link SLWritePropertyNode} use a cached
 * {@link DynamicObjectLibrary} as the polymorphic inline cache for property reads and writes,
 * respectively.
 * </ul>
 *
 * <p>
 * <b>Syntax and parsing:</b><br>
 * The syntax is described as an attributed grammar. The {@link SimpleLanguageParser} and
 * {@link SimpleLanguageLexer} are automatically generated by ANTLR 4. The grammar contains semantic
 * actions that build the AST for a method. To keep these semantic actions short, they are mostly
 * calls to the {@link SLNodeFactory} that performs the actual node creation. All functions found in
 * the SL source are added to the {@link SLFunctionRegistry}, which is accessible from the
 * {@link SLContext}.
 *
 * <p>
 * <b>Builtin functions:</b><br>
 * Library functions that are available to every SL source without prior definition are called
 * builtin functions. They are added to the {@link SLFunctionRegistry} when the {@link SLContext} is
 * created. Some of the current builtin functions are
 * <ul>
 * <li>{@link SLReadlnBuiltin readln}: Read a String from the {@link SLContext#getInput() standard
 * input}.
 * <li>{@link SLPrintlnBuiltin println}: Write a value to the {@link SLContext#getOutput() standard
 * output}.
 * <li>{@link SLNanoTimeBuiltin nanoTime}: Returns the value of a high-resolution time, in
 * nanoseconds.
 * <li>{@link SLDefineFunctionBuiltin defineFunction}: Parses the functions provided as a String
 * argument and adds them to the function registry. Functions that are already defined are replaced
 * with the new version.
 * <li>{@link SLStackTraceBuiltin stckTrace}: Print all function activations with all local
 * variables.
 * </ul>
 */
@TruffleLanguage.Registration(id = SLLanguage.ID, name = "SL", defaultMimeType = SLLanguage.MIME_TYPE, characterMimeTypes = SLLanguage.MIME_TYPE, contextPolicy = ContextPolicy.SHARED, fileTypeDetectors = SLFileDetector.class)
@ProvidedTags({StandardTags.CallTag.class, StandardTags.StatementTag.class, StandardTags.RootTag.class, StandardTags.RootBodyTag.class, StandardTags.ExpressionTag.class, DebuggerTags.AlwaysHalt.class,
                StandardTags.ReadVariableTag.class, StandardTags.WriteVariableTag.class})
public final class SLLanguage extends TruffleLanguage<SLContext> {
    public static volatile int counter;

    public static final String ID = "sl";
    public static final String MIME_TYPE = "application/x-sl";
    private static final Source BUILTIN_SOURCE = Source.newBuilder(SLLanguage.ID, "", "SL builtin").build();

    private final Assumption singleContext = Truffle.getRuntime().createAssumption("Single SL context.");

    private final Map<NodeFactory<? extends SLBuiltinNode>, RootCallTarget> builtinTargets = new ConcurrentHashMap<>();
    private final Map<String, RootCallTarget> undefinedFunctions = new ConcurrentHashMap<>();

    private final Shape rootShape;

    public SLLanguage() {
        counter++;
        this.rootShape = Shape.newBuilder().layout(SLObject.class).build();
    }

    @Override
    protected SLContext createContext(Env env) {
        return new SLContext(this, env, new ArrayList<>(EXTERNAL_BUILTINS));
    }

    public RootCallTarget getOrCreateUndefinedFunction(String name) {
        RootCallTarget target = undefinedFunctions.get(name);
        if (target == null) {
            target = Truffle.getRuntime().createCallTarget(new SLUndefinedFunctionRootNode(this, name));
            RootCallTarget other = undefinedFunctions.putIfAbsent(name, target);
            if (other != null) {
                target = other;
            }
        }
        return target;
    }

    public RootCallTarget lookupBuiltin(NodeFactory<? extends SLBuiltinNode> factory) {
        RootCallTarget target = builtinTargets.get(factory);
        if (target != null) {
            return target;
        }

        /*
         * The builtin node factory is a class that is automatically generated by the Truffle DSL.
         * The signature returned by the factory reflects the signature of the @Specialization
         *
         * methods in the builtin classes.
         */
        int argumentCount = factory.getExecutionSignature().size();
        SLExpressionNode[] argumentNodes = new SLExpressionNode[argumentCount];
        /*
         * Builtin functions are like normal functions, i.e., the arguments are passed in as an
         * Object[] array encapsulated in SLArguments. A SLReadArgumentNode extracts a parameter
         * from this array.
         */
        for (int i = 0; i < argumentCount; i++) {
            argumentNodes[i] = new SLReadArgumentNode(i);
        }
        /* Instantiate the builtin node. This node performs the actual functionality. */
        SLBuiltinNode builtinBodyNode = factory.createNode((Object) argumentNodes);
        builtinBodyNode.addRootTag();
        /* The name of the builtin function is specified via an annotation on the node class. */
        String name = lookupNodeInfo(builtinBodyNode.getClass()).shortName();
        builtinBodyNode.setUnavailableSourceSection();

        /* Wrap the builtin in a RootNode. Truffle requires all AST to start with a RootNode. */
        SLRootNode rootNode = new SLRootNode(this, new FrameDescriptor(), builtinBodyNode, BUILTIN_SOURCE.createUnavailableSection(), name);

        /*
         * Register the builtin function in the builtin registry. Call targets for builtins may be
         * reused across multiple contexts.
         */
        RootCallTarget newTarget = Truffle.getRuntime().createCallTarget(rootNode);
        RootCallTarget oldTarget = builtinTargets.put(factory, newTarget);
        if (oldTarget != null) {
            return oldTarget;
        }
        return newTarget;
    }

    public static NodeInfo lookupNodeInfo(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        NodeInfo info = clazz.getAnnotation(NodeInfo.class);
        if (info != null) {
            return info;
        } else {
            return lookupNodeInfo(clazz.getSuperclass());
        }
    }

    @Override
    protected CallTarget parse(ParsingRequest request) throws Exception {
        Source source = request.getSource();
        Map<String, RootCallTarget> functions;
        /*
         * Parse the provided source. At this point, we do not have a SLContext yet. Registration of
         * the functions with the SLContext happens lazily in SLEvalRootNode.
         */
        if (request.getArgumentNames().isEmpty()) {
            functions = SimpleLanguageParser.parseSL(this, source);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("function main(");
            String sep = "";
            for (String argumentName : request.getArgumentNames()) {
                sb.append(sep);
                sb.append(argumentName);
                sep = ",";
            }
            sb.append(") { return ");
            sb.append(source.getCharacters());
            sb.append(";}");
            String language = source.getLanguage() == null ? ID : source.getLanguage();
            Source decoratedSource = Source.newBuilder(language, sb.toString(), source.getName()).build();
            functions = SimpleLanguageParser.parseSL(this, decoratedSource);
        }

        RootCallTarget main = functions.get("main");
        RootNode evalMain;
        if (main != null) {
            /*
             * We have a main function, so "evaluating" the parsed source means invoking that main
             * function. However, we need to lazily register functions into the SLContext first, so
             * we cannot use the original SLRootNode for the main function. Instead, we create a new
             * SLEvalRootNode that does everything we need.
             */
            evalMain = new SLEvalRootNode(this, main, functions);
        } else {
            /*
             * Even without a main function, "evaluating" the parsed source needs to register the
             * functions into the SLContext.
             */
            evalMain = new SLEvalRootNode(this, null, functions);
        }
        return Truffle.getRuntime().createCallTarget(evalMain);
    }

    /**
     * SLLanguage specifies the {@link ContextPolicy#SHARED} in
     * {@link Registration#contextPolicy()}. This means that a single {@link TruffleLanguage}
     * instance can be reused for multiple language contexts. Before this happens the Truffle
     * framework notifies the language by invoking {@link #initializeMultipleContexts()}. This
     * allows the language to invalidate certain assumptions taken for the single context case. One
     * assumption SL takes for single context case is located in {@link SLEvalRootNode}. There
     * functions are only tried to be registered once in the single context case, but produce a
     * boundary call in the multi context case, as function registration is expected to happen more
     * than once.
     *
     * Value identity caches should be avoided and invalidated for the multiple contexts case as no
     * value will be the same. Instead, in multi context case, a language should only use types,
     * shapes and code to speculate.
     *
     * For a new language it is recommended to start with {@link ContextPolicy#EXCLUSIVE} and as the
     * language gets more mature switch to {@link ContextPolicy#SHARED}.
     */
    @Override
    protected void initializeMultipleContexts() {
        singleContext.invalidate();
    }

    public boolean isSingleContext() {
        return singleContext.isValid();
    }

    @Override
    protected Object getLanguageView(SLContext context, Object value) {
        return SLLanguageView.create(value);
    }

    /*
     * Still necessary for the old SL TCK to pass. We should remove with the old TCK. New language
     * should not override this.
     */
    @SuppressWarnings("deprecation")
    @Override
    protected Object findExportedSymbol(SLContext context, String globalName, boolean onlyExplicit) {
        return context.getFunctionRegistry().lookup(globalName, false);
    }

    @Override
    protected boolean isVisible(SLContext context, Object value) {
        return !InteropLibrary.getFactory().getUncached(value).isNull(value);
    }

    @Override
    protected Object getScope(SLContext context) {
        return context.getFunctionRegistry().getFunctionsObject();
    }

    public Shape getRootShape() {
        return rootShape;
    }

    /**
     * Allocate an empty object. All new objects initially have no properties. Properties are added
     * when they are first stored, i.e., the store triggers a shape change of the object.
     */
    public SLObject createObject(AllocationReporter reporter) {
        reporter.onEnter(null, 0, AllocationReporter.SIZE_UNKNOWN);
        SLObject object = new SLObject(rootShape);
        reporter.onReturnValue(object, 0, AllocationReporter.SIZE_UNKNOWN);
        return object;
    }

    public static SLContext getCurrentContext() {
        return getCurrentContext(SLLanguage.class);
    }

    private static final List<NodeFactory<? extends SLBuiltinNode>> EXTERNAL_BUILTINS = Collections.synchronizedList(new ArrayList<>());

    public static void installBuiltin(NodeFactory<? extends SLBuiltinNode> builtin) {
        EXTERNAL_BUILTINS.add(builtin);
    }

}
