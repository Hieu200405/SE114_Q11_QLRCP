from app import db



class Firm(db.Model):
    __tablename__ = 'firm'
    ID = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(255))
    description = db.Column(db.Text)
    start_date = db.Column(db.Date)
    end_date = db.Column(db.Date)
    thumbnail_path = db.Column(db.String(255))
    rating_count = db.Column(db.Integer, default=0)
    rating = db.Column(db.Float, default=0.0)
    runtime = db.Column(db.Integer, default=60)  # Runtime in seconds
    is_delete = db.Column(db.Boolean, default=False)
    

    images = db.relationship('ImageFirm', backref='firm', lazy=True)
    broadcasts = db.relationship('Broadcast', backref='firm', lazy=True)


    def __init__(self, name, description, thumbnail, start_date, end_date: None, rating_count=0, rating=0.0, runtime=60):
        self.name = name
        self.description = description
        self.start_date = start_date
        self.end_date = end_date
        self.thumbnail_path = thumbnail
        self.rating_count = rating_count
        self.rating = rating
        self.runtime = runtime

    def serialize(self):
        return {
            'ID': self.ID,
            'Name': self.name,
            'StartDate': self.start_date.isoformat() if self.start_date else None,
            'EndDate': self.end_date.isoformat() if self.end_date else None,
            'ThumbnailPath': self.thumbnail_path,
            'RatingCount': self.rating_count,
            'Rating': self.rating,
            'Runtime': self.runtime
        }
    

    def serialize_detail(self):
        return {
            'ID': self.ID,
            'Name': self.name,
            'Description': self.description,
            'StartDate': self.start_date.isoformat() if self.start_date else None,
            'EndDate': self.end_date.isoformat() if self.end_date else None,
            'ThumbnailPath': self.thumbnail_path,
            'RatingCount': self.rating_count,
            'Rating': self.rating,
            'Runtime': self.runtime,
            'Images': [image.serialize() for image in self.images]
        }
    
