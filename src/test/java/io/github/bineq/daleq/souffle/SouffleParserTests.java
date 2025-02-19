package io.github.bineq.daleq.souffle;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SouffleParserTests {

    @Test
    public void test1() {
        String input =
            ".decl ICONST_0(factid: symbol,methodid: symbol,instructioncounter: number)\n";
            //".input ICONST_0 // facts are imported from ICONST_0.facts";
        List<SouffleElement> elements = SouffleParser2.parse(CharStreams.fromString(input));

        assertEquals(1,elements.size());

        Declaration declaration = (Declaration) elements.get(0);
        assertEquals("ICONST_0",declaration.relation());
        assertEquals(3,declaration.attributes().size());

        assertEquals("factid",declaration.attributes().get(0).name());
        assertEquals("symbol",declaration.attributes().get(0).type());

        assertEquals("methodid",declaration.attributes().get(1).name());
        assertEquals("symbol",declaration.attributes().get(1).type());

        assertEquals("instructioncounter",declaration.attributes().get(2).name());
        assertEquals("number",declaration.attributes().get(2).type());


    }
}
