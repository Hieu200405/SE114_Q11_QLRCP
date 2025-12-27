from flask import Blueprint, request, jsonify
from app.extension import db
from app.models.Review import Review
from app.models.Film import Film
from flask_jwt_extended import jwt_required, get_jwt_identity, get_jwt

REVIEW_BLUEPRINT = Blueprint('review', __name__)

@REVIEW_BLUEPRINT.route('/add', methods=['POST'])
@jwt_required()
def add_review():
    try:
        data = request.json
        user_id = get_jwt_identity() # Get User ID from Token JWT
        film_id = data.get('FilmID')
        rating = data.get('Rating')
        comment = data.get('Comment', '')

        if not film_id or not rating:
            return jsonify({"code": "01", "desc": "Missing FilmID or Rating"}), 400

        # Create and save new review
        new_review = Review(user_id=user_id, film_id=film_id, rating=rating, comment=comment)
        db.session.add(new_review)

        # Update rating score and rating amount 
        film = Film.query.get(film_id)
        if film:
            # Re-calculate average rating
            total_score = (film.rating * film.rating_count) + rating
            film.rating_count += 1
            film.rating = total_score / film.rating_count
        
        db.session.commit()

        return jsonify({
            "code": "00",
            "desc": "Success",
            "data": new_review.serialize()
        }), 201

    except Exception as e:
        db.session.rollback()
        return jsonify({"code": "99", "desc": str(e)}), 500

@REVIEW_BLUEPRINT.route('/film/<int:film_id>', methods=['GET'])
def get_film_reviews(film_id):
    try:
        # Only get reviews with status = 1 (not hidden)
        reviews = Review.query.filter_by(film_id=film_id, status=1).all()
        return jsonify({
            "code": "00",
            "desc": "Success",
            "data": [r.serialize() for r in reviews]
        }), 200
    except Exception as e:
        return jsonify({"code": "99", "desc": str(e)}), 500

# API hide review for Admin 
@REVIEW_BLUEPRINT.route('/admin/hide/<int:review_id>', methods=['PATCH'])
@jwt_required()
def hide_review(review_id):
    try:
        # Get role from JWT Token
        claims = get_jwt()
        user_role = claims.get('role')

        # Check Admin auth
        if user_role != 'admin':
            return jsonify({
                "code": "03",
                "desc": "Permission denied. Admin role required."
            }), 403
            
        # 3. Hide review
        review = Review.query.get(review_id)
        if not review:
            return jsonify({
                "code": "01", 
                "desc": "Review not found"
            }), 404
        
        review.status = 0 # Status = 0: hidden
        db.session.commit()
        
        return jsonify({
            "code": "00", 
            "desc": "Review has been hidden by Admin"
        }), 200
        
    except Exception as e:
        db.session.rollback()
        return jsonify({
            "code": "99", 
            "desc": str(e)
        }), 500