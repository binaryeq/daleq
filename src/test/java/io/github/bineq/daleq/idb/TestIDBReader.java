package io.github.bineq.daleq.idb;

import io.github.bineq.daleq.Fact;
import io.github.bineq.daleq.edb.EBDAdditionalPredicates;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

// oracles are sourced from src/test/resources/idb/idb1/XMLPropertyListConfiguration.javap
public class TestIDBReader {

    private IDB idb = null;

    // constants for easy reference
    public static final String FIELD1 = "org/apache/commons/configuration2/plist/XMLPropertyListConfiguration::DATA_ENCODING(Ljava/lang/String;";
    public static final String FIELD2 = "org/apache/commons/configuration2/plist/XMLPropertyListConfiguration::INDENT_SIZE(I";
    public static final String FIELD3 = "org/apache/commons/configuration2/plist/XMLPropertyListConfiguration::locator(Lorg/apache/commons/configuration2/io/FileLocator;";

    public static final String METHOD1 = "org/apache/commons/configuration2/plist/XMLPropertyListConfiguration::<init>()V";
    public static final String METHOD2 = "org/apache/commons/configuration2/plist/XMLPropertyListConfiguration::<init>(Lorg/apache/commons/configuration2/HierarchicalConfiguration;)V";
    public static final String METHOD3 = "org/apache/commons/configuration2/plist/XMLPropertyListConfiguration::<init>(Lorg/apache/commons/configuration2/tree/ImmutableNode;)V";
    public static final String METHOD4 = "org/apache/commons/configuration2/plist/XMLPropertyListConfiguration::addPropertyInternal(Ljava/lang/String;Ljava/lang/Object;)V";
    public static final String METHOD5 = "org/apache/commons/configuration2/plist/XMLPropertyListConfiguration::initFileLocator(Lorg/apache/commons/configuration2/io/FileLocator;)V";
    public static final String METHOD6 = "org/apache/commons/configuration2/plist/XMLPropertyListConfiguration::lambda$read$0(Ljava/lang/String;Ljava/lang/String;)Lorg/xml/sax/InputSource;";
    public static final String METHOD7 = "org/apache/commons/configuration2/plist/XMLPropertyListConfiguration::printNode(Ljava/io/PrintWriter;ILorg/apache/commons/configuration2/tree/ImmutableNode;)V";
    public static final String METHOD8 = "org/apache/commons/configuration2/plist/XMLPropertyListConfiguration::printValue(Ljava/io/PrintWriter;ILjava/lang/Object;)V";
    public static final String METHOD9 = "org/apache/commons/configuration2/plist/XMLPropertyListConfiguration::read(Ljava/io/Reader;)V";
    public static final String METHOD10 = "org/apache/commons/configuration2/plist/XMLPropertyListConfiguration::setPropertyDirect(Ljava/lang/String;Ljava/lang/Object;)V";
    public static final String METHOD11 = "org/apache/commons/configuration2/plist/XMLPropertyListConfiguration::setPropertyInternal(Ljava/lang/String;Ljava/lang/Object;)V";
    public static final String METHOD12 = "org/apache/commons/configuration2/plist/XMLPropertyListConfiguration::transformMap(Ljava/util/Map;)Ljava/util/Map;";
    public static final String METHOD13 = "org/apache/commons/configuration2/plist/XMLPropertyListConfiguration::write(Ljava/io/Writer;)V";


    @BeforeEach
    public void setup() throws IOException {
        Path idbFolder = Path.of(TestIDBReader.class.getResource("/idb/idb1").getPath());
        idb = IDBReader.read(idbFolder);
    }

    @Test
    public void testSuperClass() {
        Fact fact = idb.classSuperclassFact;
        assertNotNull(fact);
        assertEquals(IDBPredicates.convertPredicateNameToIDB(EBDAdditionalPredicates.SUPERCLASS.getName()),fact.predicate().getName());
        assertEquals("org/apache/commons/configuration2/BaseHierarchicalConfiguration",fact.values()[2]);
    }

    @Test
    public void testInterfaces() {
        assertNotNull(idb.classInterfaceFacts);
        assertEquals(2,idb.classInterfaceFacts.size());
        idb.classInterfaceFacts.stream().forEach(fact ->
            assertEquals(IDBPredicates.convertPredicateNameToIDB(EBDAdditionalPredicates.INTERFACE.getName()),fact.predicate().getName())
        );
        Set<String> interfaceNames = idb.classInterfaceFacts.stream()
            .map(fact -> fact.values()[2].toString())
            .collect(Collectors.toSet());
        assertTrue(interfaceNames.contains("org/apache/commons/configuration2/FileBasedConfiguration"));
        assertTrue(interfaceNames.contains("org/apache/commons/configuration2/io/FileLocatorAware"));
    }

    @Test
    public void testBytecodeVersion() {
        // from src/test/resources/idb/idb1/XMLPropertyListConfiguration.javap: major version: 52
        Fact fact = idb.bytecodeVersionFact;
        assertNotNull(fact);
        assertEquals(IDBPredicates.convertPredicateNameToIDB(EBDAdditionalPredicates.VERSION.getName()),fact.predicate().getName());
        assertEquals(52,fact.values()[2]);
    }

    @Test
    public void testClassRawAccess() {
        // from src/test/resources/idb/idb1/XMLPropertyListConfiguration.javap: flags: (0x0021) ACC_PUBLIC, ACC_SUPER
        Fact fact = idb.classRawAccessFact;
        assertNotNull(fact);
        assertEquals(IDBPredicates.convertPredicateNameToIDB(EBDAdditionalPredicates.ACCESS.getName()),fact.predicate().getName());
        assertEquals(0x0021,fact.values()[2]);
    }

    @Test
    public void testClassAccessFlags() {
        // from src/test/resources/idb/idb1/XMLPropertyListConfiguration.javap: flags: (0x0021) ACC_PUBLIC, ACC_SUPER
        Set<Fact> facts = idb.classAccessFacts;
        Set<String> predicateNames = facts.stream().map(fact -> fact.predicate().getName()).collect(Collectors.toSet());
        assertTrue(predicateNames.contains(IDBAccessPredicates.ACCESS_PREDICATE_PREFIX+"PUBLIC"));
        assertTrue(predicateNames.contains(IDBAccessPredicates.ACCESS_PREDICATE_PREFIX+"SUPER"));
    }


    @Test
    public void testFields() {
        // from src/test/resources/idb/idb1/XMLPropertyListConfiguration.javap
        Set<Fact> facts = idb.fieldFacts;
        Set<String> fieldIds = facts.stream().map(fact -> fact.values()[1].toString()).collect(Collectors.toSet());
        assertEquals(3,fieldIds.size());
        assertTrue(fieldIds.contains(FIELD1));
        assertTrue(fieldIds.contains(FIELD2));
        assertTrue(fieldIds.contains(FIELD3));
    }

    @Test
    public void testMethods() {
        // from src/test/resources/idb/idb1/XMLPropertyListConfiguration.javap
        Set<Fact> facts = idb.methodFacts;
        Set<String> methodIds = facts.stream().map(fact -> fact.values()[1].toString()).collect(Collectors.toSet());
        assertEquals(13,methodIds.size());
        assertTrue(methodIds.contains(METHOD1));
        assertTrue(methodIds.contains(METHOD2));
        assertTrue(methodIds.contains(METHOD3));
        assertTrue(methodIds.contains(METHOD4));
        assertTrue(methodIds.contains(METHOD5));
        assertTrue(methodIds.contains(METHOD6));
        assertTrue(methodIds.contains(METHOD7));
        assertTrue(methodIds.contains(METHOD8));
        assertTrue(methodIds.contains(METHOD9));
        assertTrue(methodIds.contains(METHOD10));
        assertTrue(methodIds.contains(METHOD11));
        assertTrue(methodIds.contains(METHOD12));
        assertTrue(methodIds.contains(METHOD13));

    }



}
