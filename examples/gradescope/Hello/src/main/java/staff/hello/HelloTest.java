package staff.hello;

import com.github.dscpsyl.jgrade2.gradedtest.GradedTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.assertEquals;

import student.hello.Greeting;

// I like to add this no matter what so if the submission loops,
// Gradescope doesn't get stuck for more than 10 seconds.
@Timeout(10)
public class HelloTest {

    // I use this so I can run locally and verify output
    static final boolean DEBUG = false;

    private static final String GREETING = "Hello";
    private Greeting unit;

    // Makes it so can verify tests work for instructor solution.
    @BeforeEach
    public void initUnit() {
        this.unit = DEBUG ? new Hello(GREETING) : new student.hello.Hello(GREETING);
    }

    @Test
    @GradedTest(name="greet() works")
    public void defaultGreeting() {
        assertEquals(GREETING, unit.greet());
    }

    @Test
    @GradedTest(name="greet(String who) works", points=2.0)
    public void greetSomebody() {
        assertEquals(GREETING + ", World!", unit.greet("World"));
    }

    @Test
    @GradedTest(name="prints greeting", points=0.0)
    public void printGreeting() {
        unit.printGreeting();
    }
}
