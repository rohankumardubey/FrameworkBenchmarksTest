#/bin/bash

# Change to target directory
cd ../../FrameworkBenchmarks/frameworks/Java/officefloor/src/woof_benchmark_raw/target/

# Start the server
java \
	-XX:+FlightRecorder -XX:StartFlightRecording=filename=profile.jfr \
	-Xmx2g -Xms2g \
	-server \
	-XX:+UseNUMA \
	-DOFFICE.net_officefloor_jdbc_DataSourceManagedObjectSource.server=localhost \
	-jar woof_benchmark_raw-1.0.0.jar \
	8181
