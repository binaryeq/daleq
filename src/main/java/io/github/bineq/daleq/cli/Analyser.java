package io.github.bineq.daleq.cli;

import io.github.bineq.daleq.IOUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Set;

/**
 * Interface for analysers.
 * @author jens dietrich
 */
public interface Analyser {


    /**
     * Whether the analysis is sound, as opposed to soundy.
     * Soundness means that equivalence under-approximates behavioural equivalence.
     * Soundiness means that equivalence under-approximates behavioural equivalence only when no reflection-like programming patterns are used.
     * @return
     */
    SoundnessLevel isSound() ;

    /**
     * An enum of file types an analyser can analyse.
     * @return
     */
    EnumSet<AnalysedResourceType> analysedFiletypes() ;

    // contextDir is the folder where the report is being generated
    // used to create resources that need to be linked
    AnalysisResult analyse (String resource, Path jar1, Path jar2, Path contextDir) throws IOException ;

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
            return new AnalysisResult(AnalysisResultState.SKIP,"resource " + resource + " is missing in package 1");
        }
        else if (!resources2.contains(resource)) {
            return new AnalysisResult(AnalysisResultState.SKIP,"resource " + resource + " is missing in package 2");
        }
        else return null;
    }
}
