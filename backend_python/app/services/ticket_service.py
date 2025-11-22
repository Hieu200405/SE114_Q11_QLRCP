from app.models.Ticket import Ticket
from app.models.User import User
from app.models.Seat import Seat
from app import db
from app.utils.helper import format_date, format_datetime, format_time
from .totalDayly_service import create_or_update_total_day
from datetime import datetime
from .broadcast_service import get_broadcast_by_id
from sqlalchemy import and_
from .seat_service import get_seat_available, set_seat_bought, set_seat_available


def create_ticket_service(user_id, broadcast_id, seat_id):
    try:
        broadcast = get_broadcast_by_id(broadcast_id)
        if not broadcast:
            raise ValueError("Broadcast not found")
        if not get_seat_available(seat_id):
            raise ValueError("Seat not available")
        price = broadcast.price
        ticket = Ticket(
            broadcast_id=broadcast_id,
            seat_id=seat_id,
            date_order=datetime.now().date(),
            time_order=datetime.now().time(),
            user_id=user_id,
            price=price
        )
        db.session.add(ticket)
        db.session.commit()
        # Set the seat as bought
        set_seat_bought(seat_id)
        create_or_update_total_day(price, 1, ticket.dateOrder.day, ticket.dateOrder.month, ticket.dateOrder.year)
        return get_ticket_detail_by_id_service(ticket.ID)
    except Exception as e:
        db.session.rollback()
        raise ValueError(f"Error creating ticket: {str(e)}")

def get_ticket_by_id_service(ticket_id):
    """Retrieve a ticket by its ID."""
    ticket = Ticket.query.filter_by(ID=ticket_id, is_delete=False).first()
    if not ticket:
        raise ValueError("Ticket not found")
    return ticket



def get_all_tickets_service():
    """Retrieve all tickets."""
    tickets = Ticket.query.filter_by(is_delete=False).order_by(Ticket.dateOrder.desc(), Ticket.timeOrder.desc()).all()
    return tickets



def get_tickets_by_user_service(user_id):
    """Retrieve all tickets for a specific user."""
    user = User.query.filter_by(ID=user_id, is_delete=False).first()
    if not user:
        raise ValueError("User not found")

    tickets = Ticket.query.filter_by(userID=user_id, is_delete=False).order_by(Ticket.dateOrder.desc(), Ticket.timeOrder.desc()).all()
    return tickets




def delete_ticket_service(ticket_id):
    """Soft delete a ticket by setting is_delete to True."""
    try:
        ticket = Ticket.query.filter_by(ID=ticket_id, is_delete=False).first()
        if not ticket:
            return {"message": "Ticket not found"}, 404
        time_order = ticket.timeOrder
        broadcast = get_broadcast_by_id(ticket.BroadcastID)
        if not broadcast:
            return {"message": "Broadcast not found"}, 404
        if broadcast.dateBroadcast < datetime.now().date() or (broadcast.dateBroadcast == datetime.now().date() and time_order < datetime.now().time()):
            return {"message": "Cannot delete ticket for past broadcasts"}, 400
        
        ticket.is_delete = True
        db.session.commit()
        create_or_update_total_day(-ticket.price, -1, ticket.dateOrder.day, ticket.dateOrder.month, ticket.dateOrder.year)
        set_seat_available(ticket.SeatID)
        return {"message": "Ticket deleted successfully"}, 200
    except Exception as e:
        db.session.rollback()
        raise ValueError(f"Error deleting ticket: {str(e)}")


'''
cần trả về thông tin chi tiết của vé bao gồm:
- id vé, ngày đặt,  mã phòng, giá vé
- thông tin buổi chiếu (ID, thời gian, ngày chiếu, giờ chiếu)
- tên phim, thumbnail, runtime

'''

def get_ticket_detail_by_id_service(ticket_id):
    """Retrieve detailed information about a ticket by its ID."""
    try:
        res = {}
        ticket = Ticket.query.filter_by(ID=ticket_id, is_delete=False).first()
        if not ticket:
            raise ValueError("Ticket not found")
        
        broadcast = get_broadcast_by_id(ticket.BroadcastID)
        firm = broadcast.firm if broadcast else None
        if not firm:
            raise ValueError("Firm not found")
        
        if not broadcast or not ticket.SeatID:
            raise ValueError("Broadcast or Seat not found")
        
        res['ID'] = ticket.ID
        res['DateOrder'] = str(ticket.dateOrder)
        res['TimeOrder'] = str(ticket.timeOrder)
        res['RoomID'] = broadcast.RoomID
        res['UserID'] = ticket.userID
        res['Price'] = ticket.price
        res['Broadcast'] = {
            'ID': broadcast.ID,
            'TimeBroadcast': str(broadcast.timeBroadcast),
            'DateBroadcast': str(broadcast.dateBroadcast),
            'FirmID': broadcast.FirmID,
            'FirmName': firm.name,
            'Thumbnail': firm.thumbnail_path,
            'Runtime': firm.runtime
        }
        res['SeatID'] = ticket.SeatID
        return res
    except Exception as e:
        raise ValueError(f"Error retrieving ticket detail: {str(e)}")
    

def getDetailTicketBySeatID(seat_id):
    """Retrieve detailed information about a ticket by its Seat ID."""
    try:
        ticket = Ticket.query.filter_by(SeatID=seat_id, is_delete=False).first()
        if not ticket:
            raise ValueError("Ticket not found for this seat")
        
        return get_ticket_detail_by_id_service(ticket.ID)
    except Exception as e:
        raise ValueError(f"Error retrieving ticket detail by seat ID: {str(e)}")