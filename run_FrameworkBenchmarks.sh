#!/bin/bash
set -e
set -x

# Enable running docker in CI
export COMPOSE_INTERACTIVE_NO_CLI=1

# Obtain the script directory
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Clear any previous results
if [  -f "${DIR}/results.txt" ]; then
	rm "${DIR}/results.txt"
fi
if [ -f "${DIR}/results.zip" ]; then
	rm "${DIR}/results.zip"
fi

# Run the comparison (don't fail build if run fails)
cd "${DIR}/FrameworkBenchmarks"
./tfb --clean
./tfb --test officefloor officefloor-raw officefloor-micro officefloor-thread_affinity officefloor-tpr officefloor-netty officefloor-spring_data || true

# Find the latest results directory
RESULTS_DIR=''
for CHECK_DIR in $(ls -t "${DIR}/FrameworkBenchmarks/results"); do
	if [  -z "${RESULTS_DIR}" ]; then
		RESULTS_DIR="${CHECK_DIR}"
	fi
done

# Move the results to top level (avoids duplicating results in zip)
if [ -f "${DIR}/results.json" ]; then
	rm "${DIR}/results.json"
fi
cp "${DIR}/FrameworkBenchmarks/results/${RESULTS_DIR}/results.json" "${DIR}/results.txt"

# Create zip of results directory (to aid identifying causes of failure)
if [ -f "${DIR}/results.zip" ]; then
	rm "${DIR}/results.zip"
fi
zip -r "${DIR}/results.zip" "${DIR}/FrameworkBenchmarks/results/${RESULTS_DIR}/"
