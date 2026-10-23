#!/bin/bash
set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

ANDROID_SDK="${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}"
PLATFORM="$ANDROID_SDK/platforms/android-36/android.jar"
if [ ! -f "$PLATFORM" ]; then
    PLATFORM="$ANDROID_SDK/platforms/android-34/android.jar"
fi

BUILD_TOOLS=$(find "$ANDROID_SDK/build-tools" -maxdepth 1 -mindepth 1 | sort -V | tail -n 1)

echo "Using Platform: $PLATFORM"
echo "Using Build Tools: $BUILD_TOOLS"

rm -rf bin base.apk res.zip ShopeeMuteTile_aligned.apk
mkdir -p bin

echo "1. Compiling resources..."
"$BUILD_TOOLS/aapt2" compile --dir res -o res.zip
"$BUILD_TOOLS/aapt2" link res.zip -I "$PLATFORM" \
    --manifest AndroidManifest.xml \
    --java src \
    -o base.apk

echo "2. Compiling Java sources..."
javac -source 8 -target 8 -cp "$PLATFORM" -d bin $(find src -name "*.java")

echo "3. Converting bytecode to DEX..."
"$BUILD_TOOLS/d8" --lib "$PLATFORM" --output bin/ $(find bin -name "*.class")

echo "4. Packaging classes.dex..."
cd bin && zip -u ../base.apk classes.dex && cd ..

echo "5. Aligning APK..."
"$BUILD_TOOLS/zipalign" -f -p 4 base.apk ShopeeMuteTile_aligned.apk

echo "6. Signing APK..."
KEYSTORE="/tmp/debug.keystore"
if [ ! -f "$KEYSTORE" ]; then
    keytool -genkey -v -keystore "$KEYSTORE" -alias debug -keyalg RSA -keysize 2048 -validity 10000 -storepass android -keypass android -dname "CN=AndroidDebug,O=Android,C=US"
fi

"$BUILD_TOOLS/apksigner" sign --ks "$KEYSTORE" --ks-pass pass:android --out ShopeeMuteTile.apk ShopeeMuteTile_aligned.apk

# Clean build artifacts
rm -rf bin res.zip base.apk ShopeeMuteTile_aligned.apk

echo "=== Build Complete ==="
ls -lh ShopeeMuteTile.apk
