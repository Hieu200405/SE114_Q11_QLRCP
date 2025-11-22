from app import db
from .Seat import Seat


class Ticket(db.Model):
    __tablename__ = 'ticket'
    ID = db.Column(db.Integer, primary_key=True)
    BroadcastID = db.Column(db.Integer, db.ForeignKey('broadcast.ID'))
    SeatID = db.Column(db.Integer)
    dateOrder = db.Column(db.Date, nullable=True)
    timeOrder = db.Column(db.Time, nullable=True)
    userID = db.Column(db.Integer, db.ForeignKey('user.ID'))
    price = db.Column(db.Float)
    is_delete = db.Column(db.Boolean, default=False)



    def __init__(self, broadcast_id, seat_id, date_order, time_order, user_id, price):
        self.BroadcastID = broadcast_id
        self.SeatID = seat_id
        self.dateOrder = date_order
        self.timeOrder = time_order
        self.userID = user_id
        self.price = price



    def serialize(self):
        return {
            'ID': self.ID,
            'BroadcastID': self.BroadcastID,
            'SeatID': self.SeatID,
            'SeatName': Seat.query.filter_by(ID=self.SeatID).first().name if self.SeatID else None,
            'DateOrder': str(self.dateOrder),
            'TimeOrder': str(self.timeOrder),
            'UserID': self.userID,
            'Price': self.price,
            'IsDeleted': self.is_delete
        }