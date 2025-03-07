package io.github.bineq.daleq.idb;

import io.github.bineq.daleq.Fact;

import java.util.*;

/**
 * A representation of the IDB itself generated for a given method.
 * This is organised by a set of global facts, and instruction facts organised by method.
 * @author jens dietrich
 */
public class IDB {

    Fact classSuperclassFact = null;
    Fact classSignatureFact = null;
    Fact bytecodeVersionFact = null;
    List<Fact> classInterfaceFacts = new ArrayList<>();
    List<Fact> classRawAccessFacts = new ArrayList<>(); // raw, value is single fact for all int-encoded access flags
    List<Set<Fact>> classAccessFacts = new ArrayList<>(); // raw, value is single fact for all int-encoded access flags
    
    Map<String,Fact> methodRawAccessFacts = new LinkedHashMap<>(); // raw, value is single fact for all int-encoded access flags
    Map<String,Set<Fact>> methodAccessFacts = new LinkedHashMap<>();
    Map<String,Fact> methodSignatureFacts = new LinkedHashMap<>();
    Map<String,List<Fact>> methodInstructionFacts = new LinkedHashMap<>();

    Map<String,Set<Fact>> fieldAccessFacts = new LinkedHashMap<>();
    Map<String,Fact> fieldRawAccessFacts = new LinkedHashMap<>(); // raw, value is single fact for all int-encoded access flags
    Map<String,Fact> fieldSignatureFacts = new LinkedHashMap<>();

}
