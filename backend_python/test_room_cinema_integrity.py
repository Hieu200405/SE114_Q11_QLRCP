import requests
import json

# Base URL của API
BASE_URL = "http://localhost:5000/api/rooms"

def test_room_cinema_integrity():
    """Test kiểm tra data integrity của Room 2 và Room 3"""
    
    print("=" * 70)
    print("KIỂM TRA DATA INTEGRITY - ROOM 2 & ROOM 3")
    print("=" * 70)
    
    # Danh sách room cần kiểm tra
    room_ids_to_check = [2, 3, 7, 8, 9]
    
    # Test 1: Get All Rooms và kiểm tra Room 2, 3
    print("\n1. GET ALL ROOMS - Tìm Room 2 và Room 3...")
    print(f"   Endpoint: GET {BASE_URL}/get_all")
    try:
        response = requests.get(f"{BASE_URL}/get_all")
        print(f"   Status Code: {response.status_code}")
        
        if response.status_code == 200:
            all_rooms = response.json()
            print(f"   ✓ Lấy danh sách thành công: {len(all_rooms)} phòng\n")
            
            # Tìm Room 2 và Room 3 trong danh sách
            for room_id in room_ids_to_check:
                room_found = next((r for r in all_rooms if r['ID'] == room_id), None)
                if room_found:
                    cinema_status = f"CinemaID: {room_found.get('CinemaID')}" if room_found.get('CinemaID') is not None else "❌ CinemaID: NULL"
                    status_icon = "✓" if room_found.get('CinemaID') is not None else "❌"
                    print(f"   {status_icon} Room {room_id}: Name='{room_found['Name']}', Seats={room_found['Seats']}, {cinema_status}")
                else:
                    print(f"   ⚠ Room {room_id}: KHÔNG TÌM THẤY trong danh sách")
            
            print("\n   Chi tiết đầy đủ tất cả phòng:")
            for room in all_rooms:
                cinema_val = room.get('CinemaID')
                status = "NULL ❌" if cinema_val is None else cinema_val
                print(f"     - Room {room['ID']}: {room['Name']} | CinemaID: {status}")
        else:
            print(f"   ✗ Lỗi: {response.json()}")
            return
    except Exception as e:
        print(f"   ✗ Lỗi kết nối: {e}")
        return
    
    # Test 2: Get Room by ID chi tiết cho Room 2
    print(f"\n2. GET ROOM BY ID - Room 2...")
    print(f"   Endpoint: GET {BASE_URL}/get/2")
    try:
        response = requests.get(f"{BASE_URL}/get/2")
        print(f"   Status Code: {response.status_code}")
        
        if response.status_code == 200:
            room2 = response.json()
            print("   ✓ Response nhận được:")
            print(f"   {json.dumps(room2, indent=2, ensure_ascii=False)}")
            
            cinema_id = room2.get('CinemaID')
            if cinema_id is not None:
                print(f"\n   ✓ Room 2 CÓ CinemaID: {cinema_id}")
            else:
                print(f"\n   ❌ Room 2 CinemaID = NULL")
        else:
            print(f"   ✗ Lỗi: {response.json()}")
    except Exception as e:
        print(f"   ✗ Lỗi: {e}")
    
    # Test 3: Get Room by ID chi tiết cho Room 3
    print(f"\n3. GET ROOM BY ID - Room 3...")
    print(f"   Endpoint: GET {BASE_URL}/get/3")
    try:
        response = requests.get(f"{BASE_URL}/get/3")
        print(f"   Status Code: {response.status_code}")
        
        if response.status_code == 200:
            room3 = response.json()
            print("   ✓ Response nhận được:")
            print(f"   {json.dumps(room3, indent=2, ensure_ascii=False)}")
            
            cinema_id = room3.get('CinemaID')
            if cinema_id is not None:
                print(f"\n   ✓ Room 3 CÓ CinemaID: {cinema_id}")
            else:
                print(f"\n   ❌ Room 3 CinemaID = NULL")
        else:
            print(f"   ✗ Lỗi: {response.json()}")
    except Exception as e:
        print(f"   ✗ Lỗi: {e}")
    
    # Test 4: Get Room with Cinema Info - Room 2
    print(f"\n4. GET ROOM WITH CINEMA - Room 2...")
    print(f"   Endpoint: GET {BASE_URL}/get-with-cinema/2")
    try:
        response = requests.get(f"{BASE_URL}/get-with-cinema/2")
        print(f"   Status Code: {response.status_code}")
        
        if response.status_code == 200:
            room2_with_cinema = response.json()
            print("   ✓ Response nhận được:")
            print(f"   {json.dumps(room2_with_cinema, indent=2, ensure_ascii=False)}")
            
            cinema_id = room2_with_cinema.get('CinemaID')
            has_cinema_obj = 'Cinema' in room2_with_cinema
            
            if cinema_id is not None:
                print(f"\n   ✓ Room 2 CinemaID: {cinema_id}")
            else:
                print(f"\n   ❌ Room 2 CinemaID: NULL")
                
            if has_cinema_obj:
                print(f"   ✓ Room 2 CÓ Cinema Object: {room2_with_cinema['Cinema']['Name']}")
            else:
                print(f"   ⚠ Room 2 KHÔNG có Cinema Object chi tiết")
        else:
            print(f"   ✗ Lỗi: {response.json()}")
    except Exception as e:
        print(f"   ✗ Lỗi: {e}")
    
    # Test 5: Get Room with Cinema Info - Room 3
    print(f"\n5. GET ROOM WITH CINEMA - Room 3...")
    print(f"   Endpoint: GET {BASE_URL}/get-with-cinema/3")
    try:
        response = requests.get(f"{BASE_URL}/get-with-cinema/3")
        print(f"   Status Code: {response.status_code}")
        
        if response.status_code == 200:
            room3_with_cinema = response.json()
            print("   ✓ Response nhận được:")
            print(f"   {json.dumps(room3_with_cinema, indent=2, ensure_ascii=False)}")
            
            cinema_id = room3_with_cinema.get('CinemaID')
            has_cinema_obj = 'Cinema' in room3_with_cinema
            
            if cinema_id is not None:
                print(f"\n   ✓ Room 3 CinemaID: {cinema_id}")
            else:
                print(f"\n   ❌ Room 3 CinemaID: NULL")
                
            if has_cinema_obj:
                print(f"   ✓ Room 3 CÓ Cinema Object: {room3_with_cinema['Cinema']['Name']}")
            else:
                print(f"   ⚠ Room 3 KHÔNG có Cinema Object chi tiết")
        else:
            print(f"   ✗ Lỗi: {response.json()}")
    except Exception as e:
        print(f"   ✗ Lỗi: {e}")
    
    # Test 6: Kiểm tra trực tiếp database qua raw query
    print(f"\n6. SO SÁNH VỚI DATABASE...")
    print("   Gợi ý: Chạy query SQL sau trong database:")
    print("   ")
    print("   SELECT ID, name, seats, cinema_id FROM room WHERE ID IN (2, 3) AND is_delete = 0;")
    print("   ")
    print("   Hoặc kiểm tra tất cả rooms:")
    print("   SELECT ID, name, seats, cinema_id, is_delete FROM room ORDER BY ID;")
    
    # Summary
    print("\n" + "=" * 70)
    print("TÓM TẮT KẾT QUẢ")
    print("=" * 70)
    
    print("\nKiểm tra từ API:")
    print(f"  Room 2 CinemaID: {room2.get('CinemaID') if 'room2' in locals() else 'Không lấy được'}")
    print(f"  Room 3 CinemaID: {room3.get('CinemaID') if 'room3' in locals() else 'Không lấy được'}")
    
    print("\nNếu API trả về CinemaID = null nhưng DB có data:")
    print("  → Kiểm tra serialize() method trong Room model")
    print("  → Kiểm tra filter query trong service (is_delete flag)")
    print("  → Kiểm tra relationship mapping trong SQLAlchemy")
    
    print("\nNếu API trả về đúng CinemaID:")
    print("  → Vấn đề có thể ở .NET backend đang đọc data cũ")
    print("  → Kiểm tra cache trong .NET application")
    print("  → Kiểm tra connection string của .NET app")
    
    print("=" * 70)


if __name__ == "__main__":
    test_room_cinema_integrity()
