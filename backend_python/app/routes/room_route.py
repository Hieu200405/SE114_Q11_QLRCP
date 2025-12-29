from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.models.Room import Room
from app.services.room_service import (
    get_all_rooms,
    get_room_by_id,
    create_room,
    update_room,
    delete_room,
    update_room_cinema,
    get_rooms_by_cinema
)



ROOM_BLUEPRINT = Blueprint('room', __name__)



# Get all rooms
# # link: localhost:5000/api/rooms/get_all
@ROOM_BLUEPRINT.route('/get_all', methods=['GET'])
# @jwt_required()
def get_rooms():
    """Retrieve all rooms."""
    try:
        rooms = get_all_rooms()
        return jsonify([room.serialize() for room in rooms]), 200
    except Exception as e:
        return jsonify({'message': str(e)}), 500
    

# Get room by ID
# # link: localhost:5000/api/rooms/get/<room_id>
@ROOM_BLUEPRINT.route('/get/<int:room_id>', methods=['GET'])
# @jwt_required()
def get_room(room_id):
    """Retrieve a room by its ID."""
    try:
        room = get_room_by_id(room_id)
        if not room:
            return jsonify({'message': 'Room not found'}), 404
        return jsonify(room.serialize()), 200
    except Exception as e:
        return jsonify({'message': str(e)}), 500
    

# Create a new room
'''
{
    "name": "Room 20",
    "seats": 20,
    "cinema_id": 1
}

'''
# # link: localhost:5000/api/rooms/create
@ROOM_BLUEPRINT.route('/create', methods=['POST'])
# @jwt_required()
def create_new_room():
    """Create a new room."""
    data = request.get_json()
    try:
        name = data.get('name')
        seats = data.get('seats')
        cinema_id = data.get('cinema_id')
        if not name or seats is None:
            return jsonify({'message': 'Name and seats are required'}), 400
        
        room = create_room(name, seats, cinema_id)
        return jsonify(room.serialize()), 201
    except ValueError as e:
        return jsonify({'message': str(e)}), 400
    except Exception as e:
        return jsonify({'message': str(e)}), 500
    

# Update an existing room
'''
{
    "name": "Updated Room 20",
    "seats": 25
}

'''
# # link: localhost:5000/api/rooms/update/<room_id>
@ROOM_BLUEPRINT.route('/update/<int:room_id>', methods=['PUT'])
# @jwt_required()
def update_existing_room(room_id):
    """Update an existing room."""
    data = request.get_json()
    try:
        name = data.get('name')
        seats = data.get('seats')
        if not name and seats is None:
            return jsonify({'message': 'At least one field (name or seats) is required'}), 400
        
        room = update_room(room_id, name=name, seats=seats)
        return jsonify(room.serialize()), 200
    except ValueError as e:
        return jsonify({'message': str(e)}), 400
    except Exception as e:
        return jsonify({'message': str(e)}), 500
    

# Delete a room
# link: localhost:5000/api/rooms/delete/<room_id>

@ROOM_BLUEPRINT.route('/delete/<int:room_id>', methods=['DELETE'])
# @jwt_required()
def delete_existing_room(room_id):
    """Soft delete a room by setting is_delete to True."""
    try:
        room = delete_room(room_id)
        return jsonify({'message': 'Room deleted successfully', 'room': room.serialize()}), 200
    except ValueError as e:
        return jsonify({'message': str(e)}), 400
    except Exception as e:
        return jsonify({'message': str(e)}), 500


# Assign room to cinema
# link: localhost:5000/api/rooms/<room_id>/assign-cinema
@ROOM_BLUEPRINT.route('/<int:room_id>/assign-cinema', methods=['PUT'])
# @jwt_required()
def assign_room_to_cinema(room_id):
    """Assign a room to a cinema."""
    data = request.get_json()
    try:
        cinema_id = data.get('cinema_id')
        room = update_room_cinema(room_id, cinema_id)
        return jsonify(room.serialize()), 200
    except ValueError as e:
        return jsonify({'message': str(e)}), 400
    except Exception as e:
        return jsonify({'message': str(e)}), 500


# Get rooms by cinema
# link: localhost:5000/api/rooms/by-cinema/<cinema_id>
@ROOM_BLUEPRINT.route('/by-cinema/<int:cinema_id>', methods=['GET'])
def get_rooms_for_cinema(cinema_id):
    """Get all rooms belonging to a specific cinema."""
    try:
        rooms = get_rooms_by_cinema(cinema_id)
        return jsonify([room.serialize() for room in rooms]), 200
    except Exception as e:
        return jsonify({'message': str(e)}), 500


# Get room with cinema info
# link: localhost:5000/api/rooms/get-with-cinema/<room_id>
@ROOM_BLUEPRINT.route('/get-with-cinema/<int:room_id>', methods=['GET'])
def get_room_with_cinema(room_id):
    """Get a room with its cinema information."""
    try:
        room = get_room_by_id(room_id)
        if not room:
            return jsonify({'message': 'Room not found'}), 404
        return jsonify(room.serialize_with_cinema()), 200
    except Exception as e:
        return jsonify({'message': str(e)}), 500