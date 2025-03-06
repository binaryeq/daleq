package io.github.bineq.daleq.idb;

import io.github.bineq.daleq.Souffle;
import io.github.bineq.daleq.edb.FactExtractor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestAccess extends AbstractIDBTest {

    @Override
    public String getRulesPath() {
        return "/rules/remove-redundant-checkcast.souffle";
    }

    @Override
    public String getPathOfClassUnderTest() {
        return "/test-scenarios/scenario2/version1/XMLPropertyListConfiguration.class";
    }

    @Test
    public void testClass1() throws Exception {
        // inspect javap output to verify oracle
        String classOrMethodOrFieldId = "org/apache/commons/configuration2/plist/XMLPropertyListConfiguration";
        assertHasAccess(classOrMethodOrFieldId, "IDB_IS_PUBLIC");
        assertDoesNotHaveAccess(classOrMethodOrFieldId,"IDB_IS_PROTECTED");
        assertDoesNotHaveAccess(classOrMethodOrFieldId,"IDB_IS_ABSTRACT");
        assertDoesNotHaveAccess(classOrMethodOrFieldId,"IDB_IS_ENUM");
        assertDoesNotHaveAccess(classOrMethodOrFieldId,"IDB_IS_INTERFACE");
        assertDoesNotHaveAccess(classOrMethodOrFieldId,"IDB_IS_ANNOTATION");
        assertDoesNotHaveAccess(classOrMethodOrFieldId,"IDB_IS_STATIC");
        assertDoesNotHaveAccess(classOrMethodOrFieldId,"IDB_IS_SYNTHETIC");
    }

    @Test
    public void testMethod1() throws Exception {
        // inspect javap output to verify oracle
        String classOrMethodOrFieldId = "org/apache/commons/configuration2/plist/XMLPropertyListConfiguration::addPropertyInternal(Ljava/lang/String;Ljava/lang/Object;)V";
        assertHasAccess(classOrMethodOrFieldId, "IDB_IS_PROTECTED");
        assertDoesNotHaveAccess(classOrMethodOrFieldId,"IDB_IS_PUBLIC");
        assertDoesNotHaveAccess(classOrMethodOrFieldId,"IDB_IS_PRIVATE");
        assertDoesNotHaveAccess(classOrMethodOrFieldId,"IDB_IS_ABSTRACT");
        assertDoesNotHaveAccess(classOrMethodOrFieldId,"IDB_IS_SYNCHRONIZED");
        assertDoesNotHaveAccess(classOrMethodOrFieldId,"IDB_IS_STATIC");
        assertDoesNotHaveAccess(classOrMethodOrFieldId,"IDB_IS_SYNTHETIC");
    }

    @Test
    public void testField1() throws Exception {
        // inspect javap output to verify oracle
        String classOrMethodOrFieldId = "org/apache/commons/configuration2/plist/XMLPropertyListConfiguration::locator(Lorg/apache/commons/configuration2/io/FileLocator;";
        assertHasAccess(classOrMethodOrFieldId, "IDB_IS_PRIVATE");
        assertDoesNotHaveAccess(classOrMethodOrFieldId,"IDB_IS_PUBLIC");
        assertDoesNotHaveAccess(classOrMethodOrFieldId,"IDB_IS_PROTECTED");
        assertDoesNotHaveAccess(classOrMethodOrFieldId,"IDB_IS_ABSTRACT");
        assertDoesNotHaveAccess(classOrMethodOrFieldId,"IDB_IS_VOLATILE");
        assertDoesNotHaveAccess(classOrMethodOrFieldId,"IDB_IS_SYNCHRONIZED");
        assertDoesNotHaveAccess(classOrMethodOrFieldId,"IDB_IS_SYNTHETIC");
        assertDoesNotHaveAccess(classOrMethodOrFieldId,"IDB_IS_STATIC");
    }


    private void assertHasAccess(String classOrMethodOrFieldId,String accessPredicate) throws IOException {
        Predicate<String[]> filter = line -> line[1].equals(classOrMethodOrFieldId);
        assertEquals(1,this.getIDBFacts(accessPredicate,filter).size());
    }

    private void assertDoesNotHaveAccess(String classOrMethodOrFieldId,String accessPredicate) throws IOException {
        Predicate<String[]> filter = line -> line[1].equals(classOrMethodOrFieldId);
        assertEquals(0,this.getIDBFacts(accessPredicate,filter).size());
    }

}
