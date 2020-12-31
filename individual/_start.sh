#/bin/bash

# Obtain the project directory
PROJECT_DIR=$1
POSSIBLE_PORT=$2
JAR_SUFFIX=$3

# Change to target directory
cd ../../FrameworkBenchmarks/frameworks/Java/officefloor/src/${PROJECT_DIR}/target/

# Start the server
java \
	-XX:+FlightRecorder -XX:StartFlightRecording=filename=profile.jfr,dumponexit=true \
	-Xmx2g -Xms2g \
	-server \
	-XX:+UseNUMA \
	-XX:+UseParallelGC \
	-Dhttp.port=8181 \
	-Dhttp.server.name=OF -Dhttp.date.header=true \
	-DOFFICE.net_officefloor_jdbc_DataSourceManagedObjectSource.server=localhost \
	-DOFFICE.net_officefloor_r2dbc_R2dbcManagedObjectSource.host=localhost \
	-Dspring.datasource.url=jdbc:postgresql://localhost:5432/hello_world \
	-jar ${PROJECT_DIR}-1.0.0${JAR_SUFFIX}.jar \
	${POSSIBLE_PORT}
