# Tích hợp Goong Map API - Quản lý Rạp Chiếu Phim

## Tổng quan

Đã tích hợp chức năng quản lý địa điểm rạp chiếu phim với Goong Map API để:
- Hiển thị danh sách rạp chiếu phim
- Tính khoảng cách thực tế từ vị trí người dùng đến các rạp (Distance Matrix API)
- Chọn vị trí trên bản đồ (Map Picker với Mapbox SDK + Goong tiles)
- Tìm kiếm địa chỉ với autocomplete (Places API - qua Backend)
- Điều hướng đến rạp chiếu phim
- Upload ảnh rạp qua Cloudinary

## ⚠️ Bảo mật API Keys

### Nguyên tắc quan trọng:
- **GOONG_SERVICE_KEY**: CHỈ đặt ở Backend Flask (file `.env`), KHÔNG bao giờ đưa vào Frontend
- **GOONG_MAP_KEY**: Có thể đặt ở Frontend cho Map tiles (đã được restrict theo package name)

### Luồng hoạt động an toàn:
```
Frontend (Android) → Backend (Flask) → Goong API
                     ↑ API keys ở đây
```

Frontend chỉ gọi Backend endpoints, Backend mới gọi Goong API với API keys.

## Cấu hình API Keys

### Frontend (`local.properties`):
```properties
# Chỉ cần Map key cho hiển thị tiles
GOONG_MAP_KEY=6VHjsUG8uZPGmmT9gf59pRMQG8UbM8pRLUXb44pE
```

### Backend Flask (`.env`):
```properties
GOONG_MAP_KEY=6VHjsUG8uZPGmmT9gf59pRMQG8UbM8pRLUXb44pE
GOONG_SERVICE_KEY=o6zS44bMPEBonYrqtIZyXNaWQtCv5O6LTQEJEN0e
```

## Files đã tạo/sửa đổi

### Models (app/src/main/java/com/example/myapplication/models/)
- `Cinema.java` - Model cho rạp chiếu phim với thông tin khoảng cách
- `CinemaRequest.java` - Request tạo/cập nhật rạp
- `NearbyCinemaRequest.java` - Request lấy rạp gần vị trí
- `DistanceRequest.java` - Request tính khoảng cách
- `DistanceResponse.java` - Response khoảng cách
- `PlaceAutocomplete.java` - Model cho Goong Places Autocomplete
- `PlaceDetail.java` - Model chi tiết địa điểm
- `MapConfig.java` - Cấu hình map
- `RoomResponse.java` - Đã thêm cinemaId
- `RoomRequest.java` - Đã thêm cinema_id

### Network (app/src/main/java/com/example/myapplication/network/)
- `ApiCinemaService.java` - API service cho Cinema operations
- `ApiRoomService.java` - Đã thêm endpoints liên quan đến cinema

### Adapters (app/src/main/java/com/example/myapplication/adapters/)
- `CinemaAdapter.java` - Adapter hiển thị danh sách rạp
- `PlaceAutocompleteAdapter.java` - Adapter cho gợi ý địa chỉ

### Helper (app/src/main/java/com/example/myapplication/helper/)
- `LocationHelper.java` - Helper xử lý vị trí GPS

### Activities (app/src/main/java/com/example/myapplication/activities/)
- `CinemaListActivity.java` - Danh sách rạp chiếu phim
- `CinemaFormActivity.java` - Form thêm/sửa rạp
- `CinemaDetailActivity.java` - Chi tiết rạp
- `AdminMainActivity.java` - Đã thêm nút quản lý rạp

### Layouts (app/src/main/res/layout/)
- `activity_cinema_list.xml` - Layout danh sách rạp
- `activity_cinema_form.xml` - Layout form rạp
- `activity_cinema_detail.xml` - Layout chi tiết rạp
- `item_cinema.xml` - Layout item rạp
- `item_place_autocomplete.xml` - Layout item gợi ý địa chỉ
- `admin_activity_main.xml` - Đã thêm icon quản lý rạp

### Drawables (app/src/main/res/drawable/)
- `bg_distance_tag.xml` - Background tag khoảng cách
- `bg_duration_tag.xml` - Background tag thời gian
- `bg_circle_button.xml` - Background nút tròn
- `ic_navigate.xml` - Icon điều hướng
- `ic_phone.xml` - Icon điện thoại
- `ic_cinema.xml` - Icon rạp chiếu phim

### Configuration
- `AndroidManifest.xml` - Đã thêm permissions và activities
- `build.gradle.kts` - Đã thêm Google Play Services Location

## API Endpoints (Backend Flask)

### CRUD Operations
- `GET /api/cinemas/get_all` - Lấy tất cả rạp
- `GET /api/cinemas/get/<id>` - Lấy rạp theo ID
- `POST /api/cinemas/create` - Tạo rạp mới
- `PUT /api/cinemas/update/<id>` - Cập nhật rạp
- `DELETE /api/cinemas/delete/<id>` - Xóa rạp

### Location & Distance
- `POST /api/cinemas/nearby` - Lấy rạp gần vị trí
- `POST /api/cinemas/for-film/<film_id>` - Lấy rạp đang chiếu phim
- `POST /api/cinemas/distance` - Tính khoảng cách

### Goong Map Utilities
- `GET /api/cinemas/search-places` - Tìm địa chỉ autocomplete
- `GET /api/cinemas/place-detail` - Chi tiết địa điểm
- `GET /api/cinemas/geocode` - Địa chỉ → Tọa độ
- `GET /api/cinemas/reverse-geocode` - Tọa độ → Địa chỉ
- `GET /api/cinemas/map-config` - Lấy map key

## Cách sử dụng

### Admin - Quản lý rạp
1. Đăng nhập với tài khoản Admin
2. Nhấn vào icon rạp chiếu phim (🎬) ở menu dưới
3. Có thể:
   - Xem danh sách rạp với khoảng cách
   - Thêm rạp mới (FAB +)
   - Sửa/Xóa rạp (swipe hoặc nhấn vào icon)
   - Điều hướng đến rạp (nhấn icon navigate)

### User - Xem rạp gần nhất
1. Khi chọn phim, hệ thống sẽ hiển thị danh sách rạp đang chiếu
2. Rạp được sắp xếp theo khoảng cách thực tế (từ gần đến xa)
3. Nhấn vào rạp để xem chi tiết và điều hướng

## Permissions cần thiết
- `ACCESS_FINE_LOCATION` - Vị trí chính xác GPS
- `ACCESS_COARSE_LOCATION` - Vị trí thô
- `INTERNET` - Kết nối mạng

## Dependencies
```kotlin
implementation ("com.google.android.gms:play-services-location:21.0.1")
```

## Lưu ý
- Cần sync Gradle sau khi thêm dependencies
- Đảm bảo bật GPS trên thiết bị để tính khoảng cách chính xác
- API key Goong có giới hạn request/ngày

## Sau khi triển khai

### Bước 1: Sync Gradle
Trong Android Studio, nhấn **Sync Now** hoặc **File > Sync Project with Gradle Files**

### Bước 2: Build Project
**Build > Rebuild Project** để compile tất cả các file mới

### Bước 3: Chạy ứng dụng
- Đảm bảo thiết bị/emulator có Google Play Services
- Cho phép quyền truy cập vị trí khi được hỏi

### Bước 4: Backend Flask
Đảm bảo backend Flask đã có các endpoint theo file `cinema_routes.py` đã được cung cấp

## Troubleshooting

### Lỗi "Cannot resolve symbol"
- Sync Gradle và Rebuild Project
- Invalidate Caches: **File > Invalidate Caches / Restart**

### Lỗi GPS không hoạt động
- Kiểm tra quyền trong Settings > Apps > [App Name] > Permissions
- Bật Location Services trên thiết bị

### Lỗi API call failed
- Kiểm tra kết nối mạng
- Kiểm tra BASE_URL trong local.properties
- Kiểm tra Goong API keys còn hiệu lực
