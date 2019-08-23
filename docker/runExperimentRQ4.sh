#!/usr/bin/env bash
cd uigrammar

INPUT_DIR_RQ1=/test/experiment/input
OUTPUT_DIR=/test/experiment/output_rq4
INPUT_DIR=/test/experiment/input_rq4
APKS_DIR=/test/experiment/apks
NR_SEEDS=11

mkdir ${INPUT_DIR}
chmod 777 ${INPUT_DIR}

echo "Copying initial exploration from RQ1"
cp -r ${INPUT_DIR_RQ1}/* ${INPUT_DIR}/

echo "Cleaning output folder ${OUTPUT_DIR}"
rm -rf ${OUTPUT_DIR}
mkdir ${OUTPUT_DIR}
chmod 777 ${OUTPUT_DIR}

echo "Removing previous grammar and input values from ${INPUT_DIR}/apks/"
rm -rf ${INPUT_DIR}/apks/*.txt

echo "Extracting grammar and input values from ${INPUT_DIR}/apks/"
./gradlew run --args="loc ${INPUT_DIR}/apks/droidMate/model/ ${INPUT_DIR}/apks/ 11 true"

for s in 00 01 02 03 04 05 06 07 08 09 10
do
	echo "Stopping emulator"
	cd ..
	./stopEmu.sh

	echo "Starting the emulator"
	./startEmu.sh
	cd uigrammar

        sleep 20s

	echo "Running grammar inputs from ${INPUT_DIR}/apks/"
	./gradlew run --args="-i ${INPUT_DIR}/apks/ -s ${s} -f symbolInputs --Exploration-apksDir=${APKS_DIR} --Output-outputDir=${OUTPUT_DIR} --Exploration-launchActivityDelay=3000 --Exploration-widgetActionDelay=800 --Selectors-randomSeed=1 --Deploy-installApk=true --Deploy-uninstallApk=true --Selectors-pressBackProbability=0.00 --Strategies-explore=false --StatementCoverage-enableCoverage=true" || true
done

echo "Summary"
cat ${OUTPUT_DIR}/summary.txt

echo "Done"
