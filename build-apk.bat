@echo off
set ANDROID_SDK_ROOT=C:\Users\ksh01\AppData\Local\Android\Sdk
set ANDROID_HOME=C:\Users\ksh01\AppData\Local\Android\Sdk
cd /d D:\danfish-android
call gradlew.bat assembleDebug --no-daemon > D:\danfish-android\build-log.txt 2>&1
echo EXIT_CODE=%ERRORLEVEL% >> D:\danfish-android\build-log.txt