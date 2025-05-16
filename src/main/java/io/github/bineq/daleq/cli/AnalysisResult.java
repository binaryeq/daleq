package io.github.bineq.daleq.cli;

import java.util.Collections;
import java.util.List;

/**
 * Result of an analysis.
 * @author jens dietrich
 */
public record AnalysisResult(AnalysisResultState state, String message, List<AnalysisResultAttachment> attachments) {
    public AnalysisResult(AnalysisResultState state, String message) {
        this(state, message, Collections.EMPTY_LIST);
    }
}
