from flask_jwt_extended import create_refresh_token, create_access_token, get_jwt_identity, jwt_required, get_jwt
from app.models.User import User
from werkzeug.security import check_password_hash, generate_password_hash
from flask import jsonify
from app import db
from sqlalchemy import and_

def authenticate(username, password):
    user = User.query.filter(and_(User.username==username, 
                                   User.is_delete == 0)).first()
    if not user:
        raise ValueError("Tên tài khoản không tồn tại")
    if not check_password_hash(user.password, password):
        raise ValueError("Mật khẩu không chính xác")
    return user


def login_service(username, password):
    print(username)
    print(password)
    user = authenticate(username, password)
    data = {
        'username': user.username,
        'ID': user.ID,
        'role': user.role,
    }
    access_token = create_access_token(identity=str(user.ID), additional_claims=data)
    refresh_token = create_refresh_token(identity=str(user.ID), additional_claims=data)
    return {
        'access_token': access_token,
        'refresh_token': refresh_token, 
        'username': user.username,
        'role': user.role,
        'ID': user.ID
    }



def logout_service():
    return jsonify({'message': 'Đăng xuất thành công'}), 200


def register_service(username, password, name, phone, email, role= 'user'):
    if (User.query.filter_by(username=username, is_delete=False).first() 
        or User.query.filter_by(email=email, is_delete=False).first()
        or User.query.filter_by(phone=phone, is_delete=False).first()):
        raise ValueError("Username, email or phone already exists")
    
    hashed_password = generate_password_hash(password)
    new_user = User(
        username=username,
        password=hashed_password,
        name=name,
        phone=phone,
        email=email,
        role=role
    )
    
    new_user.save()
    return jsonify({'message': 'Đăng ký thành công'}), 201



