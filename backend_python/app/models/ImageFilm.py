from app import db

class ImageFilm(db.Model):
    __tablename__ = 'image_film'
    ID = db.Column(db.Integer, primary_key=True)
    FilmID = db.Column(db.Integer, db.ForeignKey('film.ID'))
    image_url = db.Column(db.String(255))

    def __init__(self, film_id, image_url):
        self.FilmID = film_id
        self.image_url = image_url

    def serialize(self):
        return {
            'ImageURL': self.image_url,
            'ImageID': self.ID
        }