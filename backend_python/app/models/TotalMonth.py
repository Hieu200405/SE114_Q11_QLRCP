from app import db


class TotalMonth(db.Model):
    __tablename__ = 'totalmonth'
    ID = db.Column(db.Integer, primary_key=True)
    totalMoney = db.Column(db.Float)
    month = db.Column(db.Integer)
    year = db.Column(db.Integer)