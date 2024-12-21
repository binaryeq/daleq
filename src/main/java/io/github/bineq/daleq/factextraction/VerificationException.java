package io.github.bineq.daleq.factextraction;

/**
 * Exception to signal an inconsistent state of a fact.
 * @author jens dietrich
 */
public class VerificationException extends Exception {
    public VerificationException(String message) {
        super(message);
    }
}
