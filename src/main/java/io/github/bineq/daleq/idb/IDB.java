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

    Fact classSuperclassFact = null;
    Fact classSignatureFact = null;
    Fact bytecodeVersionFact = null;
    List<Fact> classInterfaceFacts = new ArrayList<>();
    Fact classRawAccessFact = null; // raw, value is single fact for all int-encoded access flags
    Set<Fact> classAccessFacts = new HashSet<>();

    Set<Fact> methodFacts = new TreeSet<>(Comparator.comparing(f -> f.values()[1].toString()));
    Set<Fact> fieldFacts = new TreeSet<>(Comparator.comparing(f -> f.values()[1].toString()));

    Map<String,Fact> methodRawAccessFacts = new LinkedHashMap<>(); // raw, value is single fact for all int-encoded access flags
    Map<String,Set<Fact>> methodAccessFacts = new LinkedHashMap<>();
    Map<String,Fact> methodSignatureFacts = new LinkedHashMap<>();
    Map<String,Set<Fact>> methodInstructionFacts = new LinkedHashMap<>();

    Map<String,Set<Fact>> fieldAccessFacts = new LinkedHashMap<>();
    Map<String,Fact> fieldRawAccessFacts = new LinkedHashMap<>(); // raw, value is single fact for all int-encoded access flags
    Map<String,Fact> fieldSignatureFacts = new LinkedHashMap<>();

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
