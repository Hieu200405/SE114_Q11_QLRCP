from app import db
from app.models.Room import Room
from app.models.BroadCast import Broadcast


def get_all_rooms():
    """Retrieve all rooms."""
    return Room.query.filter_by(is_delete=False).all()

def get_room_by_id(room_id):
    """Retrieve a room by its ID."""
    return Room.query.filter_by(ID=room_id, is_delete=False).first()


def create_room(name, seats):
    """Create a new room."""
    if Room.query.filter_by(name=name, is_delete=False).first():
        raise ValueError("Room with this name already exists")

    new_room = Room(name=name, seats=seats)
    db.session.add(new_room)
    db.session.commit()
    return new_room

def update_room(room_id, name=None, seats=None):
    """Update an existing room."""
    room = get_room_by_id(room_id)
    if not room:
        raise ValueError("Room not found")
    if Room.query.filter(Room.ID != room_id, Room.name == name, Room.is_delete == False).first():
        raise ValueError("Room with this name already exists")
    broadcast = Broadcast.query.filter(
        Broadcast.RoomID == room.ID,
        Broadcast.is_delete == False,
        Broadcast.dateBroadcast >= db.func.current_date()
    ).first()
    if broadcast:
        raise ValueError("Room has associated broadcasts and cannot be updated")

    if name:
        room.name = name
    if seats is not None:  # Allow seats to be updated to 0
        room.seats = seats

    db.session.commit()
    return room

def delete_room(room_id):
    """Soft delete a room by setting is_delete to True."""
    room = get_room_by_id(room_id)
    if not room:
        raise ValueError("Room not found")
    # Check if the room has any broadcasts or seats associated with it
    broadcast = Broadcast.query.filter(
        Broadcast.RoomID == room.ID,
        Broadcast.is_delete == False,
        Broadcast.dateBroadcast >= db.func.current_date()
    ).first()
    if broadcast:
        raise ValueError("Room has associated broadcasts and cannot be deleted")

    room.is_delete = True
    db.session.commit()
    return room