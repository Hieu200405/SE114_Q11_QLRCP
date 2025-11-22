from app.models.BroadCast import Broadcast
from app.models.Room import Room
from app.models.Seat import Seat
from app.models.Firm import Firm
from app import db
from app.utils.helper import format_date, format_time, format_datetime
from datetime import timedelta, datetime
from sqlalchemy import and_

# check không cho broadcast trùng thời gian của room trong khoảng thời gian 3h
def is_broadcast_time_conflict(room_id, time_broadcast, date_broadcast, running_time=0):
    """Check if the broadcast time conflicts with existing broadcasts in the room."""
    current_time = datetime.combine(date_broadcast, time_broadcast)
    start_time = current_time
    end_time = current_time + timedelta(hours =3)
    existing_broadcasts = Broadcast.query.filter(
        Broadcast.RoomID == room_id,
        Broadcast.is_delete == False,
        Broadcast.dateBroadcast == date_broadcast
    ).all()

    for broadcast in existing_broadcasts:
        existing_start = datetime.combine(broadcast.dateBroadcast, broadcast.timeBroadcast)

        existing_end = existing_start + timedelta(hours=2)
        if (start_time < existing_end and end_time > existing_start):
            return True
    return False


def create_seats_for_broadcast(broadcast_id, seats, room_id):
    """Create seats for a broadcast."""
    try:
        for seat_number in range(1, seats + 1):
            new_seat = Seat(
                broadcast_id,
                'seat_' + str(seat_number),
                room_id
            )
            db.session.add(new_seat)
        db.session.commit()
    except Exception as e:
        db.session.rollback()
        raise ValueError(f"Error creating seats for broadcast: {e}")



# Create a new broadcast
def create_broadcast(room_id, firm_id, time_broadcast, date_broadcast, price, seats):
    """Create a new broadcast."""

    if not Room.query.filter_by(ID=room_id, is_delete=False).first():
        raise ValueError("Room not found or is deleted")
    if seats > Room.query.get(room_id).seats:
        raise ValueError("Number of seats exceeds room capacity")

    time_broadcast = format_time(time_broadcast)
    date_broadcast = format_date(date_broadcast)

    firm = Firm.query.filter_by(ID=firm_id, is_delete=False).first()
    if not firm:
        raise ValueError("Firm not found or is deleted")
    if firm.start_date > date_broadcast:
        raise ValueError("Broadcast date is outside the firm's active period")

    if firm.end_date != None and firm.end_date < date_broadcast:
        raise ValueError("Broadcast date is outside the firm's active period")
    running_time = firm.runtime if firm.runtime else 0

    print(f"Formatted time: {time_broadcast}, Formatted date: {date_broadcast}")
    if is_broadcast_time_conflict(room_id, time_broadcast, date_broadcast, running_time):
        raise ValueError("Broadcast time conflicts with existing broadcasts in the room")
    
    if datetime.combine(date_broadcast, time_broadcast) < datetime.now():
        raise ValueError("Broadcast time cannot be in the past")

    try:
        print(f"Creating broadcast for room {room_id} at {time_broadcast} on {date_broadcast} with price {price} and seats {seats}")
        new_broadcast = Broadcast(
            RoomID=room_id,
            FirmID=firm_id,
            timeBroadcast=time_broadcast,
            dateBroadcast=date_broadcast,
            price=price,
            seats=seats
        )
        db.session.add(new_broadcast)
        db.session.commit()

        create_seats_for_broadcast(new_broadcast.ID, seats, room_id)
        return new_broadcast
    except Exception as e:
        db.session.rollback()
        raise ValueError(f"Error creating broadcast: {e}")
    



# GET

def get_broadcast_by_id(broadcast_id):
    """Retrieve a broadcast by its ID."""
    broadcast = Broadcast.query.filter(
        and_(
            Broadcast.ID == broadcast_id,
            Broadcast.is_delete == False
        )
    ).first()
    if not broadcast:
        raise ValueError("Broadcast not found")
    return broadcast


def get_all_broadcasts_for_room(room_id):
    """Retrieve all broadcasts for a specific room."""
    broadcasts = Broadcast.query.filter(
        and_(
            Broadcast.RoomID == room_id,
            Broadcast.is_delete == False,
            Broadcast.dateBroadcast >= datetime.now().date()  # nếu là kiểu date
        )
    ).all()
    return broadcasts


def get_all_broadcasts_for_firm(firm_id, date_broadcast=None):
    """Retrieve all broadcasts for a specific firm."""
    broadcasts = Broadcast.query.filter(
        and_(
            Broadcast.FirmID == firm_id,
            Broadcast.is_delete == False,
            Broadcast.dateBroadcast >= datetime.now().date()
        )
    ).order_by(Broadcast.dateBroadcast, Broadcast.timeBroadcast).all()
    if date_broadcast:
        broadcasts = broadcasts.filter(Broadcast.dateBroadcast == date_broadcast)
    return broadcasts



def get_all_broadcasts_on_date(date_broadcast):
    """Retrieve all broadcasts on a specific date."""
    date_broadcast = format_date(date_broadcast)
    if not date_broadcast:
        raise ValueError("Invalid date format")
    broadcasts = Broadcast.query.filter(
        and_(
            Broadcast.dateBroadcast == date_broadcast,
            Broadcast.is_delete == False
        )
    ).order_by(Broadcast.timeBroadcast).all()
    if not broadcasts:
        raise ValueError("No broadcasts found for this date")
    return broadcasts


# UPDATE
def update_broadcast(broadcast_id, room_id=None, firm_id=None, time_broadcast=None, date_broadcast=None, price=None, seats=None):
    """Update an existing broadcast."""
    broadcast = get_broadcast_by_id(broadcast_id)

    
    if room_id:
        broadcast.RoomID = room_id
    if firm_id:
        broadcast.FirmID = firm_id
    if time_broadcast:
        broadcast.timeBroadcast = time_broadcast
    if date_broadcast:
        broadcast.dateBroadcast = date_broadcast
    if price is not None:
        broadcast.price = price
    if seats is not None:
        if seats > Room.query.filter_by(ID=broadcast.RoomID).first().seats:
            raise ValueError("Number of seats exceeds room capacity")
        broadcast.seats = seats

    db.session.commit()
    return broadcast



# DELETE
def delete_broadcast(broadcast_id):
    """Soft delete a broadcast by setting is_delete to True."""
    broadcast = get_broadcast_by_id(broadcast_id)
    if broadcast.tickets:
        raise ValueError("Cannot delete broadcast with existing tickets")
    broadcast.is_delete = True
    db.session.commit()
    return broadcast