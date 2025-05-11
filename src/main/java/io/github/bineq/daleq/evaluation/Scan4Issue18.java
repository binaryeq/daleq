package io.github.bineq.daleq.evaluation;

import com.google.common.base.Preconditions;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Scan evaluation results for occurences of org.objectweb.asm.tree.LabelNode@ to find instructions
 * where the current serialisation ddoes not rely on state, causing pairs of bytecodes to be flaged as non-equivalent
 * that may otherwise be equivalent.
 * @author jens dietrich
 */
public class Scan4Issue18 {

    public static final String PROJECTED_IDB_SUMMARY_FILE_NAME = "idb-projected.txt";
    public static final String SEARCH_TERM = "org.objectweb.asm.tree.LabelNode@";

    public static void main (String[] args) throws Exception {

        Preconditions.checkArgument(args.length > 0, "the root folder of the databases generated is required");
        Path experimentDbRootFolder = Path.of(args[0]);
        Preconditions.checkArgument(Files.exists(experimentDbRootFolder), "the experiment db root folder does not exist");
        Preconditions.checkArgument(Files.isDirectory(experimentDbRootFolder), "the experiment db root folder is not a directory");


        List<Path> summaryFiles = Files.walk(experimentDbRootFolder)
            .filter(Files::isRegularFile)
            .filter(f -> f.getFileName().toString().equals(PROJECTED_IDB_SUMMARY_FILE_NAME))
            .collect(Collectors.toList());

        System.out.println(""+summaryFiles.size()+ " found");
        List<Path> affectedSummaries =  new ArrayList<>(summaryFiles.size());
        List<String> affectedLines =  new ArrayList<>(summaryFiles.size());
        Set<String> affectedInstructions = new HashSet<>();
        for (Path summary : summaryFiles) {
            List<String> lines = Files.readAllLines(summary);
            for (String line : lines) {
                if (line.contains(SEARCH_TERM)) {
                    affectedSummaries.add(summary);
                    affectedLines.add(line);
                    String firstToken = line.split("\t")[0];
                    affectedInstructions.add(firstToken);
                }
            }
        }


        System.out.println("summaries found: ");
        for (int i=0;i<affectedSummaries.size();i++) {
            System.out.println(affectedSummaries.get(i));
            System.out.println("\t\tfirst line: " + affectedLines.get(i));
        }

        System.out.println("summaries found (count): " + affectedSummaries.size());
        System.out.println("instructions: " + affectedInstructions.stream().collect(Collectors.joining(",")));
    }
}