package com.github.jgrade2.jgrade2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class CLITesterDeadlockTest {

    @Test
    @org.junit.jupiter.api.Timeout(10) // 10 seconds timeout
    public void testLargeOutputDoesNotDeadlock() {
        // This command produces a lot of output to stdout.
        // On most systems, 100,000 lines should be enough to fill the buffer.
        ProcessBuilder pb = new ProcessBuilder("seq", "100000");
        CLIResult result = CLITester.executeProcess(pb);
        assertFalse(result.getOutput().isEmpty());
    }
}
