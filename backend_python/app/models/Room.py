from app import db

class Room(db.Model):
    __tablename__ = 'room'
    ID = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(100))
    seats = db.Column(db.Integer)
    is_delete = db.Column(db.Boolean, default=False)
    cinema_id = db.Column(db.Integer, db.ForeignKey('cinema.ID'), nullable=True)

    broadcasts = db.relationship('Broadcast', backref='room', lazy=True)
    seats_list = db.relationship('Seat', backref='room', lazy=True)


    def __init__(self, name, seats, cinema_id=None):
        self.name = name
        self.seats = seats
        self.cinema_id = cinema_id

    def serialize(self):
        return {
            'ID': self.ID,
            'Name': self.name,
            'Seats': self.seats,
            'CinemaID': self.cinema_id
        }
    
    def serialize_with_cinema(self):
        data = self.serialize()
        if self.cinema:
            data['Cinema'] = self.cinema.serialize()
        return data