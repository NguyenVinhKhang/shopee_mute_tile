package com.nvkhang.shopeemutetile;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ShopeeMuteTileService extends TileService {

    private static final String TARGET_PACKAGE = "com.shopee.vn";
    private static final int OP_PLAY_AUDIO = 28;
    private static final int OP_TAKE_AUDIO_FOCUS = 32;
    private static final int MODE_ALLOWED = 0;
    private static final int MODE_IGNORED = 1;

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 19876;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onStartListening() {
        super.onStartListening();
        checkStatus();
    }

    @Override
    public void onClick() {
        super.onClick();
        toggleMute();
    }

    private void checkStatus() {
        new Thread(() -> {
            String status = queryStatusDirect();
            if (status == null) {
                // Fallback to socket daemon
                status = sendSocketCommand("GET");
            }
            final String finalStatus = status;
            mHandler.post(() -> updateTileUI(finalStatus));
        }).start();
    }

    private void toggleMute() {
        new Thread(() -> {
            String currentStatus = queryStatusDirect();
            String newStatus = null;

            if (currentStatus != null) {
                // Direct AppOps permission available (Privileged System App)
                boolean shouldMute = !"MUTED".equals(currentStatus);
                if (setMuteDirect(shouldMute)) {
                    newStatus = shouldMute ? "MUTED" : "UNMUTED";
                }
            }

            if (newStatus == null) {
                // Fallback to socket daemon
                newStatus = sendSocketCommand("TOGGLE");
            }

            final String result = newStatus;
            mHandler.post(() -> {
                updateTileUI(result);
                if ("MUTED".equals(result)) {
                    Toast.makeText(getApplicationContext(), "Shopee: Đã tắt âm", Toast.LENGTH_SHORT).show();
                } else if ("UNMUTED".equals(result)) {
                    Toast.makeText(getApplicationContext(), "Shopee: Đã bật lại âm thanh", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Chưa khởi động dịch vụ nền", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    /**
     * Query status using direct AppOpsManager (works when running as Privileged App).
     * Returns "MUTED", "UNMUTED", or null if not permitted.
     */
    private String queryStatusDirect() {
        try {
            PackageManager pm = getPackageManager();
            int uid = pm.getPackageUid(TARGET_PACKAGE, 0);
            AppOpsManager aom = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            Method checkOpMethod = AppOpsManager.class.getMethod("checkOpNoThrow", int.class, int.class, String.class);
            int mode = (int) checkOpMethod.invoke(aom, OP_PLAY_AUDIO, uid, TARGET_PACKAGE);
            return (mode == MODE_IGNORED) ? "MUTED" : "UNMUTED";
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Set mute state using direct AppOpsManager (works when running as Privileged App).
     */
    private boolean setMuteDirect(boolean mute) {
        try {
            PackageManager pm = getPackageManager();
            int uid = pm.getPackageUid(TARGET_PACKAGE, 0);
            AppOpsManager aom = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            Method setModeMethod = AppOpsManager.class.getMethod("setMode", int.class, int.class, String.class, int.class);
            int targetMode = mute ? MODE_IGNORED : MODE_ALLOWED;
            setModeMethod.invoke(aom, OP_PLAY_AUDIO, uid, TARGET_PACKAGE, targetMode);
            setModeMethod.invoke(aom, OP_TAKE_AUDIO_FOCUS, uid, TARGET_PACKAGE, targetMode);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void updateTileUI(String status) {
        Tile tile = getQsTile();
        if (tile == null) return;

        if ("MUTED".equals(status)) {
            tile.setState(Tile.STATE_ACTIVE);
            tile.setLabel("Shopee: Tắt âm");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                tile.setSubtitle("Đã tắt âm");
            }
        } else if ("UNMUTED".equals(status)) {
            tile.setState(Tile.STATE_INACTIVE);
            tile.setLabel("Shopee: Bật âm");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                tile.setSubtitle("Bình thường");
            }
        } else {
            tile.setState(Tile.STATE_UNAVAILABLE);
            tile.setLabel("Mute Shopee");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                tile.setSubtitle("Cần kết nối");
            }
        }
        tile.updateTile();
    }

    private String sendSocketCommand(String cmd) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(HOST, PORT), 800);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println(cmd);
            return in.readLine();
        } catch (Exception e) {
            return "ERROR";
        }
    }
}
