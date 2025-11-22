from app.models.Seat import Seat
from app import db


def set_seat_bought(seat_id):
    """Set a seat as bought."""
    try:
        seat = Seat.query.filter_by(ID=seat_id, is_delete=False).first()
        if not seat:
            raise ValueError("Seat not found or already deleted")
        seat.is_bought = True
        db.session.commit()
        return True
    except Exception as e:
        db.session.rollback()
        return False
    
def set_seat_available(seat_id):
    """Set a seat as available."""
    try:
        seat = Seat.query.filter_by(ID=seat_id, is_delete=False).first()
        if not seat:
            raise ValueError("Seat not found or already deleted")
        seat.is_bought = False
        db.session.commit()
        return True
    except Exception as e:
        db.session.rollback()
        return False
    

def get_seat_available(seat_id):
    """Check if a seat is available."""
    try:
        seat = Seat.query.filter_by(ID=seat_id, is_delete=False).first()
        if not seat:
            raise ValueError("Seat not found or already deleted")
        return not seat.is_bought
    except Exception as e:
        db.session.rollback()
        raise ValueError(f"Error checking seat availability: {str(e)}")
    

