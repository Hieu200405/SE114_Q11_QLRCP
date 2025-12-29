from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity, get_jwt
from app.services.review_service import (
    add_review_service, 
    get_film_reviews_service, 
    hide_review_service
)

REVIEW_BLUEPRINT = Blueprint('review', __name__)

# Add review
# link: localhost:5000/api/reviews/add

@REVIEW_BLUEPRINT.route('/add', methods=['POST'])
@jwt_required()
def add_review():
    try:
        data = request.json
        user_id = get_jwt_identity()
        
        result = add_review_service(
            user_id=user_id,
            film_id=data.get('FilmID'),
            rating=data.get('Rating'),
            comment=data.get('Comment')
        )
        return jsonify({"code": "00", "desc": "Success", "data": [result]}), 201
    except ValueError as e:
        return jsonify({"code": "01", "desc": str(e)}), 400
    except Exception as e:
        return jsonify({"code": "99", "desc": str(e)}), 500

# Get reviews
# link: localhost:5000/api/reviews/film/<film_id>


@REVIEW_BLUEPRINT.route('/film/<int:film_id>', methods=['GET'])
def get_film_reviews(film_id):
    try:
        reviews = get_film_reviews_service(film_id)
        return jsonify({"code": "00", "desc": "Success", "data": reviews}), 200
    except Exception as e:
        return jsonify({"code": "99", "desc": str(e)}), 500

# Hide review (Admin only)
# link: localhost:5000/api/reviews/admin/hide/<review_id>

@REVIEW_BLUEPRINT.route('/admin/hide/<int:review_id>', methods=['PATCH'])
@jwt_required()
def hide_review(review_id):
    try:
        if get_jwt().get('role') != 'admin':
            return jsonify({"code": "03", "desc": "Chỉ Admin mới có quyền này"}), 403
            
        hide_review_service(review_id)
        return jsonify({"code": "00", "desc": "Đã ẩn đánh giá thành công"}), 200
    except ValueError as e:
        return jsonify({"code": "01", "desc": str(e)}), 404
    except Exception as e:
        return jsonify({"code": "99", "desc": str(e)}), 500