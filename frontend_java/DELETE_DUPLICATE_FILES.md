# ⚠️ ACTION REQUIRED - XÓA FILE TRÙNG LẶP

## Các file cần XÓA NGAY:

1. `app/src/main/res/drawable/ic_add_photo.xml` 
2. `app/src/main/res/drawable/ic_close.xml`

## Lý do:
Các file này bị TRÙNG tên với file `.png` đã tồn tại sẵn, gây lỗi build:
```
Duplicate resources
[drawable/ic_add_photo] ic_add_photo.png ic_add_photo.xml: Error: Duplicate resources
```

## Thay thế:
- ✅ `ic_add_photo.xml` → dùng `ic_add_photo.png` (đã có sẵn)
- ✅ `ic_close.xml` → dùng `ic_clear.xml` (mới tạo, đã update layout)

## Cách xóa:

### Cách 1: Trong Android Studio (Khuyến nghị)
1. Mở Android Studio
2. Vào thư mục `app/src/main/res/drawable/`
3. Click phải vào `ic_add_photo.xml` → Delete
4. Click phải vào `ic_close.xml` → Delete
5. Sync Gradle

### Cách 2: PowerShell
```powershell
Remove-Item "D:\1. UIT\HK5\SE114.Q11\SE114_Q11_QLRCP\frontend_java\app\src\main\res\drawable\ic_add_photo.xml"
Remove-Item "D:\1. UIT\HK5\SE114.Q11\SE114_Q11_QLRCP\frontend_java\app\src\main\res\drawable\ic_close.xml"
```

## Sau khi xóa:
1. Sync Gradle
2. Build lại project
3. Test app

---

## ✅ Đã sửa các lỗi khác:

### 1. Lỗi Google Maps không tìm thấy đường (Locale Issue)
**Vấn đề**: Khi format tọa độ `10.7769, 106.7009`, Java dùng locale Tiếng Việt nên ra `10,7769, 106,7009` (dấu phẩy). Google Maps yêu cầu dấu chấm.

**Đã sửa**:
- `LocationHelper.java`: openNavigationApp() - dùng `Locale.US`
- `LocationHelper.java`: formatDistance() - dùng `Locale.US`  
- `MapPickerActivity.java`: updateSelectedLocationDisplay() - dùng `Locale.US`
- `CinemaFormActivity.java`: Tất cả String.valueOf(double) → String.format(Locale.US, ...)

### 2. Lỗi Map Picker mở liên tục khi Edit Cinema
**Vấn đề**: EditText latitude/longitude có onClick listener, khi populate data trigger click event.

**Đã sửa**:
- Set `etLatitude.setFocusable(false)` và `etLongitude.setFocusable(false)`
- Chỉ cho click để mở map picker, không cho nhập trực tiếp


