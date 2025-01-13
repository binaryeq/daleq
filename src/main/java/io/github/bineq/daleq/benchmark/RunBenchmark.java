package io.github.bineq.daleq.benchmark;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import io.github.bineq.daleq.DBCompare;
import io.github.bineq.daleq.factextraction.FactExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * A benchmark to assess the souffle-based equivalence.
 * @author jens dietrich
 */
public class RunBenchmark {

    private static final Logger LOG = LoggerFactory.getLogger(RunBenchmark.class);
    public static final Path BENCHMARK_ROOT = Path.of(RunBenchmark.class.getResource("/benchmark").getPath());
    public static final String SPEC = "scenario.json";
    public static final Path DB_ROOT = Path.of(".benchmarks");
    public static final String EDB = "EDB";
    public static final String IDB = "IDB";

    public static void main(String[] args) throws IOException {
        Preconditions.checkState(Files.exists(BENCHMARK_ROOT));
        LOG.info("Benchmark root: {}", BENCHMARK_ROOT);

        Files.walk(BENCHMARK_ROOT, 1)
            .filter(Files::isDirectory)
            .filter(dir -> Files.exists(dir.resolve(SPEC)))
            .forEach(dir -> {
                LOG.info("Evaluating benchmark scenario: {}", dir);
                Path spec = dir.resolve(SPEC);
                Preconditions.checkState(Files.exists(spec),"File not found: " + spec);
                try (FileReader reader = new FileReader(spec.toFile())) {
                    Scenario scenario = new Gson().fromJson(reader, Scenario.class);
                    LOG.info("\tname: {}", scenario.name());
                    LOG.info("\tdescription: {}", scenario.description());
                    LOG.info("\tcategory: {}", scenario.category());

                    Path cl1 = dir.resolve(scenario.class1());
                    Preconditions.checkState(Files.exists(cl1),"File not found: " + cl1);
                    Path cl2 = dir.resolve(scenario.class2());
                    Preconditions.checkState(Files.exists(cl2),"File not found: " + cl2);

                    // establish pre-condition - bytecode should be different
                    if (compareBytecodes4Equality(cl1,cl2)) {
                        LOG.warn("Assumption violated: bytecodes are identical");
                    }
                    else {
                        LOG.info("Assumption confirmed: bytecodes are different");
                    }

                    // check equivalence
                    if (compareBytecodes4Equivalence(scenario, cl1, cl2)) {
                        LOG.info("Bytescodes are equivalent");
                    }
                    else {
                        LOG.info("Bytescodes are different");
                    }

                } catch (Exception e) {
                    LOG.error("Failed to read benchmark scenario: {}", spec, e);
                }
            });
    }

    private static boolean compareBytecodes4Equality(Path cl1, Path cl2) throws IOException {
        byte[] bytes1 = Files.readAllBytes(cl1);
        byte[] bytes2 = Files.readAllBytes(cl2);
        return Arrays.equals(bytes1, bytes2);
    }

    private static boolean compareBytecodes4Equivalence(Scenario scenario,Path cl1, Path cl2) throws Exception {

        // create tmp folders for dbs
        Path scenarioDBRoot = DB_ROOT.resolve(scenario.asDirName());
        if (Files.exists(scenarioDBRoot)) {
            delDir(scenarioDBRoot);
        }
        Files.createDirectories(scenarioDBRoot);

        Path factDB1 = scenarioDBRoot.resolve("db1").resolve(EDB);
        Files.createDirectories(factDB1);
        LOG.debug("Fact folder created for class version1: {}",factDB1);
        Path edb1 = scenarioDBRoot.resolve("db1").resolve("db.souffle");
        LOG.debug("EDB for class version1 will be written to: {}",factDB1);

        Path factDB2 = scenarioDBRoot.resolve("db2").resolve(EDB);
        Files.createDirectories(factDB2);
        LOG.debug("DB folder created for class version2: {}",factDB2);
        Path edb2 = scenarioDBRoot.resolve("db2").resolve("db.souffle");
        LOG.debug("EDB for class version2 will be written to: {}",factDB2);


        FactExtractor.extractAndExport(cl1,edb1,factDB1,true);
        FactExtractor.extractAndExport(cl2,edb2,factDB2,true);

        LOG.info("Comparing EDBs");
        boolean edbEqual = DBCompare.compareAll(factDB1,factDB2);
        if (edbEqual) {
            LOG.info("EDBs are equal");
        }
        else {
            LOG.info("EDBs are different");
        }

        // TODO do: compare IDBs
        LOG.info("TODO: Generate and comparie IDBs");
        return false;
    }

    private static void delDir(Path dir) throws IOException {
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(java.io.File::delete);
        }
    }


}
