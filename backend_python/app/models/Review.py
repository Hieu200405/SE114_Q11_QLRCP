from app import db
from datetime import datetime

class Review(db.Model):
    __tablename__ = 'review'
    
    ID = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.ID'), nullable=False)
    film_id = db.Column(db.Integer, db.ForeignKey('film.ID'), nullable=False)
    
    rating = db.Column(db.Integer, nullable=False) # 1-5 sao
    comment = db.Column(db.Text)
    created_at = db.Column(db.DateTime, default=datetime.now)
    status = db.Column(db.Integer, default=1) 

    user_info = db.relationship('User', backref=db.backref('reviews', lazy=True))
    film_info = db.relationship('Film', backref=db.backref('reviews', lazy=True))

    def __init__(self, user_id, film_id, rating, comment, status=1):
        self.user_id = user_id
        self.film_id = film_id
        self.rating = rating
        self.comment = comment
        self.status = status

    def serialize(self):
        return {
            'ID': self.ID,
            'UserID': self.user_id,
            'UserName': self.user_info.Name if self.user_info else "Unknown",
            'FilmID': self.film_id,
            'Rating': self.rating,
            'Comment': self.comment,
            'CreatedAt': self.created_at.strftime("%Y-%m-%d %H:%M:%S"),
            'Status': self.status
        }

    def save(self):
        db.session.add(self)
        db.session.commit()