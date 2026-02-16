#!/bin/bash

# ReceiptKeeper - Environment Initialization Script
# Validates Android development environment for SOP compliance

set -e

echo "=== ReceiptKeeper Environment Validation ==="
echo ""

# Check for Android SDK
if [ -z "$ANDROID_HOME" ] && [ -z "$ANDROID_SDK_ROOT" ]; then
    echo "❌ ANDROID_HOME or ANDROID_SDK_ROOT not set"
    exit 1
fi

ANDROID_SDK="${ANDROID_HOME:-$ANDROID_SDK_ROOT}"
echo "✓ Android SDK found at: $ANDROID_SDK"

# Check for adb
if ! command -v adb &> /dev/null; then
    echo "❌ adb not found in PATH"
    exit 1
fi
echo "✓ adb found: $(which adb)"

# Check device/emulator connectivity
echo ""
echo "Checking connected devices..."
DEVICES=$(adb devices | grep -v "List of devices" | grep -v "^$" | wc -l)

if [ "$DEVICES" -eq 0 ]; then
    echo "❌ No devices or emulators connected"
    echo "   Run: emulator -avd <avd_name> or connect a physical device"
    exit 1
fi

echo "✓ Connected devices: $DEVICES"
adb devices | grep -v "List of devices"

# Check for Java/JDK
if ! command -v java &> /dev/null; then
    echo "❌ Java not found in PATH"
    exit 1
fi
JAVA_VERSION=$(java -version 2>&1 | head -n 1)
echo "✓ Java found: $JAVA_VERSION"

# Check for Gradle wrapper
if [ ! -f "./gradlew" ]; then
    echo "⚠️  Gradle wrapper not found (expected for new projects)"
else
    echo "✓ Gradle wrapper present"
fi

# Verify tracking artifacts exist
echo ""
echo "Checking SOP tracking artifacts..."

if [ ! -f "ai/feature_list.json" ]; then
    echo "⚠️  ai/feature_list.json not found"
else
    echo "✓ ai/feature_list.json exists"
fi

if [ ! -f "ai/progress.md" ]; then
    echo "⚠️  ai/progress.md not found"
else
    echo "✓ ai/progress.md exists"
fi

if [ ! -f "CLAUDE.md" ]; then
    echo "⚠️  CLAUDE.md not found"
else
    echo "✓ CLAUDE.md exists"
fi

echo ""
echo "=== Environment validation complete ==="
echo ""

exit 0
