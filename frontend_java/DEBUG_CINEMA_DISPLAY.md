# Debug Guide - Cinema Info Display Issues

## Vấn đề hiện tại
1. ❌ Thông tin rạp chiếu không hiển thị trong item room
2. ❌ Thông tin rạp chiếu không hiển thị trong item broadcast  
3. ❌ Spinner trong EditRoom không hiển thị rạp tương ứng

## Các thay đổi đã thực hiện để fix

### 1. Fix AdminActivityEditRoom - Spinner không hiển thị đúng cinema
**Vấn đề:** `loadCinemas()` được gọi trước khi `currentCinemaId` được set từ Intent
**Fix:** Đổi thứ tự - set `currentCinemaId` trước, gọi `loadCinemas()` sau

```java
// BEFORE (SAI)
loadCinemas();          // Setup spinner trước
setListeners(roomId);
// currentCinemaId được set trong onCreate nhưng spinner đã được setup

// AFTER (ĐÚNG)  
// Set currentCinemaId từ Intent trước
if (roomResponse.getCinemaId() != null) {
    currentCinemaId = roomResponse.getCinemaId();
    selectedCinemaId = currentCinemaId;
}
setListeners(roomId);
loadCinemas();          // Setup spinner sau khi đã có currentCinemaId
```

### 2. Fix RoomAdapter - Hiển thị thông tin cinema
**Vấn đề:** Không xử lý trường hợp cinema chưa có trong cache
**Fix:** Thêm fallback hiển thị Cinema ID nếu không tìm thấy trong cache

```java
// AFTER
if (room.getCinemaId() != null && room.getCinemaId() > 0) {
    Cinema cinema = CinemaCache.getCinemaById(room.getCinemaId());
    if (cinema != null) {
        tvCinemaInfo.setText("🎬 " + cinema.getName());
        tvCinemaInfo.setVisibility(View.VISIBLE);
    } else {
        // Fallback: hiển thị ID nếu không tìm thấy
        tvCinemaInfo.setText("🎬 Rạp ID: " + room.getCinemaId());
        tvCinemaInfo.setVisibility(View.VISIBLE);
        Log.w("RoomAdapter", "Cinema not found in cache for ID: " + room.getCinemaId());
    }
} else {
    tvCinemaInfo.setVisibility(View.GONE);
}
```

### 3. Fix AdminActivityManageRoom - Thêm logging
**Vấn đề:** Khó debug khi không biết cache status
**Fix:** Thêm logging để track cache status và room data

```java
// Log cinema cache status
if (CinemaCache.isCached()) {
    Log.d(TAG, "Cinema cache ready with " + CinemaCache.getCachedCinemas().size() + " cinemas");
} else {
    Log.w(TAG, "Warning: Cinema cache not ready!");
}

// Log room cinema IDs
for (RoomResponse room : roomList) {
    if (room.getCinemaId() != null) {
        Log.d(TAG, "Room " + room.getName() + " -> Cinema ID: " + room.getCinemaId());
    } else {
        Log.w(TAG, "Room " + room.getName() + " has no cinema assigned");
    }
}
```

## Cách test để verify fix

### Test 1: AdminActivityManageRoom - Hiển thị cinema trong room list
1. Mở AdminActivityManageRoom
2. Check Logcat:
   - Tìm: `Cinema cache ready with X cinemas`
   - Tìm: `Room [name] -> Cinema ID: Y`
3. Verify UI: Mỗi room phải hiển thị `🎬 [Cinema Name]` hoặc `🎬 Rạp ID: X`

**Expected logs:**
```
D/AdminActivityManageRoom: Cinema cache ready with 5 cinemas
D/AdminActivityManageRoom: Room Phòng 1 -> Cinema ID: 1
D/AdminActivityManageRoom: Room Phòng 2 -> Cinema ID: 2
D/RoomAdapter: Cinema not found in cache for ID: 99  // Nếu có room với cinema không tồn tại
```

### Test 2: AdminActivityEditRoom - Spinner hiển thị đúng cinema
1. Từ ManageRoom, click Edit một room đã có cinema
2. Verify: Spinner phải hiển thị đúng tên cinema hiện tại (không phải "-- Chọn rạp chiếu phim --")
3. Verify: Hiển thị thông tin cinema (address, phone) bên dưới spinner

**Expected behavior:**
- Spinner selected: Tên cinema của room (vd: "CGV Vincom")
- Cinema info visible: Address và Phone được hiển thị

### Test 3: Broadcast - Hiển thị cinema info
1. Mở UserShowListBroadcast hoặc AdminActivityListBroadcast
2. Check Logcat:
   - Tìm: `BroadcastCinemaEnricher: Enriched broadcast X with cinema: Y`
   - Tìm: `BroadcastCinemaEnricher: Cinema not found in cache for ID: Z`
3. Verify UI: Mỗi broadcast phải hiển thị:
   - `🎬 [Cinema Name]` (nếu có)
   - `• [Distance]` (nếu có)
   - `(~[Duration])` (nếu có)
   - Nút Navigate (nếu có lat/lng)

**Expected logs:**
```
D/BroadcastCinemaEnricher: Enriched broadcast 1 with cinema: CGV Vincom
D/BroadcastCinemaEnricher: Enriched broadcast 2 with cinema: Lotte Cinema
W/BroadcastCinemaEnricher: Cinema not found in cache for ID: 99
```

## Các trường hợp edge case cần test

### Case 1: Room không có cinema
- Room.cinemaId = null
- Expected: `tvCinemaInfo` bị ẩn (GONE)
- Log: `Room [name] has no cinema assigned`

### Case 2: Room có cinemaId nhưng cinema không tồn tại trong cache
- Room.cinemaId = 999 (không tồn tại)
- Expected: Hiển thị `🎬 Rạp ID: 999`
- Log: `Cinema not found in cache for ID: 999`

### Case 3: Cinema cache chưa load
- Scenario: Load rooms trước khi cinema cache ready (race condition)
- Expected: Vẫn hiển thị fallback `🎬 Rạp ID: X`
- Log: `Warning: Cinema cache not ready!`

### Case 4: Broadcast không có room hoặc cinema
- Broadcast -> Room không tìm thấy
- Expected: `textCinemaInfo` bị ẩn (GONE)
- Log: `Failed to load room X`

## Kiểm tra Layout

### admin_item_room.xml
- ✅ TextView `tvCinemaInfo` đã được thêm
- ✅ Constraints đúng: below tvSeats, before buttonEdit
- ✅ Visibility mặc định: visible (sẽ được set GONE trong code nếu cần)
- ✅ Color: #4CAF50 (green)

### item_broadcast_film.xml
- ✅ TextView `textCinemaInfo` đã có sẵn
- ✅ Position: below textRoomSeats
- ✅ Icon: 🎬
- ✅ btnNavigate: Aligned to end, centered vertically

## Checklist hoàn thành

- [x] Fix AdminActivityEditRoom order (currentCinemaId before loadCinemas)
- [x] Fix RoomAdapter cinema display with fallback
- [x] Add logging to AdminActivityManageRoom
- [x] Verify ApiRoomService.getRoomById exists
- [x] Verify layout constraints are correct
- [ ] **TODO: Test trên device/emulator**
- [ ] **TODO: Check logs để verify cache loading**
- [ ] **TODO: Verify spinner EditRoom hiển thị đúng**
- [ ] **TODO: Verify broadcast cinema info hiển thị đúng**

## Nếu vẫn còn lỗi

### Scenario A: Cinema cache bị null
**Triệu chứng:** Log shows "Cinema cache not ready!"
**Nguyên nhân:** API getAllCinemas() fail hoặc response null
**Fix:** 
1. Check backend có chạy không
2. Check API endpoint `/api/cinemas/get_all`
3. Check network logs trong Logcat

### Scenario B: Room.cinemaId là null
**Triệu chứng:** Log shows "Room X has no cinema assigned"
**Nguyên nhân:** Room chưa được gán cinema
**Fix:**
1. Vào AdminActivityEditRoom
2. Chọn cinema cho room
3. Save

### Scenario C: Layout bị overlap
**Triệu chứng:** Text bị chồng lên nhau
**Nguyên nhân:** Constraints sai
**Fix:**
1. Check constraints trong XML
2. Đảm bảo `layout_constraintTop_toBottomOf` đúng
3. Thêm margin nếu cần

### Scenario D: BroadcastCinemaEnricher không được gọi
**Triệu chứng:** Không có log "Enriched broadcast..."
**Nguyên nhân:** UserShowListBroadcast/AdminActivityListBroadcast chưa gọi enrichment
**Fix:** 
- Verify đã import BroadcastCinemaEnricher
- Verify loadListBroadcast có gọi enrichBroadcastsWithCinemaInfo

## Logs quan trọng cần tìm

```bash
# Filter logs trong Android Studio Logcat:

# Cinema Cache
adb logcat | grep -E "CinemaCache|AdminActivityManageRoom"

# Broadcast Enrichment  
adb logcat | grep -E "BroadcastCinemaEnricher|UserShowListBroadcast"

# Room Adapter
adb logcat | grep "RoomAdapter"

# All related
adb logcat | grep -E "Cinema|Room|Broadcast" | grep -E "cache|enrich|load"
```

## Kết luận

Các fix đã thực hiện:
1. ✅ Fix timing issue trong AdminActivityEditRoom
2. ✅ Add fallback display trong RoomAdapter
3. ✅ Add extensive logging cho debugging
4. ✅ Verify API endpoints tồn tại

**Next steps:**
- Build & Run app
- Check logs
- Verify UI hiển thị đúng
- Report lại nếu vẫn có vấn đề

