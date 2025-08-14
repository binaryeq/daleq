package io.github.bineq.daleq.cli;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestAnalyserAcquisition {

    @Test
    public void testSourceAnalysersIncluded1 () {
        List<Analyser> analysers = Main.getAnalysers(true, Main.DaleqAnalyserType.sound);
        assertTrue(
            analysers.stream().anyMatch(analyser -> analyser instanceof SameSourceCodeAnalyser)
        );
    }

    @Test
    public void testSourceAnalysersIncluded2 () {
        List<Analyser> analysers = Main.getAnalysers(true, Main.DaleqAnalyserType.soundy);
        assertTrue(
            analysers.stream().anyMatch(analyser -> analyser instanceof SameSourceCodeAnalyser)
        );
    }

    @Test
    public void testSourceAnalysersIncluded3 () {
        List<Analyser> analysers = Main.getAnalysers(true, Main.DaleqAnalyserType.both);
        assertTrue(
            analysers.stream().anyMatch(analyser -> analyser instanceof SameSourceCodeAnalyser)
        );
    }

    @Test
    public void testSourceAnalysersExcluded1 () {
        List<Analyser> analysers = Main.getAnalysers(false, Main.DaleqAnalyserType.sound);
        assertFalse(
            analysers.stream().anyMatch(analyser -> analyser instanceof SameSourceCodeAnalyser)
        );
    }

    @Test
    public void testSourceAnalysersExcluded2 () {
        List<Analyser> analysers = Main.getAnalysers(false, Main.DaleqAnalyserType.soundy);
        assertFalse(
            analysers.stream().anyMatch(analyser -> analyser instanceof SameSourceCodeAnalyser)
        );
    }

    @Test
    public void testSourceAnalysersExcluded3 () {
        List<Analyser> analysers = Main.getAnalysers(false, Main.DaleqAnalyserType.both);
        assertFalse(
            analysers.stream().anyMatch(analyser -> analyser instanceof SameSourceCodeAnalyser)
        );
    }

    @Test
    public void testSoundDaleqAnalysersIncluded1 () {
        List<Analyser> analysers = Main.getAnalysers(true, Main.DaleqAnalyserType.sound);
        assertTrue(
            analysers.stream().anyMatch(analyser -> analyser instanceof SoundDaleqAnalyser)
        );
    }

    @Test
    public void testSoundDaleqAnalysersIncluded2 () {
        List<Analyser> analysers = Main.getAnalysers(false, Main.DaleqAnalyserType.sound);
        assertTrue(
            analysers.stream().anyMatch(analyser -> analyser instanceof SoundDaleqAnalyser)
        );
    }

    @Test
    public void testSoundDaleqAnalysersIncluded3 () {
        List<Analyser> analysers = Main.getAnalysers(true, Main.DaleqAnalyserType.both);
        assertTrue(
            analysers.stream().anyMatch(analyser -> analyser instanceof SoundDaleqAnalyser)
        );
    }

    @Test
    public void testSoundDaleqAnalysersIncluded4 () {
        List<Analyser> analysers = Main.getAnalysers(false, Main.DaleqAnalyserType.both);
        assertTrue(
            analysers.stream().anyMatch(analyser -> analyser instanceof SoundDaleqAnalyser)
        );
    }

    @Test
    public void testSoundDaleqAnalysersExcluded1 () {
        List<Analyser> analysers = Main.getAnalysers(true, Main.DaleqAnalyserType.soundy);
        assertFalse(
            analysers.stream().anyMatch(analyser -> analyser instanceof SoundDaleqAnalyser)
        );
    }

    @Test
    public void testSoundDaleqAnalysersExcluded2 () {
        List<Analyser> analysers = Main.getAnalysers(false, Main.DaleqAnalyserType.soundy);
        assertFalse(
            analysers.stream().anyMatch(analyser -> analyser instanceof SoundDaleqAnalyser)
        );
    }

    @Test
    public void testSoundyDaleqAnalysersIncluded1 () {
        List<Analyser> analysers = Main.getAnalysers(true, Main.DaleqAnalyserType.soundy);
        assertTrue(
            analysers.stream().anyMatch(analyser -> analyser instanceof SoundyDaleqAnalyser)
        );
    }

    @Test
    public void testSoundyDaleqAnalysersIncluded2 () {
        List<Analyser> analysers = Main.getAnalysers(false, Main.DaleqAnalyserType.soundy);
        assertTrue(
            analysers.stream().anyMatch(analyser -> analyser instanceof SoundyDaleqAnalyser)
        );
    }

    @Test
    public void testSoundyDaleqAnalysersIncluded3 () {
        List<Analyser> analysers = Main.getAnalysers(true, Main.DaleqAnalyserType.both);
        assertTrue(
            analysers.stream().anyMatch(analyser -> analyser instanceof SoundyDaleqAnalyser)
        );
    }

    @Test
    public void testSoundyDaleqAnalysersIncluded4 () {
        List<Analyser> analysers = Main.getAnalysers(false, Main.DaleqAnalyserType.both);
        assertTrue(
            analysers.stream().anyMatch(analyser -> analyser instanceof SoundyDaleqAnalyser)
        );
    }

    @Test
    public void testSoundyDaleqAnalysersExcluded1 () {
        List<Analyser> analysers = Main.getAnalysers(true, Main.DaleqAnalyserType.sound);
        assertFalse(
            analysers.stream().anyMatch(analyser -> analyser instanceof SoundyDaleqAnalyser)
        );
    }

    @Test
    public void testSoundyDaleqAnalysersExcluded2 () {
        List<Analyser> analysers = Main.getAnalysers(false, Main.DaleqAnalyserType.sound);
        assertFalse(
            analysers.stream().anyMatch(analyser -> analyser instanceof SoundyDaleqAnalyser)
        );
    }


}
