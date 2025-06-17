package io.github.bineq.daleq;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The interface to create the IDB using the souffle engine.
 * @author jens dietrich
 */
public class Souffle {

    private static final Logger LOG = LoggerFactory.getLogger(Souffle.class);
    public static final String SOUFFLE = "SOUFFLE"; // a system property that must be set and point to the souffle binary
    public static final String COMMENT_SEP = "// ************************************************";

    public static final Path COMMON_RULES_DIR = Path.of(Souffle.class.getResource("/rules/commons/").getPath());
    public static final String LINE_SEP = System.getProperty("line.separator");


    /**
     * Create the DB.
     * @param edb - a file containing input predicate defs and facts
     * @param rules - a file containing output predicate defs and rules
     * @param edbDir - a folder containing .fact files referenced in the edb
     * @param idbDir - a folder where inferred facts for output relations will be stored
     * @param mergedEDBAndRules - a file where all rules and files will be merged into
     */
    public static void createIDB(Path edb, Path rules, Path edbDir, Path idbDir,Path mergedEDBAndRules) throws IOException, InterruptedException {
        Preconditions.checkState(Files.exists(rules));
        List<String> ruleDefs = Files.readAllLines(rules);
        createIDB(edb,ruleDefs,edbDir,idbDir,mergedEDBAndRules,rules.toString());
    }

    /**
     * Create the DB.
     * @param edb - a file containing input predicate defs and facts
     * @param rules - a URL containing output predicate defs and rules
     * @param edbDir - a folder containing .fact files referenced in the edb
     * @param idbDir - a folder where inferred facts for output relations will be stored
     * @param mergedEDBAndRules - a file where all rules and files will be merged into
     */
    public static void createIDB(Path edb, URL rules, Path edbDir, Path idbDir, Path mergedEDBAndRules) throws IOException, InterruptedException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(rules.openStream()));) {
            List<String> ruleDefs = reader.lines().collect(Collectors.toUnmodifiableList());
            createIDB(edb,ruleDefs,edbDir,idbDir,mergedEDBAndRules,rules.toString());
        } catch (IOException x) {
            LOG.error("Failed to load rules from {}", rules);
            throw x;
        }

    }

    private static void createIDB(Path edb, List<String> rules, Path edbDir, Path idbDir,Path mergedEDBAndRules,String rulesLoc) throws IOException, InterruptedException {
        Path souffle = getAndCheckSouffleExe();

        LOG.info("Using souffle {}", souffle);

        Preconditions.checkState(Files.exists(edb));
        LOG.info("Using edb {}", edb);
        LOG.info("Using rules {}", rulesLoc);
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
        lines.addAll(List.of("",COMMENT_SEP,"// RULES from "+rulesLoc,COMMENT_SEP,""));
        lines.addAll(rules);

        // common rule sets
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath*:rules/commons/**/*souffle");
        for (Resource resource : resources) {
            lines.addAll(List.of("",COMMENT_SEP,"// COMMON RULES from " + resource,COMMENT_SEP,""));
            try {
                List<String> moreLines = resource.getContentAsString(StandardCharsets.UTF_8).lines().collect(Collectors.toUnmodifiableList());
                lines.addAll(moreLines);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };


        Files.write(mergedEDBAndRules, lines);
        LOG.info("Merged rules and facts into single souffle program {}", mergedEDBAndRules.toFile().getAbsolutePath());

        LOG.info("Starting souffle");
        Process process = new ProcessBuilder(souffle.toString(),"-F",edbDir.toString(),"-D",idbDir.toString(),mergedEDBAndRules.toString())
            .start();

//        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//        StringJoiner sj = new StringJoiner(LINE_SEP);
//        reader.lines().iterator().forEachRemaining(sj::add);
//        String text = sj.toString();

        int result = process.waitFor();
        if (result != 0) {
            LOG.error("Souffle exited with result " + result);
            throw new IOException("Souffle exited with " + result);
        }
    }


    // public to check earlier, e.g. in test fixture
    public static Path getAndCheckSouffleExe() {
        String souffleExe = System.getProperty(SOUFFLE);
        Preconditions.checkNotNull(souffleExe, SOUFFLE + " property not set, must point to the souffle binary, pass to JVM as follows: \"-DSOUFFLE=<dir>\"");
        Path souffle = Path.of(souffleExe);
        Preconditions.checkState(souffle.toFile().exists(), SOUFFLE + " does not exist");
        return souffle;
    }

    // public to check earlier, e.g. in test fixture
    public static boolean checkSouffleExe() {
        String souffleExe = System.getProperty(SOUFFLE);
        if (souffleExe == null) {
            LOG.warn(SOUFFLE + " property not set, must point to the souffle binary, pass to JVM as follows: \"-DSOUFFLE=<dir>\"");
            return false;
        }
        Path souffle = Path.of(souffleExe);
        if (!Files.exists(souffle)) {
            LOG.warn(SOUFFLE + " does not exist");
            return false;
        }
        return true;
    }
}
