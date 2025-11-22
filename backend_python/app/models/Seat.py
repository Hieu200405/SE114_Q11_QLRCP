from app import db


class Seat(db.Model):
    __tablename__ = 'seat'
    ID = db.Column(db.Integer, primary_key=True)
    BroadcastID = db.Column(db.Integer, db.ForeignKey('broadcast.ID'))
    name = db.Column(db.String(50))
    is_delete = db.Column(db.Boolean, default=False)
    roomID = db.Column(db.Integer, db.ForeignKey('room.ID'))
    is_bought = db.Column(db.Boolean, default=False)


    def __init__(self, broadcast_id, name, room_id):
        self.BroadcastID = broadcast_id
        self.name = name
        self.roomID = room_id


    def serialize(self):
        return {
            'ID': self.ID,
            'BroadcastID': self.BroadcastID,
            'Name': self.name,
            'RoomID': self.roomID,
            'IsBought': self.is_bought
        }