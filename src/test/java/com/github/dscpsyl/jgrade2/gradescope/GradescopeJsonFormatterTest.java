package com.github.dscpsyl.jgrade2.gradescope;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.junit.jupiter.api.Test;

import com.github.dscpsyl.jgrade2.Grader;
import com.github.dscpsyl.jgrade2.gradedtest.GradedTestResult;

import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GradescopeJsonFormatterTest {

    private GradescopeJsonFormatter unit;
    private Grader grader;

    @BeforeEach
    public void initUnit() {
        unit = new GradescopeJsonFormatter();
        grader = new Grader();
    }

    private static void assertValidJson(String s) throws JSONException {
        new JSONObject(s);
    }

    @Test
    public void invalidIfEmpty() {
        assertThrows(GradescopeJsonException.class, () -> {
            unit.format(grader);
        });
    }

    @Test
    public void invalidIfNoTestsOrScore() {
        assertThrows(GradescopeJsonException.class, () -> {
            grader.setExecutionTime(45);
            unit.format(grader);
        });
    }

    @Test
    public void validIfScoreSet() throws JSONException {
        grader.setScore(20.0);
        assertValidJson(unit.format(grader));
    }

    @Test
    public void validIfTests() throws JSONException {
        grader.addGradedTestResult(new GradedTestResult("", "", 20.0, "visible"));
        assertValidJson(unit.format(grader));
    }

    @Test
    public void validIfZeroPointTestExists() throws JSONException {
        grader.addGradedTestResult(new GradedTestResult("", "", 0.0, "visible"));
        assertValidJson(unit.format(grader));
    }

    @Test
    public void validIfZeroPointTestMarkedFailed() throws JSONException {
        GradedTestResult result = new GradedTestResult("", "", 0.0, "visible");
        result.setPassed(false);
        grader.addGradedTestResult(result);
        assertValidJson(unit.format(grader));
    }

    /**
     * A test that is explicitly marked as failed should put a status in the output JSON.
     * This prevent Gradescope from marking a failing zero-point test as passing (because the
     * points earned equal the max points).
     * @throws JSONException
     */
    @Test
    public void zeroPointFailedTestGeneratesStatus() throws JSONException {
        GradedTestResult result = new GradedTestResult("", "", 0.0, "visible");
        result.setPassed(false);
        grader.addGradedTestResult(result);

        String jsonString = unit.format(grader);

        JSONObject obj = new JSONObject(jsonString);
        assertNotNull(obj);
        JSONArray testArray = obj.getJSONArray("tests");
        assertNotNull(testArray);
        assertEquals(1, testArray.length());
        JSONObject testObject = testArray.getJSONObject(0);
        assertEquals("failed", testObject.getString("status"));
    }

    @Test
    public void catchesInvalidVisibility() {
        assertThrows(GradescopeJsonException.class, () -> {
        unit.setVisibility("invisible");
        });
    }

    @Test
    public void catchesInvalidStdoutVisibility() {
        assertThrows(GradescopeJsonException.class, () -> {
        unit.setStdoutVisibility("invisible");
        });
    }

    @Test
    public void visibilitySelect(){
        assertAll(()->{unit.setVisibility("hidden");});
    }

    @Test
    public void stdOutVisibilitySelect(){
        assertAll(()->{unit.setStdoutVisibility("after_published");});
    }

    @Test
    public void prettyPrintSelect(){
        assertAll(()->{unit.setPrettyPrint(2);});
    }
}
