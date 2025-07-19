package io.github.bineq.daleq.edb;

import org.junit.jupiter.api.Test;
import static io.github.bineq.daleq.edb.FactExtractor.INNER_ANO_CLASS_PATTERN;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InnerAnoClassRegexTest {

    @Test
    public void testIsAnoInnerClass1 () {
        String className = "com/example/Foo$1";
        assertTrue(INNER_ANO_CLASS_PATTERN.matcher(className).matches());
    }

    @Test
    public void testIsAnoInnerClass2 () {
        String className = "com/example_abc43jj/Foo_Bar42$1234";
        assertTrue(INNER_ANO_CLASS_PATTERN.matcher(className).matches());
    }

    @Test
    public void testIsAnoInnerClass3 () {
        String className = "Foo$1";
        assertTrue(INNER_ANO_CLASS_PATTERN.matcher(className).matches());
    }

    @Test
    public void testIsntAnoInnerClass1 () {
        String className = "com/example/Foo";
        assertFalse(INNER_ANO_CLASS_PATTERN.matcher(className).matches());
    }

    @Test
    public void testIsAnoInnerClass4 () {
        String className = "com/example/Foo$Bar$1";
        assertTrue(INNER_ANO_CLASS_PATTERN.matcher(className).matches());
    }

    @Test
    public void testIsAnoInnerClass5 () {
        String className = "com/example/Foo$Bar$1$2";
        assertTrue(INNER_ANO_CLASS_PATTERN.matcher(className).matches());
    }

    @Test
    public void testIsntAnoInnerClass3 () {
        String className = "com/example/Foo$Bar$Blur";
        assertFalse(INNER_ANO_CLASS_PATTERN.matcher(className).matches());
    }

    @Test
    public void testIsntAnoInnerClass2 () {
        String className = "com/example/Foo$Bar";
        assertFalse(INNER_ANO_CLASS_PATTERN.matcher(className).matches());
    }

}
