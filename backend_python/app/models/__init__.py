from .BroadCast import Broadcast
from .Film import Film
from .ImageFilm import ImageFilm
from .TotalMonth import TotalMonth
from .Room import Room
from .User import User
from .TotalDay import TotalDay
from .Seat import Seat
from .Ticket import Ticket
from .Cinema import Cinema
from app import db





def create_db(app):
    try:
        with app.app_context():
            # Chỉ chạy nếu chưa có bảng nào
            print("Checking if database tables exist...")
            db.init_app(app)

            db.create_all()
            print("Database tables created successfully.")
    except Exception as e:
        print(f"Database error: {e}")
        raise