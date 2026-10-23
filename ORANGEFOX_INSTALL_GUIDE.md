# Hướng Dẫn Cài Đặt Vĩnh Viễn Qua OrangeFox Recovery (Privileged System App)

> **Mục tiêu:** Cài đặt `ShopeeMuteTile` trực tiếp vào phân vùng hệ thống (`/system/priv-app/`) bằng cách boot tạm thời OrangeFox Recovery (không cần flash đè Recovery gốc).  
> **Lợi ích:** 
> * ✅ **Hoạt động vĩnh viễn:** Khởi động lại điện thoại (Reboot) nút vẫn hoạt động ngay lập tức, không bao giờ bị xám hay mất kết nối.
> * ✅ **Không cần cắm cáp máy tính lại:** Tự động điều khiển âm thanh qua API hệ thống nội bộ, không cần chạy daemon hay mở cổng socket 19876.
> * ✅ **An toàn & Sạch sẽ:** Không thay đổi Recovery gốc của điện thoại, không cần cài Root (Magisk).

---

## 🛠️ 1. Chuẩn Bị Trên Máy Tính

### 1.1. Cài đặt ADB & Fastboot
* **Trên Ubuntu / Debian:**
  ```bash
  sudo apt update && sudo apt install -y android-tools-adb android-tools-fastboot
  ```
* **Trên Windows / macOS:**  
  Tải [Android SDK Platform Tools](https://developer.android.com/tools/releases/platform-tools) chính thức từ Google và giải nén.

### 1.2. Clone Kho Lưu Trữ (Repo)
```bash
git clone https://github.com/NguyenVinhKhang/shopee_mute_tile.git
cd shopee_mute_tile
```

### 1.3. Tải File Recovery OrangeFox cho Redmi Note 7 Pro (`violet`)
1. Truy cập trang tải chính thức của OrangeFox cho `violet`:  
   👉 **[OrangeFox Recovery for Redmi Note 7 Pro (violet)](https://orangefox.download/device/violet)**
2. Tải bản mới nhất (file `.zip`, ví dụ: `OrangeFox-R11.1_...-violet.zip`).
3. Giải nén file `.zip` đó ra để lấy file **`recovery.img`**.
4. Chép file `recovery.img` vào thẳng thư mục dự án `shopee_mute_tile`.

---

## 🚀 2. Các Bước Thực Hiện Chi Tiết

### Bước 1: Gỡ bản App cũ đã cài qua ADB thường (nếu có)
Nếu trên máy đang có bản app cài thông thường dạng User App, hãy gỡ sạch trước:
```bash
adb uninstall com.nvkhang.shopeemutetile
```

### Bước 2: Đưa điện thoại vào chế độ Fastboot
Kết nối điện thoại với máy tính qua cáp USB và chạy:
```bash
adb reboot bootloader
```
*(Hoặc tắt hẳn nguồn điện thoại, sau đó nhấn giữ đồng thời **Nguồn + Giảm âm lượng** cho đến khi màn hình hiện logo FASTBOOT màu cam/thỏ sửa robot).*

Kiểm tra máy tính đã nhận Fastboot chưa:
```bash
fastboot devices
```
*(Nếu hiện mã serial là đã kết nối thành công).*

---

### Bước 3: Boot tạm thời vào OrangeFox (Không Flash)
Chạy lệnh sau trên terminal máy tính:
```bash
fastboot boot recovery.img
```
* **Ý nghĩa:** Lệnh này tải trực tiếp kernel của OrangeFox vào bộ nhớ RAM của điện thoại để khởi động 1 lần duy nhất mà **không chạm vào phân vùng Recovery gốc**.
* *Lưu ý:* Chờ khoảng 10 - 20 giây, màn hình điện thoại sẽ sáng lên logo màu cam của OrangeFox Recovery.

> [!NOTE]
> Nếu máy báo lỗi `FAILED (remote: unknown command)` do Bootloader chặn lệnh `boot`, bạn có thể flash recovery bằng lệnh:
> ```bash
> fastboot flash recovery recovery.img
> fastboot reboot recovery
> ```

---

### Bước 4: Mount phân vùng Hệ Thống (`/system`) trong OrangeFox
Khi điện thoại đã vào giao diện chính của OrangeFox:
1. Nhìn thanh menu dưới cùng, chọn biểu tượng **Menu (3 dấu gạch ngang)**.
2. Chọn mục **Mount (Gắn kết)**.
3. Tìm dòng **System** (hoặc *System Image* / *Vendor*) và **tích chọn vào ô vuông System**.
4. *(Đảm bảo KHÔNG tích vào ô "Mount system partition read-only" để cho phép máy tính ghi dữ liệu).*

---

### Bước 5: Chạy Script cài đặt tự động từ máy tính
Ngay tại thư mục `shopee_mute_tile` trên máy tính, chạy script đi kèm:
```bash
./scripts/install_priv_app.sh
```

#### Những việc script sẽ tự động thực hiện qua ADB Root:
1. Tạo thư mục hệ thống: `/system/priv-app/ShopeeMuteTile/`.
2. Đẩy file `ShopeeMuteTile.apk` vào `/system/priv-app/ShopeeMuteTile/` và set quyền `644`.
3. Đẩy file `privapp-permissions-shopeemute.xml` vào `/system/etc/permissions/` để cấp đặc quyền `MANAGE_APP_OPS_MODES`.

*(Nếu muốn gõ lệnh tay thủ công thay vì chạy script, xem phụ lục ở mục 5 bên dưới).*

---

### Bước 6: Khởi động lại điện thoại vào hệ thống
Sau khi script báo thành công, chạy lệnh:
```bash
adb reboot
```
Điện thoại sẽ tự động khởi động lại vào hệ điều hành MIUI bình thường.

---

### Bước 7: Kích hoạt nút trong Trung tâm điều khiển
1. Vuốt mở **Trung tâm điều khiển (Control Center)** trên Redmi Note 7 Pro.
2. Nhấn vào biểu tượng **Chỉnh sửa / Sắp xếp nút (hình cây bút ở góc trên bên phải)**.
3. Kéo xuống phần các nút chưa dùng ở bên dưới, tìm nút **`Mute Shopee`** (hoặc `Shopee: Tắt âm`).
4. Nhấn dấu `+` để đưa nút lên vị trí thuận tay nhất của bạn.
5. Nhấn **Xong (Done)**.

---

## 🎯 3. Kiểm Tra & Tận Hưởng

* **Chạm vào nút:** Nút lập tức chuyển đổi giữa **Shopee: Tắt âm** (sáng đèn) và **Shopee: Bật âm** (tắt đèn) kèm thông báo Toast.
* **Test tắt nguồn / Reboot:**
  * Thử khởi động lại điện thoại (`adb reboot` hoặc giữ nút nguồn chọn Khởi động lại).
  * Khi máy lên, vuốt Control Center xuống &rarr; **Nút bấm hoạt động ngay lập tức, không bao giờ bị xám nữa!**

---

## 🗑️ 4. Cách Gỡ Bỏ Hoàn Toàn (Khi không muốn dùng nữa)

Nếu sau này bạn muốn gỡ sạch ứng dụng khỏi hệ thống:
1. Boot vào OrangeFox và Mount `/system` như Bước 4.
2. Chạy lệnh từ máy tính:
   ```bash
   adb shell "rm -rf /system/priv-app/ShopeeMuteTile"
   adb shell "rm -f /system/etc/permissions/privapp-permissions-shopeemute.xml"
   adb reboot
   ```

---

## 📖 5. Phụ Lục: Các Lệnh Thủ Công (Dành Cho Ai Muốn Tự Gõ)

```bash
# Kiểm tra ADB root trong OrangeFox
adb shell id

# Remount phân vùng hệ thống cho phép ghi
adb shell "mount -o rw,remount /system || mount -o rw,remount /system_root || mount -o rw,remount /"

# Tạo thư mục và chép APK
adb shell "mkdir -p /system/priv-app/ShopeeMuteTile"
adb push ShopeeMuteTile.apk /system/priv-app/ShopeeMuteTile/ShopeeMuteTile.apk
adb shell "chmod 755 /system/priv-app/ShopeeMuteTile"
adb shell "chmod 644 /system/priv-app/ShopeeMuteTile/ShopeeMuteTile.apk"

# Chép file cấu hình phân quyền hệ thống
adb push scripts/privapp-permissions-shopeemute.xml /system/etc/permissions/privapp-permissions-shopeemute.xml
adb shell "chmod 644 /system/etc/permissions/privapp-permissions-shopeemute.xml"

# Khởi động lại
adb reboot
```
