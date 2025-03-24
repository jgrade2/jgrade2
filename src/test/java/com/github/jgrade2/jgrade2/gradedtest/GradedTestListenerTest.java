package com.github.jgrade2.jgrade2.gradedtest;

import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import java.util.List;


public class GradedTestListenerTest {

    private static final String EXAMPLE_STRING = "t3ST StR1nG";
    private static final String EXAMPLE_MESSAGE = "FAILURE";
    private static final String EXAMPLE_NAME = "Graded Test";
    private static final String EXAMPLE_NUMBER = "Example Number";
    private static final double EXAMPLE_POINTS = 2.0;

    private GradedTestListener listener;

    @BeforeEach
    public void initUnit() {
        this.listener = new GradedTestListener();
    }

    // Method established in Grader.runJUnitGradedTests
    private void runWithListenerForExample(Class<?> exampleUnitTests, GradedTestListener listener) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(exampleUnitTests))
                .build();

        LauncherSession session = LauncherFactory.openSession();
        Launcher launcher = session.getLauncher();
        TestPlan testPlan = launcher.discover(request);
        launcher.execute(testPlan, listener);
    }

    private GradedTestResult getOnlyGradedTestResult(Class<?> exampleUnitTests, GradedTestListener listener) {
        runWithListenerForExample(exampleUnitTests, listener);
        List<GradedTestResult> results = this.listener.getGradedTestResults();
        assertEquals(1, results.size());
        return results.get(0);
    }

    @Test
    public void newListenerNoTests() {
        assertEquals(0, listener.getNumGradedTests());
    }

    @Test
    public void basicCountGradedTests() {
        runWithListenerForExample(BasicGradedTests.class, this.listener);
        assertEquals(2, listener.getNumGradedTests());
    }

    @Test
    public void onlyCountsGradedTests() {
        runWithListenerForExample(NotAllTestsGraded.class, this.listener);
        assertEquals(1, listener.getNumGradedTests());
    }

    @Test
    public void addsDefaultGradedTestResult() {
        GradedTestResult result = getOnlyGradedTestResult(SingleDefaultGradedTest.class, this.listener);
        assertEquals(GradedTestResult.DEFAULT_NAME, result.getName());
        assertEquals(GradedTestResult.DEFAULT_NUMBER, result.getNumber());
        assertEquals(GradedTestResult.DEFAULT_POINTS, result.getPoints(), 0.0);
        assertEquals(GradedTestResult.DEFAULT_VISIBILITY, result.getVisibility());
    }

    @Test
    public void addsCustomGradedTestResult() {
        GradedTestResult result = getOnlyGradedTestResult(SingleCustomGradedTest.class, this.listener);
        assertEquals(EXAMPLE_NAME, result.getName());
        assertEquals(EXAMPLE_NUMBER, result.getNumber());
        assertEquals(EXAMPLE_POINTS, result.getPoints(), 0.0);
        assertEquals(GradedTestResult.HIDDEN, result.getVisibility());
    }

    @Test
    public void correctlyCountsFailedGradedTest() {
        GradedTestResult result = getOnlyGradedTestResult(SingleFailedGradedTest.class, this.listener);
        assertEquals(1, listener.getNumFailedGradedTests());
        assertEquals(0.0, result.getScore(), 0.0);
    }

    @Test
    public void setsScoreToZeroForFailedTest() {
        GradedTestResult result = getOnlyGradedTestResult(SingleFailedGradedTest.class, this.listener);
        assertEquals(0.0, result.getScore(), 0.0);
    }

    @Test
    public void setsScoreToMaxForPassedTest() {
        GradedTestResult result = getOnlyGradedTestResult(SingleCustomGradedTest.class, this.listener);
        assertEquals(EXAMPLE_POINTS, result.getScore(), 0.0);
    }

    @Test
    public void onlyCountsFailedIfGradedTest() {
        runWithListenerForExample(MultipleFailOneGradedTest.class, this.listener);
        assertEquals(1, listener.getNumFailedGradedTests());
    }

    @Test
    public void capturesOutputFromTest() {
        GradedTestResult result = getOnlyGradedTestResult(TestWithOutput.class, this.listener);
        assertEquals(EXAMPLE_STRING, result.getOutput());
    }

    @Test
    public void outputHasFailMessage() {
        GradedTestResult result = getOnlyGradedTestResult(SingleFailWithMessageGradedTest.class, this.listener);
        System.out.println(EXAMPLE_MESSAGE);
        System.out.println(result.getOutput());
        assertTrue(result.getOutput().contains(EXAMPLE_MESSAGE));
    }

    @Test
    public void addsRegularFailureToStringIfNoMessage() {
        GradedTestResult result = getOnlyGradedTestResult(SingleFailedGradedTest.class, this.listener);
        assertNotEquals("", result.getOutput());
    }

    /* * HELPER EXAMPLE "UNIT TEST" CLASSES * */

    public static class BasicGradedTests {
        @Test
        @GradedTest(name=EXAMPLE_NAME, points=2.0)
        public void trueIsTrue() { assertTrue(true); }

        @Test
        @GradedTest(name=EXAMPLE_NAME, points=2.0)
        public void falseIsFalse() { assertFalse(false); }
    }

    public static class NotAllTestsGraded {
        @Test
        @GradedTest(name=EXAMPLE_NAME, points=2.0)
        public void trueIsTrue() { assertTrue(true); }

        @Test
        public void falseIsFalse() { assertFalse(false); }
    }

    public static class SingleDefaultGradedTest {
        @Test
        @GradedTest public void gradedTest() { assertTrue(true); }
    }

    public static class SingleCustomGradedTest {
        @Test
        @GradedTest(
                name=EXAMPLE_NAME,
                number=EXAMPLE_NUMBER,
                points=EXAMPLE_POINTS,
                visibility= GradedTestResult.HIDDEN)
        public void gradedTest() { assertTrue(true); }
    }

    public static class SingleFailedGradedTest {
        @Test
        @GradedTest public void gradedTest() { fail(); }
    }

    public static class SingleFailWithMessageGradedTest {
        @Test
        @GradedTest public void gradedTest() { fail(EXAMPLE_MESSAGE); }
    }

    public static class MultipleFailOneGradedTest {
        @Test
        public void nonGradedTest() { fail(); }
        @Test
        @GradedTest public void gradedTest() { fail(); }
    }

    public static class TestWithOutput {
        @Test
        @GradedTest public void gradedTest() { System.out.print(EXAMPLE_STRING); }
    }
}
