package io.github.bineq.daleq.idb;

import com.google.common.base.Preconditions;
import io.github.bineq.daleq.*;
import io.github.bineq.daleq.edb.EBDAdditionalPredicates;
import io.github.bineq.daleq.edb.EDBPredicateRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


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
        Collection<Path> files = Files.walk(idbFolder)
            .filter(file -> !Files.isDirectory(file))
            .filter(file -> Files.isRegularFile(file))
            .filter(file -> file.getFileName().toString().endsWith(EXTENSION))
            .collect(Collectors.toUnmodifiableList());
        for (Path file : files) {
            LOG.info("Reading IDB file {}", file);
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
                String methodId = getMethodId(fact);
                List<Fact> instructionFacts = idb.methodInstructionFacts.computeIfAbsent(methodId,mId -> new ArrayList<>());
                instructionFacts.add(fact);
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
                    String methodId = getMethodId(fact);
                    idb.methodFacts.add(fact);
                }
                else if (isIDBVersionOf(predicate,EBDAdditionalPredicates.FIELD)) {
                    String fieldid = getMethodId(fact);
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
                        idb.classRawAccessFacts = fact;
                    }
                    else if (type==Type.FIELD) {
                        Set<Fact> facts2 = idb.fieldAccessFacts.computeIfAbsent(classOrMethodOrFieldId, mId -> new HashSet<>());
                        facts2.add(fact);
                    }
                    else if (type==Type.METHOD) {
                        Set<Fact> facts2 = idb.methodAccessFacts.computeIfAbsent(classOrMethodOrFieldId,mId -> new HashSet<>());
                        facts2.add(fact);                    }
                }

                else if (IDBAccessPredicates.ALL.contains(predicate)) {
                    String classOrMethodOrFieldId = getClassOrMethodOrFieldId(fact);
                    Type type = classify(classOrMethodOrFieldId);
                    if (type==Type.CLASS) {
                        idb.classAccessFacts.add(fact);
                    }
                    else if (type==Type.FIELD) {

                    }
                    else if (type==Type.METHOD) {

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
        assert tokens.length==predicate.getSlots().length;

        List<Object> values = IntStream.range(0, tokens.length)
            .mapToObj(i -> readValue(predicate.getSlots()[i],tokens[i]))
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
        assert fact.predicate().getSlots()[1].name().equals("methodid");
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
        if (id.contains("::")) {
            if (id.contains("(") && id.contains(")")) {
                return Type.METHOD;
            }
            else {
                return Type.FIELD;
            }
        }
        else {
            return Type.CLASS;
        }
    }

    private static boolean isIDBVersionOf(Predicate idbPredicate,Predicate edbPredicate) {
        assert edbPredicate.isEDBPredicate();
        assert !edbPredicate.isIDBPredicate();
        assert idbPredicate.isIDBPredicate();
        assert !idbPredicate.isEDBPredicate();
        return idbPredicate.getName().equals(IDBPredicates.convertPredicateNameToIDB(edbPredicate.getName()));
    }

}
