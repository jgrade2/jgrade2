#! /usr/bin/env bash

# This script is used to create the autograder.zip file for the autograder
# submission on Gradescope. 
echo "Creating autograder.zip file for Gradescope submission..."
echo "--------------------------------------------------"

AUTOGRADER=hello
if (( $# > 0 )); then
    AUTOGRADER=$1
fi

echo "Autograder directory: examples/gradescope/${AUTOGRADER}"

echo "Making jGrade2 jar file..."
./mvnw clean package -DskipTests

echo "Copying jGrade2 jar file to examples/gradescope/${AUTOGRADER}/lib/..."
cp target/jgrade2-2.0.0-a2-all.jar examples/gradescope/${AUTOGRADER}/lib/

echo "Zipping up autograder files..."
cd examples/gradescope/${AUTOGRADER}
zip -r autograder.zip lib/ res/ src/ compile.sh run.sh setup.sh run_autograder
mv autograder.zip zips/

echo "Moving autograder.zip file to root directory..."
mv zips/autograder.zip ../../..

echo "Cleaning up..."
rm lib/jgrade2-2.0.0-a2-all.jar
cd ../../..

