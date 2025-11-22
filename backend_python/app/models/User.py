from app import db


class User(db.Model):
    __tablename__ = 'user'
    ID = db.Column(db.Integer, primary_key=True)
    Name = db.Column(db.String(100))
    username = db.Column(db.String(100), unique=True)
    password = db.Column(db.String(255))
    phone = db.Column(db.String(20))
    email = db.Column(db.String(255))
    role = db.Column(db.String(20))  # user or admin
    is_delete = db.Column(db.Boolean, default=False)


    def __init__(self, name, username, password, phone, email, role='user'):
        self.Name = name
        self.username = username
        self.password = password
        self.phone = phone
        self.email = email
        self.role = role

    def serialize(self):
        return {
            'ID': self.ID,
            'Name': self.Name,
            'Username': self.username,
            'Phone': self.phone,
            'Email': self.email,
            'Role': self.role
        }

    def save(self):
        db.session.add(self)
        db.session.commit()

    def delete(self):
        self.is_delete = True
        db.session.add(self)
        db.session.commit()

    tickets = db.relationship('Ticket', backref='user', lazy=True)