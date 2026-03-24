@echo off
set KEYSTORE_PASSWORD=danfish2024
set KEY_PASSWORD=danfish2024
set ANDROID_SDK_ROOT=C:\Users\ksh01\AppData\Local\Android\Sdk
set ANDROID_HOME=C:\Users\ksh01\AppData\Local\Android\Sdk
cd /d D:\danfish-android
echo BUILD_START > D:\danfish-android\build-output.txt
call gradlew.bat bundleRelease --no-daemon >> D:\danfish-android\build-output.txt 2>&1
echo EXIT_CODE=%ERRORLEVEL% >> D:\danfish-android\build-output.txt
echo BUILD_DONE >> D:\danfish-android\build-output.txt
