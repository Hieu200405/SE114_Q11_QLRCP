import requests
import json

# Base URL của API
BASE_URL = "http://localhost:5000/api/rooms"
CINEMA_BASE_URL = "http://localhost:5000/api/cinemas"

def test_update_room_cinema():
    """Test API chuyển phòng sang rạp khác"""
    
    print("=" * 60)
    print("TEST API CHUYỂN PHÒNG SANG RẠP KHÁC")
    print("=" * 60)
    
    # Bước 1: Lấy danh sách rạp
    print("\n1. Lấy danh sách rạp...")
    try:
        response = requests.get(f"{CINEMA_BASE_URL}/get_all")
        if response.status_code == 200:
            cinemas = response.json()
            if len(cinemas) >= 2:
                print(f"✓ Tìm thấy {len(cinemas)} rạp")
                print("\nDanh sách rạp:")
                for cinema in cinemas[:5]:
                    print(f"  - ID: {cinema['ID']}, Name: {cinema['Name']}, Address: {cinema.get('Address', 'N/A')}")
                
                cinema_1_id = cinemas[0]['ID']
                cinema_2_id = cinemas[1]['ID'] if len(cinemas) > 1 else cinemas[0]['ID']
            else:
                print("✗ Cần ít nhất 2 rạp để test. Chỉ có thể test với 1 rạp")
                if len(cinemas) > 0:
                    cinema_1_id = cinemas[0]['ID']
                    cinema_2_id = cinemas[0]['ID']
                else:
                    print("✗ Không có rạp nào trong hệ thống")
                    return
        else:
            print(f"✗ Không thể lấy danh sách rạp")
            return
    except Exception as e:
        print(f"✗ Lỗi kết nối: {e}")
        print("  Hãy chắc chắn server đang chạy tại http://localhost:5000")
        return
    
    # Bước 2: Tạo phòng mới thuộc rạp 1
    print(f"\n2. Tạo phòng mới thuộc rạp ID: {cinema_1_id}...")
    import random
    random_num = random.randint(1000, 9999)
    new_room_data = {
        "name": f"Test Room Cinema {random_num}",
        "seats": 60,
        "cinema_id": cinema_1_id
    }
    
    try:
        create_response = requests.post(f"{BASE_URL}/create", json=new_room_data)
        if create_response.status_code == 201:
            created_room = create_response.json()
            test_room_id = created_room['ID']
            print(f"✓ Đã tạo phòng ID: {test_room_id}")
            print(f"  Tên: {created_room['Name']}")
            print(f"  Thuộc rạp ID: {created_room['CinemaID']}")
        else:
            print(f"✗ Không thể tạo phòng mới: {create_response.json()}")
            return
    except Exception as e:
        print(f"✗ Lỗi: {e}")
        return
    
    # Bước 3: Kiểm tra thông tin phòng với cinema
    print(f"\n3. Kiểm tra thông tin phòng với cinema (ID: {test_room_id})...")
    try:
        response = requests.get(f"{BASE_URL}/get-with-cinema/{test_room_id}")
        if response.status_code == 200:
            room_info = response.json()
            print(f"   Phòng: {room_info['Name']}")
            if 'Cinema' in room_info:
                print(f"   Rạp hiện tại: {room_info['Cinema']['Name']} (ID: {room_info['Cinema']['ID']})")
            else:
                print(f"   Rạp hiện tại: ID {room_info['CinemaID']}")
        else:
            print(f"   ⚠ Không thể lấy thông tin: {response.json()}")
    except Exception as e:
        print(f"   ✗ Lỗi: {e}")
    
    # Bước 4: Chuyển phòng sang rạp khác
    print(f"\n4. Chuyển phòng sang rạp ID: {cinema_2_id}...")
    update_cinema_data = {
        "cinema_id": cinema_2_id
    }
    try:
        response = requests.put(f"{BASE_URL}/{test_room_id}/assign-cinema", json=update_cinema_data)
        print(f"   Status Code: {response.status_code}")
        print(f"   Response: {json.dumps(response.json(), indent=2, ensure_ascii=False)}")
        
        if response.status_code == 200:
            updated_room = response.json()
            if updated_room['CinemaID'] == cinema_2_id:
                print(f"   ✓ Chuyển phòng sang rạp thành công!")
                print(f"   Rạp mới: ID {cinema_2_id}")
            else:
                print("   ✗ Cinema ID không được cập nhật đúng")
        else:
            print(f"   ✗ Lỗi khi chuyển rạp: {response.json()}")
    except Exception as e:
        print(f"   ✗ Lỗi: {e}")
    
    # Bước 5: Xác nhận thay đổi
    print(f"\n5. Xác nhận thay đổi...")
    try:
        response = requests.get(f"{BASE_URL}/get-with-cinema/{test_room_id}")
        if response.status_code == 200:
            room_info = response.json()
            print(f"   Phòng: {room_info['Name']}")
            if 'Cinema' in room_info:
                print(f"   Rạp sau khi cập nhật: {room_info['Cinema']['Name']} (ID: {room_info['Cinema']['ID']})")
                if room_info['Cinema']['ID'] == cinema_2_id:
                    print("   ✓ Xác nhận: Phòng đã được chuyển sang rạp mới!")
                else:
                    print("   ✗ Cảnh báo: Cinema ID không khớp")
            else:
                print(f"   Rạp sau khi cập nhật: ID {room_info['CinemaID']}")
        else:
            print(f"   ⚠ Không thể xác nhận: {response.json()}")
    except Exception as e:
        print(f"   ✗ Lỗi: {e}")
    
    # Bước 6: Test chuyển về rạp ban đầu
    print(f"\n6. Test chuyển về rạp ban đầu (ID: {cinema_1_id})...")
    update_cinema_data = {
        "cinema_id": cinema_1_id
    }
    try:
        response = requests.put(f"{BASE_URL}/{test_room_id}/assign-cinema", json=update_cinema_data)
        if response.status_code == 200:
            print(f"   ✓ Chuyển về rạp ban đầu thành công!")
        else:
            print(f"   ✗ Lỗi: {response.json()}")
    except Exception as e:
        print(f"   ✗ Lỗi: {e}")
    
    # Bước 7: Test với cinema_id không hợp lệ
    print(f"\n7. Test với cinema_id không hợp lệ (ID: 99999)...")
    update_cinema_data = {
        "cinema_id": 99999
    }
    try:
        response = requests.put(f"{BASE_URL}/{test_room_id}/assign-cinema", json=update_cinema_data)
        print(f"   Status Code: {response.status_code}")
        print(f"   Response: {json.dumps(response.json(), indent=2, ensure_ascii=False)}")
        
        if response.status_code in [400, 404]:
            print("   ✓ API xử lý lỗi cinema không tồn tại đúng")
        else:
            print("   ⚠ API không validate cinema_id")
    except Exception as e:
        print(f"   ✗ Lỗi: {e}")
    
    # Bước 8: Test với cinema_id = null
    print(f"\n8. Test gỡ phòng khỏi rạp (cinema_id = null)...")
    update_cinema_data = {
        "cinema_id": None
    }
    try:
        response = requests.put(f"{BASE_URL}/{test_room_id}/assign-cinema", json=update_cinema_data)
        print(f"   Status Code: {response.status_code}")
        print(f"   Response: {json.dumps(response.json(), indent=2, ensure_ascii=False)}")
        
        if response.status_code == 200:
            updated_room = response.json()
            if updated_room['CinemaID'] is None:
                print("   ✓ Gỡ phòng khỏi rạp thành công!")
            else:
                print("   ⚠ Cinema ID vẫn còn giá trị")
        else:
            print(f"   ⚠ API không cho phép cinema_id = null")
    except Exception as e:
        print(f"   ✗ Lỗi: {e}")
    
    # Bước 9: Xóa phòng test
    print(f"\n9. Xóa phòng test (ID: {test_room_id})...")
    try:
        response = requests.delete(f"{BASE_URL}/delete/{test_room_id}")
        if response.status_code == 200:
            print("   ✓ Đã xóa phòng test thành công")
        else:
            print(f"   ⚠ Không thể xóa phòng: {response.json()}")
    except Exception as e:
        print(f"   ✗ Lỗi: {e}")
    
    print("\n" + "=" * 60)
    print("HOÀN THÀNH TEST")
    print("=" * 60)


if __name__ == "__main__":
    test_update_room_cinema()
