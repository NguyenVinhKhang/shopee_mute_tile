package com.nvkhang.shopeemutetile;

import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ShopeeMuteTileService extends TileService {

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
            String resp = sendCommand("GET");
            mHandler.post(() -> updateTileUI(resp));
        }).start();
    }

    private void toggleMute() {
        new Thread(() -> {
            String resp = sendCommand("TOGGLE");
            mHandler.post(() -> {
                updateTileUI(resp);
                if ("MUTED".equals(resp)) {
                    Toast.makeText(getApplicationContext(), "Shopee: Đã tắt âm", Toast.LENGTH_SHORT).show();
                } else if ("UNMUTED".equals(resp)) {
                    Toast.makeText(getApplicationContext(), "Shopee: Đã bật lại âm thanh", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Chưa khởi động dịch vụ nền", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
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

    private String sendCommand(String cmd) {
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
