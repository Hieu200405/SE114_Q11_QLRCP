import requests
import json

# Base URL của API
BASE_URL = "http://localhost:5000/api/rooms"

def test_get_rooms():
    """Test API get all rooms và get room by id - kiểm tra trường cinema_id"""
    
    print("=" * 60)
    print("TEST API GET ROOMS - KIỂM TRA TRƯỜNG CINEMA_ID")
    print("=" * 60)
    
    # Test 1: Get all rooms
    print("\n1. Test GET ALL ROOMS...")
    print(f"   Endpoint: GET {BASE_URL}/get_all")
    try:
        response = requests.get(f"{BASE_URL}/get_all")
        print(f"   Status Code: {response.status_code}")
        
        if response.status_code == 200:
            rooms = response.json()
            print(f"   ✓ Lấy danh sách thành công: {len(rooms)} phòng")
            
            if len(rooms) > 0:
                print("\n   Kiểm tra cấu trúc dữ liệu phòng đầu tiên:")
                first_room = rooms[0]
                print(f"   {json.dumps(first_room, indent=2, ensure_ascii=False)}")
                
                # Kiểm tra có trường CinemaID không
                if 'CinemaID' in first_room:
                    print(f"\n   ✓ CÓ trường 'CinemaID': {first_room['CinemaID']}")
                else:
                    print("\n   ✗ KHÔNG có trường 'CinemaID'")
                
                # Hiển thị tất cả các trường
                print("\n   Các trường có trong response:")
                for key in first_room.keys():
                    print(f"     - {key}: {first_room[key]}")
                
                # Hiển thị thêm một vài phòng
                print("\n   Danh sách phòng (top 5):")
                for i, room in enumerate(rooms[:5]):
                    cinema_info = f"CinemaID: {room.get('CinemaID', 'N/A')}" if 'CinemaID' in room else "Không có CinemaID"
                    print(f"     {i+1}. ID: {room['ID']}, Name: {room['Name']}, Seats: {room['Seats']}, {cinema_info}")
            else:
                print("   ⚠ Không có phòng nào trong hệ thống")
        else:
            print(f"   ✗ Lỗi: {response.json()}")
    except Exception as e:
        print(f"   ✗ Lỗi kết nối: {e}")
        print("   Hãy chắc chắn server đang chạy tại http://localhost:5000")
        return
    
    # Test 2: Get room by ID
    if len(rooms) > 0:
        test_room_id = rooms[0]['ID']
        print(f"\n2. Test GET ROOM BY ID (ID: {test_room_id})...")
        print(f"   Endpoint: GET {BASE_URL}/get/{test_room_id}")
        
        try:
            response = requests.get(f"{BASE_URL}/get/{test_room_id}")
            print(f"   Status Code: {response.status_code}")
            
            if response.status_code == 200:
                room = response.json()
                print("   ✓ Lấy thông tin phòng thành công")
                print(f"\n   Chi tiết phòng:")
                print(f"   {json.dumps(room, indent=2, ensure_ascii=False)}")
                
                # Kiểm tra có trường CinemaID không
                if 'CinemaID' in room:
                    print(f"\n   ✓ CÓ trường 'CinemaID': {room['CinemaID']}")
                else:
                    print("\n   ✗ KHÔNG có trường 'CinemaID'")
                
                # Hiển thị tất cả các trường
                print("\n   Các trường có trong response:")
                for key in room.keys():
                    print(f"     - {key}: {room[key]}")
            else:
                print(f"   ✗ Lỗi: {response.json()}")
        except Exception as e:
            print(f"   ✗ Lỗi: {e}")
        
        # Test 3: Get room with cinema info
        print(f"\n3. Test GET ROOM WITH CINEMA INFO (ID: {test_room_id})...")
        print(f"   Endpoint: GET {BASE_URL}/get-with-cinema/{test_room_id}")
        
        try:
            response = requests.get(f"{BASE_URL}/get-with-cinema/{test_room_id}")
            print(f"   Status Code: {response.status_code}")
            
            if response.status_code == 200:
                room = response.json()
                print("   ✓ Lấy thông tin phòng với cinema thành công")
                print(f"\n   Chi tiết phòng (bao gồm thông tin rạp):")
                print(f"   {json.dumps(room, indent=2, ensure_ascii=False)}")
                
                # Kiểm tra có object Cinema không
                if 'Cinema' in room:
                    print(f"\n   ✓ CÓ object 'Cinema':")
                    print(f"     {json.dumps(room['Cinema'], indent=2, ensure_ascii=False)}")
                else:
                    print("\n   ⚠ KHÔNG có object 'Cinema' chi tiết")
                    if 'CinemaID' in room:
                        print(f"     Chỉ có CinemaID: {room['CinemaID']}")
            else:
                print(f"   ✗ Lỗi: {response.json()}")
        except Exception as e:
            print(f"   ✗ Lỗi: {e}")
    
    print("\n" + "=" * 60)
    print("KẾT LUẬN")
    print("=" * 60)
    print("API Get All Rooms:")
    print(f"  - Endpoint: GET {BASE_URL}/get_all")
    print(f"  - Có trường CinemaID: {'✓ CÓ' if 'CinemaID' in first_room else '✗ KHÔNG'}")
    print("\nAPI Get Room by ID:")
    print(f"  - Endpoint: GET {BASE_URL}/get/<room_id>")
    print(f"  - Có trường CinemaID: {'✓ CÓ' if 'CinemaID' in room else '✗ KHÔNG'}")
    print("\nAPI Get Room with Cinema:")
    print(f"  - Endpoint: GET {BASE_URL}/get-with-cinema/<room_id>")
    print(f"  - Có thông tin Cinema đầy đủ: {'✓ CÓ' if 'Cinema' in room else '✗ KHÔNG'}")
    print("=" * 60)


if __name__ == "__main__":
    test_get_rooms()
