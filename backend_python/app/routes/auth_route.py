from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.services.auth_service import login_service, logout_service, register_service



AUTH_BLUEPRINT = Blueprint('auth', __name__)


#link: localhost:5000/api/auth/login

'''
{
    
}


trả về:
{
    "ID": 1,
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJmcmVzaCI6ZmFsc2UsImlhdCI6MTc0OTg3MDA1NSwianRpIjoiZDk4MTMwMmEtZGRkOS00OGIxLWFlNTYtZDZhOWNiN2JhN2YwIiwidHlwZSI6ImFjY2VzcyIsInN1YiI6IjEiLCJuYmYiOjE3NDk4NzAwNTUsImNzcmYiOiIxNGEwYWEzOC0zNmRmLTQ4YjMtYmE4My0yYjJmZTg5NjJmOWMiLCJleHAiOjE3NTAyMzAwNTUsInVzZXJuYW1lIjoidGVzdHVzZXIiLCJJRCI6MSwicm9sZSI6InVzZXIifQ.HimdVH-Gcmj7hQ1uPrWtYVwyQ_xY_8rPLy5rabxnjuQ",
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJmcmVzaCI6ZmFsc2UsImlhdCI6MTc0OTg3MDA1NSwianRpIjoiYzE0MDIwZjgtNGI5MS00YWMwLThmODYtZjczNDZiY2UzMDAyIiwidHlwZSI6InJlZnJlc2giLCJzdWIiOiIxIiwibmJmIjoxNzQ5ODcwMDU1LCJjc3JmIjoiNWMzYTg4OGEtZTM2NC00ODg5LTk4MmItNDNjNDJhZGM2NWEwIiwiZXhwIjoxNzUzNDcwMDU1LCJ1c2VybmFtZSI6InRlc3R1c2VyIiwiSUQiOjEsInJvbGUiOiJ1c2VyIn0.DUfPC-C4Nm-em7pz5eEoD0DHzHdFAhmzceMnrUpP3Us",
    "role": "user",
    "username": "testuser"
}

'''
@AUTH_BLUEPRINT.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    username = data.get('username')
    password = data.get('password')
    
    if not username or not password:
        return jsonify({'message': 'Username and password are required'}), 400
    
    try:
        result = login_service(username, password)
        return jsonify(result), 200
    except ValueError as e:
        return jsonify({'message': str(e)}), 400
    



#link: localhost:5000/api/auth/logout
@AUTH_BLUEPRINT.route('/logout', methods=['POST'])
@jwt_required()
def logout():
    return logout_service()



'''
{
    "username": "testuser",
    "password": "testpassword",
    "name": "Test User",
    "phone": "1234567890",
    "email": "testuser@example.com"
}
'''

#link: localhost:5000/api/auth/register
@AUTH_BLUEPRINT.route('/register', methods=['POST'])
def register():
    data = request.get_json()
    fields = ['username', 'password', 'name', 'phone', 'email']
    for field in fields:
        if field not in data:
            return jsonify({'message': f'{field} is required'}), 400

    username = data.get('username')
    password = data.get('password')
    name = data.get('name')
    phone = data.get('phone')
    email = data.get('email')


    if not all([username, password, name, phone, email]):
        return jsonify({'message': 'All fields are required'}), 400
    
    try:
        result = register_service(username, password, name, phone, email)
        return result
    except ValueError as e:
        return jsonify({'message': str(e)}), 400