from app import db

class ImageFirm(db.Model):
    __tablename__ = 'image_firm'
    ID = db.Column(db.Integer, primary_key=True)
    FirmID = db.Column(db.Integer, db.ForeignKey('firm.ID'))
    image_url = db.Column(db.String(255))

    def __init__(self, firm_id, image_url):
        self.FirmID = firm_id
        self.image_url = image_url

    def serialize(self):
        return {
            'ImageURL': self.image_url,
            'ImageID': self.ID
        }