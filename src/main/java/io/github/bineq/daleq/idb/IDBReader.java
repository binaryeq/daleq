package io.github.bineq.daleq.idb;

import com.google.common.base.Preconditions;
import io.github.bineq.daleq.*;
import io.github.bineq.daleq.edb.EBDAdditionalPredicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.github.bineq.daleq.idb.IDB.COMPARE_INSTRUCTION_FACTS_BY_POSITION;

/**
 * Utility to read all facts from an IDB folder.
 * @author jens dietrich
 */
public class IDBReader {

    public static final Logger LOG = LoggerFactory.getLogger(IDBReader.class);
    public static final String EXTENSION = ".csv";

    public static IDB read (Path idbFolder) throws IOException {
        Preconditions.checkArgument(Files.exists(idbFolder));
        Preconditions.checkArgument(Files.isDirectory(idbFolder));
        IDB idb = new IDB();
        boolean DEBUG = LOG.isDebugEnabled();
        Collection<Path> files = Files.walk(idbFolder)
            .filter(file -> !Files.isDirectory(file))
            .filter(file -> Files.isRegularFile(file))
            .filter(file -> file.getFileName().toString().endsWith(EXTENSION))
            .collect(Collectors.toUnmodifiableList());
        for (Path file : files) {
            if (DEBUG) {
                LOG.debug("Reading IDB file {}", file);
            }
            readFacts(file,idb);
        }
        return idb;
    }

    private static void readFacts(Path file,IDB idb) throws IOException {
        String predicateName = file.getFileName().toString().replace(EXTENSION, "");
        List<Fact> facts = Files.readAllLines(file).stream()
            .map(line -> line.split("\t"))
            .map(tokens -> parse(predicateName,tokens))
            .collect(Collectors.toUnmodifiableList());

        for (Fact fact : facts) {
            Predicate predicate = fact.predicate();
            assert IDBPredicateRegistry.ALL.values().contains(predicate);
            if (predicate.isInstructionPredicate()) {
                if (predicate==IDBRemovalPredicates.REMOVED_INSTRUCTION) {
                    String methodId = getMethodId(fact);
                    Collection<Fact> instructionFacts = idb.removedInstructionFacts.computeIfAbsent(methodId,mId -> new TreeSet<>(COMPARE_INSTRUCTION_FACTS_BY_POSITION));
                    instructionFacts.add(fact);
                }
                else if (predicate==IDBRemovalPredicates.MOVED_INSTRUCTION) {
                    String methodId1 = (String)fact.values()[1]; // from
                    String methodId2 = (String)fact.values()[3]; // to
                    Collection<Fact> instructionFacts = idb.removedInstructionFacts.computeIfAbsent(methodId1,mId -> new TreeSet<>(COMPARE_INSTRUCTION_FACTS_BY_POSITION));
                    instructionFacts.add(fact);
                    instructionFacts = idb.removedInstructionFacts.computeIfAbsent(methodId2,mId -> new TreeSet<>(COMPARE_INSTRUCTION_FACTS_BY_POSITION));
                    instructionFacts.add(fact);
                }
                else {
                    String methodId = getMethodId(fact);
                    Collection<Fact> instructionFacts = idb.methodInstructionFacts.computeIfAbsent(methodId, mId -> new TreeSet<>(COMPARE_INSTRUCTION_FACTS_BY_POSITION));
                    instructionFacts.add(fact);
                }
            }
            else {
                if (isIDBVersionOf(predicate,EBDAdditionalPredicates.SUPERCLASS)) {
                    idb.classSuperclassFact = fact;
                }
                else if (isIDBVersionOf(predicate,EBDAdditionalPredicates.INTERFACE)) {
                    idb.classInterfaceFacts.add(fact);
                }
                else if (isIDBVersionOf(predicate,EBDAdditionalPredicates.CLASS_SIGNATURE)) {
                    idb.classSignatureFact = fact;
                }
                else if (isIDBVersionOf(predicate,EBDAdditionalPredicates.VERSION)) {
                    idb.bytecodeVersionFact = fact;
                }
                else if (isIDBVersionOf(predicate,EBDAdditionalPredicates.METHOD)) {
                    idb.methodFacts.add(fact);
                }
                else if (predicate==IDBRemovalPredicates.REMOVED_METHOD) {
                    idb.removedMethodFacts.add(fact);
                }
                else if (predicate==IDBRemovalPredicates.REMOVED_FIELD) {
                    idb.removedFieldFacts.add(fact);
                }
                else if (isIDBVersionOf(predicate,EBDAdditionalPredicates.FIELD)) {
                    idb.fieldFacts.add(fact);
                }
                else if (isIDBVersionOf(predicate,EBDAdditionalPredicates.METHOD_SIGNATURE)) {
                    String methodId = getMethodId(fact);
                    idb.methodSignatureFacts.put(methodId,fact);
                }
                else if (isIDBVersionOf(predicate,EBDAdditionalPredicates.FIELD_SIGNATURE)) {
                    String fieldId = getFieldId(fact);
                    idb.fieldSignatureFacts.put(fieldId,fact);
                }

                else if (isIDBVersionOf(predicate,EBDAdditionalPredicates.ACCESS)) {
                    String classOrMethodOrFieldId = getClassOrMethodOrFieldId(fact);
                    Type type = classify(classOrMethodOrFieldId);
                    if (type==Type.CLASS) {
                        idb.classRawAccessFact = fact;
                    }
                    else if (type==Type.FIELD) {
                        idb.fieldRawAccessFacts.put(classOrMethodOrFieldId,fact);
                    }
                    else if (type==Type.METHOD) {
                        idb.methodRawAccessFacts.put(classOrMethodOrFieldId,fact);
                    }
                }

                else if (IDBAccessPredicates.ALL.contains(predicate)) {
                    String classOrMethodOrFieldId = getClassOrMethodOrFieldId(fact);
                    Type type = classify(classOrMethodOrFieldId);
                    if (type==Type.CLASS) {
                        idb.classAccessFacts.add(fact);
                    }
                    else if (type==Type.FIELD) {
                        Set<Fact> facts2 = idb.fieldAccessFacts.computeIfAbsent(classOrMethodOrFieldId, mId -> new TreeSet<>(IDB.COMPARE_BY_PREDICATE_NAME));
                        facts2.add(fact);
                    }
                    else if (type==Type.METHOD) {
                        Set<Fact> facts2 = idb.methodAccessFacts.computeIfAbsent(classOrMethodOrFieldId, mId -> new TreeSet<>(IDB.COMPARE_BY_PREDICATE_NAME));
                        facts2.add(fact);
                    }
                }

                else {
                    LOG.warn("TODO: classify fact for predicate {}",predicateName);
                }
            }
        }
    }

    private static Fact parse(String predicateName,String[] tokens) {

        Predicate predicate = IDBPredicateRegistry.ALL.get(predicateName);

        // we should allow predicates introduced in rules sets not known at this stage, perhaps just
        // creating them "on-the-fly" here. Downside: make all slots symbol typed
        Preconditions.checkState(predicate != null,"unknown predicate " + predicateName);
        tokens = Predicate.pad(predicate,tokens);
        assert tokens.length==predicate.getSlots().length:"token length was " + tokens.length + " but predicate " + predicateName + " has " + predicate.getSlots().length + " slots";

        String[] tokens3 = tokens; // alias for use in lambda
        List<Object> values = IntStream.range(0, tokens.length)
            .mapToObj(i -> readValue(predicate.getSlots()[i],tokens3[i]))
            .collect(Collectors.toList());

        return new SimpleFact(predicate,values.toArray(new Object[values.size()]));
    }

    private static Object readValue(Slot slot, String token) {
        if (slot.type()== SlotType.SYMBOL) {
            return token;
        }
        else if (slot.type()== SlotType.NUMBER) {
            if (slot.jtype().equals(Integer.TYPE.getName())) {
                return Integer.parseInt(token);
            }
            else if (slot.jtype().equals(Boolean.TYPE.getName())) {
                return Boolean.parseBoolean(token);
            }
        }

        throw new UnsupportedOperationException("conversion is not yet supported");
    }

    private static String getMethodId(Fact fact) {
        assert fact.predicate().getSlots().length > 1;
        // assert fact.predicate().getSlots()[1].name().equals("methodid");
        assert fact.predicate().getSlots()[1].type()==SlotType.SYMBOL;
        return (String)fact.values()[1];
    }

    private static String getFieldId(Fact fact) {
        assert fact.predicate().getSlots().length > 1;
        assert fact.predicate().getSlots()[1].name().equals("fieldid");
        assert fact.predicate().getSlots()[1].type()==SlotType.SYMBOL;
        return (String)fact.values()[1];
    }

    private static String getClassOrMethodOrFieldId(Fact fact) {
        assert fact.predicate().getSlots().length > 1;
        assert fact.predicate().getSlots()[1].type()==SlotType.SYMBOL;
        return (String)fact.values()[1];
    }

    private enum Type {CLASS,METHOD,FIELD};

    private static Type classify(String id) {
        Type type = null;
        if (id.contains("::")) {
            if (id.contains("(") && id.contains(")")) {
                type = Type.METHOD;
            }
            else {
                type = Type.FIELD;
            }
        }
        else {
            type = Type.CLASS;
        }
        assert type != null;
        return type;
    }

    private static boolean isIDBVersionOf(Predicate idbPredicate,Predicate edbPredicate) {
        assert edbPredicate.isEDBPredicate();
        assert !edbPredicate.isIDBPredicate();
        assert idbPredicate.isIDBPredicate();
        assert !idbPredicate.isEDBPredicate();
        return idbPredicate.getName().equals(IDBPredicates.convertPredicateNameToIDB(edbPredicate.getName()));
    }

}
