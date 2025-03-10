package io.github.bineq.daleq.idb;

import io.github.bineq.daleq.Fact;

import java.util.*;

/**
 * A representation of the IDB itself generated for a given method.
 * This is organised by a set of global facts, and instruction facts organised by method.
 * This is an utility to compare generated IDBs for equivalence.
 * @author jens dietrich
 */
public class IDB {

    Fact classSuperclassFact = null;
    Fact classSignatureFact = null;
    Fact bytecodeVersionFact = null;
    List<Fact> classInterfaceFacts = new ArrayList<>();
    Fact classRawAccessFacts = null; // raw, value is single fact for all int-encoded access flags
    Set<Fact> classAccessFacts = new HashSet<>();

    Set<Fact> methodFacts = new TreeSet<>(Comparator.comparing(f -> f.values()[1].toString()));
    Set<Fact> fieldFacts = new TreeSet<>(Comparator.comparing(f -> f.values()[1].toString()));

    Map<String,Fact> methodRawAccessFacts = new LinkedHashMap<>(); // raw, value is single fact for all int-encoded access flags
    Map<String,Set<Fact>> methodAccessFacts = new LinkedHashMap<>();
    Map<String,Fact> methodSignatureFacts = new LinkedHashMap<>();
    Map<String,List<Fact>> methodInstructionFacts = new LinkedHashMap<>();

    Map<String,Set<Fact>> fieldAccessFacts = new LinkedHashMap<>();
    Map<String,Fact> fieldRawAccessFacts = new LinkedHashMap<>(); // raw, value is single fact for all int-encoded access flags
    Map<String,Fact> fieldSignatureFacts = new LinkedHashMap<>();

}
