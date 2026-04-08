package io.github.bineq.daleq.cli;

/**
 * Flag indicating whether the equivalence checked by an analyser is sound.
 * @author jens dietrich
 */
public enum SoundnessLevel {

    NA ,        // does not apply, e.g. this would apply to resources like licenses
    SOUND,      // equivalence under-approximates behavioural equivalence.
    SOUNDY, // equivalence under-approximates behavioural equivalence only when no reflection-like programming patterns are used.

}
