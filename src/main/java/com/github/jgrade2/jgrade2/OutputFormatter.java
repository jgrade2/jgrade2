package com.github.jgrade2.jgrade2;

/**
 * A formatter that formats data of a {@link Grader} to produce output.
 */
public interface OutputFormatter {

    /**
     * Get the formatted output of the grader.
     * @param grader The grader observing.
     * @return The formatted output.
     */
    String format(Grader grader);
}
