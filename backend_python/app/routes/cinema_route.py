from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required
from app.services.cinema_service import (
    get_all_cinemas,
    get_cinema_by_id,
    create_cinema,
    update_cinema,
    delete_cinema,
    get_nearby_cinemas,
    get_cinemas_for_film,
    search_places_autocomplete,
    get_place_detail,
    geocode_address,
    reverse_geocode,
    get_actual_distance
)

CINEMA_BLUEPRINT = Blueprint('cinema', __name__)


# ==================== CRUD OPERATIONS ====================

@CINEMA_BLUEPRINT.route('/get_all', methods=['GET'])
def get_cinemas():
    """
    Lấy tất cả rạp chiếu phim
    URL: GET /api/cinemas/get_all
    """
    try:
        cinemas = get_all_cinemas()
        return jsonify([cinema.serialize() for cinema in cinemas]), 200
    except Exception as e:
        return jsonify({'message': str(e)}), 500


@CINEMA_BLUEPRINT.route('/get/<int:cinema_id>', methods=['GET'])
def get_cinema(cinema_id):
    """
    Lấy thông tin rạp theo ID
    URL: GET /api/cinemas/get/<cinema_id>
    """
    try:
        cinema = get_cinema_by_id(cinema_id)
        if not cinema:
            return jsonify({'message': 'Không tìm thấy rạp chiếu phim'}), 404
        return jsonify(cinema.serialize_with_rooms()), 200
    except Exception as e:
        return jsonify({'message': str(e)}), 500


@CINEMA_BLUEPRINT.route('/create', methods=['POST'])
@jwt_required()
def create_new_cinema():
    """
    Tạo rạp chiếu phim mới
    URL: POST /api/cinemas/create
    Body: {
        "name": "CGV Vincom",
        "address": "72 Lê Thánh Tôn, Bến Nghé, Quận 1, TP.HCM",
        "latitude": 10.776889,
        "longitude": 106.701686,
        "phone": "1900 6017",
        "image_url": "https://example.com/image.jpg",
        "description": "Rạp chiếu phim CGV tại Vincom Center"
    }
    """
    data = request.get_json()
    try:
        name = data.get('name')
        address = data.get('address')
        latitude = data.get('latitude')
        longitude = data.get('longitude')
        phone = data.get('phone')
        image_url = data.get('image_url')
        description = data.get('description')
        
        if not all([name, address, latitude is not None, longitude is not None]):
            return jsonify({'message': 'Tên, địa chỉ, vĩ độ và kinh độ là bắt buộc'}), 400
        
        cinema = create_cinema(name, address, latitude, longitude, phone, image_url, description)
        return jsonify(cinema.serialize()), 201
    except ValueError as e:
        return jsonify({'message': str(e)}), 400
    except Exception as e:
        return jsonify({'message': str(e)}), 500


@CINEMA_BLUEPRINT.route('/update/<int:cinema_id>', methods=['PUT'])
@jwt_required()
def update_existing_cinema(cinema_id):
    """
    Cập nhật thông tin rạp chiếu phim
    URL: PUT /api/cinemas/update/<cinema_id>
    Body: {
        "name": "CGV Vincom Đồng Khởi",
        "address": "...",
        "latitude": 10.776889,
        "longitude": 106.701686,
        "phone": "...",
        "image_url": "...",
        "description": "..."
    }
    """
    data = request.get_json()
    try:
        cinema = update_cinema(
            cinema_id,
            name=data.get('name'),
            address=data.get('address'),
            latitude=data.get('latitude'),
            longitude=data.get('longitude'),
            phone=data.get('phone'),
            image_url=data.get('image_url'),
            description=data.get('description')
        )
        return jsonify(cinema.serialize()), 200
    except ValueError as e:
        return jsonify({'message': str(e)}), 400
    except Exception as e:
        return jsonify({'message': str(e)}), 500


@CINEMA_BLUEPRINT.route('/delete/<int:cinema_id>', methods=['DELETE'])
@jwt_required()
def delete_existing_cinema(cinema_id):
    """
    Xóa rạp chiếu phim (soft delete)
    URL: DELETE /api/cinemas/delete/<cinema_id>
    """
    try:
        delete_cinema(cinema_id)
        return jsonify({'message': 'Xóa rạp chiếu phim thành công'}), 200
    except ValueError as e:
        return jsonify({'message': str(e)}), 400
    except Exception as e:
        return jsonify({'message': str(e)}), 500


# ==================== LOCATION & DISTANCE OPERATIONS ====================

@CINEMA_BLUEPRINT.route('/nearby', methods=['POST'])
def get_nearby():
    """
    Lấy danh sách rạp gần vị trí người dùng
    URL: POST /api/cinemas/nearby
    Body: {
        "lat": 10.7769,
        "lng": 106.7009,
        "max_distance": 50,       // km, optional, default 50
        "use_actual_distance": true // optional, default true
    }
    Response: [
        {
            "ID": 1,
            "Name": "CGV Vincom",
            "Address": "...",
            "Distance": {"text": "5.2 km", "value": 5200},
            "Duration": {"text": "15 phút", "value": 900}
        }
    ]
    """
    data = request.get_json()
    try:
        lat = data.get('lat')
        lng = data.get('lng')
        
        if lat is None or lng is None:
            return jsonify({'message': 'Vĩ độ (lat) và kinh độ (lng) là bắt buộc'}), 400
        
        max_distance = data.get('max_distance', 50)
        use_actual_distance = data.get('use_actual_distance', True)
        
        cinemas = get_nearby_cinemas(lat, lng, max_distance, use_actual_distance)
        return jsonify(cinemas), 200
    except Exception as e:
        return jsonify({'message': str(e)}), 500


@CINEMA_BLUEPRINT.route('/for-film/<int:film_id>', methods=['POST'])
def get_cinemas_showing_film(film_id):
    """
    Lấy danh sách rạp đang chiếu phim cụ thể
    URL: POST /api/cinemas/for-film/<film_id>
    Body (optional): {
        "lat": 10.7769,
        "lng": 106.7009
    }
    """
    data = request.get_json() or {}
    try:
        lat = data.get('lat')
        lng = data.get('lng')
        
        cinemas = get_cinemas_for_film(film_id, lat, lng)
        return jsonify(cinemas), 200
    except Exception as e:
        return jsonify({'message': str(e)}), 500


@CINEMA_BLUEPRINT.route('/distance', methods=['POST'])
def calculate_distance():
    """
    Tính khoảng cách từ vị trí người dùng đến một hoặc nhiều rạp
    URL: POST /api/cinemas/distance
    Body: {
        "origin_lat": 10.7769,
        "origin_lng": 106.7009,
        "destinations": [
            {"lat": 10.776889, "lng": 106.701686},
            {"lat": 10.780123, "lng": 106.705432}
        ]
    }
    """
    data = request.get_json()
    try:
        origin_lat = data.get('origin_lat')
        origin_lng = data.get('origin_lng')
        destinations = data.get('destinations', [])
        
        if origin_lat is None or origin_lng is None:
            return jsonify({'message': 'Vĩ độ và kinh độ điểm xuất phát là bắt buộc'}), 400
        
        if not destinations:
            return jsonify({'message': 'Danh sách điểm đến không được trống'}), 400
        
        dest_coords = [f"{d['lat']},{d['lng']}" for d in destinations]
        distances = get_actual_distance(origin_lat, origin_lng, dest_coords)
        
        return jsonify({
            'origin': {'lat': origin_lat, 'lng': origin_lng},
            'distances': distances
        }), 200
    except Exception as e:
        return jsonify({'message': str(e)}), 500


# ==================== GOONG MAP UTILITIES ====================

@CINEMA_BLUEPRINT.route('/search-places', methods=['GET'])
def search_places():
    """
    Tìm kiếm địa điểm với autocomplete
    URL: GET /api/cinemas/search-places?keyword=CGV&lat=10.7769&lng=106.7009
    """
    try:
        keyword = request.args.get('keyword')
        if not keyword:
            return jsonify({'message': 'Từ khóa tìm kiếm là bắt buộc'}), 400
        
        lat = request.args.get('lat', type=float)
        lng = request.args.get('lng', type=float)
        location = (lat, lng) if lat and lng else None
        
        predictions = search_places_autocomplete(keyword, location)
        return jsonify(predictions), 200
    except Exception as e:
        return jsonify({'message': str(e)}), 500


@CINEMA_BLUEPRINT.route('/place-detail', methods=['GET'])
def place_detail():
    """
    Lấy chi tiết địa điểm từ place_id
    URL: GET /api/cinemas/place-detail?place_id=xxx
    """
    try:
        place_id = request.args.get('place_id')
        if not place_id:
            return jsonify({'message': 'place_id là bắt buộc'}), 400
        
        detail = get_place_detail(place_id)
        if not detail:
            return jsonify({'message': 'Không tìm thấy địa điểm'}), 404
        
        return jsonify(detail), 200
    except Exception as e:
        return jsonify({'message': str(e)}), 500


@CINEMA_BLUEPRINT.route('/geocode', methods=['GET'])
def geocode():
    """
    Chuyển đổi địa chỉ thành tọa độ
    URL: GET /api/cinemas/geocode?address=72 Lê Thánh Tôn, Quận 1, TP.HCM
    """
    try:
        address = request.args.get('address')
        if not address:
            return jsonify({'message': 'Địa chỉ là bắt buộc'}), 400
        
        result = geocode_address(address)
        if not result:
            return jsonify({'message': 'Không tìm thấy tọa độ cho địa chỉ này'}), 404
        
        return jsonify(result), 200
    except Exception as e:
        return jsonify({'message': str(e)}), 500


@CINEMA_BLUEPRINT.route('/reverse-geocode', methods=['GET'])
def reverse_geocode_endpoint():
    """
    Chuyển đổi tọa độ thành địa chỉ
    URL: GET /api/cinemas/reverse-geocode?lat=10.7769&lng=106.7009
    """
    try:
        lat = request.args.get('lat', type=float)
        lng = request.args.get('lng', type=float)
        
        if lat is None or lng is None:
            return jsonify({'message': 'Vĩ độ và kinh độ là bắt buộc'}), 400
        
        result = reverse_geocode(lat, lng)
        if not result:
            return jsonify({'message': 'Không tìm thấy địa chỉ cho tọa độ này'}), 404
        
        return jsonify(result), 200
    except Exception as e:
        return jsonify({'message': str(e)}), 500


@CINEMA_BLUEPRINT.route('/map-config', methods=['GET'])
def get_map_config():
    """
    Lấy cấu hình map key cho frontend (chỉ trả về map key, không trả về service key)
    URL: GET /api/cinemas/map-config
    """
    import os
    from dotenv import load_dotenv
    load_dotenv()
    
    return jsonify({
        'mapKey': os.getenv('GOONG_MAP_KEY'),
        'mapStyle': 'https://tiles.goong.io/assets/goong_map_web.json'
    }), 200
