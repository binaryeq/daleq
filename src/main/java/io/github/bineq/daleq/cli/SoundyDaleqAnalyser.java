package io.github.bineq.daleq.cli;

import io.github.bineq.daleq.Rules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Analyser based on comparing the output of daleq reports.
 * Sound and soundy rules are used.
 * @author jens dietrich
 */
public class SoundyDaleqAnalyser extends AbstractDaleqAnalyser {

    @Override
    protected Rules getRules() {
        return Rules.DEFAULT_RULES;
    }

    private static Logger LOG = LoggerFactory.getLogger(SoundyDaleqAnalyser.class);
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public String name() {
        return "daleq (soundy)";
    }

    @Override
    public String description() {
        return "daleq-based analyser (soundy)";
    }

    @Override
    public int positionHint() {
        return 91;
    }

    @Override
    public boolean isSound() {
        return false;
    }
}
