package io.github.bineq.daleq.souffle.provenance;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProvenanceParserTests {

    @Test
    public void test1() {
        String input = "R1";
        DerivationNode root = ProvenanceParser.parse(input);

        printDerivation(input,root);

        assertEquals("R1",root.getId());
        assertEquals(0,root.getChildren().size());
    }

    @Test
    public void test2() {
        String input = "R1[F1,F2]";
        DerivationNode root = ProvenanceParser.parse(input);

        printDerivation(input,root);

        assertEquals("R1",root.getId());
        assertEquals(2,root.getChildren().size());
        assertEquals("F1",root.getChildren().get(0).getId());
        assertEquals("F2",root.getChildren().get(1).getId());
    }

    @Test
    public void test3() {
        String input = "R1[F1,R2[F2,F3,F4]]";
        DerivationNode root = ProvenanceParser.parse(input);

        printDerivation(input,root);

        assertEquals("R1",root.getId());
        assertEquals(2,root.getChildren().size());
        assertEquals("F1",root.getChildren().get(0).getId());

        DerivationNode complexChild = root.getChildren().get(1);
        assertEquals("R2",complexChild.getId());
        assertEquals(3,complexChild.getChildren().size());
        assertEquals("F2",complexChild.getChildren().get(0).getId());
        assertEquals("F3",complexChild.getChildren().get(1).getId());
        assertEquals("F4",complexChild.getChildren().get(2).getId());
    }

    @Test
    public void test4() {
        String input = "_R1_xX[_F1_xX,_F2_xX]";
        DerivationNode root = ProvenanceParser.parse(input);

        printDerivation(input,root);

        assertEquals("_R1_xX",root.getId());
        assertEquals(2,root.getChildren().size());
        assertEquals("_F1_xX",root.getChildren().get(0).getId());
        assertEquals("_F2_xX",root.getChildren().get(1).getId());
    }

    @Test
    public void test5() {
        String input = "R1 [ F1 ,  F2 ]";
        DerivationNode root = ProvenanceParser.parse("R1 [ F1 ,  F2 ] ");

        printDerivation(input,root);

        assertEquals("R1",root.getId());
        assertEquals(2,root.getChildren().size());
        assertEquals("F1",root.getChildren().get(0).getId());
        assertEquals("F2",root.getChildren().get(1).getId());
    }

    private void printDerivation(String input, DerivationNode root) {
        System.out.println("***************************");
        System.out.println("derivation tree for \"" + input + "\":");
        DerivationPrinter.toConsole(root);
        System.out.println("***************************");
    }

}
