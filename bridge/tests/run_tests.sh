#!/bin/bash

readonly OUT_DIR="$1"
readonly DIST_DIR="$2"
readonly BUILD_NUMBER="$3"

readonly SCRIPT_DIR="$(dirname "$0")"

readonly FAILURE_DIR=layoutlib-test-failures
readonly FAILURE_ZIP=layoutlib-test-failures.zip

STUDIO_JDK=${SCRIPT_DIR}"/../../../../prebuilts/studio/jdk/linux"
MISC_COMMON=${SCRIPT_DIR}"/../../../../prebuilts/misc/common"
OUT_INTERMEDIATES=${SCRIPT_DIR}"/../../../../out/soong/.intermediates"
NATIVE_LIBRARIES=${SCRIPT_DIR}"/../../../../out/host/linux-x86/lib64/"
SDK=${SCRIPT_DIR}"/../../../../out/host/linux-x86/sdk/sdk*/android-sdk*"
ICU_DIR=${SCRIPT_DIR}"/../../../../out/host/linux-x86/com.android.runtime/etc/icu"
TMP_DIR=$(mktemp -d)
PLATFORM=${TMP_DIR}/"android"

# Copy resources to a temp directory
cp -r ${SDK}/platforms/android* ${PLATFORM}

# Compile 9-patch files
mkdir ${TMP_DIR}/compiled
mkdir ${TMP_DIR}/manifest
echo \
'<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.google.android.layoutlib" />' \
> ${TMP_DIR}/manifest/AndroidManifest.xml
${SDK}/build-tools/android-*/aapt2 compile ${SDK}/platforms/android*/data/res/**/*.9.png -o ${TMP_DIR}/compiled/
${SDK}/build-tools/android-*/aapt2 link -R ${TMP_DIR}/compiled/* -o ${TMP_DIR}/compiled.apk --manifest ${TMP_DIR}/manifest/AndroidManifest.xml
unzip -q ${TMP_DIR}/compiled.apk -d ${TMP_DIR}
for f in ${TMP_DIR}/res/*; do mv "$f" "${f/-v4/}";done
cp -rL ${TMP_DIR}/res ${PLATFORM}/data

# Run layoutlib tests
${STUDIO_JDK}/bin/java -ea \
    -Dnative.lib.path=${NATIVE_LIBRARIES} \
    -Dicu.dir=${ICU_DIR} \
    -Dplatform.dir=${PLATFORM} \
    -Dtest_res.dir=${SCRIPT_DIR}/res \
    -Dtest_failure.dir=${OUT_DIR}/${FAILURE_DIR} \
    -cp ${MISC_COMMON}/tools-common/tools-common-prebuilt.jar:${MISC_COMMON}/ninepatch/ninepatch-prebuilt.jar:${MISC_COMMON}/sdk-common/sdk-common.jar:${MISC_COMMON}/kxml2/kxml2-2.3.0.jar:${MISC_COMMON}/layoutlib_api/layoutlib_api-prebuilt.jar:${OUT_INTERMEDIATES}/external/junit/junit/linux_glibc_common/javac/junit.jar:${OUT_INTERMEDIATES}/external/guava/guava-jre/linux_glibc_common/javac/guava-jre.jar:${OUT_INTERMEDIATES}/external/hamcrest/hamcrest-core/hamcrest/linux_glibc_common/javac/hamcrest.jar:${OUT_INTERMEDIATES}/external/mockito/mockito/linux_glibc_common/combined/mockito.jar:${OUT_INTERMEDIATES}/external/objenesis/objenesis/linux_glibc_common/javac/objenesis.jar:${OUT_INTERMEDIATES}/frameworks/layoutlib/bridge/layoutlib/linux_glibc_common/withres/layoutlib.jar:${OUT_INTERMEDIATES}/frameworks/layoutlib/temp_layoutlib/linux_glibc_common/gen/temp_layoutlib.jar:${OUT_INTERMEDIATES}/frameworks/layoutlib/bridge/tests/layoutlib-tests/linux_glibc_common/withres/layoutlib-tests.jar \
    org.junit.runner.JUnitCore \
    com.android.layoutlib.bridge.intensive.Main

test_exit_code=$?

# Create zip of all failure screenshots
if [[ -d "${OUT_DIR}/${FAILURE_DIR}" ]]; then
    zip -q -j -r ${OUT_DIR}/${FAILURE_ZIP} ${OUT_DIR}/${FAILURE_DIR}
fi

# Move failure zip to dist directory if specified
if [[ -d "${DIST_DIR}" ]] && [[ -e "${OUT_DIR}/${FAILURE_ZIP}" ]]; then
    mv ${OUT_DIR}/${FAILURE_ZIP} ${DIST_DIR}
fi

# Clean
rm -rf ${TMP_DIR}
rm -rf ${OUT_DIR}/${FAILURE_DIR}

exit ${test_exit_code}
