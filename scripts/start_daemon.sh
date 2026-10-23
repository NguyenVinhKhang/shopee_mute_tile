#!/bin/bash
# Push and start daemon on the connected Android device
set -e

DEVICE_ARG=""
if [ -n "$1" ]; then
    DEVICE_ARG="-s $1"
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "=== Pushing handler script to device ==="
adb $DEVICE_ARG push "$SCRIPT_DIR/handler.sh" /data/local/tmp/shopee_handler.sh
adb $DEVICE_ARG shell "chmod +x /data/local/tmp/shopee_handler.sh"

echo "=== Starting background daemon on device (port 19876) ==="
adb $DEVICE_ARG shell "nohup toybox nc -s 127.0.0.1 -p 19876 -L /data/local/tmp/shopee_handler.sh >/dev/null 2>&1 &"

echo "=== Testing daemon connection ==="
sleep 1
RESP=$(adb $DEVICE_ARG shell "echo 'GET' | toybox nc 127.0.0.1 19876" | tr -d '\r\n')
echo "Daemon response: $RESP"
if [ "$RESP" = "MUTED" ] || [ "$RESP" = "UNMUTED" ]; then
    echo " Daemon is running successfully!"
else
    echo "❌ Daemon failed to start. Please check adb connection."
fi
