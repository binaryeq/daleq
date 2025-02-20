package io.github.bineq.daleq.souffle.provenance;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.TerminalNode;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility to parse encoded provenance tree.
 * @author jens dietrich
 */
public class ProvenanceParser {

    public static DerivationNode parse (String input) {
        ProofLexer lexer = new ProofLexer(CharStreams.fromString(input));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ProofParser parser = new ProofParser(tokens);
        parser.setErrorHandler(new BailErrorStrategy());


        ProofListener listener = new ProofBaseListener() {
            List<DerivationNode> children = null;
            String label = null;
            DerivationNode parent = null;

            @Override
            public void exitProof(ProofParser.ProofContext ctx) {
                super.exitProof(ctx);
                System.out.println("exit proof: "+ctx.getText());
            }

            @Override
            public void enterChildren(ProofParser.ChildrenContext ctx) {
                super.enterChildren(ctx);
                children = new ArrayList<>();
                System.out.println("enter children: "+ctx.getText());
            }

            @Override
            public void exitChildren(ProofParser.ChildrenContext ctx) {
                super.exitChildren(ctx);
                System.out.println("exit children: "+ctx.getText());
            }

            @Override
            public void visitTerminal(TerminalNode node) {
                super.visitTerminal(node);
                String txt = node.getText();
                if (!(txt.equals("[") || txt.equals("]") || txt.equals(","))) {
                    if (children!=null) {
                        children.add(new DerivationNode(txt));
                    }
                    else {
                        label = txt;
                    }
                }
            }

            @Override
            public void exitNode(ProofParser.NodeContext ctx) {
                super.exitNode(ctx);
                parent = new DerivationNode(label, children==null?List.of():children);
                children = null;
                label = null;
                System.out.println("exit node: "+ctx.getText());
            }
        };

        parser.addParseListener(listener);
        parser.setBuildParseTree(true);


        return rootDerivation;
    }


}
