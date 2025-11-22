from app.models.User import User
from app.models import db
from werkzeug.security import generate_password_hash, check_password_hash

def get_user_by_id(user_id):
    """Retrieve a user by their ID."""
    return User.query.filter_by(ID=user_id, is_delete=False).first()

def get_all_users():
    """Retrieve all users."""
    return User.query.filter(User.is_delete == False, User.role == 'user').all()


def create_user(name, username, password, phone, email):
    """Create a new user."""
    if (User.query.filter_by(username=username, is_delete=False).first() 
        or User.query.filter_by(email=email, is_delete=False).first()
        or User.query.filter_by(phone=phone, is_delete=False).first()):
        raise ValueError("Username, email or phone already exists")

    new_user = User(
        name=name,
        username=username,
        password=generate_password_hash(password),  # Password should be hashed before saving
        phone=phone,
        email=email
    )
    
    db.session.add(new_user)
    db.session.commit()
    return new_user


def update_user(user_id, name=None, phone=None, email=None):
    user = get_user_by_id(user_id)
    if not user:
        raise ValueError("User not found")

    if name:
        user.Name = name
    if phone:
        user.phone = phone
    if email:
        user.email = email

    db.session.commit()
    return user



def delete_user(user_id):
    """Soft delete a user by setting is_delete to True."""
    user = get_user_by_id(user_id)
    if not user:
        raise ValueError("User not found")
    
    # nếu tài khoản đã mua vé thì không được xóa
    if user.tickets:
        raise ValueError("User has associated tickets and cannot be deleted")
    user.is_delete = True
    db.session.commit()
    return user
