package io.github.bineq.daleq.cli;

/**
 * An attachment that will be displayed in its own web page.
 * @author jens dietrich
 */
public record AnalysisResultAttachment (String name, String link, Kind kind) {
    enum Kind {DIFF, ERROR, UNKNOWN}
}
