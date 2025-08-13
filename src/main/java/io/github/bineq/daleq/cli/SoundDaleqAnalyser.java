package io.github.bineq.daleq.cli;

import io.github.bineq.daleq.Rules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Analyser based on comparing the output of daleq reports.
 * @author jens dietrich
 */
public class SoundDaleqAnalyser extends AbstractDaleqAnalyser {

    @Override
    protected Rules getRules() {
        return Rules.SOUND_RULES;
    }

    private static Logger LOG = LoggerFactory.getLogger(SoundDaleqAnalyser.class);
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public String name() {
        return "daleq (sound)";
    }

    @Override
    public String description() {
        return "daleq-based analyser (sound)";
    }

    @Override
    public int positionHint() {
        return 90;
    }






}
