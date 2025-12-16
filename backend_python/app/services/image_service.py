from app.models.ImageFilm import ImageFilm
from app import db


def add_image_to_film(film_id, image_url):
    """Add an image to a film."""
    new_image = ImageFilm(film_id, image_url)
    db.session.add(new_image)
    db.session.commit()
    return new_image


def delete_image_from_film(image_id):
    """Delete an image from a film."""
    image = ImageFilm.query.filter_by(ID=image_id).first()
    if not image:
        raise ValueError("Image not found")
    
    db.session.delete(image)
    db.session.commit()
    return image