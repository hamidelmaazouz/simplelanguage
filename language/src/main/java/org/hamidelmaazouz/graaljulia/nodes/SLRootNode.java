package org.hamidelmaazouz.graaljulia.nodes;

import java.util.ArrayList;
import java.util.List;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.InstrumentableNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.nodes.NodeVisitor;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.SourceSection;
import org.hamidelmaazouz.graaljulia.SLLanguage;
import org.hamidelmaazouz.graaljulia.builtins.SLBuiltinNode;
import org.hamidelmaazouz.graaljulia.nodes.controlflow.SLBlockNode;
import org.hamidelmaazouz.graaljulia.nodes.controlflow.SLFunctionBodyNode;
import org.hamidelmaazouz.graaljulia.nodes.local.SLReadArgumentNode;
import org.hamidelmaazouz.graaljulia.nodes.local.SLWriteLocalVariableNode;

/**
 * The root of all SL execution trees. It is a Truffle requirement that the tree root extends the
 * class {@link RootNode}. This class is used for both builtin and user-defined functions. For
 * builtin functions, the {@link #bodyNode} is a subclass of {@link SLBuiltinNode}. For user-defined
 * functions, the {@link #bodyNode} is a {@link SLFunctionBodyNode}.
 */
@NodeInfo(language = "SL", description = "The root of all SL execution trees")
public class SLRootNode extends RootNode {
    /** The function body that is executed, and specialized during execution. */
    @Child private SLExpressionNode bodyNode;

    /** The name of the function, for printing purposes only. */
    private final String name;

    private boolean isCloningAllowed;

    private final SourceSection sourceSection;

    @CompilerDirectives.CompilationFinal(dimensions = 1) private volatile SLWriteLocalVariableNode[] argumentNodesCache;

    public SLRootNode(SLLanguage language, FrameDescriptor frameDescriptor, SLExpressionNode bodyNode, SourceSection sourceSection, String name) {
        super(language, frameDescriptor);
        this.bodyNode = bodyNode;
        this.name = name;
        this.sourceSection = sourceSection;
    }

    @Override
    public SourceSection getSourceSection() {
        return sourceSection;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        assert lookupContextReference(SLLanguage.class).get() != null;
        return bodyNode.executeGeneric(frame);
    }

    public SLExpressionNode getBodyNode() {
        return bodyNode;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setCloningAllowed(boolean isCloningAllowed) {
        this.isCloningAllowed = isCloningAllowed;
    }

    @Override
    public boolean isCloningAllowed() {
        return isCloningAllowed;
    }

    @Override
    public String toString() {
        return "root " + name;
    }

    public final SLWriteLocalVariableNode[] getDeclaredArguments() {
        SLWriteLocalVariableNode[] argumentNodes = argumentNodesCache;
        if (argumentNodes == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            argumentNodesCache = argumentNodes = findArgumentNodes();
        }
        return argumentNodes;
    }

    private SLWriteLocalVariableNode[] findArgumentNodes() {
        List<SLWriteLocalVariableNode> writeArgNodes = new ArrayList<>(4);
        NodeUtil.forEachChild(this.getBodyNode(), new NodeVisitor() {

            private SLWriteLocalVariableNode wn; // The current write node containing a slot

            @Override
            public boolean visit(Node node) {
                // When there is a write node, search for SLReadArgumentNode among its children:
                if (node instanceof InstrumentableNode.WrapperNode) {
                    return NodeUtil.forEachChild(node, this);
                }
                if (node instanceof SLWriteLocalVariableNode) {
                    wn = (SLWriteLocalVariableNode) node;
                    boolean all = NodeUtil.forEachChild(node, this);
                    wn = null;
                    return all;
                } else if (wn != null && (node instanceof SLReadArgumentNode)) {
                    writeArgNodes.add(wn);
                    return true;
                } else if (wn == null && (node instanceof SLStatementNode && !(node instanceof SLBlockNode || node instanceof SLFunctionBodyNode))) {
                    // A different SL node - we're done.
                    return false;
                } else {
                    return NodeUtil.forEachChild(node, this);
                }
            }
        });
        return writeArgNodes.toArray(new SLWriteLocalVariableNode[writeArgNodes.size()]);
    }

}
