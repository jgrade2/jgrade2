package com.github.jgrade2.jgrade2.gradedtest;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import org.junit.platform.commons.PreconditionViolationException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;



/**
 * A class that extends a JUnit {@link TestExecutionListener} to check for unit test
 * methods annotated with the {@link GradedTest} annotation. It builds up a
 * list of {@link GradedTestResult}s, one for each method with the annotation.
 * Captures anything printed to standard out during the test run and adds it
 * to the output of the {@link GradedTestResult}.
 * <p>
 *     Note: Not thread-safe. In order to set the score of a result it relies
 *     on the assumption that the test that just finished was the one that
 *     also most recently started (for annotated methods).
 * </p>
 */
public class GradedTestListener implements TestExecutionListener {

    private List<GradedTestResult> gradedTestResults;
    private int numFailedGradedTests;
    private ByteArrayOutputStream testOutput;
    private PrintStream originalOutStream;

    /**
     * Constructor for a new listener. Initializes a list of
     * {@link GradedTestResult}s and remembers the original
     * <code>System.out</code> to restore it.
     */
    public GradedTestListener() {
        this.gradedTestResults = new ArrayList<>();
        this.numFailedGradedTests = 0;
        this.testOutput = new ByteArrayOutputStream();
        this.originalOutStream = System.out;
    }

    // <editor-fold "desc="accessors">

    /**
     * Get the count of graded tests for this listener.
     * @return The number of graded tests.
     */
    public int getNumGradedTests() {
        return this.gradedTestResults.size();
    }

    /**
     * Get the list of {@link GradedTestResult}.
     * @return The list of {@link GradedTestResult}.
     */
    public List<GradedTestResult> getGradedTestResults() {
        return this.gradedTestResults;
    }

    /**
     * Get the number of failed graded tests.
     * @return The number of graded tests that failed.
     */
    public int getNumFailedGradedTests() {
        return numFailedGradedTests;
    }

    // </editor-fold>

    // <editor-fold "desc="helpers">

    /**
     * Called when analyzing the test that finished executing. Given a
     * {@link TestIdentifier}, it will try to get the {@link TestSource}.
     * Using this, it will try to return a {@link MethodSource}.
     *
     * @param testIdentifier the identifier of the test that finished
     *
     * @return An optional {@link MethodSource}
     */
    public Optional<MethodSource> getTestMethodSource(TestIdentifier testIdentifier) {

        if (!testIdentifier.isTest()) {
            return Optional.empty();
        }

        Optional<TestSource> oTestSource = testIdentifier.getSource();
        if (!oTestSource.isPresent()) {
            return Optional.empty();
        }


        TestSource abstraTestSource = oTestSource.get();
        try {
            MethodSource source = (MethodSource) abstraTestSource;
            return Optional.of(source);

        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }

    /**
     * Called when analyzing the test that finished executing. Given a
     * {@link MethodSource}, it will see if the method has a {@link GradedTest}
     * annotation and return it.
     *
     * @param ms of the test that finished. Can be returned from {@link #getTestMethodSource(TestIdentifier)}
     *
     * @return An optional {@link GradedTest}
     */
    public Optional<GradedTest> getGradedTestAnnotation(MethodSource ms) {

        try {
            Method testMethod = ms.getJavaMethod();
            GradedTest gradedTestAnnotation = testMethod.getAnnotation(GradedTest.class);
            return Optional.ofNullable(gradedTestAnnotation);

        } catch (PreconditionViolationException pve) { // required for getJavaMethod() to be safe
            return Optional.empty();
        }
    }

    // </editor-fold>

    /**
     * Called when the execution of the {@link TestPlan} has started,
     * <em>before</em> any test has been executed. Currently empty and
     * does not have any use.
     *
     * @param testPlan describes the tree of tests about to be executed
     */
    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
    }

    /**
     * Called when the execution of the {@link TestPlan} has finished,
     * <em>after</em> all tests have been executed.Currently empty and
     * does not have any use.
     *
     * @param testPlan describes the tree of tests that have been executed
     */
    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
    }

    /**
     * Called when a new, dynamic {@link TestIdentifier} has been registered.
     *
     * <p>
     * A <em>dynamic test</em> is a test that is not known a-priori and
     * therefore not contained in the original {@link TestPlan}.
     *
     * <p>
     * Currently, it will log a warning as this is not expected to happen.
     * Tests should be created and registered before the test plan is
     * executed. However, even if it is registered dynamically, there is no
     * problem as the {@link GradedTestResult} will be created when the test
     * is finished.
     *
     *
     * @param testIdentifier the identifier of the newly registered test
     *                       or container
     */
    @Override
    public void dynamicTestRegistered(TestIdentifier testIdentifier) {
        System.out.println("WARNING:: dynamicTestRegistered");
    }

    /**
     * Called when the execution of a leaf or subtree of the {@link TestPlan}
     * has been skipped.
     *
     * <p>
     * The {@link TestIdentifier} may represent a test or a container. In
     * the case of a container, no listener methods will be called for any of
     * its descendants.
     *
     * <p>
     * A skipped test or subtree of tests will never be reported as
     * {@linkplain #executionStarted started} or
     * {@linkplain #executionFinished finished}.
     *
     * <p>
     * This will log out the warning and reason for skipping the test. It will be
     * up to the user to determine if this is a problem or not.
     *
     * @param testIdentifier the identifier of the skipped test or container
     * @param reason         a human-readable message describing why the execution
     *                       has been skipped
     */
    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        System.out.println("WARNING:: executionSkipped - " + reason);

    }

    /**
     * Called when the execution of a leaf or subtree of the {@link TestPlan}
     * is about to be started.
     *
     * <p>
     * The {@link TestIdentifier} may represent a test or a container.
     *
     * <p>
     * This method will only be called if the test or container has not
     * been {@linkplain #executionSkipped skipped}.
     *
     * <p>
     * This method will be called for a container {@link TestIdentifier}
     * <em>before</em> {@linkplain #executionStarted starting} or
     * {@linkplain #executionSkipped skipping} any of its children.
     *
     * <p>
     * For backwards compatability, this function sets the {@link testOutput} for
     * the class.
     *
     *
     * @param testIdentifier the identifier of the started test or container
     */
    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        this.testOutput = new ByteArrayOutputStream();
        System.setOut(new PrintStream(this.testOutput));
    }

    /**
     * Called when the execution of a leaf or subtree of the {@link TestPlan}
     * has finished, regardless of the outcome.
     *
     * <p>
     * The {@link TestIdentifier} may represent a test or a container.
     *
     * <p>
     * This method will only be called if the test or container has not
     * been {@linkplain #executionSkipped skipped}.
     *
     * <p>
     * This method will be called for a container {@code TestIdentifier}
     * <em>after</em> all of its children have been
     * {@linkplain #executionSkipped skipped} or have
     * {@linkplain #executionFinished finished}.
     *
     * <p>
     * The {@link TestExecutionResult} describes the result of the execution
     * for the supplied {@code TestIdentifier}. The result does not include or
     * aggregate the results of its children. For example, a container with a
     * failing test will be reported as SUCCESSFUL even if one or more of its
     * children are reported as FAILED.
     *
     * <p> Compared to the origional jGrade, this is a combination of <code>testFinished</code>
     * <code>testStarted</code>, and <code>testFailure</code>. It will create the
     * new {@link GradedTestResult} and add it to the list of results after the test
     * has finished. It will also set the correct score for the test.
     *
     * @param testIdentifier      the identifier of the finished test or container
     * @param testExecutionResult the (unaggregated) result of the execution for
     *                            the supplied {@code TestIdentifier}
     */
    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {

        // Create the current graded test result
        Optional<MethodSource> ms = getTestMethodSource(testIdentifier);
        if (!ms.isPresent()) {
            return;
        }

        Optional<GradedTest> gradedTestAnnotations = getGradedTestAnnotation(ms.get());
        if (!gradedTestAnnotations.isPresent()) {
            return;
        }

        GradedTest gt = gradedTestAnnotations.get();
        GradedTestResult currentGradedTestResult = new GradedTestResult(
            gt.name(),
            gt.number(),
            gt.points(),
            gt.visibility()
        );

        // Check the status of the test and set the score
        if (testExecutionResult.getStatus() == TestExecutionResult.Status.SUCCESSFUL) { // All passed, full points
            currentGradedTestResult.setScore(gt.points());
        } else { // Failed or aborted, no points
            currentGradedTestResult.setScore(0);
            currentGradedTestResult.addOutput("FAILED/ABORTED:: \n");
            Optional<Throwable> t = testExecutionResult.getThrowable();
            if (t.isPresent()) {
                currentGradedTestResult.addOutput(t.get().toString());
            }
            this.numFailedGradedTests++;
            currentGradedTestResult.setPassed(false);
        }

        // Add any output and add to the list of results for this listener
        currentGradedTestResult.addOutput(this.testOutput.toString());
        this.gradedTestResults.add(currentGradedTestResult);

        System.setOut(originalOutStream);
    }

    /**
     * Called when additional test reporting data has been published for
     * the supplied {@link TestIdentifier}.
     *
     * <p>
     * Can be called at any time during the execution of a test plan.
     *
     * <p> Currently, it will log out a warning and the entry. It will be
     * up to the user to determine if this is a problem or not.
     *
     * @param testIdentifier describes the test or container to which the entry
     *                       pertains
     * @param entry          the published {@code ReportEntry}
     */
    @Override
    public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
        System.out.println("WARNING:: reportingEntryPublished" + entry.toString());
    }
}
