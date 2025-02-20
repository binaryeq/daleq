package io.github.bineq.daleq.souffle.provenance;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.TerminalNode;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * Utility to parse encoded provenance tree.
 * @author jens dietrich
 */
public class ProvenanceParser {

    private DerivationNode root = null;

    public static DerivationNode parse (String input) {
        ProvenanceParser parser = new ProvenanceParser();
        parser.doParse(input);
        return parser.root;
    }

    // instance method to access field from anonymous class
    private void doParse (String input) {
        ProofLexer lexer = new ProofLexer(CharStreams.fromString(input));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ProofParser parser = new ProofParser(tokens);
        parser.setErrorHandler(new BailErrorStrategy());

        ProofListener listener = new ProofBaseListener() {
            Stack<List<DerivationNode>> stack = new Stack<>();
            List<DerivationNode> lastChildren = null;
            DerivationNode node = null;

            @Override
            public void exitProof(ProofParser.ProofContext ctx) {
                super.exitProof(ctx);
                assert node != null;
                root = node;
            }

            @Override
            public void enterChildren(ProofParser.ChildrenContext ctx) {
                super.enterChildren(ctx);
                stack.push(new ArrayList<>());
            }

            @Override
            public void exitChildren(ProofParser.ChildrenContext ctx) {
                super.exitChildren(ctx);
                assert stack.size() > 0;
                assert stack.peek().size() > 0;
                lastChildren = stack.pop();
            }

            @Override
            public void enterNode(ProofParser.NodeContext ctx) {
                super.enterNode(ctx);}

            @Override
            public void exitNode(ProofParser.NodeContext ctx) {
                super.exitNode(ctx);

                List<DerivationNode> children = null;
                if (lastChildren != null) {
                    children = lastChildren;
                    lastChildren = null;
                }
                else {
                    children = List.of();
                }
                DerivationNode node = new DerivationNode(ctx.ID().getText(),children);
                if (stack.isEmpty()) {
                    this.node = node;
                }
                else {
                    stack.peek().add(node);
                }
            }
        };

        parser.addParseListener(listener);
        parser.setBuildParseTree(true);
        parser.proof();

    }


}
