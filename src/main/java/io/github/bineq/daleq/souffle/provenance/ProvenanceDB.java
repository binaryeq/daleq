package io.github.bineq.daleq.souffle.provenance;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Data structure to index provenance data.
 */
public class ProvenanceDB {

    private Path idbDir = null;  // folder containing *.csv files  (tsv formatted)
    private Path edbDir = null;  // folder containing *.fact files (tsv formatted)
    private Path rules = null;
    private Map<String,FlatFact> edbFactsById = new HashMap<>();
    private Map<String,FlatFact> idbFactsById = new HashMap<>();
    private Map<String,String> rulesById = new HashMap<>();
    private Map<String,DerivationNode> derivationTrees = new HashMap<>();

    /**
     * A simplified representation of a fact.
     * Instead of using a predicate, only the predicate name is used.
     * @param predicateName
     * @param values
     */
    public record FlatFact(String predicateName,String[] values){};

    public ProvenanceDB(Path edbDir, Path idbDir, Path rules) throws IOException {
        this.idbDir = idbDir;
        this.edbDir = edbDir;
        this.rules = rules;
        init();
    }

    private void init() throws IOException {
        for (Path f:getTSVFiles(edbDir)) {
            String predicateName = f.getFileName().toString();
            predicateName = predicateName.substring(0, predicateName.lastIndexOf('.')); // remove extension
            for (String line:Files.readAllLines(f)) {
                String[] parts = line.split("\t");
                edbFactsById.put(parts[0],new FlatFact(predicateName,parts));
            }
        }
        for (Path f:getTSVFiles(idbDir)) {
            String predicateName = f.getFileName().toString();
            predicateName = predicateName.substring(0, predicateName.lastIndexOf('.')); // remove extension
            for (String line:Files.readAllLines(f)) {
                String[] parts = line.split("\t");
                idbFactsById.put(parts[0],new FlatFact(predicateName,parts));
            }
        }

        List<String> lines = Files.readAllLines(rules);
        lines = getRuleHeads(lines);
        for (String line:lines) {
            String id = parseSouffleRule(line);
            assert id != null;
            rulesById.put(id,line);
        }
    }

    public FlatFact getEdbFact(String id) {
        return edbFactsById.get(id);
    }

    public FlatFact getIdbFact(String id) {
        return idbFactsById.get(id);
    }

    public String getRule(String id) {
        return rulesById.get(id);
    }

    List<Path> getTSVFiles(Path dir) throws IOException {
        return Files.walk(dir)
            .filter(Files::isRegularFile)
            .filter(ProvenanceDB::isTSV)
            .collect(Collectors.toUnmodifiableList());
    }

    private static boolean isTSV(Path file) {
            String fileName = file.getFileName().toString();
            return fileName.endsWith(".tsv") || fileName.endsWith(".csv") || fileName.endsWith(".facts");
    }

    /**
     * Parse a line as a rule. Extract its id.
     * @param line a rule definition
     * @return - the rule id. Return null if this line does not represent a rule (like an empty line or comment)
     */
    static String parseSouffleRule(String line) {  // testable
        line = line.trim();


        if (!isPartOfSouffleRule(line)) {
            return null;
        }

        // parse rule
        // the rule id is the first string literal in double quotes
        // rules can also be facts
        // example1: REMOVED_INSTRUCTION(cat("R_REMOVE_PR5165","[",factid1,",",factid2,"]"),methodid,instructioncounter) :- ..
        // example2: IS_ROOT_METHOD("R_IS_ROOT_METHOD_GETCLASS","getClass", "()Ljava/lang/Class;").

        int idx = line.indexOf("\"");
        assert idx != -1;
        line = line.substring(idx+1);
        idx = line.indexOf("\"");
        assert idx != -1;

        return line.substring(0,idx).trim();

    }

    private static List<String> getRuleHeads(List<String> lines) {
        List<String> heads = new ArrayList<>();
        boolean ruleIsActive = false;
        for (String line:lines) {
            if (isPartOfSouffleRule(line)) {
                if (!ruleIsActive) {
                    heads.add(line);
                }
                ruleIsActive = !isSouffleRuleEnd(line);
            }
        }
        return heads;
    }


    private static boolean isSouffleRuleStart(String line) {
        return isPartOfSouffleRule(line) && line.contains(":-");
    }

    private static boolean isSouffleRuleEnd(String line) {
        return isPartOfSouffleRule(line) && line.trim().endsWith(".");
    }

    private static boolean isPartOfSouffleRule(String line) {
        line = line.trim();
        if (line.startsWith(".")) {
            return false; // .decl , .ouput etc
        }
        else if (line.equals("")) {
            return false; // empty line
        }
        else if (line.startsWith("//")) {
            return false; // empty line
        }
        return true;
    }

}
