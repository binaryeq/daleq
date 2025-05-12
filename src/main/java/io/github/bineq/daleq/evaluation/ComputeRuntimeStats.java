package io.github.bineq.daleq.evaluation;

import com.google.common.base.Preconditions;
import com.google.common.math.Stats;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Compute stats about the runtime of building the EDBs/IDBs.
 * Relies on the timestamp files generated in the cache.
 * @author jens dietrich
 */
public class ComputeRuntimeStats {

    public static final String TIMESTAMP_FILENAME = "computation-time-in-ms.txt";

    public static void main (String[] args) throws Exception {

        Preconditions.checkArgument(args.length > 0, "the root folder of the databases generated with timestamp files is required");
        Path experimentDbRootFolder = Path.of(args[0]);
        Preconditions.checkArgument(Files.exists(experimentDbRootFolder), "the experiment db root folder does not exist");
        Preconditions.checkArgument(Files.isDirectory(experimentDbRootFolder), "the experiment db root folder is not a directory");


        List<Path> timestampFiles = Files.walk(experimentDbRootFolder)
            .filter(Files::isRegularFile)
            .filter(f -> f.getFileName().toString().equals(TIMESTAMP_FILENAME))
            .collect(Collectors.toList());

        System.out.println(""+timestampFiles.size()+ " found");
        List<Integer> timestamps =  new ArrayList<>(timestampFiles.size());
        for (Path timestampFile : timestampFiles) {
            List<String> lines = Files.readAllLines(timestampFile);
            int timestamp = Integer.parseInt(lines.get(0));
            if (timestamp>10_000) {
                System.out.println("big timestamp " + timestamp + " for " + timestampFile);
            }
            timestamps.add(timestamp);
        }

        Stats stats = Stats.of(timestamps);

        System.out.println("timestamps analysed: " + stats.count());
        System.out.println("mean: " + stats.mean());
        System.out.println("max: " + stats.max());
        System.out.println("min: " + stats.min());
        System.out.println("stddev: " + stats.populationStandardDeviation());
    }
}