package io.github.bineq.daleq.cli;

import io.github.bineq.daleq.IOUtil;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

/**
 * Interface for analysers.
 * @author jens dietrich
 */
public interface Analyser {

    // contextDir is the folder where the report is being generated
    // used to create resources that need to be linked
    AnalysisResult analyse (String resource, Path jar1, Path jar2, Path contextDir) throws IOException ;

    String name();

    String description();

    default AnalysisResult checkResourceIsPresent(Path jar1, Path jar2, String resource) throws IOException {
        Set<String> resources1 = IOUtil.nonDirEntries(jar1);
        Set<String> resources2 = IOUtil.nonDirEntries(jar2);
        if (!resources1.contains(resource)) {
            return new AnalysisResult(AnalysisResultState.SKIP,"resource is missing in jar1");
        }
        else if (!resources2.contains(resource)) {
            return new AnalysisResult(AnalysisResultState.SKIP,"resource is missing in jar2");
        }
        else return null;
    }
}
