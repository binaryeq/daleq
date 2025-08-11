package io.github.bineq.daleq;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The interface to create the IDB using the souffle engine.
 * @author jens dietrich
 */
public class Souffle {

    private static final Logger LOG = LoggerFactory.getLogger(Souffle.class);
    public static final String SOUFFLE = "SOUFFLE"; // a system property that must be set and point to the souffle binary
    public static final String COMMENT_SEP = "// ************************************************";
    public static final String LINE_SEP = System.getProperty("line.separator");

    private static Map<Resource,List<String>> RULE_CACHE = new HashMap<>();

    private static String SOUFFLE_EXE = null;

    static {
        String souffleProp = System.getProperty(SOUFFLE);
        if (souffleProp != null) {
            LOG.info("Found souffle property: {}", souffleProp);
            if (testSouffle(souffleProp)) {
                SOUFFLE_EXE = souffleProp;
                LOG.info("Using souffle executable: {}", souffleProp);
            }
        }

        if (SOUFFLE_EXE == null) {
            souffleProp = "souffle";
            LOG.info("Try using souffle in path: {}", souffleProp);
            if (testSouffle(souffleProp)) {
                SOUFFLE_EXE = souffleProp;
                LOG.info("Using souffle executable: {}", souffleProp);
            }
        }
    }

    private static boolean testSouffle(String souffleExe) {
        try {
            Process process = new ProcessBuilder(souffleExe, "--version")
                .inheritIO()
                .start();
            process.waitFor();
            LOG.info("Souffle exited with result {}", process.exitValue());
            return process.exitValue() == 0;
        }
        catch (Exception x) {
            LOG.error("Error trying to test souffle executable " + souffleExe,x);
            return false;
        }
    }

    public static String getAndCheckSouffleExe() {
        Preconditions.checkState(SOUFFLE_EXE != null,"Souffle executable not found");
        return SOUFFLE_EXE;
    }

    // public to check earlier, e.g. in test fixture
    public static boolean checkSouffleExe() {
        return SOUFFLE_EXE != null;
    }


    /**
     * Create the DB.
     * @param edb - a file containing input predicate defs and facts
     * @param rules - resources containing definitions of output predicate defs and rules
     * @param edbDir - a folder containing .fact files referenced in the edb
     * @param idbDir - a folder where inferred facts for output relations will be stored
     * @param mergedEDBAndRules - a file where all rules and files will be merged into
     */
    public static void createIDB(Path edb, Rules rules, Path edbDir, Path idbDir,Path mergedEDBAndRules) throws IOException, InterruptedException {
        createIDB(edb,rules.get(),edbDir,idbDir,mergedEDBAndRules);
    }

    private static void createIDB(Path edb, Resource[] rules, Path edbDir, Path idbDir,Path mergedEDBAndRules) throws IOException, InterruptedException {
        String souffle = getAndCheckSouffleExe();

        LOG.info("Using souffle {}", souffle);

        Preconditions.checkState(Files.exists(edb));
        LOG.info("Using edb {}", edb);
        LOG.info("Using rules {}", Stream.of(rules).map(Resource::getFilename).collect(Collectors.joining(LINE_SEP)));
        Preconditions.checkState(Files.exists(edbDir));
        Preconditions.checkState(Files.isDirectory(edbDir));
        LOG.info("Using edb direcory {}", edbDir);

        if (!Files.exists(idbDir)) {
            Files.createDirectories(idbDir);
            LOG.info("Created directory {}", idbDir);
        }
        LOG.info("Using idb direcory {}", idbDir);

        // merge programs
        List<String> lines = new ArrayList();
        lines.addAll(List.of("",COMMENT_SEP,"// EDB from "+edb,COMMENT_SEP,""));
        lines.addAll(Files.readAllLines(edb));

        for (Resource resource : rules) {
            if (RULE_CACHE.containsKey(resource)) {
                LOG.info("Using cached rules from {}", resource.getFilename());
                lines.addAll(RULE_CACHE.get(resource));
            }
            else {
                try {
                    List<String> lines2 = new ArrayList<>();
                    LOG.info("reading rules from {}", resource.getFilename());
                    lines2.addAll(List.of("",COMMENT_SEP,"// COMMON RULES from " + resource,COMMENT_SEP,""));
                    List<String> moreLines = resource.getContentAsString(StandardCharsets.UTF_8).lines().collect(Collectors.toUnmodifiableList());
                    lines2.addAll(moreLines);
                    LOG.info("Caching rules from {}", resource.getFilename());
                    RULE_CACHE.put(resource, lines2);
                    lines.addAll(lines2);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        Files.write(mergedEDBAndRules, lines);
        LOG.info("Merged rules and facts into single souffle program {}", mergedEDBAndRules.toFile().getAbsolutePath());

        LOG.info("Starting souffle");
        Process process = new ProcessBuilder(souffle,"-F",edbDir.toString(),"-D",idbDir.toString(),mergedEDBAndRules.toString())
            .inheritIO()
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



}
