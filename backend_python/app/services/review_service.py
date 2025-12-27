from app.extension import db
from app.models.Review import Review
from app.models.Film import Film

def add_review_service(user_id, film_id, rating, comment):
    try:
        # Create new review
        new_review = Review(
            user_id=user_id, 
            film_id=film_id, 
            rating=rating, 
            comment=comment
        )
        db.session.add(new_review)

        # Update Film table
        film = Film.query.get(film_id)
        if not film:
            raise ValueError("Phim không tồn tại")

        current_total_score = (film.rating * film.rating_count) + rating
        film.rating_count += 1
        film.rating = round(current_total_score / film.rating_count, 1) 

        db.session.commit()
        return new_review.serialize()

    except Exception as e:
        db.session.rollback()
        raise e

def get_film_reviews_service(film_id):
    # Get reviews with visible status (status=1)
    reviews = Review.query.filter_by(film_id=film_id, status=1).all()
    return [r.serialize() for r in reviews]

def hide_review_service(review_id):
    review = Review.query.get(review_id)
    if not review:
        raise ValueError("Không tìm thấy đánh giá")
    
    review.status = 0
    db.session.commit()
    return True