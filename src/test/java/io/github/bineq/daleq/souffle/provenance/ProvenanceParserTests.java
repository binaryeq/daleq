package io.github.bineq.daleq.souffle.provenance;

import org.junit.jupiter.api.Test;

public class ProvenanceParserTests {

    @Test
    public void test1() {
        ProvenanceParser.parse("R1[F1,F2]");
    }
}
