package io.github.bineq.daleq.cli;

import io.github.bineq.daleq.IOUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * Interface for analysers.
 * @author jens dietrich
 */
public interface Analyser {

    /**
     * By default, analysers are checking bytecode. If set to false, the analyser will check the jars passed for source code.
     * @return
     */
    default boolean isBytecodeAnalyser() {
        return true;
    }

    // contextDir is the folder where the report is being generated
    // used to create resources that need to be linked
    // the options argument can be used to pass CLI parameters to analysers
    AnalysisResult analyse (String resource, Path jar1, Path jar2, Path contextDir, Map<String,Object> options) throws IOException ;

    String name();

    String description();

    // this will be used to sort analyser output and determine the order of columns in generated reports
    // Use values from 0-100
    // columns with outputs of analysers with low positionHint values go to the left
    int positionHint();

    // init the analyser, i.e. create an analysis folder and copy static resources into it
    default void init(Path outDir) throws IOException {};

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
