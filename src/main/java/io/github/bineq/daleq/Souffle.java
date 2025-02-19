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

    /**
     * Create the DB.
     * @param edb - a file containing input predicate defs and facts
     * @param rules - a file containing output predicate defs and rules
     * @param edbDir - a folder containing .fact files referenced in the edb
     * @param idbDir - a folder where inferred facts for output relations will be stored
     */
    public static void createIDB(Path edb, Path rules, Path edbDir, Path idbDir) throws IOException {
        String souffleExe = System.getProperty(SOUFFLE);
        Preconditions.checkNotNull(souffleExe, SOUFFLE + " property not set, must point to the souffle binary, pass to JVM as follows: \"-DSOUFFLE=<dir>\"");
        Path souffle = Path.of(souffleExe);
        Preconditions.checkState(souffle.toFile().exists(), SOUFFLE + " does not exist");

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


        Path merged = Path.of("merged-facts-and-rules.souffle");

        // merge programs
        List lines = new ArrayList();
        lines.addAll(List.of("",COMMENT_SEP,"// EDB from "+edb,COMMENT_SEP,""));
        lines.addAll(Files.readAllLines(edb));
        lines.addAll(List.of("",COMMENT_SEP,"// RULES from "+rules,COMMENT_SEP,""));
        lines.addAll(Files.readAllLines(rules));

        Files.write(merged, lines);
        LOG.info("Merged rules and facts into single souffle program {}", merged.toFile());

        LOG.info("Starting souffle");
        new ProcessBuilder(souffle.toString(),"-F",edbDir.toString(),"-D",idbDir.toString(),merged.toString())
            .inheritIO()
            .start();


    }
}
