package io.github.bineq.daleq.idb;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRemoveVersion extends AbstractIDBTest {


    @Override
    public String getRulesPath() {
        return "/rules/advanced.souffle";
    }

    @Override
    public String getPathOfClassUnderTest() {
        return "/test-scenarios/scenario2/version1/XMLPropertyListConfiguration.class";
    }

    @Test
    public void testEDB() throws Exception {
        assertEquals(1,this.getEDBFacts("VERSION").size());
        String[] versionFact = this.getEDBFacts("VERSION").get(0);
        assertEquals("52",versionFact[2]);
    }

    @Test
    public void testIDB() throws Exception {
        assertEquals(1,this.getIDBFacts("IDB_VERSION").size());
        String[] versionFact = this.getIDBFacts("IDB_VERSION").get(0);
        assertEquals("0",versionFact[2]);
    }

}
