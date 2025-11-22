from app import db

class Room(db.Model):
    __tablename__ = 'room'
    ID = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(100))
    seats = db.Column(db.Integer)
    is_delete = db.Column(db.Boolean, default=False)

    broadcasts = db.relationship('Broadcast', backref='room', lazy=True)
    seats_list = db.relationship('Seat', backref='room', lazy=True)


    def __init__(self, name, seats):
        self.name = name
        self.seats = seats

    def serialize(self):
        return {
            'ID': self.ID,
            'Name': self.name,
            'Seats': self.seats
        }