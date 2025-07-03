package io.github.bineq.daleq.idb;

import io.github.bineq.daleq.Souffle;
import io.github.bineq.daleq.edb.FactExtractor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class TestEquivalence {

    public static final String RULES = "/rules/advanced.souffle";
    public static final Path TMP_DIR_ROOT = Path.of(".tests");

    private IDB computeIDB (String clazz) throws Exception {

        Path rulesPath = Path.of(Souffle.class.getResource(RULES).getPath());
        assumeTrue(Files.exists(rulesPath));

        Path classFile = Path.of(TestEquivalence.class.getResource(clazz).getPath());
        assumeTrue(Files.exists(classFile));

        String className = this.getClass().getSimpleName();
        Path root = Files.createTempDirectory(TMP_DIR_ROOT,null);
        Path edbRoot = root.resolve( "edb");
        Path edbFactDir = edbRoot.resolve( "facts");
        Path edbDef = edbRoot.resolve("db.souffle");

        Path idbRoot = root.resolve( "idb");
        Path idbFactDir = idbRoot.resolve( "facts");

        Path mergedEDBAndRules = root.resolve("mergedEDBAndRules.souffle");

        // checks some preconditions !
        //assumeTrue(Souffle.checkSouffleExe());
        assumeTrue(Souffle.checkSouffleExe(),"Souffle not set");

        // create EDB
        FactExtractor.extractAndExport(classFile,edbDef,edbFactDir,true);

        // apply rules
        Souffle.createIDB(edbDef,rulesPath,edbFactDir,idbFactDir,mergedEDBAndRules);

        // load IDB
        IDB idb = IDBReader.read(idbFactDir);
        return idb;

    }

    private IDB computeAndProjectIDB(String clazz) throws Exception {
        return computeIDB(clazz).project();
    }

    private void testEquivalence(String class1, String class2) throws Exception {
        IDB idb1 = computeAndProjectIDB(class1);
        String printed1 = IDBPrinter.print(idb1);
        IDB idb2 = computeAndProjectIDB(class2);
        String printed2 = IDBPrinter.print(idb2);
        assertEquals(printed1,printed2);
    }

    @Test
    public void testConstantPoolOrderNormalisation() throws Exception {
        String class1 = "/test-scenarios/scenario1/version1/Headers.class";
        String class2 = "/test-scenarios/scenario1/version2/Headers.class";
        testEquivalence(class1,class2);
    }

    @Disabled // need new oracles, as there are changes related to JEP280 in those classes
    @Test
    public void testRemoveDuplicatedCheckcast() throws Exception {
        String class1 = "/test-scenarios/scenario2/version1/XMLPropertyListConfiguration.class";
        String class2 = "/test-scenarios/scenario2/version2/XMLPropertyListConfiguration.class";
        testEquivalence(class1,class2);
    }

    @Test
    public void testInlinedValuesForEnums() throws Exception {
        String class1 = "/test-scenarios/scenario3/version1/NoopCallback.class";
        String class2 = "/test-scenarios/scenario3/version2/NoopCallback.class";

        // debugging
        String unProjectedDB1 = IDBPrinter.print(computeIDB(class1));
        String unProjectedDB2 = IDBPrinter.print(computeIDB(class2));

        testEquivalence(class1,class2);
    }
}
