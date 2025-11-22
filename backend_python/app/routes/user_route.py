from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required
from app.services.user_service import (
    get_all_users,
    get_user_by_id,
    create_user,
    update_user,
    delete_user
)



USER_BLUEPRINT = Blueprint('user', __name__)


# get all users
# link: localhost:5000/api/users/get_all
@USER_BLUEPRINT.route('/get_all', methods=['GET'])
def get_users():
    """Retrieve all users."""
    try:
        users = get_all_users()
        return jsonify([user.serialize() for user in users]), 200
    except Exception as e:
        return jsonify({'message': str(e)}), 500
    

# get user by id
# link: localhost:5000/api/users/get/<user_id>
@USER_BLUEPRINT.route('/get/<int:user_id>', methods=['GET'])
@jwt_required()
def get_user(user_id):
    """Retrieve a user by their ID."""
    try:
        user = get_user_by_id(user_id)
        if not user:
            return jsonify({'message': 'User not found'}), 404
        return jsonify(user.serialize()), 200
    except Exception as e:
        return jsonify({'message': str(e)}), 500
    

# create user
# link: localhost:5000/api/users/create

'''
{
    "name": "Thanh huy",
    "username": "user",
    "password": "123456",
    "phone": "1234567890",
    "email": "test@example.com"
}

'''

@USER_BLUEPRINT.route('/create', methods=['POST'])
def create_new_user():
    """Create a new user."""
    data = request.get_json()
    try:
        user = create_user(
            name=data['name'],
            username=data['username'],
            password=data['password'],  # Password should be hashed in the service
            phone=data['phone'],
            email=data['email']
        )
        return jsonify(user.serialize()), 201
    except ValueError as e:
        return jsonify({'message': str(e)}), 400
    except Exception as e:
        return jsonify({'message': str(e)}), 500
    





# update user
# link: localhost:5000/api/users/update/<user_id>
'''
{
    "name": "Updated Name",
    "phone": "0987654321",
    "email": "updated@example.com"
}

'''
@USER_BLUEPRINT.route('/update/<int:user_id>', methods=['PUT'])
def update_existing_user(user_id):
    """Update an existing user."""
    data = request.get_json()
    try:
        user = update_user(
            user_id,
            name=data.get('name'),
            phone=data.get('phone'),
            email=data.get('email')
        )
        return jsonify(user.serialize()), 200
    except ValueError as e:
        return jsonify({'message': str(e)}), 400
    except Exception as e:
        return jsonify({'message': str(e)}), 500
    

# delete user
# link: localhost:5000/api/users/delete/<user_id>
'''
{
    "message": "User deleted successfully"
}
'''

@USER_BLUEPRINT.route('/delete/<int:user_id>', methods=['DELETE'])
def delete_existing_user(user_id):
    """Soft delete a user."""
    try:
        user = delete_user(user_id)
        return jsonify({'message': 'User deleted successfully', 'user': user.serialize()}), 200
   
   
    except ValueError as e:
        return jsonify({'message': str(e)}), 404
    except Exception as e:
        return jsonify({'message': str(e)}), 500
    