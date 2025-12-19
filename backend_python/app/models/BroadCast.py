from app import db




class Broadcast(db.Model):
    __tablename__ = 'broadcast'
    ID = db.Column(db.Integer, primary_key=True)
    RoomID = db.Column(db.Integer, db.ForeignKey('room.ID'))
    FilmID = db.Column(db.Integer, db.ForeignKey('film.ID'))
    timeBroadcast = db.Column(db.Time)
    dateBroadcast = db.Column(db.Date)
    price = db.Column(db.Float)
    seats = db.Column(db.Integer)
    is_delete = db.Column(db.Boolean, default=False)

    tickets = db.relationship('Ticket', backref='broadcast', lazy=True)
    seats_list = db.relationship('Seat', backref='broadcast', lazy=True)



    def serialize(self):
        return {
            'ID': self.ID,
            'RoomID': self.RoomID,
            'FilmID': self.FilmID,
            'TimeBroadcast': self.timeBroadcast.isoformat() if self.timeBroadcast else None,
            'DateBroadcast': self.dateBroadcast.isoformat() if self.dateBroadcast else None,
            'Price': self.price,    
            'Seats': self.seats
        }
    
    def serialize_detail_seats(self):
        return {
            'ID': self.ID,
            'RoomID': self.RoomID,
            'FilmID': self.FilmID,
            'TimeBroadcast': self.timeBroadcast.isoformat() if self.timeBroadcast else None,
            'DateBroadcast': self.dateBroadcast.isoformat() if self.dateBroadcast else None,
            'Price': self.price,
            'Seats': self.seats,
            'SeatsList': [seat.serialize() for seat in self.seats_list]
        }
    

    def serialize_detail_tickets(self):
        return {
            'ID': self.ID,
            'RoomID': self.RoomID,
            'FilmID': self.FilmID,
            'TimeBroadcast': self.timeBroadcast.isoformat() if self.timeBroadcast else None,
            'DateBroadcast': self.dateBroadcast.isoformat() if self.dateBroadcast else None,
            'Price': self.price,
            'Seats': self.seats,
            'IsDeleted': self.is_delete,
            'Tickets': [ticket.serialize() for ticket in self.tickets]
        }