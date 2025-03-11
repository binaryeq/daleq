package io.github.bineq.daleq.idb;

import io.github.bineq.daleq.Fact;
import java.util.*;

/**
 * An internal representation of the IDB itself generated for a given method.
 * This is organised by a set of global facts, and instruction facts organised by method.
 * This is an utility to compare generated IDBs for equivalence.
 * @author jens dietrich
 */
class IDB {

    static final Comparator<Fact> COMPARE_BY_SLOT_1 = Comparator.comparing(f -> f.values()[1].toString());
    static final Comparator<Fact> COMPARE_BY_PREDICATE_NAME = Comparator.comparing(f -> f.predicate().getName());
    static final Comparator<Fact> COMPARE_INSTRUCTION_FACTS_BY_POSITION = Comparator.comparingInt(f -> ((Integer) f.values()[2]));

    Fact classSuperclassFact = null;
    Fact classSignatureFact = null;
    Fact bytecodeVersionFact = null;
    List<Fact> classInterfaceFacts = new ArrayList<>();
    Fact classRawAccessFact = null; // raw, value is single fact for all int-encoded access flags
    Set<Fact> classAccessFacts = new TreeSet<>(COMPARE_BY_PREDICATE_NAME);

    Set<Fact> methodFacts = new TreeSet<>(COMPARE_BY_SLOT_1);
    Set<Fact> fieldFacts = new TreeSet<>(COMPARE_BY_SLOT_1);

    Map<String,Fact> methodRawAccessFacts = new HashMap<>(); // raw, value is single fact for all int-encoded access flags
    Map<String,Set<Fact>> methodAccessFacts = new HashMap<>();
    Map<String,Fact> methodSignatureFacts = new HashMap<>();
    Map<String,Set<Fact>> methodInstructionFacts = new HashMap<>();

    Map<String,Set<Fact>> fieldAccessFacts = new HashMap<>();
    Map<String,Fact> fieldRawAccessFacts = new HashMap<>(); // raw, value is single fact for all int-encoded access flags
    Map<String,Fact> fieldSignatureFacts = new HashMap<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IDB idb = (IDB) o;
        return Objects.equals(classSuperclassFact, idb.classSuperclassFact) && Objects.equals(classSignatureFact, idb.classSignatureFact) && Objects.equals(bytecodeVersionFact, idb.bytecodeVersionFact) && Objects.equals(classInterfaceFacts, idb.classInterfaceFacts) && Objects.equals(classRawAccessFact, idb.classRawAccessFact) && Objects.equals(classAccessFacts, idb.classAccessFacts) && Objects.equals(methodFacts, idb.methodFacts) && Objects.equals(fieldFacts, idb.fieldFacts) && Objects.equals(methodRawAccessFacts, idb.methodRawAccessFacts) && Objects.equals(methodAccessFacts, idb.methodAccessFacts) && Objects.equals(methodSignatureFacts, idb.methodSignatureFacts) && Objects.equals(methodInstructionFacts, idb.methodInstructionFacts) && Objects.equals(fieldAccessFacts, idb.fieldAccessFacts) && Objects.equals(fieldRawAccessFacts, idb.fieldRawAccessFacts) && Objects.equals(fieldSignatureFacts, idb.fieldSignatureFacts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classSuperclassFact, classSignatureFact, bytecodeVersionFact, classInterfaceFacts, classRawAccessFact, classAccessFacts, methodFacts, fieldFacts, methodRawAccessFacts, methodAccessFacts, methodSignatureFacts, methodInstructionFacts, fieldAccessFacts, fieldRawAccessFacts, fieldSignatureFacts);
    }
}
