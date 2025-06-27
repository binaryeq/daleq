package io.github.bineq.daleq.edb;

import io.github.bineq.daleq.Fact;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue31Test extends AbstractFactExtractionTest {
    @Override
    protected String getTestClass() {
        return "/issue31/pck/ClassWithAnnotations.class";
    }

    @Test
    public void testClassAnnotations() {
        List<Fact> facts = getFacts(EBDAdditionalPredicates.ANNOTATION);
        assertEquals(3,facts.size());
        Fact fact = facts.stream()
            .filter(f -> f.values()[1].equals("pck/ClassWithAnnotations"))
            .findFirst().get();

        assertEquals(EBDAdditionalPredicates.ANNOTATION,fact.predicate());
        assertEquals("pck/ClassWithAnnotations",fact.values()[1]);
        assertEquals("Lpck/ClassAnnotation;",fact.values()[2]);
        assertEquals("cF1 -> 42,cF2 -> bar",fact.values()[3]);
    }

    @Test
    public void testMethodAnnotations() {
        List<Fact> facts = getFacts(EBDAdditionalPredicates.ANNOTATION);
        assertEquals(3,facts.size());
        Fact fact = facts.stream()
            .filter(f -> f.values()[1].equals("pck/ClassWithAnnotations::foo()V"))
            .findFirst().get();

        assertEquals(EBDAdditionalPredicates.ANNOTATION,fact.predicate());
        assertEquals("pck/ClassWithAnnotations::foo()V",fact.values()[1]);
        assertEquals("Lpck/MethodAnnotation;",fact.values()[2]);
        assertEquals("mF1 -> 42,mF2 -> bar",fact.values()[3]);
    }

    @Test
    public void testFieldAnnotations() {
        List<Fact> facts = getFacts(EBDAdditionalPredicates.ANNOTATION);
        assertEquals(3,facts.size());
        Fact fact = facts.stream()
            .filter(f -> f.values()[1].equals("pck/ClassWithAnnotations::f(Ljava/lang/String;"))
            .findFirst().get();

        assertEquals(EBDAdditionalPredicates.ANNOTATION,fact.predicate());
        assertEquals("pck/ClassWithAnnotations::f(Ljava/lang/String;",fact.values()[1]);
        assertEquals("Lpck/FieldAnnotation;",fact.values()[2]);
        assertEquals("fF1 -> 42,fF2 -> bar",fact.values()[3]);
    }
}
