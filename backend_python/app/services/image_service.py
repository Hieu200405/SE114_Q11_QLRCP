from app.models.ImageFirm import ImageFirm
from app import db


def add_image_to_firm(firm_id, image_url):
    """Add an image to a firm."""
    new_image = ImageFirm(firm_id, image_url)
    db.session.add(new_image)
    db.session.commit()
    return new_image


def delete_image_from_firm(image_id):
    """Delete an image from a firm."""
    image = ImageFirm.query.filter_by(ID=image_id).first()
    if not image:
        raise ValueError("Image not found")
    
    db.session.delete(image)
    db.session.commit()
    return image