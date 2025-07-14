package io.github.bineq.daleq.idb;

import io.github.bineq.daleq.Rules;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestIssue31 extends AbstractIDBTest {

    @Override
    public Rules getRules() {
        return Rules.defaultRules();
    }

    @Test
    public void testPresenceOfAnnotationsInIDBReports() throws IOException {
        IDB idb = IDBReader.read(this.idbFactDir);
        String out = IDBPrinter.print(idb);
        assertTrue(out.contains("pck/ClassWithAnnotations\tLpck/ClassAnnotation;\t[cF1,42,cF2,bar]"));
        assertTrue(out.contains("pck/ClassWithAnnotations::foo()V\tLpck/MethodAnnotation;\t[mF1,42,mF2,bar]"));
        assertTrue(out.contains("pck/ClassWithAnnotations::f(Ljava/lang/String;\tLpck/FieldAnnotation;\t[fF1,42,fF2,bar]"));
        assertTrue(out.contains("IDB_ANNOTATION"));

    }

    @Test
    public void testPresenceOfAnnotationsInProjectedIDBReports() throws IOException {
        IDB idb = IDBReader.read(this.idbFactDir).project();
        String out = IDBPrinter.print(idb);
        assertTrue(out.contains("pck/ClassWithAnnotations\tLpck/ClassAnnotation;\t[cF1,42,cF2,bar]"));
        assertTrue(out.contains("pck/ClassWithAnnotations::foo()V\tLpck/MethodAnnotation;\t[mF1,42,mF2,bar]"));
        assertTrue(out.contains("pck/ClassWithAnnotations::f(Ljava/lang/String;\tLpck/FieldAnnotation;\t[fF1,42,fF2,bar]"));
        assertTrue(out.contains("IDB_ANNOTATION"));
    }

    @Override
    public String getPathOfClassUnderTest() {
        return "/issue31/pck/ClassWithAnnotations.class";
    }
}
