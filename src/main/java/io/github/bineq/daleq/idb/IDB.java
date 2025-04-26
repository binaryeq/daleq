package io.github.bineq.daleq.idb;

import io.github.bineq.daleq.Fact;
import io.github.bineq.daleq.SimpleFact;
import io.github.bineq.daleq.Slot;
import java.util.*;
import java.util.stream.Collectors;

/**
 * An internal representation of the IDB itself generated for a given method.
 * This is organised by a set of global facts, and instruction facts organised by method.
 * This is an utility to compare generated IDBs for equivalence.
 * @author jens dietrich
 */
class IDB {

    // used during normalisation
    public static final String REMOVED_ID_VALUE = "id-removed-by-normalisation";
    public static final int REMOVED_INSTRUCTION_COUNTER_VALUE = -1;

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

    Set<Fact> removedMethodFacts = new TreeSet<>(COMPARE_BY_SLOT_1);
    Set<Fact> removedFieldFacts = new TreeSet<>(COMPARE_BY_SLOT_1);

    Map<String,Fact> methodRawAccessFacts = new HashMap<>(); // raw, value is single fact for all int-encoded access flags
    Map<String,Set<Fact>> methodAccessFacts = new HashMap<>();
    Map<String,Fact> methodSignatureFacts = new HashMap<>();
    Map<String,Collection<Fact>> methodInstructionFacts = new HashMap<>();

    Map<String,Set<Fact>> fieldAccessFacts = new HashMap<>();
    Map<String,Fact> fieldRawAccessFacts = new HashMap<>(); // raw, value is single fact for all int-encoded access flags
    Map<String,Fact> fieldSignatureFacts = new HashMap<>();

    public IDB normalise() {
        IDB idb = new IDB();
        idb.classSuperclassFact = normalise(this.classSuperclassFact);
        idb.classSignatureFact = normalise(this.classSignatureFact);
        idb.bytecodeVersionFact = normalise(this.bytecodeVersionFact);
        idb.classInterfaceFacts = normalise(this.classInterfaceFacts);
        idb.classRawAccessFact = normalise(this.classRawAccessFact);
        idb.classAccessFacts = normalise(this.classAccessFacts);
        idb.methodFacts = normalise(this.methodFacts);
        idb.fieldFacts = normalise(this.fieldFacts);
        idb.removedMethodFacts = Set.of();  // removed facts are only for provenance, ignore
        idb.removedFieldFacts = Set.of();  // removed facts are only for provenance, ignore
        idb.methodRawAccessFacts = normalise(this.methodRawAccessFacts);
        idb.methodAccessFacts = normalise2(this.methodAccessFacts);
        idb.methodSignatureFacts = normalise(this.methodSignatureFacts);
        idb.fieldAccessFacts = normalise2(this.fieldAccessFacts);
        idb.fieldRawAccessFacts = normalise(this.fieldRawAccessFacts);
        idb.fieldSignatureFacts = normalise(this.fieldSignatureFacts);
        idb.methodInstructionFacts = new HashMap<>();
        for (String method:methodInstructionFacts.keySet()) {
            Collection<Fact> facts = new ArrayList<>();
            idb.methodInstructionFacts.put(method,facts);
            for (Fact fact : methodInstructionFacts.get(method)) {
                facts.add(normaliseInstructionFact(fact));
            }
        }

        return idb;
    }

    private static List<Fact> normalise(List<Fact> facts) {
        return facts.stream()
            .map(fact -> normalise(fact))
            .collect(Collectors.toUnmodifiableList());
    }

    private static Map<String,Fact> normalise(Map<String,Fact> facts) {
        Map<String,Fact> map = new HashMap<>();
        facts.keySet().stream()
            .forEach(k -> {
                map.put(k, normalise(facts.get(k)));
            });
        return map;
    }

    private static Map<String,Set<Fact>> normalise2(Map<String,Set<Fact>> facts) {
        Map<String,Set<Fact>> map = new HashMap<>();
        facts.keySet().stream()
            .forEach(k -> {
                map.put(k, normalise(facts.get(k)));
            });
        return map;
    }

    private static Set<Fact> normalise(Set<Fact> facts) {
        // retain the order of facts -- this might be sorted
        Set<Fact> set = new LinkedHashSet<>();
        facts.stream()
            .map(fact -> normalise(fact))
            .forEach(set::add);
        return set;
    }

    private static Fact normalise(Fact fact) {
        Slot[] slots = fact.predicate().getSlots();
        assert slots[0].name().equals("factid");
        Object[] values = new Object[fact.values().length];
        for (int i = 0; i < fact.values().length; i++) {
            if (i==0) {
                values[i] = REMOVED_ID_VALUE;  // set to standard
            }
            else {
                values[i] = fact.values()[i];
            }
        }
        return new SimpleFact(fact.predicate(), values);
    }

    private static Fact normaliseInstructionFact(Fact fact) {

        // checks
        assert fact.predicate().isInstructionPredicate();
        Slot[] slots = fact.predicate().getSlots();
        assert slots[0].name().equals("factid");
        assert slots[2].name().equals("instructioncounter");

        Object[] values = new Object[fact.values().length];
        for (int i = 0; i < fact.values().length; i++) {
            if (i==0) {
                values[i] = REMOVED_ID_VALUE;  // set to standard
            }
            else if (i==2) {
                values[i] = REMOVED_INSTRUCTION_COUNTER_VALUE;
            }
            else {
                values[i] = fact.values()[i];
            }
        }

        return new SimpleFact(fact.predicate(), values);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IDB idb = (IDB) o;
        return Objects.equals(classSuperclassFact, idb.classSuperclassFact) && Objects.equals(classSignatureFact, idb.classSignatureFact) && Objects.equals(bytecodeVersionFact, idb.bytecodeVersionFact) && Objects.equals(classInterfaceFacts, idb.classInterfaceFacts) && Objects.equals(classRawAccessFact, idb.classRawAccessFact) && Objects.equals(classAccessFacts, idb.classAccessFacts) && Objects.equals(methodFacts, idb.methodFacts) && Objects.equals(fieldFacts, idb.fieldFacts) && Objects.equals(removedMethodFacts, idb.removedMethodFacts) && Objects.equals(removedFieldFacts, idb.removedFieldFacts) && Objects.equals(methodRawAccessFacts, idb.methodRawAccessFacts) && Objects.equals(methodAccessFacts, idb.methodAccessFacts) && Objects.equals(methodSignatureFacts, idb.methodSignatureFacts) && Objects.equals(methodInstructionFacts, idb.methodInstructionFacts) && Objects.equals(fieldAccessFacts, idb.fieldAccessFacts) && Objects.equals(fieldRawAccessFacts, idb.fieldRawAccessFacts) && Objects.equals(fieldSignatureFacts, idb.fieldSignatureFacts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classSuperclassFact, classSignatureFact, bytecodeVersionFact, classInterfaceFacts, classRawAccessFact, classAccessFacts, methodFacts, fieldFacts, removedMethodFacts, removedFieldFacts, methodRawAccessFacts, methodAccessFacts, methodSignatureFacts, methodInstructionFacts, fieldAccessFacts, fieldRawAccessFacts, fieldSignatureFacts);
    }
}
