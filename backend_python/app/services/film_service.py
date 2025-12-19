from app.models.Film import Film
from app.models.ImageFilm import ImageFilm
from app.models.BroadCast import Broadcast
from app import db
from datetime import datetime



def get_film_by_id(film_id):

    """Retrieve a film by its ID."""
    return Film.query.filter_by(ID=film_id, is_delete=False).first()

def get_all_films():
    """Retrieve all films."""
    return Film.query.filter_by(is_delete=False).all()


def create_film(name, description, thumbnail, start_date=None, end_date=None, rating=0, rating_count=0, images=None):
    """Create a new film."""
    if Film.query.filter_by(name=name, is_delete=False).first():
        raise ValueError("Film with this name already exists")

    if end_date == '':
        end_date = None
    if start_date == '':
        start_date = None
    
    
    try:
        new_film = Film(
            name=name,
            description=description,
            thumbnail=thumbnail,
            start_date=start_date,
            end_date=end_date,
            rating=rating,
            rating_count=rating_count
        )
        db.session.add(new_film)
        db.session.commit()
    except Exception as e:
        db.session.rollback()
        raise ValueError(f"Error creating film: {str(e)}")
    if images:
        try:
            for image in images:
                new_image = ImageFilm(
                    film_id=new_film.ID,
                    image_url=image
                )
                db.session.add(new_image)
            db.session.commit()
        except Exception as e:
            db.session.rollback()
            raise ValueError(f"Error adding images: {str(e)}")
    return new_film


def update_film(film_id, name=None, description=None, thumbnail=None, rating=None, rating_count=None, runtime=None):
    """Update an existing film."""
    film = get_film_by_id(film_id)
    if not film:
        raise ValueError("Film not found")

    if name:
        film.name = name
    if description:
        film.description = description
    if thumbnail:
        film.thumbnail_path = thumbnail
    if rating:
        film.rating = rating
    if rating_count:
        film.rating_count = rating_count
    if runtime:
        film.runtime = runtime

    try:
        db.session.commit()
    except Exception as e:
        db.session.rollback()
        raise ValueError(f"Error updating film: {str(e)}")
    
    return film


def delete_film(film_id):
    """Soft delete a film by setting is_delete to True."""
    film = get_film_by_id(film_id)
    if not film:
        raise ValueError("Film not found")
    broadcasts = Broadcast.query.filter(Broadcast.FilmID == film.ID,
                                        Broadcast.is_delete == False, 
                                        Broadcast.dateBroadcast >= datetime.now().date()
                                        ).first()
    if broadcasts:
        print(broadcasts.serialize())
        raise ValueError("Cannot delete film with active broadcasts")

    film.is_delete = True
    try:
        db.session.commit()
    except Exception as e:
        db.session.rollback()
        raise ValueError(f"Error deleting film: {str(e)}")
    
    return film


def list_filmIds_broadcast_today():
    today = datetime.today().date()
    broadcasts = Broadcast.query.filter(Broadcast.dateBroadcast == today, Broadcast.is_delete == False).all()
    list_filmIds = set()
    for broadcast in broadcasts:
        if broadcast.FilmID:
            list_filmIds.add(broadcast.FilmID)
    return list_filmIds