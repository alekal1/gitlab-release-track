@echo off
REM
pushd "%~dp0"
call gradlew.bat bootRun %*
popd

