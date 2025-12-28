from app import db

class Cinema(db.Model):
    __tablename__ = 'cinema'
    ID = db.Column(db.Integer, primary_key=True, autoincrement=True)
    name = db.Column(db.String(200), nullable=False)
    address = db.Column(db.String(500), nullable=False)
    latitude = db.Column(db.Float, nullable=False)  # Vĩ độ
    longitude = db.Column(db.Float, nullable=False)  # Kinh độ
    phone = db.Column(db.String(20))
    image_url = db.Column(db.String(500))
    description = db.Column(db.Text)
    is_delete = db.Column(db.Boolean, default=False)

    # Relationship với Room
    rooms = db.relationship('Room', backref='cinema', lazy=True)

    def __init__(self, name, address, latitude, longitude, phone=None, image_url=None, description=None):
        self.name = name
        self.address = address
        self.latitude = latitude
        self.longitude = longitude
        self.phone = phone
        self.image_url = image_url
        self.description = description

    def serialize(self):
        return {
            'ID': self.ID,
            'Name': self.name,
            'Address': self.address,
            'Latitude': self.latitude,
            'Longitude': self.longitude,
            'Phone': self.phone,
            'ImageUrl': self.image_url,
            'Description': self.description
        }

    def serialize_with_rooms(self):
        return {
            'ID': self.ID,
            'Name': self.name,
            'Address': self.address,
            'Latitude': self.latitude,
            'Longitude': self.longitude,
            'Phone': self.phone,
            'ImageUrl': self.image_url,
            'Description': self.description,
            'Rooms': [room.serialize() for room in self.rooms if not room.is_delete]
        }

    def serialize_with_distance(self, distance_text=None, duration_text=None, distance_value=None, duration_value=None):
        """Serialize với thông tin khoảng cách từ Goong API"""
        data = self.serialize()
        data['Distance'] = {
            'text': distance_text,
            'value': distance_value  # Mét
        }
        data['Duration'] = {
            'text': duration_text,
            'value': duration_value  # Giây
        }
        return data
