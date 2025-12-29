import requests
import json
from datetime import datetime

# Base URL
BASE_URL = "http://127.0.0.1:5000/api/cinemas"

def print_result(title, response):
    """Helper function to print test results"""
    print(f"\n{'='*80}")
    print(f"TEST: {title}")
    print(f"{'='*80}")
    print(f"Status Code: {response.status_code}")
    print(f"Response Time: {response.elapsed.total_seconds():.3f}s")
    try:
        data = response.json()
        print(f"Response: {json.dumps(data, indent=2, ensure_ascii=False)}")
    except:
        print(f"Response Text: {response.text}")
    print(f"{'='*80}\n")

def test_cinema_goong_api():
    """Test các Cinema và Goong Map API endpoints"""
    
    print(f"\n🎬🗺️ TESTING CINEMA & GOONG MAP API - {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
    
    # ==================== CRUD OPERATIONS ====================
    print("\n" + "="*80)
    print("SECTION 1: CRUD OPERATIONS")
    print("="*80)
    
    # Test 1: Get all cinemas
    try:
        print("\n📋 Test 1: Get All Cinemas")
        response = requests.get(f"{BASE_URL}/get_all", timeout=10)
        print_result("GET /api/cinemas/get_all", response)
        
        # Lưu cinema_id để test các endpoint khác
        cinema_id = None
        if response.status_code == 200 and response.json():
            cinema_id = response.json()[0].get('ID')
            print(f"✅ Found cinema_id: {cinema_id}")
    except Exception as e:
        print(f"❌ Error: {str(e)}")
    
    # Test 2: Get cinema by ID
    if cinema_id:
        try:
            print(f"\n🏢 Test 2: Get Cinema by ID (ID={cinema_id})")
            response = requests.get(f"{BASE_URL}/get/{cinema_id}", timeout=10)
            print_result(f"GET /api/cinemas/get/{cinema_id}", response)
        except Exception as e:
            print(f"❌ Error: {str(e)}")
    
    # ==================== GOONG MAP UTILITIES ====================
    print("\n" + "="*80)
    print("SECTION 2: GOONG MAP UTILITIES")
    print("="*80)
    
    # Test 3: Get map config
    try:
        print("\n🗺️ Test 3: Get Map Config")
        response = requests.get(f"{BASE_URL}/map-config", timeout=10)
        print_result("GET /api/cinemas/map-config", response)
    except Exception as e:
        print(f"❌ Error: {str(e)}")
    
    # Test 4: Search places (CGV)
    try:
        print("\n🔍 Test 4: Search Places - 'CGV'")
        params = {
            'keyword': 'CGV',
            'lat': 10.7769,
            'lng': 106.7009
        }
        response = requests.get(f"{BASE_URL}/search-places", params=params, timeout=10)
        print_result("GET /api/cinemas/search-places?keyword=CGV", response)
    except Exception as e:
        print(f"❌ Error: {str(e)}")
    
    # Test 5: Geocode address
    try:
        print("\n📍 Test 5: Geocode Address")
        params = {
            'address': '72 Lê Thánh Tôn, Quận 1, TP.HCM'
        }
        response = requests.get(f"{BASE_URL}/geocode", params=params, timeout=10)
        print_result("GET /api/cinemas/geocode", response)
    except Exception as e:
        print(f"❌ Error: {str(e)}")
    
    # Test 6: Reverse geocode
    try:
        print("\n📌 Test 6: Reverse Geocode")
        params = {
            'lat': 10.7769,
            'lng': 106.7009
        }
        response = requests.get(f"{BASE_URL}/reverse-geocode", params=params, timeout=10)
        print_result("GET /api/cinemas/reverse-geocode", response)
    except Exception as e:
        print(f"❌ Error: {str(e)}")
    
    # ==================== LOCATION & DISTANCE OPERATIONS ====================
    print("\n" + "="*80)
    print("SECTION 3: LOCATION & DISTANCE OPERATIONS")
    print("="*80)
    
    # Test 7: Get nearby cinemas
    try:
        print("\n🎯 Test 7: Get Nearby Cinemas")
        payload = {
            "lat": 10.7769,
            "lng": 106.7009,
            "max_distance": 50,
            "use_actual_distance": True
        }
        response = requests.post(f"{BASE_URL}/nearby", 
                                json=payload, 
                                headers={'Content-Type': 'application/json'},
                                timeout=15)
        print_result("POST /api/cinemas/nearby", response)
    except Exception as e:
        print(f"❌ Error: {str(e)}")
    
    # Test 8: Calculate distance
    try:
        print("\n📏 Test 8: Calculate Distance to Multiple Cinemas")
        payload = {
            "origin_lat": 10.7769,
            "origin_lng": 106.7009,
            "destinations": [
                {"lat": 10.776889, "lng": 106.701686},
                {"lat": 10.780123, "lng": 106.705432}
            ]
        }
        response = requests.post(f"{BASE_URL}/distance", 
                                json=payload,
                                headers={'Content-Type': 'application/json'},
                                timeout=15)
        print_result("POST /api/cinemas/distance", response)
    except Exception as e:
        print(f"❌ Error: {str(e)}")
    
    # Test 9: Get cinemas for a film (assuming film_id = 1)
    try:
        print("\n🎬 Test 9: Get Cinemas Showing Film (film_id=1)")
        payload = {
            "lat": 10.7769,
            "lng": 106.7009
        }
        response = requests.post(f"{BASE_URL}/for-film/1", 
                                json=payload,
                                headers={'Content-Type': 'application/json'},
                                timeout=15)
        print_result("POST /api/cinemas/for-film/1", response)
    except Exception as e:
        print(f"❌ Error: {str(e)}")
    
    # ==================== SUMMARY ====================
    print("\n" + "="*80)
    print("✅ TESTING COMPLETED")
    print("="*80)
    print("Summary:")
    print("- CRUD Operations: Get All, Get by ID")
    print("- Goong Map Utilities: Map Config, Search Places, Geocode, Reverse Geocode")
    print("- Location & Distance: Nearby Cinemas, Calculate Distance, Cinemas for Film")
    print("="*80 + "\n")


if __name__ == "__main__":
    try:
        test_cinema_goong_api()
    except KeyboardInterrupt:
        print("\n\n⚠️ Testing interrupted by user")
    except Exception as e:
        print(f"\n\n❌ Fatal error: {str(e)}")
