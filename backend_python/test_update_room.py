import requests
import json

# Base URL của API
BASE_URL = "http://localhost:5000/api/rooms"

def test_update_room():
    """Test API chỉnh sửa phòng"""
    
    print("=" * 60)
    print("TEST API CHỈNH SỬA PHÒNG (UPDATE ROOM)")
    print("=" * 60)
    
    # Bước 1: Tạo phòng mới để test (tránh phòng có broadcast)
    print("\n1. Tạo phòng mới để test...")
    import random
    random_num = random.randint(1000, 9999)
    new_room_data = {
        "name": f"Test Room {random_num}",
        "seats": 50,
        "cinema_id": 1
    }
    
    try:
        create_response = requests.post(f"{BASE_URL}/create", json=new_room_data)
        if create_response.status_code == 201:
            created_room = create_response.json()
            test_room_id = created_room['ID']
            original_name = created_room['Name']
            original_seats = created_room['Seats']
            print(f"✓ Đã tạo phòng mới với ID: {test_room_id}")
            print(f"  Tên: {original_name}, Số ghế: {original_seats}")
        else:
            print(f"✗ Không thể tạo phòng mới: {create_response.json()}")
            print("  Thử lấy phòng hiện có để test...")
            
            # Fallback: lấy danh sách phòng
            response = requests.get(f"{BASE_URL}/get_all")
            if response.status_code == 200:
                rooms = response.json()
                if len(rooms) > 0:
                    test_room_id = rooms[0]['ID']
                    original_name = rooms[0]['Name']
                    original_seats = rooms[0]['Seats']
                    print(f"✓ Sử dụng phòng ID: {test_room_id}")
                else:
                    print("✗ Không có phòng nào trong hệ thống")
                    return
            else:
                return
    except Exception as e:
        print(f"✗ Lỗi kết nối: {e}")
        print("  Hãy chắc chắn server đang chạy tại http://localhost:5000")
        return
    
    # Bước 2: Test chỉnh sửa phòng - chỉ sửa tên
    print(f"\n2. Test chỉnh sửa tên phòng (ID: {test_room_id})...")
    update_data_name = {
        "name": "Updated Room - Test"
    }
    try:
        response = requests.put(f"{BASE_URL}/update/{test_room_id}", json=update_data_name)
        print(f"   Status Code: {response.status_code}")
        print(f"   Response: {json.dumps(response.json(), indent=2, ensure_ascii=False)}")
        
        if response.status_code == 200:
            updated_room = response.json()
            if updated_room['Name'] == "Updated Room - Test":
                print("   ✓ Chỉnh sửa tên thành công!")
            else:
                print("   ✗ Tên không được cập nhật đúng")
        else:
            print(f"   ✗ Lỗi khi chỉnh sửa: {response.json()}")
    except Exception as e:
        print(f"   ✗ Lỗi: {e}")
    
    # Bước 3: Test chỉnh sửa phòng - chỉ sửa số ghế
    print(f"\n3. Test chỉnh sửa số ghế (ID: {test_room_id})...")
    update_data_seats = {
        "seats": 100
    }
    try:
        response = requests.put(f"{BASE_URL}/update/{test_room_id}", json=update_data_seats)
        print(f"   Status Code: {response.status_code}")
        print(f"   Response: {json.dumps(response.json(), indent=2, ensure_ascii=False)}")
        
        if response.status_code == 200:
            updated_room = response.json()
            if updated_room['Seats'] == 100:
                print("   ✓ Chỉnh sửa số ghế thành công!")
            else:
                print("   ✗ Số ghế không được cập nhật đúng")
        else:
            print(f"   ✗ Lỗi khi chỉnh sửa: {response.json()}")
    except Exception as e:
        print(f"   ✗ Lỗi: {e}")
    
    # Bước 4: Test chỉnh sửa phòng - sửa cả tên và số ghế
    print(f"\n4. Test chỉnh sửa cả tên và số ghế (ID: {test_room_id})...")
    update_data_both = {
        "name": "Fully Updated Room",
        "seats": 75
    }
    try:
        response = requests.put(f"{BASE_URL}/update/{test_room_id}", json=update_data_both)
        print(f"   Status Code: {response.status_code}")
        print(f"   Response: {json.dumps(response.json(), indent=2, ensure_ascii=False)}")
        
        if response.status_code == 200:
            updated_room = response.json()
            if updated_room['Name'] == "Fully Updated Room" and updated_room['Seats'] == 75:
                print("   ✓ Chỉnh sửa cả tên và số ghế thành công!")
            else:
                print("   ✗ Dữ liệu không được cập nhật đúng")
        else:
            print(f"   ✗ Lỗi khi chỉnh sửa: {response.json()}")
    except Exception as e:
        print(f"   ✗ Lỗi: {e}")
    
    # Bước 5: Test chỉnh sửa phòng không tồn tại
    print(f"\n5. Test chỉnh sửa phòng không tồn tại (ID: 99999)...")
    update_data = {
        "name": "This should fail"
    }
    try:
        response = requests.put(f"{BASE_URL}/update/99999", json=update_data)
        print(f"   Status Code: {response.status_code}")
        print(f"   Response: {json.dumps(response.json(), indent=2, ensure_ascii=False)}")
        
        if response.status_code in [400, 404]:
            print("   ✓ API trả về lỗi đúng cho phòng không tồn tại")
        else:
            print("   ⚠ API không xử lý lỗi đúng cách")
    except Exception as e:
        print(f"   ✗ Lỗi: {e}")
    
    # Bước 6: Test với dữ liệu rỗng
    print(f"\n6. Test với dữ liệu rỗng (ID: {test_room_id})...")
    update_data_empty = {}
    try:
        response = requests.put(f"{BASE_URL}/update/{test_room_id}", json=update_data_empty)
        print(f"   Status Code: {response.status_code}")
        print(f"   Response: {json.dumps(response.json(), indent=2, ensure_ascii=False)}")
        
        if response.status_code == 400:
            print("   ✓ API trả về lỗi đúng cho dữ liệu rỗng")
        else:
            print("   ⚠ API không xử lý validation đúng cách")
    except Exception as e:
        print(f"   ✗ Lỗi: {e}")
    
    # Bước 7: Xác nhận dữ liệu cuối cùng
    print(f"\n7. Kiểm tra dữ liệu cuối cùng của phòng (ID: {test_room_id})...")
    try:
        response = requests.get(f"{BASE_URL}/get/{test_room_id}")
        if response.status_code == 200:
            final_room = response.json()
            print(f"   Dữ liệu cuối cùng:")
            print(f"   {json.dumps(final_room, indent=2, ensure_ascii=False)}")
        else:
            print(f"   ✗ Không thể lấy dữ liệu phòng")
    except Exception as e:
        print(f"   ✗ Lỗi: {e}")
    
    # Bước 8: Xóa phòng test
    print(f"\n8. Xóa phòng test (ID: {test_room_id})...")
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
    test_update_room()
