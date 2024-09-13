#!/bin/bash

SCRIPT_DIR="$(dirname $0)"
DIST_DIR="$1"

STUDIO_JDK=${SCRIPT_DIR}"/../../../../prebuilts/jdk/jdk21/linux-x86"
TEST_JAR=${SCRIPT_DIR}"/../../../../out/host/linux-x86/framework/layoutlib-create-tests.jar"

${STUDIO_JDK}/bin/java -ea \
    -cp ${TEST_JAR}:${SCRIPT_DIR}/res \
    org.junit.runner.JUnitCore \
    com.android.tools.layoutlib.create.AllTests

