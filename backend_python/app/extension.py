from flask_sqlalchemy import SQLAlchemy
from flask_jwt_extended import JWTManager
from flask_migrate import Migrate
from flask_cors import CORS
# Initialize extensions
db = SQLAlchemy()
jwt = JWTManager()
migrate = Migrate()
cors = CORS()



@jwt.expired_token_loader
def expired_token_callback(jwt_header, jwt_payload):
    return {
        'status': 'fail',
        'message': 'The token has expired. Please log in again.'
    }, 401


@jwt.invalid_token_loader
def invalid_token_callback(error):
    return {
        'status': 'fail',
        'message': 'Invalid token. Please log in again.'
    }, 401

@jwt.unauthorized_loader
def unauthorized_callback(error):
    return {
        'status': 'fail',
        'message': 'Missing Authorization Header. Please provide a valid token.'
    }, 401

@jwt.needs_fresh_token_loader
def needs_fresh_token_callback(jwt_header, jwt_payload):
    return {
        'status': 'fail',
        'message': 'The token is not fresh. Please log in again.'
    }, 401