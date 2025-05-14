package io.github.bineq.daleq.cli;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Interface for analysers.
 * @author jens dietrich
 */
public interface Analyser {

    AnalysisResult analyse (String resource, Path jar1, Path jar2) throws IOException ;

    String name();

    String description();
}
