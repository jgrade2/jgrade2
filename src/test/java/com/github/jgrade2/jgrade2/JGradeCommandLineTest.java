package com.github.jgrade2.jgrade2;

import org.json.JSONException;
import org.json.JSONObject;

import org.junit.jupiter.api.Test;

import com.github.jgrade2.jgrade2.gradedtest.GradedTestResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JGradeCommandLineTest {

    private PrintStream origOut;
    private PrintStream origErr;
    private ByteArrayOutputStream captureOut;
    private ByteArrayOutputStream captureErr;

    @BeforeEach
    public void captureOutput() {
        origOut = System.out;
        origErr = System.err;
        captureOut = new ByteArrayOutputStream();
        captureErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captureOut));
        System.setErr(new PrintStream(captureErr));
    }

    @AfterEach
    public void resetOutput() { ;
        System.setOut(origOut);
        System.setErr(origErr);
    }

    @Test
    public void unknownFlagExits() throws IOException{
        assertThrows(RuntimeException.class, () -> {
        JGrade2.main(new String[] {"-blah"});
        });
    }

    @Test
    public void requiresClassFlag() throws IOException{
        assertThrows(RuntimeException.class, () -> {
        JGrade2.main(new String[] {"-f", "json"});
        });
    }

    @Test
    public void acceptsFoundClass() throws IOException{
        JGrade2.main(new String[] {"-c", this.getClass().getCanonicalName()});
    }

    @Test
    public void acceptsMultipleFlags() throws IOException{
        JGrade2.main(new String[] {"-f", "json", "-c", this.getClass().getCanonicalName()});
    }

    @Test
    public void acceptsLongOption() throws IOException{
        JGrade2.main(new String[] {"--format", "json", "-c", this.getClass().getCanonicalName()});
    }

    @Test
    public void rejectsNonExistentClass() throws IOException{
        assertThrows(RuntimeException.class, () -> {
            JGrade2.main(new String[] {"-c", "thisClassDoesNotExist"});
        });
    }

    @Test
    public void rejectsInvalidFormatArguments() throws IOException{
        assertThrows(RuntimeException.class, () -> {
        JGrade2.main(new String[] {"-f", "invalid", "-c", this.getClass().getCanonicalName()});
        });
    }

    @Test
    public void printedValidJson() throws JSONException, IOException{
        JGrade2.main(new String[] {"--format", "json", "-c", this.getClass().getCanonicalName()});
        JSONObject json = new JSONObject(captureOut.toString());
        assertTrue(json.has("tests"));
        JSONObject gradedTestResult = (JSONObject) json.getJSONArray("tests").get(0);
        assertTrue(gradedTestResult.has("name"));
        assertEquals("Test GradedTestResult", gradedTestResult.get("name"));
    }

    @Grade
    public void graderMethod(Grader g) {
        g.addGradedTestResult(new GradedTestResult(
                "Test GradedTestResult",
                "1",
                25.0,
                GradedTestResult.VISIBLE
        ));
    }
}
