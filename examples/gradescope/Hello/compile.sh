#!/usr/bin/env bash

echo "Compiling java files... to the classes/ directory"
echo "--------------------------------------------------"

# start with a clean classes/ directory
rm -rf classes
echo "making clean classes/ directory"
mkdir -p classes

# Compile all java files in src directory
java_files=$(find src -name "*.java")
echo "compiling java files..."
javac -cp lib/*:. -d classes ${java_files}
echo "DONE"
echo "--------------------------------------------------"