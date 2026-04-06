package com.github.jgrade2.jgrade2;

import org.junit.jupiter.api.BeforeEach;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


/**
 * A class to assist in testing the main method of programs that are designed
 * with a command line interface (as many introductory assignments are). The
 * public static methods can be used to assist in getting output, or a class
 * can extend this class, set the invocation, and build unit tests around it
 * to test functionality via JUnit.
 */
public abstract class CLITester {

    /**
     * An interface for the result of a CLI execution. This is returned from
     * {@link #runCommand()} and {@link #runCommand(String)}.
     */
    static class ExecutionResult implements CLIResult {
        private String stdOutOutput;
        private String stdErrOutput;
        private int exitValue;

        /**
         * Create a new ExecutionResult with the given output and exit value.
         * @param stdOutOutput The output from stdout.
         * @param stdErrOutput The output from stderr.
         * @param exitValue The exit value of the program.
         */
        ExecutionResult(String stdOutOutput, String stdErrOutput, int exitValue) {
            this.stdOutOutput = stdOutOutput;
            this.stdErrOutput = stdErrOutput;
            this.exitValue = exitValue;
        }

        /**
         * Get the output from the execution.
         * @param stream The stream to get the output from.
         * @return The output from the execution.
         */
        @Override
        public String getOutput(STREAM stream) {
            return stream == STREAM.STDOUT ? this.stdOutOutput : this.stdErrOutput;
        }

        /**
         * Get the output from stdout.
         * @return The output from stdout.
         */
        @Override
        public String getOutput() {
            return getOutput(STREAM.STDOUT);
        }

        /**
         * Get the output in line form.
         * @param stream The stream to get the output from.
         * @return The output from the execution in line form.
         */
        @Override
        public List<String> getOutputByLine(STREAM stream) {
            return splitByLines(getOutput(stream));
        }

        /**
         * Get the output from stdout in line form.
         * @return The output from stdout in line form.
         */
        @Override
        public int exitValue() {
            return this.exitValue;
        }

        /**
         * Dumps the output into System.out. If there is output from stderr,
         * it will be printed after the stdout output.
         */
        void dump() {
            System.out.println(stdOutOutput);
            if (stdErrOutput.length() > 0) {
                System.out.println("---\nSTD_ERR:");
                System.out.println(stdErrOutput);
            }
        }

        /**
         * Splits a string by lines.
         * @param s The string to split.
         * @return The list of lines.
         */
        private static List<String> splitByLines(String s) {
            return s.isEmpty() ? new ArrayList<>() : Arrays.asList(s.split("[\\r\\n]+"));
        }
    }


    private List<String> command; // Subclassing classes can append to this to add arguments
    private ProcessBuilder builder; // Subclassing classes can edit things from this like redirectErrorStream
    private boolean printOutput; // If true, print the result's output

    /**
     * Get the invocation for the CLI program. {@link #initCommand()} (which
     * is annotated with @Before) calls this method to set the command.
     * Subclasses implement this method to tell it how to setup the command.
     * For example, if a user had main program <code>Hello</code>, they may
     * do the following to implement this method:
     * <code>
     *     return Arrays.asList(new String[] {"java", "-cp", "lib/:.", "Hello"});
     * </code>
     * @return The List of Strings that would invoke the program.
     */
    protected abstract List<String> getInvocation();

    /**
     * Initializes the command for the test (and is annotated with
     * {@link BeforeEach}. Calls the abstract {@link #getInvocation()} and
     * initializes a new {@link ProcessBuilder}.
     */
    @BeforeEach
    public void initCommand() {
        this.command = getInvocation();
        this.builder = new ProcessBuilder();
    }

    /**
     * Get the command that was initialized from {@link #initCommand()}.
     * If adding command line arguments, call this method and append to the
     * command the arguments. For example, in a specific method the user
     * could do <code> getCommand().append("arg1"); </code> before calling
     * {@link #runCommand()}.
     * @return The current command (which is a List of Strings).
     */
    public List<String> getCommand() {
        return this.command;
    }

    /**
     * Add a command line argument to the command to be run.
     * @param s The command line argument to be added
     */
    public void addCommandLineArg(String s) {
        this.command.add(s);
    }

    /**
     * Get the {@link ProcessBuilder} that was initialized from
     * {@link #initCommand()}.
     * If wanting to edit things about the builder for instance
     * {@link ProcessBuilder#redirectErrorStream()} use this method to access
     * the builder.
     * @return The {@link ProcessBuilder} being used.
     */
    public ProcessBuilder getBuilder() {
        return this.builder;
    }

    /**
     * Set whether or not to print the captured output. If true, then when
     * {@link #runCommand()} is called it will automatically print the output
     * to System.out.
     * @param to The boolean value to set whether or not to print output.
     */
    public void setPrintOutput(boolean to) {
        this.printOutput = to;
    }

    /**
     * Run a command with input. See {@link #runCommand()}.
     * @param withInput The input to input to the execution.
     * @return The result of the execution of the program.
     */
    protected CLIResult runCommand(String withInput) {
        this.builder.command(this.command);
        CLIResult output = executeProcess(this.builder, withInput);
        if (printOutput) {
            ((ExecutionResult) output).dump();
        }
        return output;
    }

    /**
     * Run the command as it is currently set for this class without feeding
     * in anything to stdin. If the program takes command line input, see
     * {@link #runCommand(String)}.
     * @return The result of the execution of the program.
     */
    protected CLIResult runCommand() {
        return runCommand(null);
    }

    /**
     * Execute a process provided a {@link ProcessBuilder} that has a command
     * set to invoke the process, and a String for input to feed to the
     * program.
     * @param builder The {@link ProcessBuilder} to use for the command.
     * @param toWriteIn Input for the program that it reads from stdin.
     * @return The {@link CLIResult} containing the output from the run.
     */
    public static CLIResult executeProcess(ProcessBuilder builder,
                                           String toWriteIn) {
        try {
            Process proc = builder.start();

            CompletableFuture<String> stdoutFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return getStringFromStream(proc.getInputStream());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            CompletableFuture<String> stderrFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return getStringFromStream(proc.getErrorStream());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            if (toWriteIn != null) {
                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(proc.getOutputStream()))) {
                    writer.write(toWriteIn);
                    writer.flush();
                }
            }

            int exitValue = proc.waitFor();

            return new ExecutionResult(stdoutFuture.get(), stderrFuture.get(), exitValue);

        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new InternalError(e);
        }
    }

    /**
     * Executes a process with a provided {@link ProcessBuilder} but sends
     * no input to the program. Other than that, same as
     * {@link #executeProcess(ProcessBuilder, String)}.
     * @param builder The {@link ProcessBuilder} to use to run.
     * @return The {@link CLIResult} containing the output from the run.
     */
    public static CLIResult executeProcess(ProcessBuilder builder) {
        return executeProcess(builder, null);
    }

    /**
     * Get a String from an InputStream.
     * @param stream The stream to get the String from.
     * @return The String from the stream.
     * @throws IOException If there is an error reading from the stream.
     */
    private static String getStringFromStream(InputStream stream) throws IOException {
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }
}
