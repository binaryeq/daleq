package io.github.bineq.daleq.idb;

import io.github.bineq.daleq.Rules;
import io.github.bineq.daleq.Souffle;
import io.github.bineq.daleq.edb.FactExtractor;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public abstract class AbstractEquivalenceTest {

    public static final Path TMP_DIR_ROOT = Path.of(".tests");

    public abstract Rules getRules() ;

    protected IDB computeIDB (String clazz) throws Exception {

        Path classFile = Path.of(AbstractEquivalenceTest.class.getResource(clazz).getPath());
        assumeTrue(Files.exists(classFile));

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
        Souffle.createIDB(edbDef, getRules(),edbFactDir,idbFactDir,mergedEDBAndRules);

        // load IDB
        IDB idb = IDBReader.read(idbFactDir);
        return idb;

    }

    protected IDB computeAndProjectIDB(String clazz) throws Exception {
        return computeIDB(clazz).project();
    }

    protected void testEquivalence(String class1, String class2) throws Exception {
        IDB idb1 = computeAndProjectIDB(class1);
        String printed1 = IDBPrinter.print(idb1);
        IDB idb2 = computeAndProjectIDB(class2);
        String printed2 = IDBPrinter.print(idb2);
        assertEquals(printed1,printed2);
    }

}
