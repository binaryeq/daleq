package io.github.bineq.daleq.idb;

import io.github.bineq.daleq.Souffle;
import io.github.bineq.daleq.edb.FactExtractor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

public abstract class AbstractIDBTest {

    protected Path rules = null;
    protected Path classFile = null;
    protected Path edbDef = null;
    protected Path edbFactDir = null;
    protected Path idbFactDir = null;
    protected Path mergedEDBAndRules = null;

    @BeforeEach
    public void setup() throws Exception {
        rules = Path.of(Souffle.class.getResource(getRulesPath()).getPath());
        assumeTrue(Files.exists(rules));
        classFile = Path.of(AbstractIDBTest.class.getResource(getPathOfClassUnderTest()).getPath());
        assumeTrue(Files.exists(classFile));

        String className = this.getClass().getSimpleName();
        Path edbRoot = Path.of(".tests/" + className + "/edb");
        edbFactDir = createOrEmpty(edbRoot.resolve( "facts"));
        edbDef = edbRoot.resolve("db.souffle");

        Path idbRoot = Path.of(".tests/" + className + "/idb");
        idbFactDir = createOrEmpty(idbRoot.resolve( "facts"));

        mergedEDBAndRules = Path.of(".tests/" + className + "/mergedEDBAndRules.souffle");

        // checks some preconditions !
        //assumeTrue(Souffle.checkSouffleExe());
        assumeTrue(Souffle.checkSouffleExe(),"Souffle not set");

        // create EDB
        FactExtractor.extractAndExport(this.classFile,this.edbDef,this.edbFactDir,true);

        // apply rules
        Souffle.createIDB(this.edbDef,rules,this.edbFactDir,this.idbFactDir,this.mergedEDBAndRules);

    }


    static Path createOrEmpty (Path dir) throws IOException {
        if (Files.exists(dir)) {
            Files.walk(dir)
                .filter(file -> !Files.isDirectory(file))
                .forEach(file -> {
                    try {
                        Files.delete(file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        }
        else {
            Files.createDirectories(dir);
        }
        assumeTrue(Files.exists(dir));
        return dir;
    }

    @AfterEach
    public void tearDown() throws IOException {
        rules = null;
        classFile = null;
    }

    public abstract String getRulesPath() ;

    public abstract String getPathOfClassUnderTest() ;

    // utilities to build oracles

    // get facts as lists of tokens (tab-seperated)
    protected List<String[]> getEDBFacts(String predicate, Predicate<String[]> filter) throws IOException {
        return getFacts(predicate,this.edbFactDir,".facts",filter);
    }

    protected List<String[]> getEDBFacts(String predicate) throws IOException {
        return getEDBFacts(predicate,f -> true);
    }

    protected List<String[]> getIDBFacts(String predicate,Predicate<String[]> filter) throws IOException {
        return getFacts(predicate,this.idbFactDir,".csv",filter);
    }

    protected List<String[]> getIDBFacts(String predicate) throws IOException {
        return getIDBFacts(predicate,f -> true);
    }

    protected List<String[]> getFacts(String predicate, Path dir, String extension, Predicate<String[]> filter) throws IOException {
        Path factFile = dir.resolve(predicate+extension);
        if (!Files.exists(factFile)) {
            return Collections.EMPTY_LIST;
        }
        return Files.readAllLines(factFile).stream()
            .filter(line -> !line.isEmpty())
            .map(line -> line.split("\t"))
            .filter(filter)
            .collect(Collectors.toUnmodifiableList());
    }

}
