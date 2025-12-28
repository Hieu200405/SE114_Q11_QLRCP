from app.extension import db
from app.models.Review import Review
from app.models.Film import Film

def add_review_service(user_id, film_id, rating, comment):
    ticket = db.session.query(Ticket).join(Broadcast).filter(
        Ticket.userID == user_id,
        Broadcast.FilmID == film_id
    ).first()

    if not ticket:
        raise ValueError("Bạn chưa mua vé cho phim này nên không thể đánh giá.")

    # check broadcast time 
    broadcast = Broadcast.query.get(ticket.BroadcastID)
    now = datetime.now()
    broadcast_datetime = datetime.combine(broadcast.dateBroadcast, broadcast.timeBroadcast)
    
    if now < broadcast_datetime:
        raise ValueError("Bạn chỉ có thể đánh giá sau khi suất chiếu bắt đầu.")

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