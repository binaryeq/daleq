package io.github.bineq.daleq.edb;

import io.github.bineq.daleq.Fact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class TestIssue24 extends AbstractFactExtractionAndComparisonTest {

    private List<Fact> instrFacts1 = null;
    private List<Fact> instrFacts2 = null;

    @BeforeEach
    public void setup() throws Exception {
        super.setup();
        instrFacts1 = this.getInstructionFacts(facts1,"org/apache/commons/configuration2/interpol/ConfigurationInterpolator","setStringConverter","(Ljava/util/function/Function;)V");
        instrFacts2 = this.getInstructionFacts(facts2,"org/apache/commons/configuration2/interpol/ConfigurationInterpolator","setStringConverter","(Ljava/util/function/Function;)V");

    }

    @Override
    protected String getTestClass1() {
        return "/issue24/jar1/ConfigurationInterpolator.class";
    }

    @Override
    protected String getTestClass2() {
        return "/issue24/jar2/ConfigurationInterpolator.class";
    }

    @Test
    public void testLabels1() {
        assumeTrue("IFNULL".equals(instrFacts1.get(2).predicate().getName()));
        assumeTrue("IFNULL".equals(instrFacts2.get(2).predicate().getName()));
        assertEquals(instrFacts1.get(2).values()[3],instrFacts2.get(2).values()[3]);
    }

    @Test
    public void testLabels2() {
        assumeTrue("GOTO".equals(instrFacts1.get(4).predicate().getName()));
        assumeTrue("GOTO".equals(instrFacts2.get(4).predicate().getName()));
        assertEquals(instrFacts1.get(4).values()[3],instrFacts2.get(4).values()[3]);
    }

    @Test
    public void testLabels3() {
        assumeTrue("LABEL".equals(instrFacts1.get(5).predicate().getName()));
        assumeTrue("LABEL".equals(instrFacts2.get(5).predicate().getName()));
        assertEquals(instrFacts1.get(5).values()[3],instrFacts2.get(5).values()[3]);
    }

    @Test
    public void testLabels4() {
        assumeTrue("LABEL".equals(instrFacts1.get(7).predicate().getName()));
        assumeTrue("LABEL".equals(instrFacts2.get(7).predicate().getName()));
        assertEquals(instrFacts1.get(7).values()[3],instrFacts2.get(7).values()[3]);
    }
}
