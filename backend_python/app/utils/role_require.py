from functools import wraps
from flask import request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity, verify_jwt_in_request
from app.models.User import User


def role_required(role):
    def decorator(fn):
        @wraps(fn)
        def wrapper(*args, **kwargs):
            verify_jwt_in_request()
            current_user_id = get_jwt_identity()
            current_user = User.query.get(current_user_id)
            if current_user.role not in role:
                return jsonify({'message': 'Access forbidden: insufficient permissions'}), 403
            return fn(*args, **kwargs)
        return wrapper
    return decorator


ROLE_ADMIN = 'admin'

def is_admin():
    return role_required(ROLE_ADMIN)