import requests
import json
from datetime import datetime

# Base URL
BASE_URL = "http://127.0.0.1:5000/api"

def print_result(title, response):
    """Helper function to print test results"""
    print(f"\n{'='*60}")
    print(f"TEST: {title}")
    print(f"{'='*60}")
    print(f"Status Code: {response.status_code}")
    print(f"Response Time: {response.elapsed.total_seconds():.3f}s")
    try:
        data = response.json()
        print(f"Response: {json.dumps(data, indent=2, ensure_ascii=False)[:500]}")
    except:
        print(f"Response Text: {response.text[:500]}")
    print(f"{'='*60}\n")

def test_api():
    """Test các API endpoints"""
    
    print(f"\n🚀 TESTING API ENDPOINTS - {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
    
    # Test 1: Get all films
    try:
        print("📽️ Testing Films API...")
        response = requests.get(f"{BASE_URL}/films/get_all", timeout=5)
        print_result("GET /api/films/get_all", response)
    except Exception as e:
        print(f"❌ Error testing films: {str(e)}")
    
    # Test 2: Get all cinemas
    try:
        print("🎬 Testing Cinemas API...")
        response = requests.get(f"{BASE_URL}/cinemas/get_all", timeout=5)
        print_result("GET /api/cinemas/get_all", response)
    except Exception as e:
        print(f"❌ Error testing cinemas: {str(e)}")
    
    # Test 3: Test login endpoint (without valid credentials)
    try:
        print("🔐 Testing Auth API...")
        payload = {
            "username": "test",
            "password": "test"
        }
        response = requests.post(f"{BASE_URL}/auth/login", 
                                json=payload, 
                                timeout=5)
        print_result("POST /api/auth/login", response)
    except Exception as e:
        print(f"❌ Error testing auth: {str(e)}")
    
    # Test 4: Get broadcasts for a specific room (using room ID 2)
    try:
        print("📡 Testing Broadcasts API...")
        response = requests.get(f"{BASE_URL}/broadcasts/get_room/2", timeout=5)
        print_result("GET /api/broadcasts/get_room/2", response)
    except Exception as e:
        print(f"❌ Error testing broadcasts: {str(e)}")
    
    # Test 5: Get all rooms
    try:
        print("🏢 Testing Rooms API...")
        response = requests.get(f"{BASE_URL}/rooms/get_all", timeout=5)
        print_result("GET /api/rooms/get_all", response)
    except Exception as e:
        print(f"❌ Error testing rooms: {str(e)}")
    
    print("\n✅ API TESTING COMPLETED!\n")

if __name__ == "__main__":
    test_api()
