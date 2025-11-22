from app.services.broadcast_service import (
    get_broadcast_by_id,
    get_all_broadcasts_for_room,
    get_all_broadcasts_for_firm,
    get_all_broadcasts_on_date,
    create_broadcast,
    update_broadcast,
    delete_broadcast
)



from flask import Blueprint, request, jsonify
BROADCAST_BLUEPRINT = Blueprint('broadcast', __name__)




# Create a new broadcast
# # link: localhost:5000/api/broadcasts/create
'''

{
    "room_id": 1,
    "firm_id": 1,
    "time_broadcast": "10:00:00",
    "date_broadcast": "2025-10-10",
    "price": 100000,
    "seats": 15
}
'''
@BROADCAST_BLUEPRINT.route('/create', methods=['POST'])
def create_broadcast_route():
    data = request.json
    fields = ['room_id', 'firm_id', 'time_broadcast', 'date_broadcast', 'price', 'seats']
    for field in fields:
        if field not in data:
            return jsonify({"error": f"{field} is required"}), 400
    try:
        new_broadcast = create_broadcast(
            room_id=data['room_id'],
            firm_id=data['firm_id'],
            time_broadcast=data['time_broadcast'],
            date_broadcast=data['date_broadcast'],
            price=data['price'],
            seats=data['seats']
        )
        return jsonify(new_broadcast.serialize()), 201
    except ValueError as e:
        return jsonify({"error": str(e)}), 400


# Get a broadcast by ID
# # link: localhost:5000/api/broadcasts/<broadcast_id>
@BROADCAST_BLUEPRINT.route('/<int:broadcast_id>', methods=['GET'])
def get_broadcast_route(broadcast_id):
    try:
        broadcast = get_broadcast_by_id(broadcast_id)
        return jsonify(broadcast.serialize_detail_seats()), 200
    except ValueError as e:
        return jsonify({"error": str(e)}), 404
    

# Get all seats for a broadcast
# # link: localhost:5000/api/broadcasts/seats/<broadcast_id>
@BROADCAST_BLUEPRINT.route('/seats/<int:broadcast_id>', methods=['GET'])
def get_all_seats_for_broadcast_route(broadcast_id):
    try:
        broadcast = get_broadcast_by_id(broadcast_id)
        seats = broadcast.seats_list
        if not seats:
            return jsonify({"message": "No seats available for this broadcast"}), 404
        return jsonify([seat.serialize() for seat in seats]), 200
    except ValueError as e:
        return jsonify({"error": str(e)}), 404


# Get all broadcasts for a room
# # link: localhost:5000/api/broadcasts/room/<room_id>
@BROADCAST_BLUEPRINT.route('/room/<int:room_id>', methods=['GET'])
def get_all_broadcasts_for_room_route(room_id):
    broadcasts = get_all_broadcasts_for_room(room_id)
    return jsonify([broadcast.serialize() for broadcast in broadcasts]), 200



# Get all broadcasts for a firm
# # link: localhost:5000/api/broadcasts/firm/<firm_id>
@BROADCAST_BLUEPRINT.route('/firm/<int:firm_id>', methods=['GET'])
def get_all_broadcasts_for_firm_route(firm_id):
    date = request.args.get('date')
    if date is None:
        broadcasts = get_all_broadcasts_for_firm(firm_id)
    else:
        broadcasts = get_all_broadcasts_for_firm(firm_id, date)
    return jsonify([broadcast.serialize() for broadcast in broadcasts]), 200




# Get all broadcasts on a specific date
# # link: localhost:5000/api/broadcasts/date?date=2025-10-10
@BROADCAST_BLUEPRINT.route('/date', methods=['GET'])
def get_all_broadcasts_on_date_route():
    date_broadcast = request.args.get('date')
    broadcasts = get_all_broadcasts_on_date(date_broadcast)
    return jsonify([broadcast.serialize() for broadcast in broadcasts]), 200




# Update a broadcast
# # link: localhost:5000/api/broadcasts/update/<broadcast_id>
'''

{
    "room_id": 1,
    "firm_id": 1,
    "time_broadcast": "11:00:00",
    "date_broadcast": "2025-10-11",
    "price": 120000,
    "seats": 20
}

'''
@BROADCAST_BLUEPRINT.route('/update/<int:broadcast_id>', methods=['PUT'])
def update_broadcast_route(broadcast_id):
    data = request.json
    try:
        updated_broadcast = update_broadcast(
            broadcast_id,
            room_id=data.get('room_id'),
            firm_id=data.get('firm_id'),
            time_broadcast=data.get('time_broadcast'),
            date_broadcast=data.get('date_broadcast'),
            price=data.get('price'),
            seats=data.get('seats')
        )
        return jsonify(updated_broadcast.serialize()), 200
    except ValueError as e:
        return jsonify({"error": str(e)}), 400
    





# Delete a broadcast
# # link: localhost:5000/api/broadcasts/delete/<broadcast_id>
'''

{
    "broadcast_id": 1
}

'''
@BROADCAST_BLUEPRINT.route('/delete/<int:broadcast_id>', methods=['DELETE'])
def delete_broadcast_route(broadcast_id):
    try:
        deleted_broadcast = delete_broadcast(broadcast_id)
        return jsonify({"message": "Broadcast deleted successfully"}), 200
    except ValueError as e:
        return jsonify({"error": str(e)}), 404
