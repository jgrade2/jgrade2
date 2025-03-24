#!/usr/bin/env bash

echo "Building Autograder..."
echo "--------------------------------------------------"

zip -r autograder.zip lib/ res/ src/ compile.sh run.sh setup.sh run_autograder
mv autograder.zip zips/

echo "DONE. Locate it in the zips directory."
echo "--------------------------------------------------"