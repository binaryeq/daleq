package io.github.bineq.daleq.ui;

import java.nio.file.Path;

/**
 * Abstract interface for an analyser.
 * @author jens dietrich
 */
public class Analyser {

    AnalysisResult analyse (String resource, Path jar1, Path jar2) {
        return new AnalysisResult(AnalysisResultState.ERROR,"?");
    }
}
