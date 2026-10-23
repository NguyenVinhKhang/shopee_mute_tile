#!/system/bin/sh
# Handler script running on Android device (invoked by toybox nc)
read cmd
STATUS=$(cmd appops get com.shopee.vn | grep -E 'PLAY_AUDIO: ignore')
if [ "$cmd" = "TOGGLE" ]; then
    if [ -n "$STATUS" ]; then
        cmd appops set com.shopee.vn PLAY_AUDIO allow
        cmd appops set com.shopee.vn TAKE_AUDIO_FOCUS allow
        echo "UNMUTED"
    else
        cmd appops set com.shopee.vn PLAY_AUDIO ignore
        cmd appops set com.shopee.vn TAKE_AUDIO_FOCUS ignore
        echo "MUTED"
    fi
else
    # Query current status (GET)
    if [ -n "$STATUS" ]; then
        echo "MUTED"
    else
        echo "UNMUTED"
    fi
fi
