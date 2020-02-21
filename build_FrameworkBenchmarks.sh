#/bin/bash
set -e
set -x

# Obtain the project directory
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Move to OfficeFloor source directory
cd ${DIR}/FrameworkBenchmarks/frameworks/Java/officefloor/src

# Build and install artifacts to make available for tests
mvn clean install
