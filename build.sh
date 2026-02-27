#!/bin/bash
# Calendar App Build Script
set -e
echo "Calendar App Build Script"
echo "========================="

# Check Java
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed"
    exit 1
fi

echo "Java version:"
java -version 2>&1 | head -1

# Build
echo ""
echo "Building release APK..."
bash gradlew assembleRelease --no-daemon

# Check result
APK_PATH="app/build/outputs/apk/release/app-release.apk"
if [ -f "$APK_PATH" ]; then
    echo ""
    echo "Build successful!"
    echo "APK location: $APK_PATH"
    echo "APK size: $(du -h "$APK_PATH" | cut -f1)"
else
    echo "Build failed!"
    exit 1
fi
