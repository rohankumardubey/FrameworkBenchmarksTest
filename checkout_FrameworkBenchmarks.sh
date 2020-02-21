#/bin/bash
set -e
set -x

# Obtain the project directory
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd ${DIR}

# Check out latest FrameworkBenchmarks
if [ -d "${DIR}/FrameworkBenchmarks" ]; then
	rm -rf "${DIR}/FrameworkBenchmarks"
fi
git clone --depth 1 https://github.com/officefloor/FrameworkBenchmarks.git
