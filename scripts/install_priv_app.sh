#!/bin/bash
# Install Shopee Mute Tile as a Privileged System App via OrangeFox / TWRP ADB
set -e

DEVICE_ARG=""
if [ -n "$1" ]; then
    DEVICE_ARG="-s $1"
fi

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "=== Installing ShopeeMuteTile as Privileged System App ==="
echo "Ensure device is in OrangeFox/TWRP with /system mounted as Read-Write!"

# Check root access
adb $DEVICE_ARG shell id

# Remount system as RW (supports both standard and system-as-root layouts)
adb $DEVICE_ARG shell "mount -o rw,remount /system 2>/dev/null || mount -o rw,remount /system_root 2>/dev/null || mount -o rw,remount / 2>/dev/null || true"

# Push APK to /system/priv-app
echo "Pushing APK to /system/priv-app/ShopeeMuteTile/..."
adb $DEVICE_ARG shell "mkdir -p /system/priv-app/ShopeeMuteTile"
adb $DEVICE_ARG push "$PROJECT_DIR/ShopeeMuteTile.apk" /system/priv-app/ShopeeMuteTile/ShopeeMuteTile.apk
adb $DEVICE_ARG shell "chmod 755 /system/priv-app/ShopeeMuteTile"
adb $DEVICE_ARG shell "chmod 644 /system/priv-app/ShopeeMuteTile/ShopeeMuteTile.apk"

# Push permissions whitelist XML to /system/etc/permissions
echo "Pushing permissions whitelist XML..."
adb $DEVICE_ARG push "$PROJECT_DIR/scripts/privapp-permissions-shopeemute.xml" /system/etc/permissions/privapp-permissions-shopeemute.xml
adb $DEVICE_ARG shell "chmod 644 /system/etc/permissions/privapp-permissions-shopeemute.xml"

echo "=== Verification ==="
adb $DEVICE_ARG shell "ls -la /system/priv-app/ShopeeMuteTile/ShopeeMuteTile.apk"
adb $DEVICE_ARG shell "ls -la /system/etc/permissions/privapp-permissions-shopeemute.xml"

echo "=========================================="
echo "SUCCESS! ShopeeMuteTile is now a System Priv-App."
echo "Run 'adb reboot' to start into Android."
echo "=========================================="
