#!/bin/bash
set -u

readonly OUT_DIR="$1"
readonly DIST_DIR="$2"
readonly SCRIPT_DIR="$(dirname "$0")"
readonly BASE_DIR=`readlink -m ${SCRIPT_DIR}/../../../../`
echo "BASE_DIR: $BASE_DIR"
readonly FAILURE_DIR=layoutlib-test-failures
readonly FAILURE_ZIP=layoutlib-test-failures.zip

readonly CLEAN_TMP_FILES=1
readonly USE_SOONG=1

readonly APP_NAME="regression"
#readonly APP_NAME="test_HelloActivity"

STUDIO_JDK="${BASE_DIR}/prebuilts/jdk/jdk21/linux-x86"
NATIVE_LIBRARIES="${BASE_DIR}/out/host/linux-x86/lib64/"
JAVA_LIBRARIES="${BASE_DIR}/out/host/common/obj/JAVA_LIBRARIES/"
HOST_LIBRARIES="${BASE_DIR}/out/host/linux-x86"
PACKAGING="${BASE_DIR}/out/host/common/obj/PACKAGING"
ICU_DATA_PATH="${BASE_DIR}/out/host/linux-x86/com.android.i18n/etc/icu/icudt78l.dat"

TEST_JARS="${HOST_LIBRARIES}/framework/layoutlib-tests.jar"
GRADLE_RES="-Dtest_res.dir=${SCRIPT_DIR}/res"

# Run layoutlib tests
#DEBUGGER=' -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8000 '
DEBUGGER=' '

set -x
${STUDIO_JDK}/bin/java -ea $DEBUGGER \
    -Dnative.lib.path=${NATIVE_LIBRARIES} \
    -Dfont.dir=${PACKAGING}/fonts_intermediates \
    -Dicu.data.path=${ICU_DATA_PATH} \
    -Dhyphen.data.dir=${PACKAGING}/hyphen_intermediates \
    -Dkeyboard.dir=${PACKAGING}/keyboards_intermediates \
    -Dplatform.res.dir=${PACKAGING}/layoutlib-res_intermediates \
    -Dbuild.prop.dir=${PACKAGING}/layoutlib-build-prop_intermediates \
    -Dtest_failure.dir=${OUT_DIR}/${FAILURE_DIR} \
    ${GRADLE_RES} \
    -cp ${TEST_JARS} \
    org.junit.runner.JUnitCore \
    com.android.layoutlib.bridge.intensive.Main
test_exit_code=$?
set +x


# Create zip of all failure screenshots
rm -f ${OUT_DIR}/${FAILURE_ZIP}
if [[ -d "${OUT_DIR}/${FAILURE_DIR}" ]]; then
    zip -q -j -r ${OUT_DIR}/${FAILURE_ZIP} ${OUT_DIR}/${FAILURE_DIR}
fi

# Move failure zip to dist directory if specified
if [[ -d "${DIST_DIR}" ]] && [[ -e "${OUT_DIR}/${FAILURE_ZIP}" ]]; then
    mv ${OUT_DIR}/${FAILURE_ZIP} ${DIST_DIR}
fi

# Clean
if [[ $CLEAN_TMP_FILES -eq 1 ]]; then
  rm -rf ${OUT_DIR}/${FAILURE_DIR}
fi

exit ${test_exit_code}
