from flask import Flask
from .extension import db, jwt, migrate, cors
from .routes.user_route import USER_BLUEPRINT
from .routes.auth_route import AUTH_BLUEPRINT
from .routes.room_route import ROOM_BLUEPRINT
from .routes.film_route import FILM_BLUEPRINT
from .routes.imageFilm_route import IMAGE_FILM_BLUEPRINT
from .routes.broadcast_route import BROADCAST_BLUEPRINT
from .routes.ticket_route import TICKET_BLUEPRINT
from .routes.totalDay_route import TOTAL_DAY_BLUEPRINT
from .routes.payment_route import PAYMENT_BLUEPRINT
from .routes.review_route import REVIEW_BLUEPRINT

from dotenv import load_dotenv
import os
load_dotenv()


def create_app(file_config = 'config.py'):
    from .models import create_db
    app = Flask(__name__)
    app.config.from_pyfile(file_config)
    create_db(app)
    jwt.init_app(app)
    migrate.init_app(app, db)
    cors.init_app(app)
    
    app.register_blueprint(AUTH_BLUEPRINT, url_prefix='/api/auth')
    app.register_blueprint(USER_BLUEPRINT, url_prefix='/api/users')
    app.register_blueprint(ROOM_BLUEPRINT, url_prefix='/api/rooms')
    app.register_blueprint(FILM_BLUEPRINT, url_prefix='/api/films')
    app.register_blueprint(IMAGE_FILM_BLUEPRINT, url_prefix='/api/image_films')
    app.register_blueprint(BROADCAST_BLUEPRINT, url_prefix='/api/broadcasts')
    app.register_blueprint(TICKET_BLUEPRINT, url_prefix='/api/tickets')
    app.register_blueprint(TOTAL_DAY_BLUEPRINT, url_prefix='/api/total_day')
    app.register_blueprint(PAYMENT_BLUEPRINT, url_prefix='/api/payment')
    app.register_blueprint(REVIEW_BLUEPRINT, url_prefix='/api/reviews')
    return app