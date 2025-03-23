# jgrade2

<a name="readme-top"></a>

<!-- PROJECT SHIELDS -->
<!--
*** I'm using markdown "reference style" links for readability.
*** Reference links are enclosed in brackets [ ] instead of parentheses ( ).
*** See the bottom of this document for the declaration of the reference variables
*** for contributors-url, forks-url, etc. This is an optional, concise syntax you may use.
*** https://www.markdownguide.org/basic-syntax/#reference-style-links
-->
[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]

<!-- Project Overview -->

<h3 align="center">jGrade2</h3>

  <p align="center">
    An annotation library used to help autograde student assignments in Java for Gradescope.
    <br />
    <a href="https://jgrade2.github.io/jgrade2/javadoc/"><strong>Explore the docs »</strong></a>
    <br />
    <br />
    <a href="https://github.com/jgrade2/jgrade2/issues">Report Bug</a>
    ·
    <a href="https://github.com/jgrade2/jgrade2/issues">Request Feature</a>
  </p>
</div>


<!-- ABOUT THE PROJECT -->
## About The Project

This is an update to the original jGrade, now supporting Java17 and [JUnit5](https://junit.org/junit5/). This provides four 
annotations: `@Grade` (+ `@BeforeGrading` and `@AfterGrading`) and `@GradedTest`, each meant to help autograde 
student assignments in Java for the Gradescope autograder. When correctly setup, instructors can 
simply use JUnit5 to write tests for assignemnts. This library will automatically capture results, 
and output the correct json format for Gradescope to read.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



## Getting Started

### Prerequisites

jGrade2 requires Java 21 and JUnit5. It is recommended to use Maven to manage dependencies. Additional 
dependencies are listed in the `pom.xml` file. For the user's convenience, a `jar` file is provided
containing all the dependencies.

### Installation

There are two ways to install jGrade2.

1. Install directly from GitHub [releases](https://github.com/dscpsyl/jgrade2/releases). You can download
   either `jar` files or build the source code from scratch. If building from source, this project uses
   maven wrapper to build. Simply run `./mvnw clean package` to build the project. Two jar files will be
   created, both reflected on the [releases](https://github.com/dscpsyl/jgrade2/releases) page.
2. Add as a dependency directly from Maven Central.

```xml
<dependency>
  <groupId>io.github.dscpsyl</groupId>
  <artifactId>jgrade2</artifactId>
  <version>${jGrade2.version}</version>
</dependency>
```

<p align="right">(<a href="#readme-top">back to top</a>)</p>



## Usage

Usage is nearly identical to the origional [jGrade](https://github.com/tkutcher/jgrade). jGrade2 comes with a commandline
to run the grader manually. This is useful for debugging and testing. After compiling, you can use

```java -jar jGrade2.jar -h```

for help and options of the commandline. However, a typical usage would be

```java -jar jGrade2.jar -c ExampleGrading -o results.json```

### Test Writting and Grading

You can take a look at the gradescope example in `examples/gradescope` for a full example.

#### Tests

Tests are written in jUnit5. The only addition will be the `@GradedTest` annotation to each test that should be counted in the
assignment on gradescope. 

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import com.github.dscpsyl.jgrade2.gradedtest.GradedTest;

import static com.github.dscpsyl.jgrade2.gradedtest.HIDDEN;
import static com.github.dscpsyl.jgrade2.gradedtest.VISIBLE;

public class ExampleTest {
    @Test
    @GradedTest(name="Example Test", points=10)
    public void testExample() {
        Assertions.assertTrue(true);
    }

    @Test
    @GradedTest(name="Hidden Test", points=10, visibility=HIDDEN)
    public void testHidden() {
        Assertions.assertTrue(true);
    }

    @Test
    @GradedTest(name="Visible Test", number="4", points=10, visibility=VISIBLE)
    public void testVisible() {
        Assertions.assertTrue(true);
    }

}
```

The `@GradedTest` annotation has the following parameters:
- `name` - The name of the test. This will be displayed in the Gradescope interface. DEFAUT: "Unnamed test"
- `number` - The test number in a string. This will be displayed in the Gradescope interface. DEFAULT: ""
- `points` - The number of points this test is worth. DEFAULT: 1.0
- `visibility` - The visibility of the test. This can be either VISIBLE or HIDDEN. DEFAULT: VISIBLE

#### Grading

Grading is done by a simple grading class. This class should be in the same package as the tests. Each method in the grading class
that should be considered for grading needs to be annotated with `@Grade`. The method should take in a single parameter of type
`Grader`. `@BeforeGrading` and `AfterGrading` can be used to run code before and after grading respectively. These methods should
also take in a single parameter of type `Grader`. 

```java
import com.github.dscpsyl.jgrade2.Grader;
import com.github.dscpsyl.jgrade2.GradedTestResult;

import com.github.dscpsyl.jgrade2.Grade;
import com.github.dscpsyl.jgrade2.AfterGrading;
import com.github.dscpsyl.jgrade2.BeforeGrading;

import static com.github.dscpsyl.jgrade2.gradedtest.HIDDEN;

public class ExampleGrading {

  /* This function will run before grading starts*/
  @BeforeGrading
  public void beforeGrading(Grader g) {
    System.out.println("Starting grading");
    grader.startTimer();
  }

  /* You can grade a full test class that is in the same package as this class */
  @Grade
  public void gradeTestClass(Grader g) {
    g.runJUnitGradedTests(ExampleTest.class);
  }

  /* If you cannot use jUnit5 or java to grade, you can manually add results */
  @Grade
  public void gradeManually(Grader g) {
    g.addGradedTestResult(new GradedTestResult("Manual Test", "4" , 10.0, HIDDEN));
  }

  /* Grader.startTimer() and Grader.stopTimer() can be used to time the grader */
  @Grade
  public void loopForTime(Grader grader) {
    long startTime = System.currentTimeMillis();
    while (System.currentTimeMillis() - startTime < 1000);
  }

  /* @AfterGrading methods are run after all other methods. */
  @AfterGrading
  public void endGrader(Grader grader) {
    grader.stopTimer();
    System.out.println("Grading finished");
  }

}
```

### Gradescope Setup

There is a full example in `examples/gradescope` that works on gradescope, and models much of the setup from the original [java example](https://github.com/gradescope/autograder_samples/tree/master/java) Gradescope links to.

It compiles all files in to a created `classes/` directory (not tracked). All the scripts will be looking for your classes in this directory. 

The `lib/` folder contains all jars and library files needed to run your test - for this example just the jGrade2 jar file (optionally a checkstyle jar for the checkstyle grading method). The `res/` directory is for resources (like the checkstyle configuration file). 

`src/` is the main source code of both your grading code and your tests.

 `test_submissions/` are submissions to test with on Gradescope. These are precompiled for you to test with the grader that is included in the example.

 The source has 2 main packages, `staff` and `student`. The staff package contains the unit tests, a solution (to debug with) and the code to do the grading. The student package contains studnet skeleton code for studnets to fill in.

 While debugging, a makefile is provided for compiling and running. `make output` will start fresh and run the autograder, pretty-printing the output to the console.

 - `setup.sh`: Installs correct JDK
 - `run_autograder`: Main script for the autograder. Copies in submission, compiles, and runs.
 - `compile.sh`: Compiles all of the source into a classes directory
 - `run.sh`: Runs JGrade, passing in the `GradeHello` file, writing output
   - If run with `--local` then prints output to console, else to the results/results.json file.

 #### To build the Autograder:
 Either:

 1. Run `./mvnw clean package` to build the jGrade2 jar
 2. Copy the jGrade2 jar to the `lib/` folder
    - Example:
    ```
    cp target/jgrade2-2.0.0-a2-all.jar examples/gradescope/lib
    ```
 3. Run either `./make_autograder.sh` or `make autograder` which will place it in the `zips/` folder.
 4. Upload the autograder to Gradescope.

 Or:

 1. Run `./make_autograder_full.sh` which will complete the steps above for you, assuming the gradescope files are still in the `examples` directory. This will put the final zip in the base directory instead of the `zips/` folder. If you want to include checkstyle in the grading, please manually add the checkstyle jar to the `lib/` folder before running this script.



<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- CONTRIBUTING -->
## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

The repository is structured like so:
- `main & master` The main branches for publishing to maven central. These should be kept clean and only update with releases. It is also protected and can only be added to via a pull request.
- `dev` The development branch. This is where all development should be done and contains pe-releases. It is protected and can only be added to via a pull request.

Other branches are for personal developnment and should only be pulled to the `dev` branch. Once a release is determined, then a pull request can be made to `main` or `master`.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Contributors

<a href="https://github.com/jgrade2/jgrade2/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=jgrade2/jgrade2" />
</a>


<!-- LICENSE -->
## License

Distributed under the MIT License. See `LICENSE.txt` for more information.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- ACKNOWLEDGMENTS -->
## Acknowledgments

* This is an update of the original [jGrade](https://github.com/tkutcher/jgrade).
* Originally developed by [dscpsyl](https://github.com/dscpsyl) and directed by [pconrad](https://github.com/pconrad)
  * Origional fork here: [dscpsyl/jGrade2](https://github.com/dscpsyl/jgrade2)
* Currently maintained by the CMPSC 192 course staff and students at University of California, Santa Barbara

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/jgrade2/jgrade2.svg?style=for-the-badge
[contributors-url]: https://github.com/jgrade2/jgrade2/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/jgrade2/jgrade2.svg?style=for-the-badge
[forks-url]: https://github.com/jgrade2/jgrade2/network/members
[stars-shield]: https://img.shields.io/github/stars/jgrade2/jgrade2.svg?style=for-the-badge
[stars-url]: https://github.com/jgrade2/jgrade2/stargazers
[issues-shield]: https://img.shields.io/github/issues/jgrade2/jgrade2.svg?style=for-the-badge
[issues-url]: https://github.com/jgrade2/jgrade2/issues
[license-shield]: https://img.shields.io/github/license/jgrade2/jgrade2.svg?style=for-the-badge
[license-url]: https://github.com/jgrade2/jgrade2/blob/master/LICENSE.txt
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[linkedin-url]: https://www.linkedin.com/in/davidjsim/
