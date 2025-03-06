package io.github.bineq.daleq;

import com.google.common.base.Preconditions;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * The interface to create the IDB using the souffle engine.
 * @author jens dietrich
 */
public class Souffle {

    private static final Logger LOG = LoggerFactory.getLogger(Souffle.class);
    public static final String SOUFFLE = "SOUFFLE"; // a system property that must be set and point to the souffle binary
    public static final String COMMENT_SEP = "// ************************************************";

    public static final Path COMMON_RULES_DIR = Path.of(Souffle.class.getResource("/rules/commons/").getPath());

    /**
     * Create the DB.
     * @param edb - a file containing input predicate defs and facts
     * @param rules - a file containing output predicate defs and rules
     * @param edbDir - a folder containing .fact files referenced in the edb
     * @param idbDir - a folder where inferred facts for output relations will be stored
     * @param mergedEDBAndRules - a file where all rules and files will be merged into
     */
    public static void createIDB(Path edb, Path rules, Path edbDir, Path idbDir,Path mergedEDBAndRules) throws IOException, InterruptedException {
        Path souffle = getAndCheckSouffleExe();

        LOG.info("Using souffle {}", souffle);

        Preconditions.checkState(Files.exists(edb));
        LOG.info("Using edb {}", edb);
        Preconditions.checkState(Files.exists(rules));
        LOG.info("Using rules {}", rules);
        Preconditions.checkState(Files.exists(edbDir));
        Preconditions.checkState(Files.isDirectory(edbDir));
        LOG.info("Using edb direcory {}", edbDir);


        if (!Files.exists(idbDir)) {
            Files.createDirectories(idbDir);
            LOG.info("Created directory {}", idbDir);
        }
        LOG.info("Using idb direcory {}", idbDir);

        // merge programs
        List lines = new ArrayList();
        lines.addAll(List.of("",COMMENT_SEP,"// EDB from "+edb,COMMENT_SEP,""));
        lines.addAll(Files.readAllLines(edb));
        lines.addAll(List.of("",COMMENT_SEP,"// RULES from "+rules,COMMENT_SEP,""));
        lines.addAll(Files.readAllLines(rules));

        // common rule sets
        Files.walk(COMMON_RULES_DIR)
            .filter(Files::isRegularFile)
            .filter(f -> f.getFileName().toString().endsWith(".souffle"))
            .forEach(f -> {
                lines.addAll(List.of("",COMMENT_SEP,"// COMMON RULES from " + f,COMMENT_SEP,""));
                try {
                    List<String> moreLines = Files.readAllLines(f);
                    lines.addAll(moreLines);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });


        Files.write(mergedEDBAndRules, lines);
        LOG.info("Merged rules and facts into single souffle program {}", mergedEDBAndRules.toFile().getAbsolutePath());

        LOG.info("Starting souffle");
        new ProcessBuilder(souffle.toString(),"-F",edbDir.toString(),"-D",idbDir.toString(),mergedEDBAndRules.toString())
            .inheritIO()
            .start()
            .waitFor();
    }


    // public to check earlier, e.g. in test fixture
    public static Path getAndCheckSouffleExe() {
        String souffleExe = System.getProperty(SOUFFLE);
        Preconditions.checkNotNull(souffleExe, SOUFFLE + " property not set, must point to the souffle binary, pass to JVM as follows: \"-DSOUFFLE=<dir>\"");
        Path souffle = Path.of(souffleExe);
        Preconditions.checkState(souffle.toFile().exists(), SOUFFLE + " does not exist");
        return souffle;
    }
}
