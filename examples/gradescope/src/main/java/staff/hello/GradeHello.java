package staff.hello;

import edu.jhu.cs.jgrade.CheckstyleGrader;
import edu.jhu.cs.jgrade.Grade;
import edu.jhu.cs.jgrade.Grader;
import edu.jhu.cs.jgrade.gradedtest.GradedTestResult;

public class GradeHello {
    @Grade
    public void runUnitTests(Grader grader) {
        grader.runJUnitGradedTests(HelloTest.class);
    }

    @Grade
    public void runCheckstyle(Grader grader) {
        // FIXME - Better than knowing running from the classes directory...
        CheckstyleGrader checker = new CheckstyleGrader(10.0, 1.0,
                "../lib/checkstyle-8.12-all.jar", "../src/main/java/student/hello/");
        checker.setConfig("../res/sun_checks.xml");
        GradedTestResult result = checker.runForGradedTestResult();
        grader.addGradedTestResult(result);
    }
}
