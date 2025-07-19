package io.github.bineq.daleq.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExitCodeTests {

    private static Analyser DALEQ = new DaleqAnalyser();
    private static Analyser SAME_SOURCE = new SameSourceCodeAnalyser();
    private static Analyser EQUIVALENT_SOURCE = new EquivalentSourceCodeAnalyser();
    private static Analyser SAME_CONTENT = new SameContentAnalyser();
    private static Analyser JAVAP = new VerboseJavapAnalyser();

    @BeforeEach
    public void init() {
        Main.EXIT_STATE = Main.EXIT_CLASSES_EQUAL__RESOURCES_EQUAL__SOURCES_EQUIVALENT;
    }

    private static AnalysisResult result(AnalysisResultState state) {
        return new AnalysisResult(state,"");
    }

    @Test
    public void test1() {
        Main.recordState(DALEQ,result(AnalysisResultState.PASS),"foo.class");
        Main.recordState(SAME_CONTENT,result(AnalysisResultState.PASS),"foo.class");
        assertEquals(Main.EXIT_CLASSES_EQUAL__RESOURCES_EQUAL__SOURCES_EQUIVALENT,Main.EXIT_STATE);
    }

    @Test
    public void test2() {
        Main.recordState(SAME_CONTENT,result(AnalysisResultState.PASS),"foo.class");
        Main.recordState(DALEQ,result(AnalysisResultState.PASS),"foo.class");
        assertEquals(Main.EXIT_CLASSES_EQUAL__RESOURCES_EQUAL__SOURCES_EQUIVALENT,Main.EXIT_STATE);
    }

    @Test
    public void test3() {
        Main.recordState(DALEQ,result(AnalysisResultState.PASS),"foo.class");
        Main.recordState(SAME_CONTENT,result(AnalysisResultState.FAIL),"foo.class");
        assertEquals(Main.EXIT_CLASSES_EQUIVALENT__RESOURCES_EQUAL__SOURCES_EQUIVALENT,Main.EXIT_STATE);
    }

    @Test
    public void test4() {
        Main.recordState(SAME_CONTENT,result(AnalysisResultState.FAIL),"foo.class");
        Main.recordState(DALEQ,result(AnalysisResultState.PASS),"foo.class");
        assertEquals(Main.EXIT_CLASSES_EQUIVALENT__RESOURCES_EQUAL__SOURCES_EQUIVALENT,Main.EXIT_STATE);
    }

    @Test
    public void test5() {
        Main.recordState(DALEQ,result(AnalysisResultState.PASS),"foo.class");
        Main.recordState(SAME_CONTENT,result(AnalysisResultState.ERROR),"foo.class");
        assertEquals(Main.EXIT_CLASSES_EQUIVALENT__RESOURCES_EQUAL__SOURCES_EQUIVALENT,Main.EXIT_STATE);
    }

    @Test
    public void test6() {
        Main.recordState(SAME_CONTENT,result(AnalysisResultState.ERROR),"foo.class");
        Main.recordState(DALEQ,result(AnalysisResultState.PASS),"foo.class");
        assertEquals(Main.EXIT_CLASSES_EQUIVALENT__RESOURCES_EQUAL__SOURCES_EQUIVALENT,Main.EXIT_STATE);
    }

    @Test
    public void test7() {
        Main.recordState(DALEQ,result(AnalysisResultState.FAIL),"foo.class");
        Main.recordState(SAME_CONTENT,result(AnalysisResultState.FAIL),"foo.class");
        assertEquals(Main.EXIT_ALERT,Main.EXIT_STATE);
    }

    @Test
    public void test8() {
        Main.recordState(SAME_CONTENT,result(AnalysisResultState.FAIL),"foo.class");
        Main.recordState(DALEQ,result(AnalysisResultState.FAIL),"foo.class");
        assertEquals(Main.EXIT_ALERT,Main.EXIT_STATE);
    }

    @Test
    public void test9() {
        Main.recordState(DALEQ,result(AnalysisResultState.FAIL),"foo.class");
        Main.recordState(SAME_CONTENT,result(AnalysisResultState.FAIL),"foo.class");
        assertEquals(Main.EXIT_ALERT,Main.EXIT_STATE);
    }

    @Test
    public void test10() {
        Main.recordState(SAME_CONTENT,result(AnalysisResultState.FAIL),"foo.class");
        Main.recordState(DALEQ,result(AnalysisResultState.FAIL),"foo.class");
        assertEquals(Main.EXIT_ALERT,Main.EXIT_STATE);
    }

    @Test
    public void test11() {
        Main.recordState(SAME_CONTENT,result(AnalysisResultState.PASS),"foo.class");
        Main.recordState(DALEQ,result(AnalysisResultState.PASS),"foo.class");
        Main.recordState(SAME_CONTENT,result(AnalysisResultState.PASS),"foo.txt");
        assertEquals(Main.EXIT_CLASSES_EQUAL__RESOURCES_EQUAL__SOURCES_EQUIVALENT,Main.EXIT_STATE);
    }

    @Test
    public void test12() {
        Main.recordState(SAME_CONTENT,result(AnalysisResultState.PASS),"foo.class");
        Main.recordState(DALEQ,result(AnalysisResultState.PASS),"foo.class");
        Main.recordState(SAME_CONTENT,result(AnalysisResultState.FAIL),"foo.txt");
        assertEquals(Main.EXIT_ALERT,Main.EXIT_STATE);
    }

    @Test
    public void test13() {
        Main.recordState(SAME_CONTENT,result(AnalysisResultState.PASS),"foo.class");
        Main.recordState(DALEQ,result(AnalysisResultState.PASS),"foo.class");
        Main.recordState(SAME_SOURCE,result(AnalysisResultState.PASS),"foo.class");
        Main.recordState(EQUIVALENT_SOURCE,result(AnalysisResultState.PASS),"foo.class");
        assertEquals(Main.EXIT_CLASSES_EQUAL__RESOURCES_EQUAL__SOURCES_EQUIVALENT,Main.EXIT_STATE);
    }

    @Test
    public void test14() {
        Main.recordState(SAME_CONTENT,result(AnalysisResultState.PASS),"foo.class");
        Main.recordState(DALEQ,result(AnalysisResultState.PASS),"foo.class");
        Main.recordState(SAME_SOURCE,result(AnalysisResultState.FAIL),"foo.class");
        Main.recordState(EQUIVALENT_SOURCE,result(AnalysisResultState.PASS),"foo.class");
        assertEquals(Main.EXIT_CLASSES_EQUAL__RESOURCES_EQUAL__SOURCES_EQUIVALENT,Main.EXIT_STATE);
    }

    @Test
    public void test15() {
        Main.recordState(SAME_CONTENT,result(AnalysisResultState.PASS),"foo.class");
        Main.recordState(DALEQ,result(AnalysisResultState.PASS),"foo.class");
        Main.recordState(SAME_SOURCE,result(AnalysisResultState.FAIL),"foo.class");
        Main.recordState(EQUIVALENT_SOURCE,result(AnalysisResultState.FAIL),"foo.class");
        assertEquals(Main.EXIT_ALERT,Main.EXIT_STATE);
    }

    // javap is not used ..
    @Test
    public void test16() {
        Main.recordState(SAME_CONTENT,result(AnalysisResultState.PASS),"foo.class");
        Main.recordState(DALEQ,result(AnalysisResultState.PASS),"foo.class");
        Main.recordState(SAME_SOURCE,result(AnalysisResultState.FAIL),"foo.class");
        Main.recordState(EQUIVALENT_SOURCE,result(AnalysisResultState.PASS),"foo.class");
        Main.recordState(JAVAP,result(AnalysisResultState.FAIL),"foo.class");
        assertEquals(Main.EXIT_CLASSES_EQUAL__RESOURCES_EQUAL__SOURCES_EQUIVALENT,Main.EXIT_STATE);
    }

}
