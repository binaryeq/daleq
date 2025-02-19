package io.github.bineq.daleq.souffle;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * High level parser.
 * @author jens dietrich
 */
public class SouffleParser2 {
    public static List<SouffleElement> parse (CharStream input) {
        List<SouffleElement> elements = new ArrayList<>();
        SouffleLexer lexer = new SouffleLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SouffleParser parser = new SouffleParser(tokens);
        parser.setErrorHandler(new BailErrorStrategy());

        SouffleListener listener = new SouffleBaseListener() {

            @Override
            public void enterDeclaration(SouffleParser.DeclarationContext ctx) {
                super.enterDeclaration(ctx);
            }

            @Override
            public void exitDeclaration(SouffleParser.DeclarationContext ctx) {
                super.exitDeclaration(ctx);
                String relation = ctx.NAME().getText();
                List<Attribute> attributes = ctx.attributes().attribute().stream()
                    .map(attrCtx -> new Attribute(attrCtx.NAME().getText(),attrCtx.TYPE().getText()))
                    .collect(Collectors.toList());
                Declaration declaration = new Declaration(relation, attributes);
                elements.add(declaration);
            }
        };
        parser.addParseListener(listener);
        parser.setBuildParseTree(true);
        parser.souffle();

        return elements;
    }
}
