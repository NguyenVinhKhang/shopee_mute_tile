# Shopee Mute Quick Settings Tile

> **Nút bấm Bật/Tắt âm thanh riêng biệt cho Shopee trong Trung tâm điều khiển Android (Quick Settings Tile)**  
> **Tác giả:** `nvkhang`  
> **Hỗ trợ:** Android 10 (API 29) đến Android 16 (API 36)  
> **Dung lượng:** ~13 KB (Siêu nhẹ, 0% chạy ngầm, không quảng cáo)  

---

## 🌟 Giới Thiệu Tổng Quan

Ứng dụng này giải quyết một vấn đề gây khó chịu kinh điển trên Android:  
Khi bạn đang nghe nhạc trên **Spotify, YouTube, Apple Music**, mỗi khi mở ứng dụng **Shopee** để mua sắm, nhận xu hoặc lướt video, Shopee thường tự động phát âm thanh quảng cáo ồn ào và **cướp quyền âm thanh (Audio Focus)** khiến bài nhạc nền của bạn bị ngắt hoặc tạm dừng.

**Shopee Mute Tile** cung cấp một nút bấm chức năng (Quick Settings Tile) ngay trong **Trung tâm điều khiển (Control Center)** của máy:
* **Không hiện icon ở màn hình chính (Launcher):** App hoàn toàn "tàng hình" ngoài giao diện, không gây vướng mắt.
* **Chỉ xuất hiện trong Trung tâm điều khiển:** Chạm 1 chạm để đổi trạng thái giữa **Tắt âm (Mute)** và **Bật âm (Unmute)**.
* **Kiểm tra trạng thái thời gian thực:** Đèn của nút tự động sáng khi Shopee bị câm và tắt khi Shopee có tiếng.

---

## ⚙️ Cơ Chế Hoạt Động (Technical Architecture)

Ứng dụng can thiệp vào tầng **AppOps (Application Operations)** ngầm của Android:

1. **`OP_PLAY_AUDIO: ignore`**:
   * Khi kích hoạt, hệ thống Android sẽ chặn toàn bộ các luồng `AudioTrack` phát ra từ Shopee và biến chúng thành dải sóng im lặng (Silence).
   * Shopee hoàn toàn bị câm tiếng, nhưng các ứng dụng khác (Nhạc nền, Zalo, Cuộc gọi thoại, Game) vẫn phát âm thanh bình thường.

2. **`OP_TAKE_AUDIO_FOCUS: ignore`**:
   * Tước quyền cướp Audio Focus của Shopee. Bài nhạc bạn đang nghe trên Spotify / YouTube sẽ tiếp tục phát liên tục mà không bao giờ bị gián đoạn.

---

## 🚀 Hướng Dẫn Cài Đặt & Sử Dụng

### Bước 1: Build hoặc Cài đặt file APK
* Bạn có thể tự build file APK bằng lệnh:
  ```bash
  ./build.sh
  ```
* Cài đặt file APK vào điện thoại qua ADB:
  ```bash
  adb install -r ShopeeMuteTile.apk
  ```

### Bước 2: Khởi động dịch vụ hỗ trợ (Daemon) trên điện thoại
Do tính năng thay đổi AppOps yêu cầu quyền bảo mật hệ thống (UID Shell 2000), chạy lệnh sau qua ADB một lần:
```bash
./scripts/start_daemon.sh
```
*(Hoặc gõ lệnh trực tiếp:)*
```bash
adb push scripts/handler.sh /data/local/tmp/shopee_handler.sh
adb shell "chmod +x /data/local/tmp/shopee_handler.sh"
adb shell "nohup toybox nc -s 127.0.0.1 -p 19876 -L /data/local/tmp/shopee_handler.sh >/dev/null 2>&1 &"
```

### Bước 3: Thêm nút vào Trung tâm điều khiển
1. Vuốt thanh **Trung tâm điều khiển (Control Center)** từ trên đỉnh màn hình xuống.
2. Bấm vào nút **Chỉnh sửa / Sắp xếp nút (icon cây bút)**.
3. Tìm nút **"🔇 Tắt tiếng Shopee"** và kéo lên vị trí thuận tiện nhất của bạn.
4. Chạm để bật/tắt bất cứ lúc nào!

---

## ⚡ Hướng Dẫn Cài Đặt Vĩnh Viễn Không Cần Daemon (Privileged System App)

Nếu điện thoại của bạn đã Unlock Bootloader (ví dụ Redmi Note 7 Pro), bạn có thể cài `ShopeeMuteTile` trực tiếp vào phân vùng hệ thống (`/system/priv-app/`) bằng cách boot tạm thời **OrangeFox / TWRP Recovery**:
* **Không cần cắm cáp sau mỗi lần reboot.**
* **Không cần chạy script daemon ngầm.**
* Xem tài liệu hướng dẫn từng bước chi tiết tại: **[ORANGEFOX_INSTALL_GUIDE.md](ORANGEFOX_INSTALL_GUIDE.md)**.

---

## 📂 Cấu Trúc Mã Nguồn

```
shopee_mute_tile/
├── AndroidManifest.xml              # Cấu hình TileService (SDK 29 - 36, No Launcher)
├── build.sh                         # Script build APK tự động bằng Android SDK
├── ORANGEFOX_INSTALL_GUIDE.md       # Hướng dẫn chi tiết cài vĩnh viễn qua OrangeFox
├── README.md                        # Hướng dẫn chi tiết
├── res/                             # Tài nguyên icon vector & chuỗi ngôn ngữ
│   ├── drawable/
│   │   └── ic_mute.xml              # Icon loa gạch chéo chuẩn vector
│   └── values/
│       └── strings.xml              # Tiêu đề và nhãn đa ngôn ngữ
├── scripts/                         # Các script quản lý daemon & priv-app
│   ├── handler.sh                   # Script xử lý lệnh trên điện thoại Android
│   ├── install_priv_app.sh          # Script tự động nạp priv-app qua OrangeFox
│   ├── privapp-permissions-shopeemute.xml  # Whitelist quyền MANAGE_APP_OPS_MODES
│   └── start_daemon.sh              # Script 1-click kích hoạt daemon qua ADB
└── src/                             # Mã nguồn Java
    └── com/nvkhang/shopeemutetile/
        └── ShopeeMuteTileService.java  # TileService điều khiển nút bấm (Dual Mode)
```

---

## 📄 Giấy phép

Mã nguồn được phân phối dưới giấy phép **MIT License**. Tác giả: `nvkhang`.
