package io.github.bineq.daleq.cli;

import java.io.IOException;
import java.nio.file.Path;

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
}
