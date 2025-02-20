package io.github.bineq.daleq.souffle.provenance;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProvenanceParserTests {

    @Test
    public void test1() {
        String input = "R1";
        DerivationNode root = ProvenanceParser.parse(input);

        printDerivation(input,root);

        assertEquals("R1",root.id());
        assertEquals(0,root.children().size());
    }

    @Test
    public void test2() {
        String input = "R1[F1,F2]";
        DerivationNode root = ProvenanceParser.parse(input);

        printDerivation(input,root);

        assertEquals("R1",root.id());
        assertEquals(2,root.children().size());
        assertEquals("F1",root.children().get(0).id());
        assertEquals("F2",root.children().get(1).id());
    }

    @Test
    public void test3() {
        String input = "R1[F1,R2[F2,F3,F4]]";
        DerivationNode root = ProvenanceParser.parse(input);

        printDerivation(input,root);

        assertEquals("R1",root.id());
        assertEquals(2,root.children().size());
        assertEquals("F1",root.children().get(0).id());

        DerivationNode complexChild = root.children().get(1);
        assertEquals("R2",complexChild.id());
        assertEquals(3,complexChild.children().size());
        assertEquals("F2",complexChild.children().get(0).id());
        assertEquals("F3",complexChild.children().get(1).id());
        assertEquals("F4",complexChild.children().get(2).id());
    }

    @Test
    public void test4() {
        String input = "_R1_xX[_F1_xX,_F2_xX]";
        DerivationNode root = ProvenanceParser.parse(input);

        printDerivation(input,root);

        assertEquals("_R1_xX",root.id());
        assertEquals(2,root.children().size());
        assertEquals("_F1_xX",root.children().get(0).id());
        assertEquals("_F2_xX",root.children().get(1).id());
    }

    @Test
    public void test5() {
        String input = "R1 [ F1 ,  F2 ]";
        DerivationNode root = ProvenanceParser.parse("R1 [ F1 ,  F2 ] ");

        printDerivation(input,root);

        assertEquals("R1",root.id());
        assertEquals(2,root.children().size());
        assertEquals("F1",root.children().get(0).id());
        assertEquals("F2",root.children().get(1).id());
    }

    private void printDerivation(String input, DerivationNode root) {
        System.out.println("***************************");
        System.out.println("derivation tree for \"" + input + "\":");
        DerivationPrinter.toConsole(root);
        System.out.println("***************************");
    }

}
