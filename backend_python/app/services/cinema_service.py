import os
import requests
from math import radians, cos, sin, asin, sqrt
from app import db
from app.models.Cinema import Cinema
from dotenv import load_dotenv

load_dotenv()

GOONG_SERVICE_KEY = os.getenv('GOONG_SERVICE_KEY')


def haversine_distance(lat1, lng1, lat2, lng2):
    """
    Tính khoảng cách đường chim bay (km) giữa 2 điểm tọa độ
    Dùng công thức Haversine
    """
    R = 6371  # Bán kính Trái Đất (km)
    
    lat1, lng1, lat2, lng2 = map(radians, [lat1, lng1, lat2, lng2])
    
    dlat = lat2 - lat1
    dlng = lng2 - lng1
    
    a = sin(dlat / 2) ** 2 + cos(lat1) * cos(lat2) * sin(dlng / 2) ** 2
    c = 2 * asin(sqrt(a))
    
    return R * c


def get_actual_distance(origin_lat, origin_lng, destinations):
    """
    Tính khoảng cách thực tế từ 1 điểm đến nhiều điểm rạp phim
    Sử dụng Goong Distance Matrix API
    
    Args:
        origin_lat: Vĩ độ điểm xuất phát (vị trí người dùng)
        origin_lng: Kinh độ điểm xuất phát
        destinations: List các chuỗi tọa độ ["lat,lng", "lat,lng"]
    
    Returns:
        List các dict chứa thông tin khoảng cách và thời gian di chuyển
    """
    if not GOONG_SERVICE_KEY:
        raise ValueError("GOONG_SERVICE_KEY is not configured")
    
    if not destinations:
        return []
    
    # Nối các điểm đến bằng dấu gạch đứng |
    dest_str = "|".join(destinations)
    
    # URL của Goong Distance Matrix API
    url = f"https://rsapi.goong.io/DistanceMatrix?origins={origin_lat},{origin_lng}&destinations={dest_str}&vehicle=car&api_key={GOONG_SERVICE_KEY}"
    
    try:
        response = requests.get(url, timeout=10)
        response.raise_for_status()
        data = response.json()
        
        results = []
        if 'rows' in data and len(data['rows']) > 0:
            elements = data['rows'][0]['elements']
            for element in elements:
                if element['status'] == 'OK':
                    results.append({
                        "distance_text": element['distance']['text'],  # Ví dụ: "5.2 km"
                        "distance_value": element['distance']['value'],  # Ví dụ: 5200 (mét)
                        "duration_text": element['duration']['text'],  # Ví dụ: "15 phút"
                        "duration_value": element['duration']['value']  # Ví dụ: 900 (giây)
                    })
                else:
                    results.append(None)  # Không tìm thấy đường
        return results

    except requests.exceptions.RequestException as e:
        print(f"Lỗi gọi Goong API: {e}")
        return []
    except Exception as e:
        print(f"Lỗi xử lý response: {e}")
        return []


def get_all_cinemas():
    """Lấy tất cả rạp chiếu phim"""
    return Cinema.query.filter_by(is_delete=False).all()


def get_cinema_by_id(cinema_id):
    """Lấy thông tin rạp theo ID"""
    return Cinema.query.filter_by(ID=cinema_id, is_delete=False).first()


def create_cinema(name, address, latitude, longitude, phone=None, image_url=None, description=None):
    """Tạo rạp chiếu phim mới"""
    if Cinema.query.filter_by(name=name, is_delete=False).first():
        raise ValueError("Rạp với tên này đã tồn tại")
    
    new_cinema = Cinema(
        name=name,
        address=address,
        latitude=latitude,
        longitude=longitude,
        phone=phone,
        image_url=image_url,
        description=description
    )
    db.session.add(new_cinema)
    db.session.commit()
    return new_cinema


def update_cinema(cinema_id, name=None, address=None, latitude=None, longitude=None, 
                  phone=None, image_url=None, description=None):
    """Cập nhật thông tin rạp chiếu phim"""
    cinema = get_cinema_by_id(cinema_id)
    if not cinema:
        raise ValueError("Không tìm thấy rạp chiếu phim")
    
    # Kiểm tra trùng tên
    if name and Cinema.query.filter(Cinema.ID != cinema_id, Cinema.name == name, Cinema.is_delete == False).first():
        raise ValueError("Rạp với tên này đã tồn tại")
    
    if name:
        cinema.name = name
    if address:
        cinema.address = address
    if latitude is not None:
        cinema.latitude = latitude
    if longitude is not None:
        cinema.longitude = longitude
    if phone is not None:
        cinema.phone = phone
    if image_url is not None:
        cinema.image_url = image_url
    if description is not None:
        cinema.description = description
    
    db.session.commit()
    return cinema


def delete_cinema(cinema_id):
    """Xóa mềm rạp chiếu phim"""
    cinema = get_cinema_by_id(cinema_id)
    if not cinema:
        raise ValueError("Không tìm thấy rạp chiếu phim")
    
    # Kiểm tra xem có phòng nào thuộc rạp này không
    if cinema.rooms and any(not room.is_delete for room in cinema.rooms):
        raise ValueError("Không thể xóa rạp có phòng chiếu đang hoạt động")
    
    cinema.is_delete = True
    db.session.commit()
    return cinema


def get_nearby_cinemas(user_lat, user_lng, max_distance_km=50, use_actual_distance=True):
    """
    Lấy danh sách rạp gần vị trí người dùng, sắp xếp theo khoảng cách
    
    Args:
        user_lat: Vĩ độ người dùng
        user_lng: Kinh độ người dùng
        max_distance_km: Khoảng cách tối đa (km) để lọc rạp (mặc định 50km)
        use_actual_distance: True = dùng Goong API tính đường thực, False = đường chim bay
    
    Returns:
        List các rạp với thông tin khoảng cách, sắp xếp từ gần đến xa
    """
    cinemas = get_all_cinemas()
    
    if not cinemas:
        return []
    
    # Bước 1: Lọc sơ bộ bằng khoảng cách đường chim bay
    filtered_cinemas = []
    for cinema in cinemas:
        straight_distance = haversine_distance(
            user_lat, user_lng,
            cinema.latitude, cinema.longitude
        )
        if straight_distance <= max_distance_km:
            filtered_cinemas.append({
                'cinema': cinema,
                'straight_distance': straight_distance
            })
    
    # Sắp xếp theo đường chim bay trước
    filtered_cinemas.sort(key=lambda x: x['straight_distance'])
    
    if not use_actual_distance:
        # Trả về kết quả với khoảng cách đường chim bay
        return [
            item['cinema'].serialize_with_distance(
                distance_text=f"{item['straight_distance']:.1f} km",
                duration_text=None,
                distance_value=int(item['straight_distance'] * 1000),
                duration_value=None
            )
            for item in filtered_cinemas
        ]
    
    # Bước 2: Tính khoảng cách thực tế bằng Goong API
    dest_coords = [f"{item['cinema'].latitude},{item['cinema'].longitude}" for item in filtered_cinemas]
    
    actual_distances = get_actual_distance(user_lat, user_lng, dest_coords)
    
    # Bước 3: Ghép kết quả và sắp xếp
    results = []
    for i, item in enumerate(filtered_cinemas):
        cinema = item['cinema']
        if i < len(actual_distances) and actual_distances[i]:
            distance_info = actual_distances[i]
            results.append({
                'data': cinema.serialize_with_distance(
                    distance_text=distance_info['distance_text'],
                    duration_text=distance_info['duration_text'],
                    distance_value=distance_info['distance_value'],
                    duration_value=distance_info['duration_value']
                ),
                'sort_key': distance_info['distance_value']
            })
        else:
            # Fallback về đường chim bay nếu API fail
            results.append({
                'data': cinema.serialize_with_distance(
                    distance_text=f"{item['straight_distance']:.1f} km",
                    duration_text=None,
                    distance_value=int(item['straight_distance'] * 1000),
                    duration_value=None
                ),
                'sort_key': int(item['straight_distance'] * 1000)
            })
    
    # Sắp xếp lại theo khoảng cách thực tế
    results.sort(key=lambda x: x['sort_key'])
    
    return [r['data'] for r in results]


def get_cinemas_for_film(film_id, user_lat=None, user_lng=None):
    """
    Lấy danh sách rạp đang chiếu phim cụ thể
    Nếu có tọa độ người dùng thì tính khoảng cách
    
    Args:
        film_id: ID của phim
        user_lat: Vĩ độ người dùng (optional)
        user_lng: Kinh độ người dùng (optional)
    
    Returns:
        List các rạp đang chiếu phim với thông tin khoảng cách (nếu có)
    """
    from app.models.BroadCast import Broadcast
    from app.models.Room import Room
    from datetime import datetime
    
    # Query các broadcast đang active cho film này
    broadcasts = Broadcast.query.filter(
        Broadcast.FilmID == film_id,
        Broadcast.is_delete == False,
        Broadcast.dateBroadcast >= datetime.now().date()
    ).all()
    
    # Lấy danh sách cinema IDs từ các rooms
    cinema_ids = set()
    for broadcast in broadcasts:
        room = Room.query.filter_by(ID=broadcast.RoomID, is_delete=False).first()
        if room and room.cinema_id:
            cinema_ids.add(room.cinema_id)
    
    if not cinema_ids:
        return []
    
    # Lấy thông tin các cinemas
    cinemas = Cinema.query.filter(
        Cinema.ID.in_(cinema_ids),
        Cinema.is_delete == False
    ).all()
    
    if user_lat is None or user_lng is None:
        return [cinema.serialize_with_rooms() for cinema in cinemas]
    
    # Tính khoảng cách nếu có tọa độ người dùng
    dest_coords = [f"{c.latitude},{c.longitude}" for c in cinemas]
    actual_distances = get_actual_distance(user_lat, user_lng, dest_coords)
    
    results = []
    for i, cinema in enumerate(cinemas):
        if i < len(actual_distances) and actual_distances[i]:
            distance_info = actual_distances[i]
            data = cinema.serialize_with_rooms()
            data['Distance'] = {
                'text': distance_info['distance_text'],
                'value': distance_info['distance_value']
            }
            data['Duration'] = {
                'text': distance_info['duration_text'],
                'value': distance_info['duration_value']
            }
            results.append({
                'data': data,
                'sort_key': distance_info['distance_value']
            })
        else:
            data = cinema.serialize_with_rooms()
            data['Distance'] = None
            data['Duration'] = None
            results.append({
                'data': data,
                'sort_key': float('inf')
            })
    
    results.sort(key=lambda x: x['sort_key'])
    return [r['data'] for r in results]


def search_places_autocomplete(keyword, location=None):
    """
    Tìm kiếm địa điểm với autocomplete từ Goong API
    
    Args:
        keyword: Từ khóa tìm kiếm
        location: Tuple (lat, lng) để bias kết quả theo vị trí
    
    Returns:
        List các predictions
    """
    if not GOONG_SERVICE_KEY:
        raise ValueError("GOONG_SERVICE_KEY is not configured")
    
    url = f"https://rsapi.goong.io/Place/AutoComplete?api_key={GOONG_SERVICE_KEY}&input={keyword}"
    
    if location:
        url += f"&location={location[0]},{location[1]}"
    
    try:
        response = requests.get(url, timeout=10)
        response.raise_for_status()
        data = response.json()
        
        if data.get('status') == 'OK':
            return data.get('predictions', [])
        return []
    except Exception as e:
        print(f"Lỗi tìm kiếm địa điểm: {e}")
        return []


def get_place_detail(place_id):
    """
    Lấy chi tiết địa điểm từ Goong API
    
    Args:
        place_id: ID của địa điểm từ autocomplete
    
    Returns:
        Dict chứa thông tin chi tiết địa điểm
    """
    if not GOONG_SERVICE_KEY:
        raise ValueError("GOONG_SERVICE_KEY is not configured")
    
    url = f"https://rsapi.goong.io/Place/Detail?api_key={GOONG_SERVICE_KEY}&place_id={place_id}"
    
    try:
        response = requests.get(url, timeout=10)
        response.raise_for_status()
        data = response.json()
        
        if data.get('status') == 'OK':
            return data.get('result')
        return None
    except Exception as e:
        print(f"Lỗi lấy chi tiết địa điểm: {e}")
        return None


def geocode_address(address):
    """
    Chuyển đổi địa chỉ thành tọa độ (Forward Geocoding)
    
    Args:
        address: Địa chỉ cần chuyển đổi
    
    Returns:
        Dict chứa lat, lng
    """
    if not GOONG_SERVICE_KEY:
        raise ValueError("GOONG_SERVICE_KEY is not configured")
    
    url = f"https://rsapi.goong.io/geocode?api_key={GOONG_SERVICE_KEY}&address={address}"
    
    try:
        response = requests.get(url, timeout=10)
        response.raise_for_status()
        data = response.json()
        
        if data.get('status') == 'OK' and data.get('results'):
            location = data['results'][0]['geometry']['location']
            return {
                'lat': location['lat'],
                'lng': location['lng'],
                'formatted_address': data['results'][0]['formatted_address']
            }
        return None
    except Exception as e:
        print(f"Lỗi geocoding: {e}")
        return None


def reverse_geocode(lat, lng):
    """
    Chuyển đổi tọa độ thành địa chỉ (Reverse Geocoding)
    
    Args:
        lat: Vĩ độ
        lng: Kinh độ
    
    Returns:
        Dict chứa địa chỉ
    """
    if not GOONG_SERVICE_KEY:
        raise ValueError("GOONG_SERVICE_KEY is not configured")
    
    url = f"https://rsapi.goong.io/Geocode?api_key={GOONG_SERVICE_KEY}&latlng={lat},{lng}"
    
    try:
        response = requests.get(url, timeout=10)
        response.raise_for_status()
        data = response.json()
        
        if data.get('status') == 'OK' and data.get('results'):
            return {
                'formatted_address': data['results'][0]['formatted_address'],
                'address_components': data['results'][0].get('address_components', [])
            }
        return None
    except Exception as e:
        print(f"Lỗi reverse geocoding: {e}")
        return None
